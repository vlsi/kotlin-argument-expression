package io.github.vlsi.ae

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration

@AutoService(CommandLineProcessor::class)
class ArgumentExpressionCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = "io.github.vlsi.argument-expression"

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
