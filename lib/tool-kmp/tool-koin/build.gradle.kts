@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
     id("site.addzero.buildlogic.kmp.kmp-core")
}

kotlin {
    dependencies {
        implementation(project.dependencies.platform(libs.io.insert.koin.koin.bom))
        implementation(libs.io.insert.koin.koin.annotations)
        implementation(libs.io.insert.koin.koin.core)
    }
    sourceSets.jvmMain.dependencies {

        implementation(libs.cn.hutool.hutool.core)
    }
}
