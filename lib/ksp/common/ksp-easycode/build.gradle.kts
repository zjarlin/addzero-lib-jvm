@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.gradle.plugin.kmp-ksp-convention")
    id("site.addzero.gradle.plugin.kmp-json-withtool-convention")
}


kotlin {
    dependencies {
        implementation(libs.addzero.ksp.support)

    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.velocity.engine.core)
        }
    }

}
