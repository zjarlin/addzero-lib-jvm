package site.addzero.gradle.plugin

import org.gradle.api.artifacts.VersionCatalogsExtension
import site.addzero.gradle.tool.lib
import site.addzero.gradle.tool.ver
import org.gradle.kotlin.dsl.the

plugins {
    id("site.addzero.gradle.plugin.spring-common-convention")
    kotlin("plugin.spring")
}

val libs = the<VersionCatalogsExtension>().named("libs")

dependencies {
    compileOnly(libs.lib("org-springframework-boot-spring-boot-starter-web"))
    compileOnly(libs.lib("org-springframework-boot-spring-boot-autoconfigure"))
    annotationProcessor(libs.lib("org-springframework-boot-spring-boot-configuration-processor-v2"))
}
