package site.addzero.buildlogic.kmp

plugins {
    id("site.addzero.buildlogic.kmp.cmp-desktop")
}

val libs = versionCatalogs.named("libs")

kotlin {
    dependencies {
        implementation(libs.findLibrary("org-jetbrains-androidx-navigation3-navigation3-ui").get())
        implementation(libs.findLibrary("org-jetbrains-androidx-lifecycle-lifecycle-viewmodel-navigation3").get())
    }
    sourceSets {
        commonTest.dependencies {
            implementation(libs.findLibrary("io-kotest-kotest-property").get())
            implementation(libs.findLibrary("io-kotest-kotest-assertions-core").get())
        }
    }
}

