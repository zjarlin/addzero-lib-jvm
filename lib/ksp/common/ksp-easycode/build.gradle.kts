@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.libs.kmp-ksp")
    id("site.addzero.buildlogic.kmp.composition.kmp-json-withtool")
}


kotlin {
    dependencies {
        implementation(libs.addzero.ksp.support)

    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.velocity.engine.core)
        }
    }

}
