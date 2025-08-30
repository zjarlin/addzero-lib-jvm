@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    id("kmp-compose-common")
    id("kmp-desktop")
    id("kmp-ios-app")
    id("kmp-wasm")
    id("kmp-test")
    id("kmp-android-app")
    alias(libs.plugins.composeHotReload)
}
kotlin {

       wasmJs {
        outputModuleName.set("composeApp")
        binaries.executable()
    }


    sourceSets {
        commonMain.dependencies {
            implementation(projects.shared)
        }
    }
}
