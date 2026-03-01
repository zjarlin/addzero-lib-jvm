package site.addzero.gradle.plugin

import org.gradle.api.artifacts.VersionCatalogsExtension
import site.addzero.gradle.tool.lib
import site.addzero.gradle.tool.ver
import org.gradle.kotlin.dsl.the

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention") 
}

val libs = the<VersionCatalogsExtension>().named("libs")

dependencies {
    val version = libs.ver("springBoot")
    implementation(platform(libs.lib("org-springframework-boot-spring-boot-dependencies-v2")))

    testImplementation(libs.lib("org-springframework-boot-boot-spring-boot-starter-test"))
    testImplementation(libs.lib("junit-junit-junit-jupiter-api"))
    testImplementation(libs.lib("com-h2database-h2"))
    testRuntimeOnly(libs.lib("junit-junit-junit-jupiter-engine"))
    testImplementation(libs.lib("org-springframework-boot-spring-boot-starter-web"))
}

description = "Spring Boot common utilities"
