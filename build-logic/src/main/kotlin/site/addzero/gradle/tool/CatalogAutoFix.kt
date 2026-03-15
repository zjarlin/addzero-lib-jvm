package site.addzero.gradle.tool

import org.gradle.api.Project
import org.gradle.api.GradleException
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 自动修复 Version Catalog - 为消费方智能添加缺失的依赖
 *
 * ⚠️ 警告：这会修改用户源代码中的 libs.versions.toml 文件
 * 使用前会征求用户同意（通过 interactive 模式或配置）
 */
object CatalogAutoFix {

    /**
     * 自动修复配置
     */
    data class AutoFixConfig(
        /** 是否启用自动修复 */
        val enabled: Boolean = false,
        /** 是否交互式询问（false 则静默添加） */
        val interactive: Boolean = true,
        /** 是否创建备份 */
        val backup: Boolean = true,
        /** 添加时使用的版本来源 */
        val versionSource: VersionSource = VersionSource.BUILD_LOGIC_DEFAULT,
        /** 自定义版本映射（覆盖 build-logic 默认值） */
        val customVersions: Map<String, String> = emptyMap()
    )

    enum class VersionSource {
        /** 使用 build-logic 内置的默认版本 */
        BUILD_LOGIC_DEFAULT,
        /** 使用最新稳定版（需要网络查询，暂未实现） */
        LATEST_STABLE,
        /** 使用 + 让 Gradle 动态解析 */
        DYNAMIC_PLUS,
        /** 使用占位符让消费方填写 */
        PLACEHOLDER
    }

    /**
     * 内置默认版本库
     * 与 build-logic 自身使用的版本保持一致
     */
    private val DEFAULT_VERSIONS = mapOf(
        "org.springframework.boot:spring-boot-starter-web" to "3.2.0",
        "org.springframework.boot:spring-boot-starter-data-jpa" to "3.2.0",
        "org.springframework.boot:spring-boot-starter-test" to "3.2.0",
        "com.google.inject:guice" to "4.2.3",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core" to "1.7.3",
        "org.jetbrains.kotlinx:kotlinx-serialization-json" to "1.6.2",
        "io.ktor:ktor-client-core" to "2.3.7",
        "io.ktor:ktor-server-core" to "2.3.7",
        "io.insert-koin:koin-core" to "3.5.0",
    )

    /**
     * 智能添加依赖到 catalog
     *
     * @param project Gradle Project
     * @param group Maven groupId
     * @param artifact Maven artifactId
     * @param config 自动修复配置
     * @return 添加后的 alias，如果失败返回 null
     */
    fun autoAddToCatalog(
        project: Project,
        group: String,
        artifact: String,
        config: AutoFixConfig = AutoFixConfig()
    ): String? {
        if (!config.enabled) {
            project.logger.warn("Auto-fix is disabled. Please manually add $group:$artifact to libs.versions.toml")
            return null
        }

        val tomlFile = project.findCatalogFile() ?: run {
            project.logger.error("Could not find libs.versions.toml")
            return null
        }

        // 生成 alias
        val alias = generateAlias(group, artifact)

        // 检查是否已存在
        if (isDependencyInCatalog(tomlFile, alias, group, artifact)) {
            project.logger.info("Dependency $group:$artifact already exists in catalog (alias: $alias)")
            return alias
        }

        // 获取版本
        val version = when (config.versionSource) {
            VersionSource.BUILD_LOGIC_DEFAULT ->
                config.customVersions["$group:$artifact"]
                    ?: DEFAULT_VERSIONS["$group:$artifact"]
                    ?: "+"
            VersionSource.DYNAMIC_PLUS -> "+"
            VersionSource.PLACEHOLDER -> "\${PLACEHOLDER}"
            else -> "+"
        }

        // 交互式确认
        if (config.interactive) {
            val confirmed = askUserConfirmation(project, group, artifact, version, alias)
            if (!confirmed) {
                project.logger.lifecycle("User declined to add $group:$artifact to catalog")
                return null
            }
        }

        // 创建备份
        if (config.backup) {
            createBackup(tomlFile)
        }

        // 添加依赖到 toml
        return try {
            addDependencyToToml(tomlFile, alias, group, artifact, version)
            project.logger.lifecycle("✓ Added $group:$artifact:$version to catalog (alias: $alias)")
            alias
        } catch (e: Exception) {
            project.logger.error("Failed to add dependency to catalog: ${e.message}")
            null
        }
    }

