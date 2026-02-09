package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.the

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

val libs = the<LibrariesForLibs>()

dependencies {
    val version = libs.versions.springBoot.get()
    implementation(platform(libs.org.springframework.boot.spring.boot.dependencies))

    testImplementation(libs.org.springframework.boot.boot.spring.boot.starter.test)
    testImplementation(libs.junit.junit.jupiter.api)
    testImplementation(libs.h2)
    testRuntimeOnly(libs.junit.junit.jupiter.engine)
    testImplementation(libs.spring.boot.starter.web)
}

description = "Spring Boot common utilities"
