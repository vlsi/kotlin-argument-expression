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
package io.github.vlsi.kae.gradle

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
        "io.github.vlsi.kotlin-argument-expression-base"

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(
            groupId = "io.github.vlsi.kae",
            artifactId = "argument-expression-plugin",
            version = "1.0.1"
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
