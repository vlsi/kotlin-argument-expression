plugins {
    id("build-logic.root-build")
    id("com.github.vlsi.stage-vote-release") version "1.86"
}

val snapshotSuffix = rootProject.releaseParams.snapshotSuffix

allprojects {
    group = "io.github.vlsi.kae"
    version = (project.findProperty("version") as? String ?: rootProject.version.toString()) +
            snapshotSuffix
}

println("Building Argument Expression $version")

releaseParams {
    tlp.set("kotlin-argument-expression")
    organizationName.set("vlsi")
    componentName.set("kotlin-argument-expression")
    prefixForProperties.set("s01")
    svnDistEnabled.set(false)
    sitePreviewEnabled.set(false)
    nexus {
        packageGroup.set("io.github.vlsi")
        prodUrl.set(uri("https://s01.oss.sonatype.org"))
    }
    voteText.set {
        """
        ${it.componentName} v${it.version}-rc${it.rc} is ready for preview.

        Git SHA: ${it.gitSha}
        Staging repository: ${it.nexusRepositoryUri}
        """.trimIndent()
    }
}
