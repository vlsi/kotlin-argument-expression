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
import io.github.vlsi.kae.gradle.KotlinArgumentExpressionExtension

plugins {
    id("io.github.vlsi.kotlin-argument-expression-base")
}

plugins.withId("java") {
    dependencies {
        "implementation"("io.github.vlsi.kae:argument-expression-annotations:1.0.0")
    }
}

configure<KotlinArgumentExpressionExtension> {
    argumentExpressionAnnotations.add("io.github.vlsi.kae.CallerArgumentExpression")
}