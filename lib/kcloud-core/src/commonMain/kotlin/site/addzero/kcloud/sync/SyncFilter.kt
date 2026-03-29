package site.addzero.kcloud.sync

/**
 * 同步过滤器 - 控制哪些文件/目录应该被同步
 *
 * 支持基于模式匹配的忽略规则，类似.gitignore
 */
class SyncFilter(
    private val patterns: List<SyncPattern> = emptyList()
) {
    /**
     * 检查文件是否应该被忽略
     *
     * @param path 文件相对路径
     * @return true 如果文件应该被忽略
     */
    fun shouldIgnore(path: String): Boolean {
        val normalizedPath = normalizePath(path)
        var ignored = false

        patterns.forEach { pattern ->
            if (pattern.matches(normalizedPath)) {
                ignored = !pattern.negation
            }
        }

        return ignored
    }

    /**
     * 检查文件是否应该被同步
     */
    fun shouldSync(path: String): Boolean = !shouldIgnore(path)

    /**
     * 获取所有启用的规则
     */
    fun getActivePatterns(): List<SyncPattern> = patterns.filter { it.enabled }

    companion object {
        /**
         * 创建默认过滤器（包含常用忽略规则）
         */
        fun default(): SyncFilter {
            val defaultPatterns = listOf(
                // 系统文件
                SyncPattern(".DS_Store", PatternType.EXACT, "macOS系统文件"),
                SyncPattern("Thumbs.db", PatternType.EXACT, "Windows缩略图缓存"),
                SyncPattern("desktop.ini", PatternType.EXACT, "Windows桌面配置"),
                SyncPattern("*.lnk", PatternType.GLOB, "Windows快捷方式"),

                // IDE和编辑器
                SyncPattern(".idea/", PatternType.GLOB, "JetBrains IDE配置"),
                SyncPattern(".vscode/", PatternType.GLOB, "VS Code配置"),
                SyncPattern("*.swp", PatternType.GLOB, "Vim交换文件"),
                SyncPattern("*.swo", PatternType.GLOB, "Vim交换文件"),
                SyncPattern("*~", PatternType.GLOB, "备份文件"),

                // 版本控制
                SyncPattern(".git/", PatternType.GLOB, "Git目录"),
                SyncPattern(".svn/", PatternType.GLOB, "SVN目录"),
                SyncPattern(".hg/", PatternType.GLOB, "Mercurial目录"),

                // 依赖和构建输出
                SyncPattern("node_modules/", PatternType.GLOB, "Node.js依赖"),
                SyncPattern("target/", PatternType.GLOB, "Maven构建输出"),
                SyncPattern("build/", PatternType.GLOB, "Gradle构建输出"),
                SyncPattern(".gradle/", PatternType.GLOB, "Gradle缓存"),
                SyncPattern("__pycache__/", PatternType.GLOB, "Python缓存"),
                SyncPattern("*.pyc", PatternType.GLOB, "Python编译文件"),
                SyncPattern("*.class", PatternType.GLOB, "Java编译文件"),

                // 日志和临时文件
                SyncPattern("*.log", PatternType.GLOB, "日志文件"),
                SyncPattern("*.tmp", PatternType.GLOB, "临时文件"),
                SyncPattern(".tmp/", PatternType.GLOB, "临时目录"),

                // 锁文件
                SyncPattern("*.lock", PatternType.GLOB, "锁文件")
            )

            return SyncFilter(defaultPatterns)
        }

        /**
         * 从.gitignore风格的内容解析过滤器
         */
        fun fromGitignore(content: String): SyncFilter {
            val patterns = content.lines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .map { line ->
                    val negation = line.startsWith("!")
                    val pattern = if (negation) line.substring(1) else line
                    val type = when {
                        pattern.contains("*") || pattern.contains("?") -> PatternType.GLOB
                        pattern.contains("/") -> PatternType.PATH
                        else -> PatternType.EXACT
                    }
                    SyncPattern(pattern, type, "用户定义", enabled = true, negation = negation)
                }

            return SyncFilter(patterns)
        }

        private fun normalizePath(path: String): String {
            return path.replace("\\", "/").trimStart('/')
        }
    }
}

/**
 * 同步模式
 *
 * @param pattern 模式字符串
 * @param type 模式类型
 * @param description 描述
 * @param enabled 是否启用
 * @param negation 是否否定模式（!前缀）
 */
data class SyncPattern(
    val pattern: String,
    val type: PatternType,
    val description: String = "",
    val enabled: Boolean = true,
    val negation: Boolean = false
) {
    /**
     * 检查路径是否匹配此模式
     */
    fun matches(path: String): Boolean {
        if (!enabled) return false

        val normalizedPattern = pattern.trim('/')
        val normalizedPath = path.trim('/')

        return when (type) {
            PatternType.EXACT -> normalizedPath == normalizedPattern
            PatternType.PATH -> {
                // 目录匹配
                normalizedPath == normalizedPattern ||
                        normalizedPath.startsWith("$normalizedPattern/")
            }
            PatternType.GLOB -> matchGlob(normalizedPath, normalizedPattern)
            PatternType.REGEX -> Regex(normalizedPattern).matches(normalizedPath)
        }
    }

    /**
     * 简化的glob匹配
     */
    private fun matchGlob(path: String, pattern: String): Boolean {
        // 处理 /**/ 匹配任意目录
        var regex = pattern
            .replace(".", "\\.")
            .replace("**", "{{GLOBSTAR}}")
            .replace("*", "[^/]*")
            .replace("?", "[^/]")
            .replace("{{GLOBSTAR}}", ".*")

        // 处理目录匹配（以/结尾的模式）
        if (pattern.endsWith("/")) {
            regex = regex.trimEnd('/')
            return path.matches(Regex("^$regex(/.*)?$"))
        }

        return path.matches(Regex("^$regex$"))
    }
}

/**
 * 模式类型
 */
enum class PatternType {
    EXACT,      // 精确匹配
    PATH,       // 路径匹配（目录）
    GLOB,       // Glob模式匹配
    REGEX       // 正则表达式匹配
}

/**
 * 同步规则配置
 */
data class SyncRules(
    val globalPatterns: List<SyncPattern> = SyncFilter.default().getActivePatterns(),
    val localPatterns: List<SyncPattern> = emptyList(),
    val enableDefaultRules: Boolean = true,
    val respectGitignore: Boolean = true
) {
    /**
     * 创建组合过滤器
     */
    fun createFilter(gitignoreContent: String? = null): SyncFilter {
        val allPatterns = mutableListOf<SyncPattern>()

        if (enableDefaultRules) {
            allPatterns.addAll(globalPatterns)
        }

        if (respectGitignore && gitignoreContent != null) {
            allPatterns.addAll(SyncFilter.fromGitignore(gitignoreContent).getActivePatterns())
        }

        allPatterns.addAll(localPatterns)

        return SyncFilter(allPatterns)
    }

    /**
     * 添加本地规则
     */
    fun addPattern(pattern: SyncPattern): SyncRules {
        return copy(localPatterns = localPatterns + pattern)
    }

    /**
     * 移除本地规则
     */
    fun removePattern(pattern: SyncPattern): SyncRules {
        return copy(localPatterns = localPatterns - pattern)
    }
}

/**
 * 过滤器统计信息
 */
data class FilterStats(
    val totalPatterns: Int,
    val enabledPatterns: Int,
    val ignoredFiles: Int,
    val ignoredDirectories: Int,
    val lastScanTime: Long?
)
