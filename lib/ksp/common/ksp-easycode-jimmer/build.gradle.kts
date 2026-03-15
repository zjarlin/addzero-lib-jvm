@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
    id("site.addzero.buildlogic.jvm.jvm-json-withtool")
    id("site.addzero.buildlogic.kmp.kmp-koin-core")

}
kotlin {
    dependencies {
        implementation(libs.site.addzero.ksp.support)
        implementation(libs.site.addzero.ksp.easycode)
        implementation(libs.site.addzero.entity2analysed.support)

        implementation(libs.site.addzero.tool.koin)

    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.org.apache.velocity.velocity.engine.core)
        }
    }

}
