package site.addzero.gradle.plugin.automodules

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import site.addzero.gradle.tool.ProjectModuleConfig
import site.addzero.gradle.tool.generateProjectDirConfigMap
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
    // 存储项目目录列表
    private lateinit var projectDirs: List<File>

    override fun apply(settings: Settings) {
        // 创建扩展配置
        val extension = settings.extensions.create("autoModules", AutoModulesPluginExtension::class.java)

        // 获取根项目目录
        val rootDir = settings.rootDir

        // 在设置阶段就扫描项目目录
        // 使用root project来调用generateProjectDirConfigMap函数
        settings.gradle.rootProject {
            // 查找所有包含build.gradle.kts的项目目录
            val blackListDirs = extension.excludeModules
            val projectConfigs = this.generateProjectDirConfigMap(blackListDirs)
            projectDirs = projectConfigs.map { File(it.sourceDir).parentFile }
        }

        // 在项目评估完成后执行包含逻辑
        settings.gradle.projectsEvaluated {
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

}
