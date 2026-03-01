package site.addzero.gradle.plugin

import org.gradle.api.artifacts.VersionCatalogsExtension
import site.addzero.gradle.tool.lib
import site.addzero.gradle.tool.ver
import org.gradle.kotlin.dsl.the

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("de.jensklingenberg.ktorfit")
}

val libs = the<VersionCatalogsExtension>().named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.lib("de-jensklingenberg-ktorfit-ktorfit-lib"))
        }
    }
}
