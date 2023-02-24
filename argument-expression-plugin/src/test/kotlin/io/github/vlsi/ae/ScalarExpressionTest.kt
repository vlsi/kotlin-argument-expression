package io.github.vlsi.ae

import org.junit.jupiter.api.Test

class ScalarExpressionTest {
    @Test
    fun `int argument`() {
        assertMessage(
            """
            fun testArgumentExpression(arg: Int, @ArgumentExpression("arg") argDescription: String = "") {
                assert(false) {
                    "arg: ${'$'}arg, description: ${'$'}argDescription"
                }
            }

            fun main() {
                val answer = 42
                testArgumentExpression(answer)
            }
            """.trimIndent(),
            "arg: 42, description: answer"
        )
    }

    @Test
    fun `two arguments test`() {
        assertMessage(
            """
            fun testArgumentExpression(
                x: Int,
                @ArgumentExpression("x") xDescription: String = "",
                y: String,
                @ArgumentExpression("y") yDescription: String = "",
            ) {
                assert(false) {
                    "${'$'}xDescription: ${'$'}x, ${'$'}yDescription: ${'$'}y"
                }
            }

            fun main() {
                val answer = 42
                val name = "world"
                testArgumentExpression(x = answer + 1 - 1, y = "hello, ${'$'}name")
            }
            """.trimIndent(),
            """answer + 1 - 1: 42, "hello, ${'$'}name": hello, world"""
        )
    }
}
