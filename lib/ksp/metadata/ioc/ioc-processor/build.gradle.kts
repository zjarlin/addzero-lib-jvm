plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}
val libs = versionCatalogs.named("libs")

kotlin {

    sourceSets {
        commonMain.dependencies {
//            implementation(libs.findLibrary("site-addzero-ksp-support").get())
            implementation(libs.findLibrary("site-addzero-ioc-core").get())
            implementation(libs.findLibrary("site-addzero-lsi-ksp").get())
        }
    }
}

//version="2026.02.18"
