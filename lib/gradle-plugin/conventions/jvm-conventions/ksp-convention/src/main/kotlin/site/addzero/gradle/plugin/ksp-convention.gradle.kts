package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("site.addzero.gradle.plugin.kotlin-convention")
}

val libs = the<LibrariesForLibs>()

kotlin {
    dependencies {
        implementation(libs.ksp.symbol.processing.api)
    }
}
