package site.addzero.jvmconvention

import site.addzero.gradle.getJavaVersion
import site.addzero.gradle.configureKotlin

plugins {
    id("site.addzero.jvmconvention.property-based-java-support")
    kotlin("jvm")
}

// 动态Kotlin配置
configureKotlin(getJavaVersion())

