package site.addzero.todo

import site.addzero.gradle.configureKotlin11

plugins {
    id("site.addzero.todo.property-based-java-support")
    kotlin("jvm")
}

// Kotlin配置 - Java 11
configureKotlin11()

