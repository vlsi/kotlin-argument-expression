rootProject.name = "kotlin-argument-expression"

pluginManagement {
    plugins {
        kotlin("jvm") version "1.7.10"
        kotlin("kapt") version "1.7.10"
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

includeBuild("build-logic")

include("argument-expression-plugin")
