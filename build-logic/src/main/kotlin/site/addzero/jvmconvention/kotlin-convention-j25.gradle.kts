package site.addzero.jvmconvention

import site.addzero.gradle.configureKotlin

plugins {
    id("site.addzero.jvmconvention.property-based-java-support")
    kotlin("jvm")
}

// Kotlin配置 - Java 25
// JVM目标版本会自动根据Java的targetCompatibility推断
configureKotlin(25)

