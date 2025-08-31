package com.addzero.gradle.plugin.automodules

import org.gradle.api.initialization.Settings
import java.io.File

/**
 * 模块扫描器核心类
 */
class ModuleScanner(
    private val settings: Settings,
    private val extension: AutoModulesExtension
) {

    companion object {
        private val DEFAULT_EXCLUDED_DIRS = setOf(
            "build", "gradle", ".gradle", ".git", ".idea",
            "node_modules", "target", "out", "bin", ".settings",
            "src", "test", "main", "kotlin", "java", "resources",
            "generated-sources", "generated-test-sources",
            "buildSrc"  // Gradle保留名称，不能作为项目名称
        )

        private val BUILD_FILES = arrayOf("build.gradle.kts", "build.gradle")
    }

    // 存储所有扫描到的模块信息
    private val discoveredModules = mutableMapOf<String, ModuleInfo>()

    /**
     * 模块信息类
     */
    data class ModuleInfo(
        val path: String,
        val directory: File,
        val relativePath: String,
        val isIncluded: Boolean = false
    )

    /**
     * 执行自动模块扫描和包含
     */
    fun scanAndIncludeModules() {
        val projectDir = settings.rootProject.projectDir
        val foundModules = mutableSetOf<String>()

        log("🔍 开始扫描模块...")
        log("📁 项目根目录: ${projectDir.absolutePath}")

        if (extension.autoScan) {
            log("✨ 使用全自动扫描模式")
            scanAllDirectories(projectDir, foundModules)
        } else {
            log("🎯 扫描根模块: ${extension.rootModules}")
            extension.rootModules.forEach { rootModule ->
                val scanDir = if (rootModule == ".") projectDir else File(projectDir, rootModule)
                if (scanDir.exists() && scanDir.isDirectory) {
                    log("📂 扫描目录: ${scanDir.absolutePath}")
                    scanForGradleModules(scanDir, rootModule, foundModules)
                } else {
                    log("⚠️ 目录不存在: ${scanDir.absolutePath}")
                }
            }
        }

        // 过滤排除的模块
        val filteredModules = filterModules(foundModules)

        // 包含所有找到的模块
        includeModules(filteredModules)

        logSummary(filteredModules)
    }

    /**
     * 全自动扫描所有目录，找到包含build.gradle.kts的叶子目录
     */
    private fun scanAllDirectories(projectDir: File, foundModules: MutableSet<String>) {
        scanDirectoryRecursively(projectDir, "", foundModules)
    }

    /**
     * 递归扫描目录，查找所有Gradle模块
     */
    private fun scanDirectoryRecursively(dir: File, currentPath: String, foundModules: MutableSet<String>) {
        // 检查当前目录是否包含构建文件
        val hasBuildFile = BUILD_FILES.any { File(dir, it).exists() }

        if (hasBuildFile) {
            val modulePath = if (currentPath.isEmpty()) "." else currentPath.replace("/", ":")
            foundModules.add(modulePath)

            // 记录模块信息
            val moduleInfo = ModuleInfo(
                path = modulePath,
                directory = dir,
                relativePath = currentPath.ifEmpty { "." }
            )
            discoveredModules[modulePath] = moduleInfo

            // 不再打印发现的模块，而是提供等价的include实现
            // log("✅ 发现模块: $modulePath")
        }

        // 无论当前目录是否包含构建文件，都继续扫描子目录寻找更多模块
        // 只有在非根目录且包含构建文件时才跳过子目录扫描（叶子节点优化）
        val isLeafModule = hasBuildFile && currentPath.isNotEmpty()

        if (!isLeafModule) {
            // 继续扫描子目录
            dir.listFiles()?.forEach { subDir ->
                if (subDir.isDirectory && !isExcludedDir(subDir.name)) {
                    val subPath = if (currentPath.isEmpty()) subDir.name else "$currentPath/${subDir.name}"
                    scanDirectoryRecursively(subDir, subPath, foundModules)
                }
            }
        }
    }

    /**
     * 递归扫描目录中的gradle模块
     */
    private fun scanForGradleModules(dir: File, relativePath: String, foundModules: MutableSet<String>) {
        // 检查当前目录是否包含构建文件
        val hasBuildFile = BUILD_FILES.any { File(dir, it).exists() }

        if (hasBuildFile) {
            val modulePath = if (relativePath == ".") "." else relativePath.replace("/", ":")
            foundModules.add(modulePath)

            // 记录模块信息
            val moduleInfo = ModuleInfo(
                path = modulePath,
                directory = dir,
                relativePath = relativePath
            )
            discoveredModules[modulePath] = moduleInfo

            // 不再打印发现的模块，而是提供等价的include实现
            // log("✅ 发现模块: $modulePath")
        }

        // 递归扫描子目录
        dir.listFiles()?.forEach { subDir ->
            if (subDir.isDirectory && !isExcludedDir(subDir.name)) {
                val subPath = if (relativePath == ".") subDir.name else "$relativePath/${subDir.name}"
                scanForGradleModules(subDir, subPath, foundModules)
            }
        }
    }

    /**
     * 过滤排除的模块
     */
    private fun filterModules(foundModules: Set<String>): List<String> {
        return foundModules.filter { modulePath ->
            val shouldExclude = extension.excludeModules.any { exclude ->
                when {
                    exclude.contains("*") -> {
                        val pattern = exclude.replace("*", ".*")
                        modulePath.matches(Regex(pattern))
                    }
                    exclude.startsWith(":") -> modulePath == exclude.substring(1)
                    else -> modulePath.contains(exclude)
                }
            } || (extension.excludeTestModules && isTestModule(modulePath))

            if (shouldExclude) {
                log("❌ 排除模块: $modulePath")
            }

            !shouldExclude
        }
    }

    /**
     * 包含模块到settings
     */
    private fun includeModules(modules: List<String>) {
        modules.forEach { modulePath ->
            if (modulePath != ".") {
                settings.include(":$modulePath")
                settings.findProject(":$modulePath")

                // 更新模块信息为已包含
                discoveredModules[modulePath]?.let { moduleInfo ->
                    discoveredModules[modulePath] = moduleInfo.copy(isIncluded = true)
                }

                // 提供等价的include实现而不是打印
                log("📦 包含模块: :$modulePath")
                // 等价的include实现:
                // include(":$modulePath")
            }
        }
    }

    /**
     * 判断是否为应排除的目录
     */
    private fun isExcludedDir(dirName: String): Boolean {
        val allExcludedDirs = DEFAULT_EXCLUDED_DIRS + extension.customExcludedDirs
        return allExcludedDirs.equals(dirName) || dirName.startsWith(".")
    }

    /**
     * 判断是否为测试模块
     */
    private fun isTestModule(modulePath: String): Boolean {
        val lowerPath = modulePath.lowercase()
        return lowerPath.contains("test") || lowerPath.contains("spec") || lowerPath.contains("mock")
    }

    /**
     * 输出日志
     */
    private fun log(message: String) {
        if (extension.verbose) {
            println(message)
        }
    }

    /**
     * 输出总结信息
     */
    private fun logSummary(modules: List<String>) {
        println("\n🎯 模块扫描完成，共找到 ${modules.size} 个模块")
        if (extension.excludeModules.isNotEmpty()) {
            println("📝 排除的模块模式: ${extension.excludeModules.joinToString(", ")}")
        }
        if (extension.excludeTestModules) {
            println("🧪 已自动排除测试模块")
        }
        // 提供等价的include实现
        println("\n📋 等价的include实现:")
        modules.forEach { modulePath ->
            if (modulePath != ".") {
                println("include(\":$modulePath\")")
            }
        }
        println("\n✨ AutoModules 插件扫描完成\n")
    }

    /**
     * 获取所有已发现的项目
     * @param includeExcluded 是否包含被排除的项目，默认为 false
     * @return 项目列表
     */
    fun getAllProjects(includeExcluded: Boolean = false): List<ModuleInfo> {
        return if (includeExcluded) {
            discoveredModules.values.toList()
        } else {
            discoveredModules.values.filter { it.isIncluded }
        }
    }

    /**
     * 检查项目是否存在
     * @param path 项目路径
     * @return 是否存在且已包含
     */
    fun hasProject(path: String): Boolean {
        val normalizedPath = normalizePath(path)
        return discoveredModules[normalizedPath]?.isIncluded == true
    }

    /**
     * 根据模式查找项目
     * @param pattern 匹配模式，支持通配符 *
     * @return 匹配的项目列表
     */
    fun findProjectsByPattern(pattern: String): List<ModuleInfo> {
        val regex = pattern.replace("*", ".*").toRegex()
        return discoveredModules.values.filter { moduleInfo ->
            moduleInfo.isIncluded && moduleInfo.path.matches(regex)
        }
    }

    /**
     * 标准化路径格式
     */
    private fun normalizePath(path: String): String {
        return when {
            path.startsWith(":") -> path.substring(1)
            path == "." -> "."
            else -> path
        }
    }
}
