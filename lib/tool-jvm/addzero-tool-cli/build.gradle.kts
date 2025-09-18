@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("kmp-json")
    id("kmp-koin-core")
    id("ksp4self")
}

kotlin {
    macosX64()
    macosArm64()
    mingwX64()
    linuxX64()

    dependencies {
        implementation(libs.kotlinx.coroutines.core)
        implementation(libs.kotlinx.cli)
    }

}
