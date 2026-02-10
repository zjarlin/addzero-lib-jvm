@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp")
}
kotlin {
    sourceSets {
        jvmMain.dependencies {
            api(libs.site.addzero.tool.jdbc) // PostgreSQL驱动
            implementation(libs.org.postgresql.postgresql) // PostgreSQL驱动
            implementation(libs.com.h2database.h2) // h2驱动
            implementation(libs.mysql.mysql.connector.java) // MySQL驱动
        }
    }
}
