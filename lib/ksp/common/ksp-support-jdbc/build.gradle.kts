@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.kmp-ksp-plugin")
}
val libs = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        jvmMain.dependencies {
            api(libs.findLibrary("site-addzero-tool-jdbc").get()) // PostgreSQL驱动
            implementation(libs.findLibrary("org-postgresql-postgresql").get()) // PostgreSQL驱动
            implementation(libs.findLibrary("com-h2database-h2").get()) // h2驱动
            implementation(libs.findLibrary("mysql-mysql-connector-java").get()) // MySQL驱动
        }
    }
}
