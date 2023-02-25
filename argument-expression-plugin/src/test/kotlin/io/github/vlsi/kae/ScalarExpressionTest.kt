package io.github.vlsi.kae

import org.junit.jupiter.api.Test

class ScalarExpressionTest {
    @Test
    fun `int argument`() {
        assertMessage(
            """
            fun testArgumentExpression(arg: Int, @ArgumentExpression("arg") argDescription: String = "") =
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
    fun `two arguments test`() {
        assertMessage(
            """
            fun testArgumentExpression(
                x: Int,
                @ArgumentExpression("x") xDescription: String = "",
                y: String,
                @ArgumentExpression("y") yDescription: String = "",
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
    fun `labmda expression`() {
        assertMessage(
            """
            class Expect<Value>(val value: Value, private val description: String, val verb : String = "I expect") {
                val messages = mutableListOf<String>()
                val nested = mutableListOf<Expect<*>>()

                fun toEqual(other: Any, @ArgumentExpression("other") otherDescription: String = "") {
                    if (value != other) {
                        messages.add("to equal ${'$'}other <<${'$'}otherDescription>>")
                    }
                }

                 val allMessages: List<String>
                     get() =
                        listOf("${'$'}verb <<${'$'}description>> ${'$'}value") +
                            messages.map { "    ${'$'}it" } +
                            nested.flatMap { it.allMessages.map { "    ${'$'}it" } }
            }

            fun <Value> expect(
                value: Value,
                @ArgumentExpression("value") description: String = "",
                verb: String = "I expect",
                expectations: Expect<Value>.() -> Unit = {}
            ): Expect<Value>
                = Expect(value, description = description, verb = verb).apply(expectations)

            fun <Value, SubValue> Expect<Value>.its(
                @ArgumentExpression("extractor")
                name: String = "",
                extractor: Value.() -> SubValue
            ): Expect<SubValue> =
                expect(extractor(value), description = name, verb = "Its").also { nested.add(it) }

            fun test() : String {
                val results = "world"
                return expect(listOf(1, 2)) {
                    its { size + 1 }.toEqual(1 + 3)
                    its { first() }.toEqual(value.get(1))
                }.allMessages.joinToString("\n")
            }
            """.trimIndent(),
            """
            I expect <<listOf(1, 2)>> [1, 2]
                Its <<{ size + 1 }>> 3
                    to equal 4 <<1 + 3>>
                Its <<{ first() }>> 1
                    to equal 2 <<get(1)>>
            """.trimIndent()
        )
    }
}
