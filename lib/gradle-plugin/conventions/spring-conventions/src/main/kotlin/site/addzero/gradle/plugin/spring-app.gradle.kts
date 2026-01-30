package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.the

plugins {
    id("site.addzero.gradle.plugin.spring-common-convention")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
}

val libs = the<LibrariesForLibs>()

dependencies {
    implementation(libs.spring.boot.starter.web)
}
