@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("site.addzero.buildlogic.kmp.platform.kmp-test")
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
