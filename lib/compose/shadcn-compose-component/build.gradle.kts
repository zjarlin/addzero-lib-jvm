@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.cmp-lib")
    id("site.addzero.buildlogic.kmp.kmp-json-withtool")
    id("site.addzero.buildlogic.kmp.kmp-datetime")
}
kotlin {
    dependencies {
        implementation("site.addzero:tool-expect:2025.09.30")
        implementation(libs.io.coil.kt.coil3.coil.compose)
        implementation(libs.io.coil.kt.coil3.coil.network.ktor3)
    }
}
