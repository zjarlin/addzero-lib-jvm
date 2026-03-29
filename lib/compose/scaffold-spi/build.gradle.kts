plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":lib:compose:app-sidebar"))
            implementation(libs.findLibrary("io-insert-koin-koin-compose").get())
        }
    }
}
