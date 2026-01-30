@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("site.addzero.gradle.plugin.kmp-core-convention") version "+"
}

kotlin {
    dependencies {
        implementation(project.dependencies.platform(libs.koin.bom))
        implementation(libs.koin.annotations)
        implementation(libs.koin.core)
    }
    sourceSets.jvmMain.dependencies {

        implementation(libs.hutool.core)
    }
}
