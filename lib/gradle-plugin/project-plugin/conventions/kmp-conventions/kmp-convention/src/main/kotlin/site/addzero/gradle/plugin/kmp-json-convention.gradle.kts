package site.addzero.gradle.plugin
plugins {
    id("site.addzero.gradle.plugin.kmp-convention")
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.10.0-RC")
        }
    }
}