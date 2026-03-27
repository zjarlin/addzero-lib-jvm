plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("site-addzero-ksp-support").get())
            implementation(libs.findLibrary("cn-hutool-hutool-all").get())
        }
    }
}
