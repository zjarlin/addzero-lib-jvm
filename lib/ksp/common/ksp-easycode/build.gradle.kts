@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("kmp-ksp")
    id("kmp-json-withtool")
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
