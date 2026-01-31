package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    kotlin("plugin.serialization")
    id("site.addzero.gradle.plugin.kotlin-convention") version "+"
}

val libs = the<LibrariesForLibs>()

dependencies {
    implementation(libs.kotlinx.serialization)
}
