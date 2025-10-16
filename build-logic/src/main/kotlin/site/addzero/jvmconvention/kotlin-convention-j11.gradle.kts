package site.addzero.jvmconvention

import site.addzero.gradle.configureKotlin

plugins {
    id("site.addzero.jvmconvention.property-based-java-support")
    kotlin("jvm")
}

// Kotlin配置 - Java 11
configureKotlin(11)

