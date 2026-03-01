@file:OptIn(ExperimentalWasmDsl::class)

package site.addzero.gradle.plugin

import org.gradle.api.artifacts.VersionCatalogsExtension
import site.addzero.gradle.tool.lib
import site.addzero.gradle.tool.ver
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import site.addzero.gradle.BuildSettings

val libs = the<VersionCatalogsExtension>().named("libs")

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(libs.ver("jdk")))
        }
    }
}

android {
    namespace = BuildSettings.PACKAGE_NAME
    compileSdk = libs.ver("android-compileSdk").toInt()

    defaultConfig {
        minSdk = libs.ver("android-minSdk").toInt()
    }
    compileOptions {
        val toVersion = JavaVersion.toVersion(libs.ver("jdk"))
        sourceCompatibility = toVersion
        targetCompatibility = toVersion
    }
}
