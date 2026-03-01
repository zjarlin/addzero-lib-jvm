@file:OptIn(ExperimentalWasmDsl::class)

package site.addzero.gradle.plugin

import org.gradle.api.artifacts.VersionCatalogsExtension
import site.addzero.gradle.tool.lib
import site.addzero.gradle.tool.ver
import org.gradle.kotlin.dsl.the
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("org.jetbrains.kotlin.multiplatform")
}

val libs = the<VersionCatalogsExtension>().named("libs")

kotlin {
    sourceSets {
        commonTest.dependencies {
            implementation(libs.lib("org-jetbrains-kotlin-kotlin-test"))
        }
    }
}
