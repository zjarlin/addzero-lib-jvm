@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("kmp-ksp")
    id("kmp-json-withtool")
    id("kmp-koin")

}
kotlin {
    dependencies {
        implementation(projects.lib.ksp.common.kspSupport)
        implementation(projects.lib.ksp.common.kspEasycode)
        implementation(projects.lib.ksp.metadata.entity2analysedSupport)

        implementation(projects.lib.toolKmp.toolKoin)

    }
    sourceSets {
        commonMain.dependencies {
            implementation("org.apache.velocity:velocity-engine-core:2.3")
        }
    }

}
