package site.addzero.todo

import site.addzero.gradle.configureKotlin

plugins {
    id("site.addzero.todo.property-based-java-support")
    kotlin("jvm")
}

// Kotlin配置 - Java 25
// JVM目标版本会自动根据Java的targetCompatibility推断
configureKotlin(25)

