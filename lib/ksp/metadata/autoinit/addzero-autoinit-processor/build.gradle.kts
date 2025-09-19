@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("kmp-ksp")
}
kotlin {
    dependencies {

        implementation(projects.lib.ksp.common.addzeroKspSupport)
        implementation(projects.lib.ksp.metadata.autoinit.addzeroAutoinitCore)
    }

    sourceSets {
        jvmMain.dependencies {
            implementation(libs.kotlinpoet.ksp)
        }
    }
}