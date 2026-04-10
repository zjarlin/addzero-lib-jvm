plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.findLibrary("app-sidebar").get())
            implementation(libs.findLibrary("io-insert-koin-koin-compose").get())
        }
    }
}
version="2026.04.11"
