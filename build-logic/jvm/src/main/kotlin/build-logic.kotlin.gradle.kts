//import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-library")
    id("build-logic.java")
    id("build-logic.test-base")
    id("com.github.vlsi.gradle-extensions")
    id("com.github.autostyle")
    kotlin("jvm")
}

java {
    withSourcesJar()
}

tasks.withType<KotlinCompile>().configureEach {
// TODO: add when upgrade to Kotlin 1.8+
//    compilerOptions {
//        jvmTarget.set(JvmTarget.JVM_1_8)
//    }
}

autostyle {
    kotlin {
        file("$rootDir/config/license-header.txt").takeIf { it.exists() }?.let {
            licenseHeader(it.readText())
        }
        trimTrailingWhitespace()
        endWithNewline()
    }
}
