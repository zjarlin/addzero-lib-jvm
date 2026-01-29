package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("site.addzero.gradle.plugin.jvm-json-convention")
}

val libs = the<LibrariesForLibs>()

dependencies {
    implementation(libs.tool.json)
}
