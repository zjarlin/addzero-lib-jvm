package site.addzero.jvmconvention

import site.addzero.gradle.getJavaVersion
import site.addzero.gradle.configureJava

plugins {
    `java-library`
}

// 动态配置Java版本
val javaVersion = getJavaVersion()
configureJava(javaVersion)

