package site.addzero.todo

import site.addzero.gradle.configureKotlin17

plugins {
    id("site.addzero.todo.property-based-java-support")
    kotlin("jvm")
}

// Kotlin配置 - Java 17
configureKotlin17()

