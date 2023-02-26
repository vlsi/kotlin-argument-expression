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

class LambdaExpressionTest {
    @Test
    fun `lambda expression`() {
        assertMessage(
            """
            class Expect<Value>(val value: Value, private val description: String, val verb : String = "I expect") {
                val messages = mutableListOf<String>()
                val nested = mutableListOf<Expect<*>>()

                fun toEqual(other: Any, @CallerArgumentExpression("other") otherDescription: String = "") {
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
                @CallerArgumentExpression("value") description: String = "",
                verb: String = "I expect",
                expectations: Expect<Value>.() -> Unit = {}
            ): Expect<Value>
                = Expect(value, description = description, verb = verb).apply(expectations)

            fun <Value, SubValue> Expect<Value>.its(
                @CallerArgumentExpression("extractor")
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
