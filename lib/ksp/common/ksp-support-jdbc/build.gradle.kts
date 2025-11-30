@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("kmp-ksp")
}
kotlin {
    sourceSets {
        jvmMain.dependencies {
            api("site.addzero:addzero-tool-jdbc:2025.09.29") // PostgreSQL驱动
            implementation(libs.postgresql.driver) // PostgreSQL驱动
            implementation(libs.h2) // h2驱动
            implementation(libs.mysql.connector.java) // MySQL驱动
        }
    }
}
