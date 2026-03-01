plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("site.addzero.buildlogic.jvm.jvm-koin")
    id("site.addzero.buildlogic.jvm.jvm-json-withtool")
}

dependencies {
    implementation(projects.lib.starterSpi)
    implementation(libs.io.ktor.ktor.server.core)
    implementation(libs.io.ktor.ktor.server.status.pages)
}
