package site.addzero.gradle.plugin.kspbuddy

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.OutputDirectory
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.io.File

private const val GEN_DIR = "build-logic/src/main/kotlin/conventions/generated"

/**
 * KspBuddyPlugin Gradle 插件主类
 * 该插件用于生成KSP处理器所需的配置脚本
 */
class KspBuddyPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // 创建扩展对象，用于配置mustMap
        val extension = project.extensions.create<KspBuddyExtension>("kspBuddy")

        // 设置默认值
        extension.kspScriptOutputDir.convention(GEN_DIR)

        // 创建生成脚本的任务
        val generateTask = project.tasks.register<GenerateKspScriptTask>("generateKspScript") {
            description = "Generates KSP configuration script based on mustMap"
            group = "build"

            // 设置输出文件路径，使用模块名称作为文件名的一部分
            val outputDirPath = extension.kspScriptOutputDir.getOrElse(GEN_DIR)
            val generatedDir = File(project.rootProject.projectDir, outputDirPath)
            val moduleName = getModuleName(project)
            outputFile = File(generatedDir, "ksp4${moduleName}.gradle.kts")

            // 设置生成代码的输出目录
            val buildOutputDir = project.layout.buildDirectory.dir("generated/ksp-buddy")
            generatedCodeOutputDir.set(buildOutputDir)

            // 传递mustMap给任务
            mustMap.set(extension.mustMap)

            // 传递SettingContext配置
            settingContextConfig.set(extension.settingContext)
            targetProject.set(project)
        }

        // 将生成的代码目录添加到 Kotlin 源集
        project.afterEvaluate {
            try {
                // 尝试添加源代码目录，简化版本避免复杂的类型推断
                val outputDir = generateTask.flatMap { it.generatedCodeOutputDir }
                project.tasks.findByName("compileKotlin")?.let { compileTask ->
                    compileTask.dependsOn(generateTask)
                }
            } catch (e: Exception) {
                project.logger.warn("Failed to configure Kotlin source sets: ${e.message}")
            }
        }

        // 只有当mustMap有配置且明确指定需要生成预编译脚本时才自动生成
        project.afterEvaluate {
            if (extension.mustMap.isPresent && extension.mustMap.get().isNotEmpty()
                && extension.generatePrecompiledScript.getOrElse(false)) {
                // 使用任务执行而不是直接调用方法，确保 Gradle 能正确跟踪依赖
                generateTask.get()
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
abstract class GenerateKspScriptTask : DefaultTask() {
    @get:org.gradle.api.tasks.Input
    abstract val mustMap: MapProperty<String, String>

    @get:org.gradle.api.tasks.Input
    abstract val settingContextConfig: Property<SettingContextConfig>

    @get:org.gradle.api.tasks.Input
    abstract val targetProject: Property<Project>

    @get:org.gradle.api.tasks.OutputFile
    abstract var outputFile: File

    @get:OutputDirectory
    abstract val generatedCodeOutputDir: DirectoryProperty

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

        // 生成Settings数据类和SettingContext对象
        val generatedFiles = generateSettingsAndContext()

        // 使用 Gradle 刷新助手
        val refreshHelper = project.objects.newInstance(GradleRefreshHelper::class.java)
        refreshHelper.requestGradleReevaluation(targetProject.get())
        refreshHelper.generateIdeRefreshHint(targetProject.get(), generatedFiles)
    }

    private fun generateSettingsAndContext(): List<File> {
        val config = settingContextConfig.get()
        if (!config.enabled) {
            logger.lifecycle("SettingContext generation is disabled")
            return emptyList()
        }

        val project = targetProject.get()
        val mustMap = mustMap.get()

        // 使用任务配置的输出目录
        val outputDir = generatedCodeOutputDir.get().asFile
        val packageDir = File(outputDir, config.packageName.replace(".", "/"))
        packageDir.mkdirs()

        logger.lifecycle("Generating Settings and SettingContext in: ${packageDir.absolutePath}")
        logger.lifecycle("Package name: ${config.packageName}")
        logger.lifecycle("MustMap content: $mustMap")

        val generatedFiles = mutableListOf<File>()

        // 生成Settings数据类
        val settingsFile = generateSettingsDataClass(packageDir, config.settingsClassName, mustMap, config.packageName)
        if (settingsFile != null) {
            generatedFiles.add(settingsFile)
        }

        // 生成SettingContext对象
        val contextFile = generateSettingContextObject(
            packageDir,
            config.contextClassName,
            config.settingsClassName,
            config.packageName,
            mustMap
        )
        if (contextFile != null) {
            generatedFiles.add(contextFile)
        }

        return generatedFiles
    }

    private fun generateSettingsDataClass(
        packageDir: File,
        className: String,
        properties: Map<String, String>,
        packageName: String
    ): File? {
        val file = File(packageDir, "${className}.kt")

        // 如果没有属性，不需要生成文件
        if (properties.isEmpty()) {
            logger.lifecycle("No properties provided, skipping Settings data class generation")
            return null
        }

        // 使用字符串模板生成代码，只根据mustMap生成属性（不包含序列化注解）
        val template = """
            package $packageName

            data class $className(
            <%properties%>
            )
        """.trimIndent()

        // 生成属性列表，完全基于传入的properties map
        val propertyLines = properties.map { (key, value) -> "    val $key: String = \"$value\"," }

        // 组合最终内容
        val propertiesContent = propertyLines.joinToString("\n")
        val finalContent = template.replace("<%properties%>", propertiesContent)
            .replace(",\n            )", "\n            )")  // 处理最后一个逗号

        file.writeText(finalContent)
        logger.lifecycle("Generated Settings data class to: ${file.absolutePath}")
        return file
    }

    private fun generateSettingContextObject(
        packageDir: File,
        objectName: String,
        settingsClassName: String,
        packageName: String,
        properties: Map<String, String>
    ): File? {
        val file = File(packageDir, "${objectName}.kt")

        // 如果没有属性，不需要生成文件
        if (properties.isEmpty()) {
            logger.lifecycle("No properties provided, skipping SettingContext object generation")
            return null
        }

        // 使用字符串模板生成代码
        val template = """
            package $packageName

            import java.util.concurrent.atomic.AtomicReference

            object $objectName {
                private val settingsRef = AtomicReference<$settingsClassName?>()

                val settings: $settingsClassName
                    get() = settingsRef.get() ?: $settingsClassName()

                fun initialize(op: Map<String, String>) {
                    // 使用简单的map到对象转换，避免依赖外部库
                    val mapToBean = $settingsClassName(
            <%properties%>
                    )
                    settingsRef.compareAndSet(null, mapToBean)
                }
            }
        """.trimIndent()

        // 生成属性列表，完全基于传入的properties map
        val propertyLines = properties.map { (key, _) -> "            $key = op[\"$key\"] ?: \"\"," }

        // 组合最终内容
        val propertiesContent = propertyLines.joinToString("\n")
        val finalContent = template.replace("<%properties%>", propertiesContent)
            .replace(",\n                    )", "\n                    )")  // 处理最后一个逗号

        file.writeText(finalContent)
        logger.lifecycle("Generated SettingContext object to: ${file.absolutePath}")
        return file
    }
}
