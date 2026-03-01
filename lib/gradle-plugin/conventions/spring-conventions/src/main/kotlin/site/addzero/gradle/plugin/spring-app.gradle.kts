package site.addzero.gradle.plugin

import org.gradle.api.artifacts.VersionCatalogsExtension
import site.addzero.gradle.tool.lib
import site.addzero.gradle.tool.ver
import org.gradle.kotlin.dsl.the

plugins {
    id("site.addzero.gradle.plugin.spring-common-convention")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
}

val libs = the<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(libs.lib("org-springframework-boot-spring-boot-starter-web"))
}
