package site.addzero.gradle.tool

import org.gradle.api.Project
import java.io.File

// 定义黑名单目录列表
val BLACKLIST_DIRS = listOf("lib", "buildSrc")

/**
 * 项目目录配置数据类
 * @property moduleName 模块名称
 * @property sourceDir 源代码目录路径
 * @property buildDir 构建输出目录路径
 * @property resourceDir 资源文件目录路径（可为空）
 */
data class ProjectDirConfig(
    val moduleName: String,
    val sourceDir: String,
    val buildDir: String,
    val resourceDir: String?
)

/**
 * 项目目录配置映射结果
 * @property configs 模块名称到目录配置的映射
 */
data class ProjectDirConfigMapResult(
    val configs: Map<String, ProjectDirConfig>
)

/**
 * 生成项目目录配置映射
 * @return 包含所有项目模块目录配置的映射结果
 */
fun Project.generateProjectDirConfigMap(): ProjectDirConfigMapResult {
    val rootDir = this.rootDir
    val allProjectDirs = findAllProjectDirs(rootDir)
        .filter { it != rootDir } // 排除根项目本身
        .filter { !isBlacklisted(it, rootDir) } // 排除黑名单目录

    val configMap = mutableMapOf<String, ProjectDirConfig>()

    logger.lifecycle("开始扫描项目目录，共找到 ${allProjectDirs.size} 个潜在项目目录")

    allProjectDirs.forEach { projectDir ->
        val relativePath = getRelativePath(rootDir, projectDir)
        val cleanPath = relativePath.replace(File.separator, "-")
        val moduleName = cleanPath.toCamelCase()

        val buildFile = File(projectDir, "build.gradle.kts")
        val buildContent = buildFile.readText()

        // 通过检查build文件内容判断是否为KMP项目
        val isKmp = buildContent.contains("kotlinMultiplatform") ||
                buildContent.contains("org.jetbrains.kotlin.multiplatform") ||
                buildContent.contains("kmp-")

        val sourceDir = if (isKmp) SOURCE_DIR_KMP else SOURCE_DIR_JVM
        val buildDir = if (isKmp) KSP_BUILD_DIR_KMP else KSP_BUILD_DIR_JVM
        val resourceDir = if (isKmp) null else RESOURCE_DIR_JVM

        val projectConfig = ProjectDirConfig(
            moduleName = moduleName,
            sourceDir = projectDir.resolve(sourceDir).absolutePath,
            buildDir = projectDir.resolve(buildDir).absolutePath,
            resourceDir = resourceDir?.let { projectDir.resolve(it).absolutePath }
        )

        configMap[moduleName] = projectConfig

        // 打印调试信息
        logger.lifecycle("处理项目: $relativePath")
        logger.lifecycle("  模块名: $moduleName")
        logger.lifecycle("  项目类型: ${if (isKmp) "KMP" else "JVM"}")
        logger.lifecycle("    ${moduleName}SourceDir = ${projectConfig.sourceDir}")
        logger.lifecycle("    ${moduleName}BuildDir = ${projectConfig.buildDir}")
        projectConfig.resourceDir?.let {
            logger.lifecycle("    ${moduleName}ResourceDir = $it")
        }
        logger.lifecycle("") // 空行分隔不同模块
    }

    logger.lifecycle("项目目录配置生成完成，共处理 ${configMap.size} 个模块")

    return ProjectDirConfigMapResult(configMap)
}

// 手动计算相对路径（兼容低版本）
private fun getRelativePath(rootDir: File, projectDir: File): String {
    val rootPath = rootDir.absolutePath
    val projectPath = projectDir.absolutePath
    return if (projectPath.startsWith(rootPath)) {
        projectPath.substring(rootPath.length).trimStart(File.separatorChar)
    } else {
        projectPath
    }
}

// 递归查找所有包含build.gradle.kts的项目目录
private fun findAllProjectDirs(dir: File): List<File> {
    val result = mutableListOf<File>()

    if (File(dir, "build.gradle.kts").exists()) {
        result.add(dir)
    }

    dir.listFiles { file: File ->
        file.isDirectory
    }?.forEach { subDir ->
        result.addAll(findAllProjectDirs(subDir))
    }

    return result
}

// 检查目录是否在黑名单中
private fun isBlacklisted(projectDir: File, rootDir: File): Boolean {
    val relativePath = getRelativePath(rootDir, projectDir)
    return BLACKLIST_DIRS.any { blacklisted ->
        relativePath == blacklisted || relativePath.startsWith("$blacklisted${File.separator}")
    }
}

/**
 * 将路径转换为小驼峰命名
 */
private fun String.toCamelCase(): String {
    val parts = this.split("-", ":")
    return parts.mapIndexed { index, part ->
        if (index == 0) {
            part.replaceFirstChar { it.lowercase() }
        } else {
            part.replaceFirstChar { it.uppercase() }
        }
    }.joinToString("")
}

/**
 * 判断是否为应排除的目录
 */
fun isExcludedDir(dirName: String): Boolean {
    val excludedDirs = setOf(
        "build", "gradle", ".gradle", ".git", ".idea",
        "node_modules", "target", "out", "bin", ".settings",
        "src", "test", "main", "kotlin", "java", "resources"
    )
    return excludedDirs.contains(dirName) || dirName.startsWith(".")
}
