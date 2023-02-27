import com.github.vlsi.gradle.dsl.configureEach
import de.thetaphi.forbiddenapis.gradle.CheckForbiddenApis

plugins {
    id("de.thetaphi.forbiddenapis")
}

forbiddenApis {
    failOnUnsupportedJava = false
    // ForbiddenApiException: Check for forbidden API calls failed while scanning class 'Dev_sigstore_sign_base_gradle'
    // (dev.sigstore.sign-base.gradle.kts): java.lang.ClassNotFoundException: kotlin.script.experimental.jvm.RunnerKt
    // (while looking up details about referenced class 'kotlin.script.experimental.jvm.RunnerKt')
    failOnMissingClasses = false
    signaturesFiles = files("$rootDir/config/forbidden-apis/forbidden-apis.txt")
    bundledSignatures.addAll(
        listOf(
            // "jdk-deprecated",
            "jdk-internal",
            "jdk-non-portable"
            // "jdk-system-out"
            // "jdk-unsafe"
        )
    )
}

tasks.configureEach<CheckForbiddenApis> {
    exclude("**/io/github/vlsi/**/internal/Unsafe.class")
}
