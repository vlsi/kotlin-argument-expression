rootProject.name = "argument-expression-sample"

pluginManagement {
    plugins {
        fun String.v() = extra["$this.version"].toString()
        kotlin("jvm") version "kotlin".v()
        id("io.github.vlsi.kotlin-argument-expression") version "kotlin-argument-expression".v()
    }
    repositories {
        mavenLocal {
            content {
                includeGroupByRegex("io\\.github\\.vlsi.*")
            }
        }
        gradlePluginPortal()
    }
}

fun property(name: String) =
    when (extra.has(name)) {
        true -> extra.get(name) as? String
        else -> null
    }

if (property("includeBuild")?.ifEmpty { "false" }?.toBoolean() == true) {
   includeBuild("..")
}
