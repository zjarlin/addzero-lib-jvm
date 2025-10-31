@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("kmp-ksp")
}
kotlin {
    sourceSets {
        jvmMain.dependencies {
            api(projects.lib.toolKmp.jdbc.toolJdbc) // PostgreSQL驱动
            implementation(libs.postgresql.driver) // PostgreSQL驱动
            implementation(libs.h2) // h2驱动
            implementation(libs.mysql.connector.java) // MySQL驱动
        }
    }
}
