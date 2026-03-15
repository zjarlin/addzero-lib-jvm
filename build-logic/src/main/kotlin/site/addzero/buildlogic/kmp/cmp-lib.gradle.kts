package site.addzero.buildlogic.kmp

plugins {
    id("site.addzero.buildlogic.kmp.cmp-android-lib")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("site.addzero.buildlogic.kmp.kmp-core")
}

val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("org-jetbrains-compose-runtime-runtime").get())
            implementation(libs.findLibrary("org-jetbrains-compose-foundation-foundation").get())
            implementation(libs.findLibrary("org-jetbrains-compose-material3-material3").get())
            implementation(libs.findLibrary("org-jetbrains-compose-ui-ui").get())
            implementation(libs.findLibrary("org-jetbrains-compose-components-components-resources").get())
            implementation(libs.findLibrary("org-jetbrains-compose-ui-ui-tooling-preview").get())
            implementation(libs.findLibrary("org-jetbrains-androidx-lifecycle-lifecycle-viewmodel-compose").get())
            implementation(libs.findLibrary("org-jetbrains-androidx-lifecycle-lifecycle-runtime-compose").get())
            implementation(libs.findLibrary("org-jetbrains-compose-material-material-icons-extended").get())
        }
    }
}
