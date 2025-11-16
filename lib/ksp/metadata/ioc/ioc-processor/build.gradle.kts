@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("kmp-ksp")
}
kotlin {
    dependencies {

        implementation(projects.lib.ksp.common.kspSupport)
        implementation(projects.lib.ksp.metadata.ioc.iocCore)
    }

    sourceSets {
        jvmMain.dependencies {
            implementation(libs.kotlinpoet.ksp)
        }
    }
}
