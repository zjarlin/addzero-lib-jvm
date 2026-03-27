@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")

}
val libs = versionCatalogs.named("libs")

kotlin {
    dependencies {
        implementation(libs.findLibrary("site-addzero-ksp-support").get())
        implementation(libs.findLibrary("site-addzero-ksp-easycode").get())
        implementation(libs.findLibrary("site-addzero-entity2analysed-support").get())

        implementation(libs.findLibrary("site-addzero-tool-koin-v2025").get())

    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.findLibrary("org-apache-velocity-velocity-engine-core").get())
        }
    }

}
