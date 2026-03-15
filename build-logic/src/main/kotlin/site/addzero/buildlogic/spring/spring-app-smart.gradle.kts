package site.addzero.buildlogic.spring

import site.addzero.gradle.tool.SmartDependencyResolver
import site.addzero.gradle.tool.configureSmartResolver
import site.addzero.gradle.tool.getPreferredVersion

plugins {
    id("site.addzero.buildlogic.spring.spring-common")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
}

// 注册扩展，让消费方可以配置版本偏好
configureSmartResolver()

val libs = versionCatalogs.named("libs")

dependencies {
    // 场景1：消费方 catalog 中只有 spring-boot-starter-web 一个版本
    // 直接找到并使用
    val webDep = SmartDependencyResolver.smartFind(
        libs = libs,
        group = "org.springframework.boot",
        artifact = "spring-boot-starter-web"
    ) ?: throw GradleException(
        "spring-boot-starter-web not found in version catalog. " +
        "Please add it to your gradle/libs.versions.toml"
    )
    implementation(webDep)

    // 场景2：消费方同时有 v2 和 v3 版本
    // spring-boot-starter-web-v2 → 2.7.18
    // spring-boot-starter-web-v3 → 3.2.0
    // 策略：使用 LATEST（默认）
    val latestWeb = SmartDependencyResolver.smartFind(
        libs = libs,
        group = "org.springframework.boot",
        artifact = "spring-boot-starter-web",
        strategy = SmartDependencyResolver.VersionStrategy.LATEST
    )
    latestWeb?.let { implementation(it) }

    // 场景3：消费方声明了版本偏好，优先使用声明的版本
    val preferredVersion = getPreferredVersion("spring-boot")
    val preferredWeb = SmartDependencyResolver.smartFind(
        libs = libs,
        group = "org.springframework.boot",
        artifact = "spring-boot-starter-web",
        strategy = SmartDependencyResolver.VersionStrategy.PREFERRED,
        preferredVersion = preferredVersion
    )
    preferredWeb?.let { implementation(it) }

    // 场景4：严格模式 - 如果存在多版本直接报错，强制消费方清理 catalog
    val strictWeb = SmartDependencyResolver.smartFind(
        libs = libs,
        group = "org.springframework.boot",
        artifact = "spring-boot-starter-web",
        strategy = SmartDependencyResolver.VersionStrategy.STRICT
    )
    strictWeb?.let { implementation(it) }
}

// 消费方配置示例（在 build.gradle.kts 中）：
// dependencyResolution {
//     prefer("spring-boot", "3")
//     lock("org.springframework:spring-web", "6.1.14")
// }
