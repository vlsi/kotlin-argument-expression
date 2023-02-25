import io.github.vlsi.ae.gradle.KotlinArgumentExpressionExtension

plugins {
    id("io.github.vlsi.argument-expression-base")
}

plugins.withId("java-library") {
    dependencies {
        "implementation"("io.github.vlsi.ae:argument-expression-annotations:1.0.0")
    }
}

configure<KotlinArgumentExpressionExtension> {
    argumentExpressionAnnotations.add("io.github.vlsi.ae.ArgumentExpression")
}
