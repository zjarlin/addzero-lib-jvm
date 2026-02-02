package site.addzero.gradle.plugin

import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the
import site.addzero.gradle.SpringConventionExtension

plugins {
    id("site.addzero.gradle.plugin.spring-common")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
}

val springConvention = the<SpringConventionExtension>()

afterEvaluate {
    dependencies {
        val bootVersion = springConvention.springBootVersion.get()
        implementation("org.springframework.boot:spring-boot-starter-web:$bootVersion")
    }
}
