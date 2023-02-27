plugins {
    id("build-logic.kotlin-dsl-published-gradle-plugin")
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))
}

pluginBundle {
    website = "https://github.com/vlsi/kotlin-argument-expression"
    vcsUrl = "https://github.com/vlsi/kotlin-argument-expression.git"
    tags = listOf("kotlin", "argument", "expression", "compiler", "plugin")
}

gradlePlugin {
    plugins {
        named("io.github.vlsi.kotlin-argument-expression-base") {
            displayName = "Base configuration of kotlin-argument-expression plugin"
            description = "Enables fine-tuning the behavior of kotlin-argument-expression plugin"
        }
        named("io.github.vlsi.kotlin-argument-expression") {
            displayName = "Default configuration for kotlin-argument-expression plugin"
            description = "Adds a default compiler plugin configuration, and a dependency on argument-expression-annotations"
        }
    }
}
