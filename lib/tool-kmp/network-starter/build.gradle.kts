plugins {
    id("site.addzero.buildlogic.kmp.kmp-ktor-client")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("de-jensklingenberg-ktorfit-ktorfit-lib").get())
            implementation(libs.findLibrary("site-addzero-tool-json").get())
        }
    }
}
