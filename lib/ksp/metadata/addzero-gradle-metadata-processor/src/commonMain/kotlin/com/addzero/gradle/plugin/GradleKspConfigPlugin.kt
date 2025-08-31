package com.addzero.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

/**
 * Gradle KSP配置插件
 *
 * 在项目刷新后自动收集所有模块的KSP配置并生成强类型的GradleSettingContext对象
 */
class GradleKspConfigPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // 在项目评估后执行
        project.afterEvaluate {
            // 只在根项目上创建任务
            if (project == project.rootProject) {
                createGenerateConfigTask(project)
            }
        }
    }

    /**
     * 创建生成配置的任务
     */
    private fun createGenerateConfigTask(project: Project) {
        val task = project.tasks.register("generateGradleKspConfig") {
            doLast {
                generateGradleSettingContext(project)
            }
        }

        // 将任务添加到构建生命周期中
        project.tasks.findByName("build")?.dependsOn(task)
    }

    /**
     * 生成GradleSettingContext对象
     */
    private fun generateGradleSettingContext(project: Project) {
        val rootProject = project.rootProject
        val generatedDir = File(rootProject.buildDir, "generated/kspconfig")
        generatedDir.mkdirs()

        val outputFile = File(generatedDir, "GradleSettingContext.kt")

        // 收集所有模块的KSP配置
        val moduleConfigs = collectModuleConfigs(rootProject)

        // 生成Kotlin代码
        val code = generateKotlinCode(moduleConfigs)

        outputFile.writeText(code)

        // 打印生成的文件路径
        project.logger.lifecycle("Generated GradleSettingContext to: ${outputFile.absolutePath}")
    }

    /**
     * 收集所有模块的KSP配置
     */
    private fun collectModuleConfigs(rootProject: Project): Map<String, Map<String, String>> {
        val moduleConfigs = mutableMapOf<String, Map<String, String>>()

        rootProject.allprojects { project ->
            val moduleName = getModuleName(project)
            val kspOptions = getKspOptions(project)

            if (kspOptions.isNotEmpty()) {
                moduleConfigs[moduleName] = kspOptions
            }
        }

        return moduleConfigs
    }

    /**
     * 获取模块名称（将路径中的特殊字符替换为下划线）
     */
    private fun getModuleName(project: Project): String {
        return if (project == project.rootProject) {
            "root"
        } else {
            project.path
                .removePrefix(":")
                .replace(":", "_")
                .replace("-", "_")
                .replace(".", "_")
        }
    }

    /**
     * 获取项目的KSP配置选项
     */
    private fun getKspOptions(project: Project): Map<String, String> {
        val kspOptions = mutableMapOf<String, String>()

        // 检查项目是否应用了KSP插件
        if (project.plugins.hasPlugin("com.google.devtools.ksp")) {
            // 通过项目属性获取KSP配置
            project.properties.forEach { (key, value) ->
                if (key is String && value is String && key.startsWith("ksp.")) {
                    val cleanKey = key.substring(4) // 移除"ksp."前缀
                    kspOptions[cleanKey] = value
                }
            }

            // 通过扩展属性获取KSP配置
            project.extensions.extraProperties.properties.forEach { (key, value) ->
                if (key is String && value is String && key.startsWith("ksp.")) {
                    val cleanKey = key.substring(4) // 移除"ksp."前缀
                    kspOptions[cleanKey] = value
                }
            }
        }

        return kspOptions
    }

    /**
     * 生成Kotlin代码
     */
    private fun generateKotlinCode(moduleConfigs: Map<String, Map<String, String>>): String {
        return buildString {
            appendLine("/**")
            appendLine(" * 自动生成的Gradle设置上下文")
            appendLine(" * 包含所有模块的KSP配置")
            appendLine(" */")
            appendLine("@file:JvmName(\"GradleSettingContext\")")
            appendLine("object GradleSettingContext {")
            appendLine()

            moduleConfigs.forEach { (moduleName, config) ->
                val objectName = toPascalCase(moduleName)
                appendLine("    /**")
                appendLine("     * $moduleName 模块的KSP配置")
                appendLine("     */")
                appendLine("    object $objectName {")

                config.forEach { (key, value) ->
                    // 清理键名，移除特殊字符并转换为合法的属性名
                    val cleanKeyName = key.replace(Regex("[^a-zA-Z0-9_]"), "_")
                    val propertyName = toCamelCase(cleanKeyName)
                    appendLine("        const val $propertyName = \"${value.replace("\"", "\\\"")}\"")
                }

                appendLine("    }")
                appendLine()
            }

            appendLine("}")
        }
    }

    /**
     * 将键名转换为驼峰命名法
     */
    private fun toCamelCase(key: String): String {
        return key.split('.', '-', '_', ' ')
            .filter { it.isNotEmpty() }
            .mapIndexed { index, part ->
                if (index == 0) {
                    part.replaceFirstChar { it.lowercase() }
                } else {
                    part.replaceFirstChar { it.uppercase() }
                }
            }
            .joinToString("")
    }

    /**
     * 将键名转换为帕斯卡命名法（首字母大写）
     */
    private fun toPascalCase(key: String): String {
        return key.split('.', '-', '_', ' ')
            .filter { it.isNotEmpty() }
            .joinToString("") { part ->
                part.replaceFirstChar { it.uppercase() }
            }
    }
}
