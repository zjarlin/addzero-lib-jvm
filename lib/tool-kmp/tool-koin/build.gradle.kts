@file:OptIn(ExperimentalKotlinGradlePluginApi::class, ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
     id("site.addzero.buildlogic.kmp.kmp-core")
}
val catalogLibs = versionCatalogs.named("libs")

kotlin {
    dependencies {
        implementation(project.dependencies.platform(libs.io.insert.koin.koin.bom))
        implementation(catalogLibs.findLibrary("io-insert-koin-koin-annotations").get())
        implementation(catalogLibs.findLibrary("io-insert-koin-koin-core").get())
    }
    sourceSets.jvmMain.dependencies {

        implementation(catalogLibs.findLibrary("cn-hutool-hutool-core").get())
    }
}
