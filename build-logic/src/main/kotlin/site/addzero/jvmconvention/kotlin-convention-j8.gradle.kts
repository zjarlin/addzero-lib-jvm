package site.addzero.jvmconvention

import site.addzero.gradle.configureKotlin

plugins {
    id("site.addzero.todo.property-based-java-support")
    kotlin("jvm")
}

// Kotlin配置 - Java 8
configureKotlin(8)

