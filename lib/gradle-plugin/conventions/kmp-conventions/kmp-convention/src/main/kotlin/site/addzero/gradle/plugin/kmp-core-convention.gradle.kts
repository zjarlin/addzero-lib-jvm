@file:OptIn(ExperimentalWasmDsl::class)

package site.addzero.gradle.plugin

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("site.addzero.gradle.plugin.kmp-convention")
}

kotlin {
    mingwX64()
    jvm()
    wasmJs {
        browser()
    }
}
