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

class ScalarExpressionTest {
    @Test
    fun `int argument`() {
        assertMessage(
            """
            fun testArgumentExpression(arg: Int, @CallerArgumentExpression("arg") argDescription: String = "") =
                "arg: ${'$'}arg, description: ${'$'}argDescription"

            fun test(): String {
                val answer = 42
                return testArgumentExpression(answer)
            }
            """.trimIndent(),
            "arg: 42, description: answer"
        )
    }

    @Test
    fun `dot expression`() {
        assertMessage(
            """
            fun testArgumentExpression(arg: String, @CallerArgumentExpression("arg") argDescription: String = "") =
                "arg: ${'$'}arg, description: ${'$'}argDescription"

            fun test(): String {
                val answer = 42
                return testArgumentExpression(answer.toString().plus("abcd"))
            }
            """.trimIndent(),
            "arg: 42abcd, description: answer.toString().plus(\"abcd\")"
        )
    }

    @Test
    fun `static java call`() {
        assertMessage(
            """
            import java.time.Duration
            fun testArgumentExpression(arg: Duration?, @CallerArgumentExpression("arg") argDescription: String = "") =
                "arg: ${'$'}arg, description: ${'$'}argDescription"

            fun test(): String {
                val answer = 42L
                return testArgumentExpression(Duration.ofSeconds(answer))
            }
            """.trimIndent(),
            "arg: PT42S, description: Duration.ofSeconds(answer)"
        )
    }

    @Test
    fun `static import java call`() {
        assertMessage(
            """
            import java.time.Duration
            import java.time.Duration.ofSeconds
            fun testArgumentExpression(arg: Duration?, @CallerArgumentExpression("arg") argDescription: String = "") =
                "arg: ${'$'}arg, description: ${'$'}argDescription"

            fun test(): String {
                val answer = 42L
                return testArgumentExpression(ofSeconds(answer))
            }
            """.trimIndent(),
            "arg: PT42S, description: Duration.ofSeconds(answer)"
        )
    }

    @Test
    fun `companion call`() {
        assertMessage(
            """
            import java.time.Duration

            class Test {
                companion object {
                    fun test(x: Int) = x
                }
            }

            fun testArgumentExpression(arg: Int, @CallerArgumentExpression("arg") argDescription: String = "") =
                "arg: ${'$'}arg, description: ${'$'}argDescription"

            fun test(): String {
                val answer = 42
                return testArgumentExpression(Test.test(answer))
            }
            """.trimIndent(),
            "arg: 42, description: Test.test(answer)"
        )
    }

    @Test
    fun `cast as`() {
        assertMessage(
            """
            import java.time.Duration
            fun testArgumentExpression(arg: Duration, @CallerArgumentExpression("arg") argDescription: String = "") =
                "arg: ${'$'}arg, description: ${'$'}argDescription"

            fun test(): String {
                val answer = Duration.ofSeconds(42)
                return testArgumentExpression(answer as Duration)
            }
            """.trimIndent(),
            "arg: PT42S, description: answer as Duration"
        )
    }

    @Test
    fun `constructor argument`() {
        assertMessage(
            """
            class Named<Value>(
                val value: Value,
                @CallerArgumentExpression("value") val name: String = ""
            ) {
                override fun toString(): String = "Named(value=${'$'}value, name='${'$'}name')"
            }

            fun test(): String = Named(1 + 2).toString()
            """.trimIndent(),
            "Named(value=3, name='1 + 2')"
        )
    }

    @Test
    fun `two arguments test`() {
        assertMessage(
            """
            fun testArgumentExpression(
                x: Int,
                @CallerArgumentExpression("x") xDescription: String = "",
                y: String,
                @CallerArgumentExpression("y") yDescription: String = "",
            ) =
                "${'$'}xDescription: ${'$'}x, ${'$'}yDescription: ${'$'}y"

            fun test(): String {
                val answer = 42
                val name = "world"
                return testArgumentExpression(x = answer + 1 - 1, y = "hello, ${'$'}name")
            }
            """.trimIndent(),
            """answer + 1 - 1: 42, "hello, ${'$'}name": hello, world"""
        )
    }

    @Test
    fun `unknown argument requested for expression`() {
        assertFailMessage(
            """
            fun testArgumentExpression(
                x: Int,
                @CallerArgumentExpression("xy") xDescription: Int = 0,
            ) = ""
            """.trimIndent(),
            """
            (4, 5): Unable to find parameter xy for function testArgumentExpression
            """.trimIndent()
        )
    }

    @Test
    fun `Int can not be used for description`() {
        assertFailMessage(
            """
            fun testArgumentExpression(
                x: Int,
                @CallerArgumentExpression("x") xDescription: Int = 0,
            ) = ""
            """.trimIndent(),
            """
            (4, 5): Only String and Array<String> are supported as argument expression types. Parameter 'xDescription' is of type kotlin.Int
            """.trimIndent()
        )
    }
}
