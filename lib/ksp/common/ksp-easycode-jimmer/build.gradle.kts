@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
    id("site.addzero.buildlogic.jvm.jvm-json-withtool")
    id("site.addzero.gradle.plugin.kmp-koin-convention")

}
kotlin {
    dependencies {
        implementation(libs.site.addzero.addzero.ksp.support)
        implementation(libs.site.addzero.addzero.ksp.easycode)
        implementation(libs.site.addzero.addzero.entity2analysed.support)

        implementation(libs.site.addzero.tool.koin)

    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.org.apache.velocity.velocity.engine.core)
        }
    }

}
