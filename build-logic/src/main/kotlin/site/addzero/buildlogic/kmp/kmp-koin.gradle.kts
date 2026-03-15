package site.addzero.buildlogic.kmp

plugins {
    id("site.addzero.buildlogic.kmp.kmp-koin-core")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("io-insert-koin-koin-compose").get())
            implementation(libs.findLibrary("io-insert-koin-koin-compose-viewmodel").get())
            implementation(libs.findLibrary("io-insert-koin-koin-compose-viewmodel-navigation").get())
        }
    }
}