    /**
     * 批量自动修复 - 添加多个缺失的依赖
     */
    fun batchAutoFix(
        project: Project,
        dependencies: List<Pair<String, String>>,
        config: AutoFixConfig = AutoFixConfig()
    ): List<String> {
        return dependencies.mapNotNull { (group, artifact) ->
            autoAddToCatalog(project, group, artifact, config)
        }
    }

    /**
     * 生成 alias（Maven 坐标转 alias）
     * com.google.inject:guice → com-google-inject-guice
     */
    fun generateAlias(group: String, artifact: String): String {
        val simplifiedGroup = when {
            group.startsWith("org.springframework.boot") -> "spring-boot"
            group.startsWith("org.springframework") -> "spring"
            group.startsWith("org.jetbrains.kotlinx") -> "kotlinx"
            group.startsWith("org.jetbrains.kotlin") -> "kotlin"
            group.startsWith("io.ktor") -> "ktor"
            group.startsWith("io.insert-koin") -> "koin"
            group.startsWith("com.google") -> group.replace(".", "-")
            else -> group.replace(".", "-")
        }

        return "$simplifiedGroup-${artifact.replace(".", "-")}"
            .replace(Regex("-+"), "-")
            .lowercase()
    }

    /**
     * 查找 catalog 文件
     */
    private fun Project.findCatalogFile(): File? {
        val candidates = listOf(
            rootProject.file("gradle/libs.versions.toml"),
            rootProject.file("libs.versions.toml"),
            file("gradle/libs.versions.toml"),
            file("libs.versions.toml")
        )
        return candidates.firstOrNull { it.exists() }
    }

    /**
     * 检查依赖是否已存在于 catalog
     */
    private fun isDependencyInCatalog(tomlFile: File, alias: String, group: String, artifact: String): Boolean {
        if (!tomlFile.exists()) return false
        val content = tomlFile.readText()

        // 检查 alias
        if (content.contains(Regex("^\\s*$alias\\s*=", RegexOption.MULTILINE))) {
            return true
        }

        // 检查 group:name 组合
        return content.contains(Regex("group\\s*=\\s*\"$group\"")) &&
               content.contains(Regex("name\\s*=\\s*\"$artifact\""))
    }

    /**
     * 交互式询问用户
     */
    private fun askUserConfirmation(
        project: Project,
        group: String,
        artifact: String,
        version: String,
        alias: String
    ): Boolean {
        project.logger.lifecycle("")
        project.logger.lifecycle("╔════════════════════════════════════════════════════════╗")
        project.logger.lifecycle("║  Missing Dependency Detected                           ║")
        project.logger.lifecycle("╠════════════════════════════════════════════════════════╣")
        project.logger.lifecycle("║  Maven: $group:$artifact:$version")
        project.logger.lifecycle("║  Alias: $alias")
        project.logger.lifecycle("╠════════════════════════════════════════════════════════╣")
        project.logger.lifecycle("║  Add this to your libs.versions.toml? [Y/n]:           ║")
        project.logger.lifecycle("╚════════════════════════════════════════════════════════╝")
        project.logger.lifecycle("")

        val autoApprove = project.findProperty("addzero.autofix.approve")?.toString()?.toBoolean() ?: false
        val autoDecline = project.findProperty("addzero.autofix.decline")?.toString()?.toBoolean() ?: false

        return when {
            autoApprove -> {
                project.logger.lifecycle("Auto-approved via property addzero.autofix.approve=true")
                true
            }
            autoDecline -> {
                project.logger.lifecycle("Auto-declined via property addzero.autofix.decline=true")
                false
            }
            else -> {
                project.logger.lifecycle("Run with -Paddzero.autofix.approve=true to auto-approve")
                project.logger.lifecycle("Or -Paddzero.autofix.decline=true to skip")
                false
            }
        }
    }

