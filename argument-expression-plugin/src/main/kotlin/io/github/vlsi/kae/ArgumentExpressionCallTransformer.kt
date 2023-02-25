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
import org.jetbrains.kotlin.backend.jvm.codegen.AnnotationCodegen.Companion.annotationClass
import org.jetbrains.kotlin.backend.jvm.ir.getValueArgument
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.util.findAnnotation
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class ArgumentExpressionCallTransformer(
    private val sourceFile: SourceFile,
    private val context: IrPluginContext,
    private val messageCollector: MessageCollector,
    private val annotations: Set<FqName>
) : IrElementTransformerVoidWithContext() {
    override fun visitCall(expression: IrCall): IrExpression {
        // If there's no arguments, there's no need to check the annotations
        if (expression.valueArgumentsCount == 0) {
            return super.visitCall(expression)
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
            for (aeAnnotation in annotations) {
                val annotation = parameter.annotations.findAnnotation(aeAnnotation)
                    ?: continue
                val valueArgument = annotation.getValueArgument(Name.identifier("value"))
                if (valueArgument == null) {
                    messageCollector.report(
                        CompilerMessageSeverity.WARNING,
                        "Unable to find 'value' argument for annotation ${annotation.annotationClass}",
                        sourceFile.getCompilerMessageLocation(annotation)
                    )
                    continue
                }
                if (valueArgument !is IrConst<*>) {
                    messageCollector.report(
                        CompilerMessageSeverity.ERROR,
                        "Value argument of ${annotation.annotationClass} should constant, got ${valueArgument::class}",
                        sourceFile.getCompilerMessageLocation(valueArgument)
                    )
                    continue
                }
                if (valueArgument.kind != IrConstKind.String) {
                    messageCollector.report(
                        CompilerMessageSeverity.ERROR,
                        "Value argument of ${annotation.annotationClass} should be a String constant, got ${valueArgument.kind}",
                        sourceFile.getCompilerMessageLocation(valueArgument)
                    )
                    continue
                }
                val argName = valueArgument.value as String

                val exprParameter = function.valueParameters
                    .firstOrNull { it.name.identifier == argName }

                if (exprParameter == null) {
                    messageCollector.report(
                        CompilerMessageSeverity.ERROR,
                        "Unable to find parameter $argName for function ${function.name}",
                        sourceFile.getCompilerMessageLocation(valueArgument)
                    )
                    continue
                }

                val expressionValue = expression.getValueArgument(exprParameter.index)
                    ?: continue

                val callInfo = sourceFile.getSourceRangeInfo(expressionValue)
                val callIndent = callInfo.startColumnNumber
                val source = sourceFile.getText(callInfo)
                    ?.replace("\n" + " ".repeat(callIndent), "\n") // Remove additional indentation
                    ?: continue
                // Actually pass the expression text
                expression.putArgument(
                    function,
                    parameter,
                    IrConstImpl.string(
                        UNDEFINED_OFFSET,
                        UNDEFINED_OFFSET,
                        context.irBuiltIns.stringType,
                        source // <-- this is a string representation of the expression
                    )
                )
            }
        }
        return super.visitCall(expression)
    }
}
