package site.addzero.spring

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("site.addzero.jvm.kotlin-convention")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
}
val libs = the<LibrariesForLibs>()
dependencies {
    implementation(platform(libs.spring.bom))
}


