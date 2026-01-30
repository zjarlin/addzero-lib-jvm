@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.gradle.plugin.kmp-ksp-convention")
}

kotlin{
    dependencies {
        implementation(libs.gen.reified.core)
    }
}
