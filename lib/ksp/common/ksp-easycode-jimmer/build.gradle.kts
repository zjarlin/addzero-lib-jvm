@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("kmp-ksp")
    id("kmp-json-withtool")
    id("kmp-koin")

}
kotlin {
    dependencies {
        implementation("site.addzero:addzero-ksp-support:2025.09.29")
        implementation("site.addzero:addzero-ksp-easycode:2025.09.29")
        implementation("site.addzero:addzero-entity2analysed-support:2025.09.29")

        implementation("site.addzero:addzero-tool-koin:2025.09.29")

    }
    sourceSets {
        commonMain.dependencies {
            implementation("org.apache.velocity:velocity-engine-core:2.3")
        }
    }

}
