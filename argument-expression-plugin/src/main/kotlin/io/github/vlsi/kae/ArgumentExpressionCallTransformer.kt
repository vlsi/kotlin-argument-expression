/*
 * Copyright 2023 Vladimir Sitnikov <sitnikov.vladimir@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.github.vlsi.kae

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.jvm.codegen.AnnotationCodegen.Companion.annotationClass
import org.jetbrains.kotlin.backend.jvm.ir.getValueArgument
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.js.utils.asString
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.types.getArrayElementType
import org.jetbrains.kotlin.ir.types.isArray
import org.jetbrains.kotlin.ir.types.isNullableArray
import org.jetbrains.kotlin.ir.types.isStringClassType
import org.jetbrains.kotlin.ir.util.fileOrNull
import org.jetbrains.kotlin.ir.util.findAnnotation
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class ArgumentExpressionCallTransformer(
    private val sourceFile: SourceFile,
    private val context: IrPluginContext,
    private val messageCollector: MessageCollector,
    private val annotations: Set<FqName>
) : IrElementTransformerVoidWithContext() {
    /**
     * Verifies if the function is properly declared with respect to `@CallerArgumentExpression`
     * annotations.
     */
    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        if (declaration.valueParameters.isEmpty()) {
            return super.visitFunctionNew(declaration)
        }
        for (parameter in declaration.valueParameters) {
            if (parameter.annotations.isEmpty()) {
                continue
            }
            val errorContext = parameter or declaration
            for (aeAnnotation in annotations) {
                val annotation = parameter.annotations.findAnnotation(aeAnnotation)
                    ?: continue
                val argName = getAnnotationValueOrNull(annotation, annotation or errorContext)
                    ?: continue

                @Suppress("NAME_SHADOWING")
                val errorContext = argName or annotation or errorContext

                if (argName.value == "this") {
                    if (declaration.extensionReceiverParameter != null) {
                        continue
                    }
                    if (declaration.dispatchReceiverParameter != null) {
                        continue
                    }
                    messageCollector.report(
                        CompilerMessageSeverity.ERROR,
                        "Function '${declaration.name}' has neither dispatch nor extension receiver, so 'this' is not available",
                        errorContext.location
                    )
                    continue
                }

                val sourceParameter = declaration.findParameterByNameOrNull(
                    argName = argName,
                    errorContext = errorContext,
                    severity = CompilerMessageSeverity.ERROR
                )
                    ?: continue

                if (parameter.type.isStringClassType()) {
                    // String is fine
                    continue
                }
                if (parameter.type.isArray() || parameter.type.isNullableArray()) {
                    if (sourceParameter.isVararg) {
                        continue
                    }

                    messageCollector.report(
                        CompilerMessageSeverity.ERROR,
                        "Only vararg arguments can be described as Array<String>. " +
                                "Parameter '${sourceParameter.name.asString()}' is not a vararg. " +
                                "Make it vararg or adjust the parameter name in annotation for parameter '${parameter.name.asString()}'",
                        errorContext.location
                    )
                }
                messageCollector.report(
                    CompilerMessageSeverity.ERROR,
                    "Only String and Array<String> are supported as argument expression types. " +
                            "Parameter '${parameter.name.asString()}' is of type ${parameter.type.asString()}",
                    errorContext.location
                )
            }
        }
        return super.visitFunctionNew(declaration)
    }

    /**
     * Adds caller argument expression to function calls that have `@CallerArgumentExpression`
     * annotation.
     */
    override fun visitFunctionAccess(expression: IrFunctionAccessExpression): IrExpression {
        // If there's no arguments, there's no need to check the annotations
        if (expression.valueArgumentsCount == 0) {
            return super.visitFunctionAccess(expression)
        }
        val function = expression.symbol.owner
        for (parameter in function.valueParameters) {
            if (expression.getValueArgument(parameter.index) != null) {
                // Parameter is passed explicitly, no need to check the annotations
                continue
            }
            if (parameter.annotations.isEmpty()) {
                continue
            }
            // We are processing user code, and parameter refers to the function declaration
            // So we should not use parameter as error context
            val errorContext = expression
            for (aeAnnotation in annotations) {
                val annotation = parameter.annotations.findAnnotation(aeAnnotation)
                    ?: continue
                val argName = getAnnotationValueOrNull(annotation, errorContext = errorContext)
                    ?: continue
                val expressionValue = getCallerArgumentExpressionOrNull(
                    argName = argName,
                    expression = expression,
                    function = function,
                    errorContext = errorContext
                )
                    ?: continue

                @Suppress("NAME_SHADOWING")
                val errorContext = expressionValue or errorContext

                val parameterType = parameter.type
                if (parameterType.isStringClassType()) {
                    val text = sourceFile.expressionTextOrNull(expressionValue)
                        ?: continue
                    // Actually pass the expression text
                    expression.putArgument(
                        function,
                        parameter,
                        IrConstImpl.string(
                            UNDEFINED_OFFSET,
                            UNDEFINED_OFFSET,
                            context.irBuiltIns.stringType,
                            text // <-- this is a string representation of the expression
                        )
                    )
                    continue
                } else if (!parameterType.isArray() && !parameterType.isNullableArray()) {
                    messageCollector.report(
                        CompilerMessageSeverity.WARNING,
                        "Function '${function.name.asString()}' is improperly declared: " +
                                "only String and Array<String> types can serve as parameter description only. " +
                                "Parameter '${parameter.name.asString()}' has type ${parameterType.asString()}",
                        errorContext.location
                    )
                    continue
                }

                // Handle vararg -> String | Array<String>
                val array = expressionValue as? IrVararg
                if (array == null) {
                    messageCollector.report(
                        CompilerMessageSeverity.WARNING,
                        "Function '${function.name.asString()}' is improperly declared: " +
                                "only vararg arguments can be described as Array<String>. " +
                                "Parameter '$argName' is not a vararg in '${function.name.asString()}', however " +
                                "'${parameter.name.asString()}' has type ${parameterType.asString()}",
                        errorContext.location
                    )
                    continue
                }
                val arrayElementType = parameterType.getArrayElementType(context.irBuiltIns)
                if (!arrayElementType.isStringClassType()) {
                    messageCollector.report(
                        CompilerMessageSeverity.WARNING,
                        "Function ${function.name.asString()} is improperly declared: " +
                                "vararg parameters can be described to String and Array<String> only. " +
                                "Parameter ${parameter.name.asString()} has type ${parameterType.asString()}",
                        errorContext.location
                    )
                    continue
                }

                // Build arrayOf(expr0, expr1, ...) expression that describes
                // caller argument expressions
                val res =
                    context.irBuiltIns.createIrBuilder(currentScope!!.scope.scopeOwnerSymbol)
                        .run {
                            irCall(context.irBuiltIns.arrayOf).apply {
                                putValueArgument(
                                    0,
                                    irVararg(
                                        context.irBuiltIns.stringType,
                                        array.elements
                                            .map {
                                                irString(
                                                    sourceFile.expressionTextOrNull(it) ?: ""
                                                )
                                            }
                                    )
                                )
                            }
                        }
                expression.putArgument(
                    function,
                    parameter,
                    res
                )
            }
        }
        return super.visitFunctionAccess(expression)
    }

    private fun getAnnotationValueOrNull(annotation: IrConstructorCall, errorContext: IrElement): IrConst<String>? {
        val valueArgument = annotation.getValueArgument(Name.identifier("value"))
        if (valueArgument == null) {
            messageCollector.report(
                CompilerMessageSeverity.WARNING,
                "Unable to find 'value' argument for annotation ${annotation.annotationClass}",
                errorContext.location
            )
            return null
        }
        @Suppress("UnnecessaryVariable")
        if (valueArgument !is IrConst<*>) {
            messageCollector.report(
                CompilerMessageSeverity.ERROR,
                "Value argument of ${annotation.annotationClass} should be a constant, got ${valueArgument.type.asString()}",
                errorContext.location
            )
            return null
        }
        if (!valueArgument.type.isStringClassType()) {
            messageCollector.report(
                CompilerMessageSeverity.ERROR,
                "Value argument of ${annotation.annotationClass} should be a String constant, got ${valueArgument.kind}",
                errorContext.location
            )
            return null
        }
        @Suppress("UNCHECKED_CAST")
        return valueArgument as IrConst<String>
    }

    private fun getCallerArgumentExpressionOrNull(
        argName: IrConst<String>,
        expression: IrFunctionAccessExpression,
        function: IrFunction,
        errorContext: IrExpression
    ): IrExpression? {
        if (argName.value == "this") {
            expression.extensionReceiver?.let { return it }
            expression.dispatchReceiver?.let { return it }
            messageCollector.report(
                CompilerMessageSeverity.WARNING,
                "Function '${function.name}' has neither dispatch nor extension receiver, so 'this' is not available",
                (argName or errorContext).location
            )
            return null
        }

        val exprParameter = function.findParameterByNameOrNull(
            argName,
            argName,
            CompilerMessageSeverity.WARNING
        )
            ?: return null

        return expression.getValueArgument(exprParameter.index)
    }

    private fun IrFunction.findParameterByNameOrNull(
        argName: IrConst<String>,
        errorContext: IrElement,
        severity: CompilerMessageSeverity
    ): IrValueParameter? {
        val parameter = valueParameters
            .firstOrNull { it.name.identifier == argName.value }
        if (parameter == null) {
            messageCollector.report(
                severity,
                "Unable to find parameter ${argName.value} for function $name",
                (argName or errorContext).location
            )
        }
        return parameter
    }

    private infix fun IrElement.or(other: IrElement) =
        if (startOffset != UNDEFINED_OFFSET) this else other

    private val IrElement.location: CompilerMessageLocation?
        get() {
            val file = when (this) {
                is IrDeclaration -> fileOrNull ?: currentFile
                else -> currentFile
            }

            val range = file.fileEntry.getSourceRangeInfo(startOffset, endOffset)
            return CompilerMessageLocation.create(
                path = range.filePath,
                line = range.startLineNumber.let { if (it < 0) it else it + 1 },
                column = range.startColumnNumber.let { if (it < 0) it else it + 1 },
                lineContent = null
            )
        }
}
