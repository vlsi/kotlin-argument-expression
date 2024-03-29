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
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.name.FqName

@AutoService(org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar::class)
// ComponentRegistrar is deprecated, see https://youtrack.jetbrains.com/issue/KT-52665
@Suppress("deprecation")
class ArgumentExpressionRegistrar(
    val argumentExpressionAnnotation: Set<FqName>
) : org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar {
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
