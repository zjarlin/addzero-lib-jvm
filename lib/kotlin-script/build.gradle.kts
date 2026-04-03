plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}
val catalogLibs = versionCatalogs.named("libs")

dependencies {
    implementation(kotlin("scripting-jvm"))
    implementation(kotlin("scripting-jvm-host"))
    testImplementation(catalogLibs.findLibrary("org-jetbrains-kotlin-kotlin-test").get())
    testImplementation(catalogLibs.findLibrary("org-junit-jupiter-junit-jupiter").get())
}

description = "Kotlin script template utilities"
