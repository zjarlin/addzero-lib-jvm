@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("kmp-ksp")
    id("kmp-json-withtool")
    id("kmp-koin")

}
kotlin {
    dependencies {
        implementation(libs.addzero.ksp.support)
        implementation(libs.addzero.ksp.easycode)
        implementation(libs.addzero.entity2analysed.support)

        implementation(libs.addzero.tool.koin)

    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.velocity.engine.core)
        }
    }

}
