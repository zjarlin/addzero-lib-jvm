plugins {
     id("site.addzero.buildlogic.kmp.kmp-core")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.findLibrary("tool-enum").get())
        }
    }
}
