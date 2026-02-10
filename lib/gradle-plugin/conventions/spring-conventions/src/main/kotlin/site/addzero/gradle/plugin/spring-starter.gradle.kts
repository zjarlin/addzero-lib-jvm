package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.the

plugins {
    id("site.addzero.gradle.plugin.spring-common-convention")
    kotlin("plugin.spring")
}

val libs = the<LibrariesForLibs>()

dependencies {
    compileOnly(libs.org.springframework.boot.spring.boot.starter.web)
    compileOnly(libs.org.springframework.boot.spring.boot.autoconfigure)
    annotationProcessor(libs.org.springframework.boot.spring.boot.configuration.processor.v2)
}
