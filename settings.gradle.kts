rootProject.name = "kotlin-argument-expression"

pluginManagement {
    plugins {
        kotlin("jvm") version "1.6.21"
        kotlin("kapt") version "1.6.21"
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal {
            content {
                includeGroupByRegex("io\\.github\\.vlsi.*")
            }
        }
    }
}

includeBuild("build-logic-commons")
includeBuild("build-logic")

include("argument-expression-annotations")
include("argument-expression-plugin")
include("argument-expression-gradle-plugin")
