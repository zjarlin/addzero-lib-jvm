@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.libs.kmp-ksp")
}

kotlin{
    dependencies {
        implementation(libs.gen.reified.core)
    }
}
