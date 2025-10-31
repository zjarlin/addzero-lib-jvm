@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("kmp")
}

kotlin {
    mingwX64()
    jvm()
    wasmJs {
        nodejs()
    }
    macosArm64()
    iosArm64()
}
