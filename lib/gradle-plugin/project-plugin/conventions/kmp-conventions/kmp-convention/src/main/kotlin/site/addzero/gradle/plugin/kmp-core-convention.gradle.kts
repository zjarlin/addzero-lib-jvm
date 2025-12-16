@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("kmp-convention")
}

kotlin {
    mingwX64()
    jvm()
    wasmJs {
        nodejs()
    }
}