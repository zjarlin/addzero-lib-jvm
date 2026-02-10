package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.the

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

val libs = the<LibrariesForLibs>()

dependencies {
    val version = libs.versions.springBoot.get()
    implementation(platform(libs.org.springframework.boot.spring.boot.dependencies.v2))

    testImplementation(libs.org.springframework.boot.boot.spring.boot.starter.test)
    testImplementation(libs.junit.junit.junit.jupiter.api)
    testImplementation(libs.com.h2database.h2)
    testRuntimeOnly(libs.junit.junit.junit.jupiter.engine)
    testImplementation(libs.org.springframework.boot.spring.boot.starter.web)
}

description = "Spring Boot common utilities"
