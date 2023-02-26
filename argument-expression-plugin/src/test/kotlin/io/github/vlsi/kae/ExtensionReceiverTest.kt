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

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class ExtensionReceiverTest {
    @Test
    fun `extension receiver`() {
        assertMessage(
            """
            fun Int?.describe(
                @CallerArgumentExpression("this") thisDescription: String = "",
            ) = "${'$'}this <<${'$'}thisDescription>>"

            fun test(): String {
                return (40 + 2).describe()
            }
            """.trimIndent(),
            """
            42 <<40 + 2>>
            """.trimIndent()
        )
    }

    @Test
    @Disabled("Infix functions must not have default value arguments, see https://github.com/vlsi/kotlin-argument-expression/issues/2")
    fun `infix fun as infix`() {
        assertMessage(
            """
            infix fun Int.describe(
                other: Int,
                @CallerArgumentExpression("this") thisDescription: String = "",
            ) = "${'$'}this <<${'$'}thisDescription>>"

            fun test(): String {
                return (40 + 2) describe(44 - 2)
            }
            """.trimIndent(),
            """
            42 <<40 + 2>>
            """.trimIndent()
        )
    }

    @Test
    fun `dispatch receiver`() {
        assertMessage(
            """
            class Wrapper(val value: Int) {
                fun describe(
                    @CallerArgumentExpression("this") thisDescription: String = "",
                ) = "${'$'}this <<${'$'}thisDescription>>"

                override fun toString() = "Wrapper(${'$'}value)"
            }

            fun test(): String {
                return Wrapper(40 + 2).describe()
            }
            """.trimIndent(),
            """
            Wrapper(42) <<Wrapper(40 + 2)>>
            """.trimIndent()
        )
    }

    @Test
    fun `dispatch and extension receiver`() {
        assertMessage(
            """
            class Wrapper(val value: Int) {
                fun Int.describe(
                    @CallerArgumentExpression("this") thisDescription: String = "",
                ) = "${'$'}this <<${'$'}thisDescription>>"

                fun test() = (value + 2).describe()

                override fun toString() = "Wrapper(${'$'}value)"
            }

            fun test(): String {
                return Wrapper(2).test()
            }
            """.trimIndent(),
            """
            4 <<value + 2>>
            """.trimIndent()
        )
    }

    @Test
    fun `neither dispatch nor extension receiver`() {
        assertFailMessage(
            """
            fun describe(@CallerArgumentExpression("this") thisDescription: String = "") = ""
            """.trimIndent(),
            """
            (2, 14): Function 'describe' has neither dispatch nor extension receiver, so 'this' is not available
            """.trimIndent()
        )
    }
}
