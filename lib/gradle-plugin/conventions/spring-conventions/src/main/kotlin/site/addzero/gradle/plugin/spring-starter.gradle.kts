package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.the

plugins {
    id("site.addzero.gradle.plugin.spring-common-convention")
    kotlin("plugin.spring")
}

val libs = the<LibrariesForLibs>()

dependencies {
    compileOnly(libs.spring.boot.starter.web)
    compileOnly(libs.spring.boot.autoconfigure)
    annotationProcessor(libs.boot.spring.boot.configuration.processor)
}
