@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("kmp-ksp")
}
kotlin {
    dependencies {

        implementation("site.addzero:addzero-ksp-support:2025.09.29")
        implementation("site.addzero:addzero-ioc-core:2025.09.29")
    }

    sourceSets {
        jvmMain.dependencies {
            implementation(libs.kotlinpoet.ksp)
        }
    }
}
