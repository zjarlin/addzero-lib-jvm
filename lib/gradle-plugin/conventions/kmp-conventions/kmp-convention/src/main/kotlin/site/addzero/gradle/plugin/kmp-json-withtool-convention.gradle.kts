package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.the

plugins {
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlin.multiplatform")
}

val libs = the<LibrariesForLibs>()

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.tool.json)
        }
    }
}
