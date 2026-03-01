package site.addzero.gradle.plugin

import org.gradle.api.artifacts.VersionCatalogsExtension
import site.addzero.gradle.tool.lib
import site.addzero.gradle.tool.ver
import org.gradle.kotlin.dsl.the

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.google.devtools.ksp")
}

val libs = the<VersionCatalogsExtension>().named("libs")

dependencies {
    kspCommonMainMetadata(libs.lib("io-insert-koin-koin-ksp-compiler"))
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.lib("io-insert-koin-koin-bom")))
            implementation(libs.lib("io-insert-koin-koin-annotations"))
            implementation(libs.lib("io-insert-koin-koin-core"))
            implementation(libs.lib("io-insert-koin-koin-compose"))
            implementation(libs.lib("io-insert-koin-koin-compose-viewmodel"))
            implementation(libs.lib("io-insert-koin-koin-compose-viewmodel-navigation"))
        }
    }
}
