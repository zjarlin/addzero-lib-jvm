package site.addzero.gradle.plugin.kspbuddy

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.MapProperty
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.mapProperty
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
        project.tasks.register<GenerateKspScriptTask>("generateKspScript") {
            description = "Generates KSP configuration script based on mustMap"
            group = "build"

            // 设置输出文件路径
            val generatedDir = File(project.rootProject.projectDir, "buildSrc/src/main/kotlin/generated")
            outputFile = File(generatedDir, "ksp-config.gradle.kts")

            // 传递mustMap给任务
            mustMap.set(extension.mustMap)
        }
    }
}

/**
 * KspBuddy插件的扩展类，用于声明KSP处理器必须的上下文
 */
open class KspBuddyExtension(project: Project) {
    /**
     * KSP处理器必须的上下文参数映射
     * 来源可以是配置文件或直接声明
     */
    val mustMap: MapProperty<String, String> = project.objects.mapProperty<String, String>().apply {
        convention(mapOf( ))
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
