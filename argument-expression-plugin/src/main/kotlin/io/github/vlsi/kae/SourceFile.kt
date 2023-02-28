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

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.SourceRangeInfo
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.isFromJava
import java.io.File

class SourceFile(
    private val irFile: IrFile
) {
    private val source: String = File(irFile.fileEntry.name).readText()
        .replace("\r\n", "\n") // https://youtrack.jetbrains.com/issue/KT-41888

    fun expressionTextOrNull(expression: IrElement): String? {
        val callInfo = getSourceRangeInfo(expression)
        if (expression.startOffset == UNDEFINED_OFFSET || expression.endOffset == UNDEFINED_OFFSET) {
            return expression.dumpKotlinLike()
        }
        val callIndent = callInfo.startColumnNumber
        val result = getText(callInfo)
            .replace(
                "\n" + " ".repeat(callIndent),
                "\n"
            ) // Remove additional indentation
        val prefix = getExtraPrefix(expression)
        return prefix?.let { it + result } ?: result
    }

    private fun getText(info: SourceRangeInfo): String =
        safeSubstring(info.startOffset, info.endOffset)

    private fun getExtraPrefix(expression: IrElement): String? =
        when (expression) {
            is IrTypeOperatorCall -> {
                when (expression.operator) {
                    IrTypeOperator.IMPLICIT_NOTNULL,
                    IrTypeOperator.IMPLICIT_DYNAMIC_CAST,
                    IrTypeOperator.IMPLICIT_INTEGER_COERCION,
                    IrTypeOperator.IMPLICIT_COERCION_TO_UNIT ->
                        getExtraPrefix(expression.argument)

                    else -> null
                }
            }

            is IrCall -> {
                if (expression.dispatchReceiver != null || expression.extensionReceiver != null ||
                    !expression.symbol.owner.isFromJava()
                ) {
                    null
                } else {
                    // offsets for Static calls to Java methods do not include the class name
                    // We can't tell if the method was statically imported or not, so we always
                    // add a class prefix
                    expression.type.classFqName
                        ?.takeIf { !it.isRoot }
                        ?.shortName()
                        ?.asString()
                        ?.let { "$it." }
                }
            }

            else -> null
        }

    private fun safeSubstring(start: Int, end: Int): String =
        source.substring(maxOf(start, 0), minOf(end, source.length))

    private fun getSourceRangeInfo(element: IrElement): SourceRangeInfo =
        irFile.fileEntry.getSourceRangeInfo(element.actualStartOffset, element.endOffset)

    private val IrElement.actualStartOffset: Int
        get() {
            var offset = startOffset
            var current = this
            while (current is IrMemberAccessExpression<*>) {
                val receiver = current.extensionReceiver ?: current.dispatchReceiver ?: break
                val receiverOffset = receiver.startOffset
                if (receiverOffset in 0 until offset) {
                    offset = receiverOffset
                }
                current = receiver
            }
            return offset
        }
}
