@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    id("kmp-convention")
}

kotlin {
    jvm()
    mingwX64()
    linuxX64()
    macosX64()
    macosArm64()
}