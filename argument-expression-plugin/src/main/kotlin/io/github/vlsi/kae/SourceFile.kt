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
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
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
        return getText(callInfo)
            ?.replace(
                "\n" + " ".repeat(callIndent),
                "\n"
            ) // Remove additional indentation
    }

    private fun getText(info: SourceRangeInfo): String? {
        if (info.startOffset == UNDEFINED_OFFSET || info.endOffset == UNDEFINED_OFFSET) {
            return null
        }
        return safeSubstring(info.startOffset, info.endOffset)
    }

    private fun safeSubstring(start: Int, end: Int): String =
        source.substring(maxOf(start, 0), minOf(end, source.length))

    private fun getSourceRangeInfo(element: IrElement): SourceRangeInfo {
        var range = element.startOffset..element.endOffset
        when (element) {
            is IrCall -> {
                val receiver = element.extensionReceiver ?: element.dispatchReceiver
                if (element.symbol.owner.isInfix && receiver != null) {
                    // When an infix function is called *not* with infix notation, the startOffset will not include the receiver.
                    // Force the range to include the receiver, so it is always present
                    range = receiver.startOffset..element.endOffset

                    // The offsets of the receiver will *not* include surrounding parentheses so these need to be checked for
                    // manually.
                    val substring = safeSubstring(receiver.startOffset - 1, receiver.endOffset + 1)
                    if (substring.startsWith('(') && substring.endsWith(')')) {
                        range = receiver.startOffset - 1..element.endOffset
                    }
                }
            }
        }
        return irFile.fileEntry.getSourceRangeInfo(range.first, range.last)
    }
}
