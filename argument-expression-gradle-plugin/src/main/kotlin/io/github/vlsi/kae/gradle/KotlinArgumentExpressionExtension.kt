package io.github.vlsi.kae.gradle

import org.gradle.api.provider.SetProperty

abstract class KotlinArgumentExpressionExtension {
    companion object {
        const val NAME = "kotlinArgumentExpression"
    }

    abstract val argumentExpressionAnnotations: SetProperty<String>
}
