@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("kmp-convention")
}

kotlin {
    wasmJs {
        nodejs()
        browser()
    }
}