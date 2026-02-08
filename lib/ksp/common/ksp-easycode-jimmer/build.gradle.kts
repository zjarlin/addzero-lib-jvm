@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
    id("site.addzero.buildlogic.jvm.jvm-json-withtool")
    id("site.addzero.gradle.plugin.kmp-koin-convention")

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
