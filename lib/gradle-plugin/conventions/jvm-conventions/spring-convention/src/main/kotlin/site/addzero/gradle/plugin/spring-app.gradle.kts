package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("site.addzero.gradle.plugin.spring-common")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
}
val libs = the<LibrariesForLibs>()

dependencies {
    implementation(libs.spring.boot.starter.web)
}

