package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.the

plugins {
    id("site.addzero.gradle.plugin.kotlin-convention")
}

val libs = the<LibrariesForLibs>()

dependencies {
    val version = libs.versions.springBoot.get()
    implementation(platform(libs.spring.bom))

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.h2)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.spring.boot.starter.web)
}

description = "Spring Boot common utilities"
