plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
    id("site.addzero.buildlogic.jvm.jvm-koin")
}

dependencies {
    api(projects.lib.starterSpi)
    implementation(libs.io.insert.koin.koin.ktor)
    implementation(libs.io.ktor.ktor.server.core)
}
