import com.github.vlsi.gradle.crlf.CrLfSpec
import com.github.vlsi.gradle.crlf.LineEndings
import com.github.vlsi.gradle.dsl.configureEach
import com.github.vlsi.jandex.JandexTask

plugins {
    id("java")
    id("com.github.vlsi.crlf")
    id("com.github.vlsi.gradle-extensions")
    id("build-logic.test-base")
    id("build-logic.build-params")
    id("build-logic.style")
    id("com.github.vlsi.jandex")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

sourceSets {
    main {
        resources {
            // TODO: remove when LICENSE is removed (it is used by Maven build for now)
            exclude("META-INF/LICENSE")
        }
    }
}

project.configure<com.github.vlsi.jandex.JandexExtension> {
    skipIndexFileGeneration()
}

tasks.withType<PluginUnderTestMetadata>().configureEach {
    dependsOn(tasks.withType<JandexTask>())
}

if (!buildParameters.enableGradleMetadata) {
    tasks.configureEach<GenerateModuleMetadata> {
        enabled = false
    }
}

if (buildParameters.coverage || gradle.startParameter.taskNames.any { it.contains("jacoco") }) {
    apply(plugin = "build-logic.jacoco")
}

tasks.configureEach<JavaCompile> {
    inputs.property("java.version", System.getProperty("java.version"))
    inputs.property("java.vm.version", System.getProperty("java.vm.version"))
    options.apply {
        encoding = "UTF-8"
        compilerArgs.add("-Xlint:deprecation")
        if (JavaVersion.current().isJava9Compatible) {
            // See https://bugs.openjdk.org/browse/JDK-8032211
            // Don't issue deprecation warnings on import statements is resolved in Java 9+
            //compilerArgs.add("-Werror")
        }
    }
}

// Add default license/notice when missing (e.g. see :src:config that overrides LICENSE)

afterEvaluate {
    tasks.configureEach<Jar> {
        CrLfSpec(LineEndings.LF).run {
            into("META-INF") {
                filteringCharset = "UTF-8"
                duplicatesStrategy = DuplicatesStrategy.EXCLUDE
                from("$rootDir/LICENSE")
                from("$rootDir/NOTICE")
            }
        }
    }
}

tasks.configureEach<Jar> {
    manifest {
        attributes["Bundle-License"] = "Apache-2.0"
        attributes["Implementation-Title"] = "Argument Expression Compiler Plugin"
        attributes["Implementation-Version"] = project.version
        attributes["Implementation-Vendor"] = "Vladimir Sitnikov"
        attributes["Implementation-Vendor-Id"] = "sitnikov.vladimir@gmail.com"
    }
}
