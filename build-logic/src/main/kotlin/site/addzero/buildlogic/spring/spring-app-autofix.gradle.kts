package site.addzero.buildlogic.spring

import site.addzero.gradle.tool.CatalogAutoFix
import site.addzero.gradle.tool.ensureCatalogDependency
import site.addzero.gradle.tool.findByCoords

plugins {
    id("site.addzero.buildlogic.spring.spring-common")
    kotlin("plugin.spring")
    id("io.spring.dependency-management")
}

val libs = versionCatalogs.named("libs")

dependencies {
    // 方式1：智能查找，缺失时自动添加（需启用 autofix）
    ensureCatalogDependency(
        group = "org.springframework.boot",
        artifact = "spring-boot-starter-web",
        autoFix = project.findProperty("addzero.autofix.enabled")?.toString()?.toBoolean() ?: false
    )

    // 方式2：纯查找，缺失时报错并给出自动修复命令
    val webDep = findByCoords(libs, "org.springframework.boot", "spring-boot-starter-web")
        ?: throw GradleException(
            buildString {
                appendLine("Missing dependency: org.springframework.boot:spring-boot-starter-web")
                appendLine()
                appendLine("Quick fix:")
                appendLine("  ./gradlew build -Paddzero.autofix.enabled=true")
                appendLine()
                appendLine("Or manually add to gradle/libs.versions.toml:")
                appendLine("  spring-boot-spring-boot-starter-web = { group = \"org.springframework.boot\", name = \"spring-boot-starter-web\", version = \"3.2.0\" }")
            }
        )
    implementation(webDep)

    // 方式3：批量自动修复
    val requiredDeps = listOf(
        "org.springframework.boot" to "spring-boot-starter-data-jpa",
        "org.springframework.boot" to "spring-boot-starter-test",
    )

    val autofixEnabled = project.findProperty("addzero.autofix.enabled")?.toString()?.toBoolean() == true
    if (autofixEnabled) {
        val addedAliases = CatalogAutoFix.batchAutoFix(
            project,
            requiredDeps,
            CatalogAutoFix.AutoFixConfig(
                enabled = true,
                interactive = false,
                backup = true,
                versionSource = CatalogAutoFix.VersionSource.BUILD_LOGIC_DEFAULT
            )
        )
        if (addedAliases.isNotEmpty()) {
            throw GradleException(
                "Added ${addedAliases.size} dependencies to catalog. " +
                "Please re-sync project (Ctrl+Shift+O) and rebuild."
            )
        }
    }

    // 添加剩余依赖
    requiredDeps.forEach { (group, artifact) ->
        findByCoords(libs, group, artifact)?.let { implementation(it) }
    }
}
