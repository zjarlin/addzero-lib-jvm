plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-json")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.findLibrary("io-insert-koin-koin-core").get())
            api(libs.findLibrary("org-jetbrains-kotlinx-kotlinx-coroutines-core").get())
            api(libs.findLibrary("org-jetbrains-compose-runtime-runtime").get())
        }
    }
}
