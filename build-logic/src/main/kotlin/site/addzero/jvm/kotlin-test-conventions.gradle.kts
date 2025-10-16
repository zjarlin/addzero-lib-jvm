package site.addzero.jvm

import site.addzero.gradle.configureJUnitPlatform
import site.addzero.gradle.configureKotlinTestDependencies

plugins {
    id("site.addzero.jvm.kotlin-convention")
}

// 配置Kotlin测试依赖和JUnit平台
configureKotlinTestDependencies()
configureJUnitPlatform()
