package site.addzero.buildlogic.spring

import site.addzero.gradle.tool.findByCoords
import site.addzero.gradle.tool.CatalogAutoFix

plugins {
    id("site.addzero.buildlogic.spring.spring-common")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
}

val libs = versionCatalogs.named("libs")

dependencies {
    // 方式1：通过 Maven 坐标查找（不需要知道 alias 命名规则）
    val springWeb = findByCoords(libs, "org.springframework.boot", "spring-boot-starter-web")
        ?: throw GradleException(
            buildString {
                appendLine("spring-boot-starter-web not found in version catalog.")
                appendLine()
                appendLine("Quick fix options:")
                appendLine("1. Run with auto-fix: ./gradlew build -Paddzero.autofix.enabled=true")
                appendLine()
                appendLine("2. Manually add to gradle/libs.versions.toml:")
                val alias = CatalogAutoFix.generateAlias("org.springframework.boot", "spring-boot-starter-web")
                appendLine("   $alias = { group = \"org.springframework.boot\", name = \"spring-boot-starter-web\", version = \"3.2.0\" }")
            }
        )

    implementation(springWeb)
}
