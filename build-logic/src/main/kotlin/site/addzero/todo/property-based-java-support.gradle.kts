package site.addzero.todo

import site.addzero.gradle.getJavaVersion
import site.addzero.gradle.configureJava

plugins {
    `java-library`
}

// 动态配置Java版本
configJava()

fun configJava() {
    val javaVersion = getJavaVersion()
    configureJava(javaVersion)
}
