package site.addzero.gradle.tool

import org.apache.tools.ant.util.FileUtils
import org.apache.tools.ant.util.FileUtils.getRelativePath
import org.gradle.api.initialization.Settings
import site.addzero.gradle.constant.Disposable.KSP_BUILD_DIR_JVM
import site.addzero.gradle.constant.Disposable.KSP_BUILD_DIR_KMP
import site.addzero.gradle.constant.Disposable.RESOURCE_DIR_JVM
import site.addzero.gradle.constant.Disposable.SOURCE_DIR_JVM
import site.addzero.gradle.constant.Disposable.SOURCE_DIR_KMP
import java.io.File

// 定义黑名单目录列表
val BLACKLIST_DIRS = listOf("buildSrc,", "build-logic")

/**
 * 项目目录配置数据类
 * @property moduleName 模块名称
 * @property sourceDir 源代码目录路径
 * @property buildDir 构建输出目录路径
 * @property resourceDir 资源文件目录路径（可为空）
 */
data class ProjectModuleConfig(
    val moduleName: String, val sourceDir: String, val buildDir: String, val resourceDir: String?
)


fun getProjectDirConfigMapWithOutLib(rootDir: File): MutableMap<String, String> {
    val projectDirConfigMapResult = getProjectDirConfigMap(BLACKLIST_DIRS + listOf("lib"), rootDir)
    return projectDirConfigMapResult
}

fun getProjectDirConfigMap(blackDir: List<String> = BLACKLIST_DIRS, rootDir: File): MutableMap<String, String> {
    val projectDirConfigMapResult = generateProjectDirConfigMap(blackDir, rootDir)
    val mutableMapOf = mutableMapOf<String, String>()
    projectDirConfigMapResult.forEach { modelConfig ->
        val moduleName = modelConfig.moduleName
        mutableMapOf["${moduleName}SourceDir"] = modelConfig.sourceDir
        mutableMapOf["${moduleName}BuildDir"] = modelConfig.buildDir
        modelConfig.resourceDir?.let {
            mutableMapOf["${moduleName}ResourceDir"] = it
        }
    }
    return mutableMapOf
}


/**
 * 生成项目目录配置映射
 * @return 包含所有项目模块目录配置的映射结果
 */
fun generateProjectDirConfigMap(blackDir: List<String>, rootDir: File): List<ProjectModuleConfig> {
    val allProjectDirs = findAllProjectDirs(rootDir).filter { it != rootDir } // 排除根项目本身
        .filter { !isBlacklisted(it, rootDir, blackDir) } // 排除黑名单目录

    val map = allProjectDirs.map { projectDir ->
        val relativePath = getRelativePath(rootDir, projectDir)
        val cleanPath = relativePath.replace(File.separator, "-")
        val moduleName = cleanPath.toCamelCase()
        val buildFile = File(projectDir, "build.gradle.kts")
        val buildContent = buildFile.readText()
        // 通过检查build文件内容判断是否为KMP项目
        val isKmp =
            buildContent.contains("kotlinMultiplatform") || buildContent.contains("org.jetbrains.kotlin.multiplatform") || buildContent.contains(
                "kmp-"
            )
        val sourceDir = if (isKmp) SOURCE_DIR_KMP else SOURCE_DIR_JVM
        val buildDir = if (isKmp) KSP_BUILD_DIR_KMP else KSP_BUILD_DIR_JVM
        val resourceDir = if (isKmp) null else RESOURCE_DIR_JVM
        val projectConfig = ProjectModuleConfig(
            moduleName = moduleName,
            sourceDir = projectDir.resolve(sourceDir).absolutePath,
            buildDir = projectDir.resolve(buildDir).absolutePath,
            resourceDir = resourceDir?.let { projectDir.resolve(it).absolutePath })
        projectConfig
    }

    return map
}


// 递归查找所有包含build.gradle.kts的项目目录
fun findAllProjectDirs(rootDir: File): List<File> {
    val result = mutableListOf<File>()
    if (File(rootDir, "build.gradle.kts").exists()) {
        result.add(rootDir)
    }
    rootDir.listFiles { file: File ->
        file.isDirectory
    }?.forEach { subDir ->
        result.addAll(findAllProjectDirs(subDir))
    }
    return result
}


data class ProjectContext(
    val buildLogics: List<File>,
    val modules: List<File>,
    val blackModules: List<File>,
)

fun getProjectContext(
    rootDir: File, predicate: (File) -> Boolean = { true }
): ProjectContext {
    val findAllProjectDirs = findAllProjectDirs(rootDir)
    val allProjectDirs = findAllProjectDirs.filter { it.absolutePath != rootDir.absolutePath } // 排除根项目本身

    val (buildProj, modules) = allProjectDirs.partition {
        val isBuildLogic = it.name.startsWith("build-logic")
                ||
                it.name.startsWith("buildLogic")
                ||
                it.name.startsWith("buildSrc")
        isBuildLogic
    }
    val partition = modules.partition {
        predicate(it)
    }
    return ProjectContext(buildProj, partition.first, partition.second)
}


fun getProjectContext(
    rootDir: File, vararg blackModuleName: String
): ProjectContext {
    return getProjectContext(rootDir) {
        val relativePath = FileUtils.getRelativePath(rootDir, it)
        val moduleName = ":${relativePath.replace(File.separator, ":")}"
        moduleName !in blackModuleName
    }
}


fun Settings.autoIncludeModules(vararg blackModuleName: String) {
    val rootDir = settings.layout.rootDirectory.asFile
    settings.autoIncludeModules {
        val relativePath = FileUtils.getRelativePath(rootDir, it)
        val moduleName = ":${relativePath.replace(File.separator, ":")}"
        val isIncluded = moduleName !in blackModuleName
        isIncluded
    }
}

fun Settings.autoIncludeModules(predicate: (File) -> Boolean = { true }) {
    val rootDir = settings.layout.rootDirectory.asFile
    val projectContext = getProjectContext(rootDir, predicate)
    projectContext.buildLogics.forEach {
        settings.includeBuild(it)
        println("🛠️find build logic: ${it.name}")
    }
    projectContext.modules
        .filterNot { it.name == "buildSrc" }
        .forEach {
            val relativePath = getRelativePath(rootDir, it)
            val moduleName = ":${relativePath.replace(File.separator, ":")}"
            settings.include(moduleName)
            println("📦find module: $moduleName")
        }

    projectContext.blackModules.forEach {
        val relativePath = getRelativePath(rootDir, it)
        val moduleName = ":${relativePath.replace(File.separator, ":")}"
        println("⏭️  Skipped module: $moduleName")
    }
}


// 检查目录是否在黑名单中
private fun isBlacklisted(projectDir: File, rootDir: File, blackDir: List<String>): Boolean {
    val relativePath = getRelativePath(rootDir, projectDir)
    return blackDir.any { blacklisted ->
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

