package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs

plugins {
    id("site.addzero.gradle.plugin.spring-common")
    kotlin("plugin.spring")
}
val libs = the<LibrariesForLibs>()
val version = libs.versions.springBoot.get()
dependencies {
    compileOnly(libs.spring.boot.starter.web)
    compileOnly(libs.spring.boot.autoconfigure)
    annotationProcessor(libs.spring.boot.configurationProcessor)


}
