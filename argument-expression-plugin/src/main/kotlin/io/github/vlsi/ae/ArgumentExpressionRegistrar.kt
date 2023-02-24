package io.github.vlsi.ae

import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.name.FqName

object ArgumentExpressionConfigurationKeys {
    val ARGUMENT_EXPRESSION_ANNOTATION: CompilerConfigurationKey<List<String>> =
        CompilerConfigurationKey.create("argument expression annotation qualified name")
}

@AutoService(ComponentRegistrar::class)
class ArgumentExpressionRegistrar(
    val argumentExpressionAnnotation: Set<FqName>
) : ComponentRegistrar {
    @Suppress("unused")
    constructor() : this(emptySet()) // Used by service loader

    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {
        val annotations =
            configuration.get(ArgumentExpressionConfigurationKeys.ARGUMENT_EXPRESSION_ANNOTATION)
                ?.mapTo(mutableSetOf()) { FqName(it) }
                ?: argumentExpressionAnnotation
        if (annotations.isEmpty()) {
            return
        }
        val messageCollector =
            configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
        IrGenerationExtension.registerExtension(
            project,
            ArgumentExpressionIrGenerationExtension(
                messageCollector,
                annotations
            )
        )
    }
}
