plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("site.addzero.buildlogic.jvm.jvm-koin")
    id("site.addzero.buildlogic.jvm.jvm-json-withtool")
}
val libs = versionCatalogs.named("libs")

dependencies {
    implementation(projects.lib.starterSpi)
    implementation(libs.findLibrary("io-ktor-ktor-server-core").get())
    implementation(libs.findLibrary("io-ktor-ktor-server-status-pages").get())
}
