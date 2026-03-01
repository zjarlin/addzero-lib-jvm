plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("site.addzero.buildlogic.jvm.jvm-koin")
}

dependencies {
    implementation(projects.lib.starterSpi)
    implementation(libs.io.ktor.ktor.server.core)
    implementation(libs.site.addzero.ktor.banner)
}
