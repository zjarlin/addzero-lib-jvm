@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("kmp-ksp")
    id("kmp-json-withtool")
}


kotlin {
    dependencies {
        implementation("site.addzero:addzero-ksp-support:2025.09.29")

    }
    sourceSets {
        commonMain.dependencies {
            implementation("org.apache.velocity:velocity-engine-core:2.3")
        }
    }

}
