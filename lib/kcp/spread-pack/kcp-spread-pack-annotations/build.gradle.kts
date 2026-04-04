@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("site.addzero.buildlogic.kmp.kmp-convention")
}

kotlin {
    jvm()
    wasmJs {
        browser()
    }
}
