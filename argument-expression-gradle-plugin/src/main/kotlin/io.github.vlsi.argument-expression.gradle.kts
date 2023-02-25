import io.github.vlsi.kae.gradle.KotlinArgumentExpressionExtension

plugins {
    id("io.github.vlsi.argument-expression-base")
}

plugins.withId("java-library") {
    dependencies {
        "implementation"("io.github.vlsi.kae:argument-expression-annotations:1.0.0")
    }
}

configure<KotlinArgumentExpressionExtension> {
    argumentExpressionAnnotations.add("io.github.vlsi.kae.ArgumentExpression")
}
