package site.addzero.gradle.plugin

import org.gradle.api.artifacts.VersionCatalogsExtension
import site.addzero.gradle.tool.lib
import site.addzero.gradle.tool.ver
import org.gradle.kotlin.dsl.the

plugins {
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlin.multiplatform")
}

val libs = the<VersionCatalogsExtension>().named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.lib("org-jetbrains-kotlinx-kotlinx-serialization-json-json"))
            implementation(libs.lib("site-addzero-tool-json"))
        }
    }
}
