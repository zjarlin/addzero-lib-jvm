plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        jvmMain.dependencies {
            implementation(libs.findLibrary("app-sidebar").get())
        }
    }
}
