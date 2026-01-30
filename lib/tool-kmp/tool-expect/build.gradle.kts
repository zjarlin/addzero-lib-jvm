@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("site.addzero.gradle.plugin.kmp-test-convention")
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
