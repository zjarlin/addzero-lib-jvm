package site.addzero.buildlogic.jvm

import site.addzero.gradle.configureJUnitPlatform
import site.addzero.gradle.configureKotlinTestDependencies

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

// 配置Kotlin测试依赖和JUnit平台
configureKotlinTestDependencies()
configureJUnitPlatform()
