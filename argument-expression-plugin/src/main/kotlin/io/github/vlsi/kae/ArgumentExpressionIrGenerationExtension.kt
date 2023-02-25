package io.github.vlsi.kae

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.name.FqName

class ArgumentExpressionIrGenerationExtension(
    private val messageCollector: MessageCollector,
    private val functions: Set<FqName>
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        for (file in moduleFragment.files) {
            ArgumentExpressionCallTransformer(
                SourceFile(file),
                pluginContext,
                messageCollector,
                functions
            )
                .visitFile(file)
        }
    }
}
