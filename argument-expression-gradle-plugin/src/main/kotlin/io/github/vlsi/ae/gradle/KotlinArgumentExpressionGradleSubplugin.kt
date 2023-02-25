package io.github.vlsi.ae.gradle

import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class KotlinArgumentExpressionGradleSubplugin : KotlinCompilerPluginSupportPlugin {
    companion object {
        const val ARGUMENT_EXPRESSION_ANNOTATION_OPTION = "argument-expression-annotation"
    }

    override fun getCompilerPluginId(): String =
        "io.github.vlsi.argument-expression"

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(
            groupId = "io.github.vlsi.ae",
            artifactId = "argument-expression-gradle-plugin",
            version = "1.0.0"
        )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean =
        true

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val extension = project.the<KotlinArgumentExpressionExtension>()
        return extension.argumentExpressionAnnotations.map { annotations ->
            annotations.map {
                SubpluginOption(key = ARGUMENT_EXPRESSION_ANNOTATION_OPTION, value = it)
            }
        }
    }
}
