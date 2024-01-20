plugins {
    id("build-logic.kotlin-published-library")
    id("build-logic.test-junit5")
    kotlin("kapt")
}

dependencies {
    compileOnly("com.google.auto.service:auto-service-annotations:1.0.1")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable")

    kapt("com.google.auto.service:auto-service:1.0.1")
    testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable")
    testImplementation("com.github.tschuchortdev:kotlin-compile-testing:1.4.9")
}

tasks.compileKotlin {
    kotlinOptions.freeCompilerArgs += "-opt-in=org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi"
}
