
package site.addzero.jvm

import site.addzero.gradle.configureWithSourcesJar
import site.addzero.gradle.configureJUnitPlatform

plugins {
    id("site.addzero.jvm.j8support")
    id("site.addzero.jvm.utf8support")
    `java-library`
}

// 配置源码JAR
configureWithSourcesJar()

// 配置测试使用JUnit平台
configureJUnitPlatform()

