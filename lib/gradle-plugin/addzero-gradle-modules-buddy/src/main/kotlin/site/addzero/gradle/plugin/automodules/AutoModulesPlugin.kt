package site.addzero.gradle.plugin.automodules

import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import site.addzero.gradle.tool.findAllProjectDirs
import site.addzero.gradle.tool.getRelativePath
import java.io.File

/**
 * 自动模块插件扩展配置
 */
open class AutoModulesPluginExtension {
    /**
     * 默认的黑名单目录列表
     */
    var excludeModules: List<String> = listOf("buildSrc", "build-logic")
}

/**
 * 自动模块插件
 * 自动扫描项目目录并包含所有Gradle子项目
 */
class AutoModulesPlugin : Plugin<Settings> {
    override fun apply(settings: Settings) {
        // 创建扩展配置
        val extension = settings.extensions.create("autoModules", AutoModulesPluginExtension::class.java)
        val rootDir = settings.layout.rootDirectory.asFile

        val allProjectDirs = findAllProjectDirs(rootDir)
            .filter { it.absolutePath != rootDir.absolutePath }
            .filterNot { extension.excludeModules.contains(it.name) }


        val (buildProj, modules) = allProjectDirs.partition {
            val isBuildLogic = it.name.startsWith("build-logic") || it.name.startsWith("buildLogic")
            isBuildLogic
        }
        buildProj.forEach {
            settings.includeBuild(it)
            // 打印调试信息
            println("Auto included build: ${it.name}")
        }
        modules.forEach {
            val relativePath = getRelativePath(rootDir, it)
            val moduleName = ":${relativePath.replace(File.separator, ":")}"
            settings.include(moduleName)
            println("Auto included module: $moduleName")
        }


    }
}

