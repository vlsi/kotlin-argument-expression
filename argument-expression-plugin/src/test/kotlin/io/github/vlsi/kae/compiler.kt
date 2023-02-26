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

// Copyright (C) 2020 Brian Norman
package io.github.vlsi.kae

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.name.FqName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.fail
import java.io.OutputStream
import java.lang.reflect.InvocationTargetException

private val DEFAULT_COMPONENT_REGISTRARS = arrayOf(
    ArgumentExpressionRegistrar(
        setOf(FqName("io.github.vlsi.kae.CallerArgumentExpression"))
    )
)

fun compile(
    list: List<SourceFile>,
    vararg plugins: ComponentRegistrar = DEFAULT_COMPONENT_REGISTRARS
): KotlinCompilation.Result {
    return KotlinCompilation().apply {
        sources = list
        useIR = true
        messageOutputStream = object : OutputStream() {
            override fun write(b: Int) {
                // black hole all writes
            }

            override fun write(b: ByteArray, off: Int, len: Int) {
                // black hole all writes
            }
        }
        compilerPlugins = plugins.toList()
        inheritClassPath = true
    }.compile()
}

fun compile(
    @Language("kotlin") source: String,
    vararg plugins: ComponentRegistrar = DEFAULT_COMPONENT_REGISTRARS
): KotlinCompilation.Result {
    return compile(
        listOf(
            SourceFile.kotlin(
                "main.kt",
                "import io.github.vlsi.kae.CallerArgumentExpression\n" +
                        source,
                trimIndent = false
            ),
            SourceFile.kotlin(
                "CallerArgumentExpression.kt",
                """
                package io.github.vlsi.kae
                @Target(AnnotationTarget.VALUE_PARAMETER)
                annotation class CallerArgumentExpression(
                    val value: String
                )
                """.trimIndent()
            )
        ),
        *plugins,
    )
}

fun executeAssertion(
    @Language("kotlin") source: String,
    vararg plugins: ComponentRegistrar = DEFAULT_COMPONENT_REGISTRARS
): String {
    val result = compile(source, *plugins)
    assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode) {
        "Compilation messages: ${result.messages}"
    }

    val kClazz = result.classLoader.loadClass("MainKt")
    val main = kClazz.declaredMethods.single { it.name == "test" && it.parameterCount == 0 }
    if (main.returnType != String::class.java) {
        fail("test() should return String, got ${main.returnType}")
    }
    try {
        return main.invoke(null) as String
    } catch (t: InvocationTargetException) {
        throw t.cause!!
    }
}

fun assertMessage(
    @Language("kotlin") source: String,
    message: String,
    vararg plugins: ComponentRegistrar = DEFAULT_COMPONENT_REGISTRARS
) {
    val actual = executeAssertion(source, *plugins)
    assertEquals(message, actual)
}

fun assertFailMessage(
    @Language("kotlin") source: String,
    vararg messages: String,
    plugins: Array<out ComponentRegistrar> = DEFAULT_COMPONENT_REGISTRARS,
) {
    val result = compile(source, *plugins)
    assertEquals(
        KotlinCompilation.ExitCode.COMPILATION_ERROR,
        result.exitCode,
        "It is expected to fail compilation"
    )
    for (message in messages) {
        if (message !in result.messages) {
            fail("Compilation output should include $message, got: ${result.messages}")
        }
    }
}
