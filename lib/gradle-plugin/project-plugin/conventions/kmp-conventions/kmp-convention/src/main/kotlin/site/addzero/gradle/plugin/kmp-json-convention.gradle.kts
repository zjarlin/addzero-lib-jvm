package site.addzero.gradle.plugin
plugins {
    id("site.addzero.gradle.plugin.kmp-convention")
    kotlin("plugin.serialization")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.core)
        }
    }
}