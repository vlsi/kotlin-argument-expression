plugins {
    id("build-logic.root-build")
}

val buildVersion = "${findProperty("version")}"

println("Building Argument Expression $buildVersion")

allprojects {
    group = "io.github.vlsi.kae"
    version = project.findProperty("version") as? String ?: rootProject.version
}
