plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.findLibrary("app-sidebar").get())
            api(libs.findLibrary("shadcn-compose-component").get())
        }
    }
}
