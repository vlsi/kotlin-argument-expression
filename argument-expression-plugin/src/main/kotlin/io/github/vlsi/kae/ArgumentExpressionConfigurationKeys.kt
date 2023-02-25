package io.github.vlsi.kae

import org.jetbrains.kotlin.config.CompilerConfigurationKey

object ArgumentExpressionConfigurationKeys {
    val ARGUMENT_EXPRESSION_ANNOTATION: CompilerConfigurationKey<List<String>> =
        CompilerConfigurationKey.create("argument expression annotation qualified name")
}
