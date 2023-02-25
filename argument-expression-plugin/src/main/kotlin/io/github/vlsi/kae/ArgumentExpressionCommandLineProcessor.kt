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

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(CommandLineProcessor::class)
class ArgumentExpressionCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = "io.github.vlsi.argument-expression-base"

    override val pluginOptions: Collection<CliOption> = listOf(
        CliOption(
            optionName = "argument-expression-annotation",
            valueDescription = "function full-qualified annotation name",
            description = "FQ name of the annotation to be used as an argument expression",
            required = false,
            allowMultipleOccurrences = true
        )
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        return when (option.optionName) {
            "argument-expression-annotation" -> configuration.add(
                ArgumentExpressionConfigurationKeys.ARGUMENT_EXPRESSION_ANNOTATION,
                value
            )

            else -> error("Unexpected config option ${option.optionName}")
        }
    }
}
