@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.gradle.plugin.kmp-ksp-convention")
}
kotlin {
    sourceSets {
        jvmMain.dependencies {
            api(libs.addzero.tool.jdbc) // PostgreSQL驱动
            implementation(libs.postgresql) // PostgreSQL驱动
            implementation(libs.h2) // h2驱动
            implementation(libs.mysql.connector.java) // MySQL驱动
        }
    }
}
