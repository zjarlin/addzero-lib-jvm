package site.addzero.gradle.plugin.automodules

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import java.io.File

/**
 * 自动模块插件扩展配置
 */
open class AutoModulesPluginExtension {
    /**
     * 默认的黑名单目录列表
     */
    var excludeModules: List<String> = listOf("buildSrc", "build-logic")

    /**
     * 默认的排除目录列表（用于过滤常见的非项目目录）
     */
    var excludedFileNames: Set<String> = setOf(
        "build", "gradle", ".gradle", ".git", ".idea",
        "node_modules", "target", "out", "bin", ".settings",
        "src", "test", "main", "kotlin", "java", "resources"
    )
}

/**
 * 自动模块插件
 * 自动扫描项目目录并包含所有Gradle子项目
 */
class AutoModulesPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        // 创建扩展配置
        val extension = settings.extensions.create("autoModules", AutoModulesPluginExtension::class.java)

        // 获取根项目目录
        val rootDir = settings.rootDir
        // 在项目评估完成后执行包含逻辑
        settings.gradle.projectsEvaluated {
            // 查找所有包含build.gradle.kts的项目目录
            val projectDirs = findAllProjectDirs(rootDir, extension.excludedFileNames)
                .filter { it != rootDir } // 排除根项目本身
                .filter { isIncludeDir(it, rootDir, extension.excludeModules) } // 过滤需要包含的目录

            // 包含所有找到的项目
            projectDirs.forEach { projectDir ->
                val relativePath = getRelativePath(rootDir, projectDir)
                val moduleName = ":${relativePath.replace(File.separator, ":")}"

                // 使用settings.include()方法包含项目
                settings.include(moduleName)

                // 设置项目目录
                settings.project(moduleName).projectDir = projectDir

                // 打印调试信息
                settings.gradle.rootProject.logger.lifecycle("Auto included project: $moduleName at $relativePath")
            }
        }
    }

    /**
     * 查找所有包含build.gradle.kts的项目目录
     */
    private fun findAllProjectDirs(dir: File, excludedDirs: Set<String>): List<File> {
        val result = mutableListOf<File>()
        if (File(dir, "build.gradle.kts").exists()) {
            result.add(dir)
        }
        dir.listFiles { file ->
            file.isDirectory && !isExcludedDir(file.name, excludedDirs)
        }?.forEach { subDir ->
            result.addAll(findAllProjectDirs(subDir, excludedDirs))
        }
        return result
    }

    /**
     * 判断是否为应排除的目录
     */
    private fun isExcludedDir(dirName: String, excludedDirs: Set<String>): Boolean {
        return excludedDirs.contains(dirName) || dirName.startsWith(".")
    }

    /**
     * 判断目录是否应该被包含
     */
    private fun isIncludeDir(projectDir: File, rootDir: File, blacklistedDirs: List<String>): Boolean {
        val relativePath = getRelativePath(rootDir, projectDir)

        return !blacklistedDirs.any { blacklisted ->
            relativePath == blacklisted || relativePath.startsWith("$blacklisted${File.separator}")
        }
    }

    /**
     * 获取相对路径
     */
    private fun getRelativePath(rootDir: File, projectDir: File): String {
        val rootPath = rootDir.absolutePath
        val projectPath = projectDir.absolutePath
        return if (projectPath.startsWith(rootPath)) {
            projectPath.substring(rootPath.length).trimStart(File.separatorChar)
        } else {
            projectPath
        }
    }
}
