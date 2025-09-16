package site.addzero.gradle.plugin.kspbuddy

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.MapProperty
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.io.File

/**
 * KspBuddyPlugin Gradle 插件主类
 * 该插件用于生成KSP处理器所需的配置脚本
 */
class KspBuddyPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // 创建扩展对象，用于配置mustMap
        val extension = project.extensions.create<KspBuddyExtension>("kspBuddy")

        // 创建生成脚本的任务
        val generateTask = project.tasks.register<GenerateKspScriptTask>("generateKspScript") {
            description = "Generates KSP configuration script based on mustMap"
            group = "build"

            // 设置输出文件路径，使用模块名称作为文件名的一部分
            val generatedDir = File(project.rootProject.projectDir, "build-logic/src/main/kotlin/convention-plugins/generated")
            val moduleName = getModuleName(project)
            outputFile = File(generatedDir, "ksp-config4${moduleName}.gradle.kts")

            // 传递mustMap给任务
            mustMap.set(extension.mustMap)
        }

        // 在项目配置完成后自动生成配置文件
        project.afterEvaluate {
            // 只有当mustMap有配置时才生成
            if (extension.mustMap.isPresent && extension.mustMap.get().isNotEmpty()) {
                generateTask.get().generate()
            }
        }
    }

    /**
     * 获取模块名称，用于生成文件名
     */
    private fun getModuleName(project: Project): String {
        // 获取项目路径并转换为合适的文件名格式
        val path = project.path
        return if (path == ":") {
            "root"
        } else {
            // 移除冒号并替换为连字符
            path.substring(1).replace(":", "-")
        }
    }
}

/**
 * 生成KSP配置脚本的任务
 */
abstract class GenerateKspScriptTask: DefaultTask() {
    @get:org.gradle.api.tasks.Input
    abstract val mustMap: MapProperty<String, String>

    @get:org.gradle.api.tasks.OutputFile
    abstract var outputFile: File

    @org.gradle.api.tasks.TaskAction
    fun generate() {
        // 确保输出目录存在
        outputFile.parentFile.mkdirs()

        // 生成脚本内容
        val content = buildString {
            appendLine("plugins {")
            appendLine("    id(\"com.google.devtools.ksp\")")
            appendLine("}")
            appendLine()
            appendLine("ksp {")

            // 添加所有配置项
            mustMap.get().forEach { (key, value) ->
                appendLine("    arg(\"$key\", \"$value\")")
            }

            appendLine("}")
        }

        // 写入文件
        outputFile.writeText(content)

        logger.lifecycle("Generated KSP configuration script to: ${outputFile.absolutePath}")
    }
}
