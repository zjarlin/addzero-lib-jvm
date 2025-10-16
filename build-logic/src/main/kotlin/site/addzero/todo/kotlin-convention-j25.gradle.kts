package site.addzero.todo

import site.addzero.gradle.configureKotlin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("site.addzero.todo.j25support")
    kotlin("jvm")
}

// Kotlin配置 - Java 25
// Java 25 toolchain，但Kotlin编译目标使用JVM 21（当前稳定版本）
configureKotlin(25, JvmTarget.JVM_21)

