@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("site.addzero.buildlogic.kmp.cmp-app")
    id("site.addzero.buildlogic.kmp.kmp-koin")
    id("site.addzero.buildlogic.kmp.kmp-json")
}

val libs = versionCatalogs.named("libs")
val desktopMainClass = "site.addzero.kcloud.MainKt"

kotlin {
    dependencies {
        implementation(project(":lib:compose:workbench-shell"))
                implementation(project(":apps:kcloud:shared"))
                implementation(project(":apps:kcloud:plugins:mcu-console"))
                implementation(project(":apps:kcloud:plugins:system:rbac"))
                implementation(project(":apps:kcloud:plugins:vibepocket"))
                implementation(libs.findLibrary("site-addzero-compose-native-component-searchbar").get())
                implementation(libs.findLibrary("site-addzero-compose-native-component-tree").get())
            }
    sourceSets {
    }
}

kotlin.jvm().mainRun {
    mainClass.set(desktopMainClass)
}

compose.desktop {
    application {
        mainClass = desktopMainClass
    }
}
