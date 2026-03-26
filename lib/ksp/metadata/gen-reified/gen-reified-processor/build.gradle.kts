@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}

kotlin{
    dependencies {
        implementation(libs.site.addzero.gen.reified.core)
    }
}
