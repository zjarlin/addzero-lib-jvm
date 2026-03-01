package site.addzero.gradle.plugin

import org.gradle.api.artifacts.VersionCatalogsExtension
import site.addzero.gradle.tool.lib
import site.addzero.gradle.tool.ver
import org.gradle.kotlin.dsl.the

plugins {
    id("site.addzero.gradle.plugin.kmp-convention")
    kotlin("plugin.serialization")
}

val libs = the<VersionCatalogsExtension>().named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.lib("org-jetbrains-kotlinx-kotlinx-serialization-json-json"))
        }
    }
}
