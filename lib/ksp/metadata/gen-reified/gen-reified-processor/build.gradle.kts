@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("kmp-ksp")
}

kotlin{
    dependencies {
        implementation("site.addzero:gen-reified-core:2026.01.01")
    }
}