    /**
     * 创建备份
     */
    private fun createBackup(originalFile: File) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val backupFile = File("${originalFile.path}.$timestamp.backup")
        originalFile.copyTo(backupFile, overwrite = true)
    }

    /**
     * 添加依赖到 toml 文件
     */
    private fun addDependencyToToml(
        tomlFile: File,
        alias: String,
        group: String,
        artifact: String,
        version: String
    ) {
        val content = tomlFile.readText()

        // 生成新的 library 条目
        val newEntry = if (version == "+") {
            """
            $alias = { group = "$group", name = "$artifact", version = "+" }
            """.trimIndent()
        } else {
            """
            $alias = { group = "$group", name = "$artifact", version = "$version" }
            """.trimIndent()
        }

        // 找到 [libraries] 部分并插入
        val librariesPattern = Regex("(\\[libraries\\].*?\\n)", RegexOption.DOT_MATCHES_ALL)
        val updatedContent = if (librariesPattern.containsMatchIn(content)) {
            content.replace(librariesPattern, "$1$newEntry\n")
        } else {
            "$content\n[libraries]\n$newEntry\n"
        }

        tomlFile.writeText(updatedContent)
    }

    /**
     * 生成缺失依赖的报告（不修改文件）
     */
    fun generateMissingDepsReport(
        project: Project,
        requiredDeps: List<Pair<String, String>>
    ): String {
        val tomlFile = project.findCatalogFile()
        val missing = requiredDeps.filter { (group, artifact) ->
            val alias = generateAlias(group, artifact)
            tomlFile == null || !isDependencyInCatalog(tomlFile, alias, group, artifact)
        }

        return buildString {
            appendLine("# Missing Dependencies Report")
            appendLine("# Generated at ${LocalDateTime.now()}")
            appendLine()
            appendLine("# Add the following to your gradle/libs.versions.toml:")
            appendLine()
            appendLine("[libraries]")
            missing.forEach { (group, artifact) ->
                val alias = generateAlias(group, artifact)
                val version = DEFAULT_VERSIONS["$group:$artifact"] ?: "+"
                appendLine("$alias = { group = \"$group\", name = \"$artifact\", version = \"$version\" }")
            }
        }
    }
}

/**
 * 扩展函数：简化调用
 */
fun Project.ensureCatalogDependency(
    group: String,
    artifact: String,
    autoFix: Boolean = false
): String {
    // 如果启用自动修复，尝试添加
    if (autoFix || findProperty("addzero.autofix.enabled")?.toString()?.toBoolean() == true) {
        val config = CatalogAutoFix.AutoFixConfig(
            enabled = true,
            interactive = false,
            backup = true
        )
        val added = CatalogAutoFix.autoAddToCatalog(this, group, artifact, config)
        if (added != null) {
            throw GradleException(
                "Added $group:$artifact to catalog. " +
                "Please re-sync the project (Ctrl+Shift+O / Cmd+Shift+O) and rebuild."
            )
        }
    }

    // 报错并给出帮助
    val report = CatalogAutoFix.generateMissingDepsReport(this, listOf(group to artifact))
    val suggestedAlias = CatalogAutoFix.generateAlias(group, artifact)
    throw GradleException(
        buildString {
            appendLine("Missing dependency in catalog: $group:$artifact")
            appendLine()
            appendLine("Solutions:")
            appendLine("1. Manual: Add to gradle/libs.versions.toml:")
            appendLine("   $suggestedAlias = { group = \"$group\", name = \"$artifact\", version = \"+\" }")
            appendLine()
            appendLine("2. Auto-fix: Run with -Paddzero.autofix.enabled=true")
            appendLine()
            appendLine("--- Generated Report ---")
            append(report)
        }
    )
}
