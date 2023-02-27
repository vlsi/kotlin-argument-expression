import com.github.vlsi.gradle.dsl.configureEach
import com.github.vlsi.gradle.publishing.dsl.simplifyXml

plugins {
    id("java-library")
    id("maven-publish")
    id("build-logic.publish-to-tmp-maven-repo")
    id("com.github.vlsi.gradle-extensions")
}

publishing {
    publications.configureEach<MavenPublication> {
        // Use the resolved versions in pom.xml
        // Gradle might have different resolution rules, so we set the versions
        // that were used in Gradle build/test.
        versionMapping {
            usage(Usage.JAVA_RUNTIME) {
                fromResolutionResult()
            }
            usage(Usage.JAVA_API) {
                fromResolutionOf("runtimeClasspath")
            }
        }
        pom {
            simplifyXml()
            val capitalizedName = project.name
            name.set(
                (project.findProperty("artifact.name") as? String) ?: "argument-expression $capitalizedName"
            )
            description.set(project.description ?: "Argument Expression $capitalizedName")
            inceptionYear.set("2023")
            url.set("https://jdbc.postgresql.org")
            licenses {
                license {
                    name.set("Apache-2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }
            organization {
                name.set("Vladimir Sitnikov")
                url.set("https://github.com/vlsi")
            }
            developers {
                developer {
                    id.set("vlsi")
                    name.set("Vladimir Sitnikov")
                }
            }
            issueManagement {
                system.set("GitHub issues")
                url.set("https://github.com/vlsi/argument-expression")
            }
            scm {
                connection.set("scm:git:https://github.com/vlsi/argument-expression.git")
                developerConnection.set("scm:git:https://github.com/vlsi/argument-expression.git")
                url.set("https://github.com/vlsi/argument-expression")
                tag.set("HEAD")
            }
        }
    }
}
