// buildSrc/src/main/kotlin/ksp/ProjectDirConfigExtension.kt
package ksp

import BuildSettings.KSP_BUILD_DIR_JVM
import BuildSettings.KSP_BUILD_DIR_KMP
import BuildSettings.RESOURCE_DIR_JVM
import BuildSettings.SOURCE_DIR_JVM
import BuildSettings.SOURCE_DIR_KMP
import org.gradle.api.Project
import java.io.File

// 定义黑名单目录列表
private val BLACKLIST_DIRS = listOf("lib", "buildSrc")

/**
 * 生成项目目录配置映射
 */
fun Project.generateProjectDirConfigMap(): Map<String, Map<String, String>> {
    val rootDir = this.rootDir
    val allProjectDirs = findAllProjectDirs(rootDir)
        .filter { it != rootDir } // 排除根项目本身
        .filter { !isBlacklisted(it, rootDir) } // 排除黑名单目录

    val configMap = mutableMapOf<String, Map<String, String>>()

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

        val moduleConfig = mutableMapOf<String, String>()
        moduleConfig["${moduleName}SourceDir"] = projectDir.resolve(sourceDir).absolutePath
        moduleConfig["${moduleName}BuildDir"] = projectDir.resolve(buildDir).absolutePath

        resourceDir?.let {
            moduleConfig["${moduleName}ResourceDir"] = projectDir.resolve(it).absolutePath
        }

        configMap[moduleName] = moduleConfig
    }

    return configMap
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
