package site.addzero.spring

import site.addzero.gradle.getLibs

plugins {
    id("site.addzero.jvm.kotlin-convention")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(platform(getLibs().spring.bom))
}


