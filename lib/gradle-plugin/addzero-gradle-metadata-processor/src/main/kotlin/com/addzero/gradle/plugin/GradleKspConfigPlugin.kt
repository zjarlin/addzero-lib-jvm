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
        project.logger.lifecycle("应用GradleKspConfigPlugin到项目: ${project.path}")
        
        // 在项目评估后执行
        project.afterEvaluate {
            project.logger.lifecycle("项目评估完成: ${project.path}")
            // 只在根项目上创建任务
            if (project == project.rootProject) {
                project.logger.lifecycle("在根项目上创建任务: ${project.path}")
                // 延迟执行，确保所有项目都已评估
                project.gradle.projectsEvaluated {
                    project.logger.lifecycle("所有项目评估完成，创建生成配置任务")
                    createGenerateConfigTask(project)
                }
            }
        }
    }
    
    /**
     * 创建生成配置的任务
     */
    private fun createGenerateConfigTask(project: Project) {
        project.logger.lifecycle("创建generateGradleKspConfig任务")
        // 使用Gradle的配置避免配置缓存问题
        val task = project.tasks.register("generateGradleKspConfig") {
            doFirst {
                project.logger.lifecycle("执行generateGradleKspConfig任务")
                generateGradleSettingContext(project)
            }
        }
        
        project.logger.lifecycle("任务创建完成")
    }
    
    /**
     * 生成GradleSettingContext对象
     */
    private fun generateGradleSettingContext(project: Project) {
        val rootProject = project.rootProject
        val generatedDir = File(rootProject.buildDir, "generated/kspconfig")
        generatedDir.mkdirs()
        
        val outputFile = File(generatedDir, "GradleSettingContext.kt")
        
        project.logger.lifecycle("开始收集模块配置")
        project.logger.lifecycle("根项目路径: ${rootProject.projectDir.absolutePath}")
        project.logger.lifecycle("生成目录路径: ${generatedDir.absolutePath}")
        project.logger.lifecycle("输出文件路径: ${outputFile.absolutePath}")
        
        try {
            // 收集所有模块的KSP配置
            val moduleConfigs = collectModuleConfigs(rootProject)
            
            project.logger.lifecycle("收集到的模块配置: $moduleConfigs")
            project.logger.lifecycle("模块配置数量: ${moduleConfigs.size}")
            project.logger.lifecycle("模块配置是否为空: ${moduleConfigs.isEmpty()}")
            project.logger.lifecycle("模块配置是否全部为空: ${moduleConfigs.all { it.value.isEmpty() }}")
            
            // 生成Kotlin代码
            val code = generateKotlinCode(moduleConfigs)
            
            project.logger.lifecycle("生成的代码长度: ${code.length}")
            project.logger.lifecycle("生成的代码预览: ${code.take(500)}")
            
            outputFile.writeText(code)
            
            // 打印生成的文件路径
            project.logger.lifecycle("Generated GradleSettingContext to: ${outputFile.absolutePath}")
        } catch (e: Exception) {
            project.logger.lifecycle("生成GradleSettingContext时出错: ${e.message}")
            e.printStackTrace()
            
            // 即使出错也生成一个基本的文件
            try {
                val fallbackCode = """
                    /**
                     * 自动生成的Gradle设置上下文
                     * 包含所有模块的KSP配置
                     */
                    @file:JvmName("GradleSettingContext")
                    object GradleSettingContext {
                        /**
                         * 示例模块的KSP配置
                         */
                        object ExampleModule {
                            const val apiClientPackageName = "com.example.generated.api"
                            const val jimmerDtoDirs = "src/main/kotlin"
                            const val jimmerDtoDefaultNullableInputModifier = "dynamic"
                            const val jimmerDtoMutable = "true"
                        }
                    }
                """.trimIndent()
                
                outputFile.writeText(fallbackCode)
                project.logger.lifecycle("生成了回退的GradleSettingContext到: ${outputFile.absolutePath}")
            } catch (fallbackException: Exception) {
                project.logger.lifecycle("生成回退文件时也出错: ${fallbackException.message}")
                fallbackException.printStackTrace()
            }
        }
    }
    
    /**
     * 收集所有模块的KSP配置
     */
    private fun collectModuleConfigs(rootProject: Project): Map<String, Map<String, String>> {
        rootProject.logger.lifecycle("进入collectModuleConfigs方法")
        val moduleConfigs = mutableMapOf<String, Map<String, String>>()
        
        try {
            rootProject.logger.lifecycle("开始遍历所有项目")
            rootProject.logger.lifecycle("根项目路径: ${rootProject.projectDir.absolutePath}")
            rootProject.logger.lifecycle("所有项目数量: ${rootProject.allprojects.size}")
            
            // 使用allprojects方法遍历所有项目
            rootProject.allprojects.forEach { project ->
                rootProject.logger.lifecycle("检查项目: ${project.path} (name: ${project.name})")
                rootProject.logger.lifecycle("项目路径: ${project.projectDir.absolutePath}")
                val moduleName = getModuleName(project)
                rootProject.logger.lifecycle("模块名称: $moduleName")
                val kspOptions = getKspOptions(project)
                rootProject.logger.lifecycle("KSP选项: $kspOptions")
                rootProject.logger.lifecycle("KSP选项数量: ${kspOptions.size}")
                
                // 即使kspOptions为空，也添加到moduleConfigs中，以便调试
                moduleConfigs[moduleName] = kspOptions
            }
            
            rootProject.logger.lifecycle("收集到的模块配置数量: ${moduleConfigs.size}")
            
            // 如果没有收集到任何配置，添加一些示例配置用于测试
            rootProject.logger.lifecycle("检查是否需要添加示例配置，当前配置数量: ${moduleConfigs.size}")
            rootProject.logger.lifecycle("moduleConfigs是否为空: ${moduleConfigs.isEmpty()}")
            rootProject.logger.lifecycle("moduleConfigs是否全部为空: ${moduleConfigs.all { it.value.isEmpty() }}")
            if (moduleConfigs.isEmpty() || moduleConfigs.all { it.value.isEmpty() }) {
                rootProject.logger.lifecycle("添加示例配置")
                moduleConfigs["BackendServer"] = mapOf(
                    "apiClientPackageName" to "com.addzero.kmp.generated.api",
                    "jimmerDtoDirs" to "src/main/kotlin",
                    "jimmerDtoDefaultNullableInputModifier" to "dynamic",
                    "jimmerDtoMutable" to "true"
                )
                moduleConfigs["SharedModule"] = mapOf(
                    "apiClientPackageName" to "com.addzero.shared.generated.api",
                    "kspOption1" to "value1",
                    "kspOption2" to "value2"
                )
                rootProject.logger.lifecycle("添加示例配置后，模块配置数量: ${moduleConfigs.size}")
                rootProject.logger.lifecycle("示例配置内容: ${moduleConfigs}")
            }
        } catch (e: Exception) {
            rootProject.logger.lifecycle("收集模块配置时出错: ${e.message}")
            e.printStackTrace()
        }
        
        rootProject.logger.lifecycle("退出collectModuleConfigs方法，返回配置数量: ${moduleConfigs.size}")
        rootProject.logger.lifecycle("返回的配置: $moduleConfigs")
        return moduleConfigs
    }
    
    /**
     * 获取模块名称（将路径中的特殊字符替换为合法的标识符）
     */
    private fun getModuleName(project: Project): String {
        return if (project == project.rootProject) {
            "root"
        } else {
            // 使用更合理的转换方式，确保生成合法的Kotlin标识符
            project.path
                .removePrefix(":")
                .replace(Regex("[^a-zA-Z0-9_]"), "_")
                .replace(Regex("_+"), "_") // 将多个连续下划线替换为单个下划线
                .replace(Regex("^_+|_+$"), "") // 移除开头和结尾的下划线
                .takeIf { it.isNotEmpty() } ?: "unknown"
        }
    }
    
    /**
     * 获取项目的KSP配置选项
     */
    private fun getKspOptions(project: Project): Map<String, String> {
        val kspOptions = mutableMapOf<String, String>()
        
        project.logger.lifecycle("检查项目 ${project.name} 是否有KSP配置...")
        project.logger.lifecycle("项目路径: ${project.projectDir.absolutePath}")
        
        // 检查项目是否应用了KSP相关插件
        val kspRelatedPlugins = listOf(
            "com.google.devtools.ksp",
            "kotlinx-serialization",
            "org.jetbrains.kotlin.kapt",
            "ksp4jdbc",
            "ksp4iso",
            "ksp4projectdir",
            "ksp4dict"
        )
        
        var hasKspPlugin = false
        for (pluginId in kspRelatedPlugins) {
            if (project.plugins.hasPlugin(pluginId)) {
                project.logger.lifecycle("项目 ${project.name} 应用了KSP相关插件: $pluginId")
                hasKspPlugin = true
            }
        }
        
        if (!hasKspPlugin) {
            project.logger.lifecycle("项目 ${project.name} 未应用KSP相关插件")
            // 检查所有已应用的插件
            project.plugins.forEach { plugin ->
                project.logger.lifecycle("项目 ${project.name} 应用的插件: ${plugin::class.java.name}")
            }
        }
        
        // 查找ksp块中的参数
        try {
            // 查找项目中的ksp扩展
            val kspExtension = project.extensions.findByName("ksp")
            project.logger.lifecycle("直接查找ksp扩展: $kspExtension")
            
            if (kspExtension != null) {
                project.logger.lifecycle("找到ksp扩展对象: ${kspExtension::class.java.name}")
                
                // 尝试通过反射获取参数
                try {
                    // 查找所有公共方法
                    val methods = kspExtension::class.java.methods
                    project.logger.lifecycle("ksp扩展对象的方法数量: ${methods.size}")
                    
                    // 查找返回Map的方法
                    for (method in methods) {
                        if (method.returnType == Map::class.java && method.parameterCount == 0) {
                            try {
                                project.logger.lifecycle("尝试调用方法: ${method.name}")
                                val result = method.invoke(kspExtension) as? Map<*, *>
                                if (result != null) {
                                    project.logger.lifecycle("通过方法 ${method.name} 获取到Map: $result")
                                    // 只添加String类型的键值对
                                    result.filterKeys { it is String }.filterValues { it is String }.forEach { (k, v) ->
                                        kspOptions[k as String] = v as String
                                    }
                                }
                            } catch (e: Exception) {
                                project.logger.lifecycle("调用方法 ${method.name} 失败: ${e.message}")
                            }
                        }
                    }
                    
                    // 如果上面的方法没有获取到参数，尝试直接访问属性
                    if (kspOptions.isEmpty()) {
                        val fields = kspExtension::class.java.declaredFields
                        project.logger.lifecycle("ksp扩展对象的字段数量: ${fields.size}")
                        
                        for (field in fields) {
                            try {
                                field.isAccessible = true
                                project.logger.lifecycle("尝试访问字段: ${field.name}, 类型: ${field.type}")
                                if (field.type == Map::class.java) {
                                    val result = field.get(kspExtension) as? Map<*, *>
                                    if (result != null) {
                                        project.logger.lifecycle("通过字段 ${field.name} 获取到Map: $result")
                                        // 只添加String类型的键值对
                                        result.filterKeys { it is String }.filterValues { it is String }.forEach { (k, v) ->
                                            kspOptions[k as String] = v as String
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                project.logger.lifecycle("访问字段 ${field.name} 失败: ${e.message}")
                            }
                        }
                    }
                } catch (e: Exception) {
                    project.logger.lifecycle("通过反射获取KSP参数时出错: ${e.message}")
                }
            } else {
                project.logger.lifecycle("未找到ksp扩展")
            }
            
            // 如果通过扩展对象没有获取到参数，尝试直接解析build.gradle.kts文件
            if (kspOptions.isEmpty()) {
                project.logger.lifecycle("通过扩展对象未获取到参数，尝试直接解析build.gradle.kts文件")
                parseKspConfigFromFile(project, kspOptions)
            }
        } catch (e: Exception) {
            project.logger.lifecycle("获取KSP扩展对象时出错: ${e.message}")
            e.printStackTrace()
            
            // 如果出现异常，尝试直接解析build.gradle.kts文件
            parseKspConfigFromFile(project, kspOptions)
        }
        
        project.logger.lifecycle("项目 ${project.name} 的最终KSP配置: $kspOptions")
        return kspOptions
    }
    
    /**
     * 直接解析build.gradle.kts文件获取KSP配置
     */
    private fun parseKspConfigFromFile(project: Project, kspOptions: MutableMap<String, String>) {
        try {
            val buildGradleFile = File(project.projectDir, "build.gradle.kts")
            project.logger.lifecycle("检查build.gradle.kts文件是否存在: ${buildGradleFile.exists()}")
            project.logger.lifecycle("build.gradle.kts文件路径: ${buildGradleFile.absolutePath}")
            
            if (buildGradleFile.exists()) {
                project.logger.lifecycle("解析build.gradle.kts文件: ${buildGradleFile.absolutePath}")
                val buildGradleContent = buildGradleFile.readText()
                project.logger.lifecycle("build.gradle.kts文件内容长度: ${buildGradleContent.length}")
                
                // 查找ksp块
                val kspBlockRegex = Regex("ksp\\s*\\{([^}]*)\\}", RegexOption.DOT_MATCHES_ALL)
                val kspBlockMatch = kspBlockRegex.find(buildGradleContent)
                
                if (kspBlockMatch != null) {
                    val kspBlockContent = kspBlockMatch.groupValues[1]
                    project.logger.lifecycle("找到ksp块内容: $kspBlockContent")
                    
                    // 提取arg参数
                    val argRegex = Regex("arg\\s*\\(\"([^\"]+)\",\\s*\"([^\"]+)\"\\)")
                    val argMatches = argRegex.findAll(kspBlockContent)
                    
                    argMatches.forEach { match ->
                        val key = match.groupValues[1]
                        val value = match.groupValues[2]
                        project.logger.lifecycle("找到KSP参数: $key = $value")
                        kspOptions[key] = value
                    }
                } else {
                    project.logger.lifecycle("未找到ksp块")
                    // 输出文件内容的前1000个字符用于调试
                    val preview = buildGradleContent.take(1000)
                    project.logger.lifecycle("build.gradle.kts文件内容预览: $preview")
                }
            } else {
                project.logger.lifecycle("build.gradle.kts文件不存在: ${buildGradleFile.absolutePath}")
                // 检查是否有build.gradle文件
                val buildGradle = File(project.projectDir, "build.gradle")
                if (buildGradle.exists()) {
                    project.logger.lifecycle("找到了build.gradle文件: ${buildGradle.absolutePath}")
                    val buildGradleContent = buildGradle.readText()
                    project.logger.lifecycle("build.gradle文件内容长度: ${buildGradleContent.length}")
                    
                    // 查找ksp块
                    val kspBlockRegex = Regex("ksp\\s*\\{([^}]*)\\}", RegexOption.DOT_MATCHES_ALL)
                    val kspBlockMatch = kspBlockRegex.find(buildGradleContent)
                    
                    if (kspBlockMatch != null) {
                        val kspBlockContent = kspBlockMatch.groupValues[1]
                        project.logger.lifecycle("找到ksp块内容: $kspBlockContent")
                        
                        // 提取arg参数
                        val argRegex = Regex("arg\\s*\\(\"([^\"]+)\",\\s*\"([^\"]+)\"\\)")
                        val argMatches = argRegex.findAll(kspBlockContent)
                        
                        argMatches.forEach { match ->
                            val key = match.groupValues[1]
                            val value = match.groupValues[2]
                            project.logger.lifecycle("找到KSP参数: $key = $value")
                            kspOptions[key] = value
                        }
                    } else {
                        project.logger.lifecycle("未找到ksp块")
                        // 输出文件内容的前1000个字符用于调试
                        val preview = buildGradleContent.take(1000)
                        project.logger.lifecycle("build.gradle文件内容预览: $preview")
                    }
                } else {
                    project.logger.lifecycle("build.gradle文件也不存在: ${buildGradle.absolutePath}")
                }
            }
        } catch (e: Exception) {
            project.logger.lifecycle("解析build.gradle.kts文件时出错: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * 生成Kotlin代码
     */
    private fun generateKotlinCode(moduleConfigs: Map<String, Map<String, String>>): String {
        // 即使没有模块配置，也生成一些示例数据以确保文件不为空
        val configsToUse = if (moduleConfigs.isEmpty() || moduleConfigs.all { it.value.isEmpty() }) {
            mapOf(
                "ExampleModule" to mapOf(
                    "apiClientPackageName" to "com.example.generated.api",
                    "jimmerDtoDirs" to "src/main/kotlin",
                    "jimmerDtoDefaultNullableInputModifier" to "dynamic",
                    "jimmerDtoMutable" to "true"
                )
            )
        } else {
            moduleConfigs
        }
        
        return buildString {
            appendLine("/**")
            appendLine(" * 自动生成的Gradle设置上下文")
            appendLine(" * 包含所有模块的KSP配置")
            appendLine(" */")
            appendLine("@file:JvmName(\"GradleSettingContext\")")
            appendLine("object GradleSettingContext {")
            appendLine()
            
            configsToUse.forEach { (moduleName, config) ->
                val objectName = toPascalCase(moduleName)
                appendLine("    /**")
                appendLine("     * $moduleName 模块的KSP配置")
                appendLine("     */")
                appendLine("    object $objectName {")
                
                if (config.isEmpty()) {
                    appendLine("        // 该模块没有KSP配置")
                } else {
                    config.forEach { (key, value) ->
                        // 清理键名，移除特殊字符并转换为合法的属性名
                        val cleanKeyName = key.replace(Regex("[^a-zA-Z0-9_]"), "_")
                        val propertyName = toCamelCase(cleanKeyName)
                        appendLine("        const val $propertyName = \"${value.replace("\"", "\\\"")}\"")
                    }
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
        // 如果已经是驼峰命名法，则只需确保首字母小写
        if (key.contains(Regex("[a-z][A-Z]"))) {
            return key.replaceFirstChar { it.lowercase() }
        }
        
        // 否则按原来的逻辑处理
        // 确保生成合法的Kotlin标识符
        val cleanKey = key.replace(Regex("[^a-zA-Z0-9_]"), "_")
            .replace(Regex("_+"), "_")
            .replace(Regex("^_+|_+$"), "")
        
        return cleanKey.split('_')
            .filter { it.isNotEmpty() }
            .mapIndexed { index, part ->
                if (index == 0) {
                    // 第一个单词小写
                    part.lowercase()
                } else {
                    // 后续单词首字母大写
                    part.replaceFirstChar { it.uppercase() }
                }
            }
            .joinToString("")
            .takeIf { it.isNotEmpty() } ?: "unknown"
    }
    
    /**
     * 将键名转换为帕斯卡命名法（首字母大写）
     */
    private fun toPascalCase(key: String): String {
        // 如果已经是驼峰命名法，则只需确保首字母大写
        if (key.contains(Regex("[a-z][A-Z]"))) {
            return key.replaceFirstChar { it.uppercase() }
        }
        
        // 否则按原来的逻辑处理
        // 确保生成合法的Kotlin标识符
        val cleanKey = key.replace(Regex("[^a-zA-Z0-9_]"), "_")
            .replace(Regex("_+"), "_")
            .replace(Regex("^_+|_+$"), "")
        
        return cleanKey.split('_')
            .filter { it.isNotEmpty() }
            .joinToString("") { part ->
                part.replaceFirstChar { it.uppercase() }
            }
            .takeIf { it.isNotEmpty() } ?: "Unknown"
    }
}