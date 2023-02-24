val buildVersion = "${findProperty("version")}"

println("Building Argument Expression $buildVersion")

allprojects {
    group = "io.github.vlsi.argument-expression"
    version = project.findProperty("version") as? String ?: rootProject.version
}
