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

import org.junit.jupiter.api.Test

class VarargTest {
    @Test
    fun `Int vararg as single String description`() {
        assertMessage(
            """
            fun varargs(vararg args: Int, @CallerArgumentExpression("args") argsDescription: String = "") =
                "args: ${'$'}{args.contentToString()}, description: ${'$'}argsDescription"

            fun test(): String {
                val x = 2
                return varargs(1, x, 3)
            }
            """.trimIndent(),
            """
            args: [1, 2, 3], description: 1, x, 3
            """.trimIndent()
        )
    }

    @Test
    fun `String vararg as single String description`() {
        assertMessage(
            """
            fun varargs(vararg args: String, @CallerArgumentExpression("args") argsDescription: String = "") =
                "args: ${'$'}{args.contentToString()}, description: ${'$'}argsDescription"

            fun test(): String {
                val x = "2"
                return varargs("1", x, "3")
            }
            """.trimIndent(),
            """
            args: [1, 2, 3], description: "1", x, "3"
            """.trimIndent()
        )
    }

    @Test
    fun `vararg as String array description`() {
        assertMessage(
            """
            fun varargs(vararg args: Int, @CallerArgumentExpression("args") argsDescription: Array<String>? = null) =
                "args: ${'$'}{args.contentToString()}, description: ${'$'}{argsDescription.contentToString()}"

            fun test(): String {
                val x = 2
                return varargs(1, x, 3)
            }
            """.trimIndent(),
            """
            args: [1, 2, 3], description: [1, x, 3]
            """.trimIndent()
        )
    }

    @Test
    fun `non-vararg should not be described as array`() {
        assertFailMessage(
            """
            fun varargs(args: Array<Int>, @CallerArgumentExpression("args") argsDescription: Array<String>? = null) =
                "args: ${'$'}{args.contentToString()}, description: ${'$'}{argsDescription.contentToString()}"
            """.trimIndent(),
            """
            (2, 31): Only vararg arguments can be described as Array<String>. Parameter 'args' is not a vararg. Make it vararg or adjust the parameter name in annotation for parameter 'argsDescription'
            """.trimIndent()
        )
    }
}
