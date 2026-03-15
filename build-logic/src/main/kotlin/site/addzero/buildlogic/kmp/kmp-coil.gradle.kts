package site.addzero.buildlogic.kmp

plugins {
    id("site.addzero.buildlogic.kmp.kmp-convention")
}

val libs = versionCatalogs.named("libs")

kotlin {
    dependencies {
        implementation(libs.findLibrary("io-coil-kt-coil3-coil-compose").get())
        implementation(libs.findLibrary("io-coil-kt-coil3-coil-svg").get())
        implementation(libs.findLibrary("io-coil-kt-coil3-coil-network-ktor3").get())
    }
}
