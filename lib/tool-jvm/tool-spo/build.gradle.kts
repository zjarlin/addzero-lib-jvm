plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

val catalogLibs = versionCatalogs.named("libs")

dependencies {
    implementation(catalogLibs.findLibrary("cn-hutool-hutool-core").get())
}
