package com.addzero.codegen.engine

import com.addzero.codegen.core.CodeGenConfig
import com.addzero.codegen.core.TemplateContext
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import java.io.File
import java.io.StringWriter

/**
 * 统一的 Velocity 代码生成引擎
 *
 * 提供基于 Velocity 模板引擎的代码生成能力
 * 支持 KSP 和传统文件IO两种输出方式
 *
 * @param config 代码生成配置
 * @param logger KSP日志记录器
 * @param kspCodeGenerator KSP代码生成器（可选，如果提供则使用KSP方式输出）
 */
class VelocityCodeGenerator(
    private val config: CodeGenConfig,
    private val logger: KSPLogger,
    private val kspCodeGenerator: CodeGenerator? = null
) {

    private val velocityEngine: VelocityEngine by lazy {
        initializeVelocityEngine()
    }

    /**
     * 批量生成代码
     *
     * @param metadataList 元数据列表
     * @param templateContext 模板上下文实现
     * @param dependencies KSP依赖文件（使用KSP输出时需要）
     */
    fun <T> generateCode(
        metadataList: List<T>,
        templateContext: TemplateContext<T>,
        dependencies: Dependencies = Dependencies.ALL_FILES
    ) {
        if (metadataList.isEmpty()) {
            logger.info("没有元数据，跳过代码生成")
            return
        }

        logger.info("开始批量生成代码，共 ${metadataList.size} 项")

        var successCount = 0
        var skipCount = 0
        var errorCount = 0

        metadataList.forEach { metadata ->
            try {
                // 检查是否应该跳过
                if (templateContext.shouldSkip(metadata)) {
                    skipCount++
                    logger.info("跳过生成: ${templateContext.getOutputFileName(metadata)}")
                    return@forEach
                }

                // 生成单个文件
                generateSingleFile(metadata, templateContext, dependencies)
                successCount++

            } catch (e: Exception) {
                errorCount++
                logger.error("生成代码失败: ${templateContext.getOutputFileName(metadata)}, 错误: ${e.message}")
                if (config.debugMode) {
                    e.printStackTrace()
                }
            }
        }

        logger.info("代码生成完成 - 成功: $successCount, 跳过: $skipCount, 失败: $errorCount")
    }

    /**
     * 生成单个文件
     *
     * @param metadata 元数据
     * @param templateContext 模板上下文
     * @param dependencies KSP依赖文件
     */
    private fun <T> generateSingleFile(
        metadata: T,
        templateContext: TemplateContext<T>,
        dependencies: Dependencies
    ) {
        // 执行生成前回调
        templateContext.beforeGenerate(metadata)

        // 构建模板上下文
        val contextMap = templateContext.buildContext(metadata)
        val velocityContext = VelocityContext().apply {
            contextMap.forEach { (key, value) ->
                put(key, value)
            }
            // 添加一些通用工具类到上下文
            put("stringUtil", StringUtils)
            put("fileUtil", FileUtils)
        }

        // 获取模板和输出信息
        val templateName = templateContext.getTemplateName(metadata)
        val fileName = templateContext.getOutputFileName(metadata)
        val packageName = templateContext.getOutputPackage(metadata)
        val shouldOverwrite = templateContext.shouldOverwriteExisting(metadata)

        // 渲染模板
        val generatedCode = renderTemplate(templateName, velocityContext)

        // 输出代码
        if (kspCodeGenerator != null) {
            // 使用KSP方式输出
            outputWithKsp(generatedCode, fileName, packageName, dependencies)
        } else {
            // 使用文件IO方式输出
            outputWithFileIO(generatedCode, fileName, packageName, shouldOverwrite)
        }

        // 执行生成后回调
        templateContext.afterGenerate(metadata, generatedCode)

        if (config.debugMode) {
            logger.info("成功生成文件: $packageName.$fileName")
        }
    }

    /**
     * 渲染Velocity模板
     *
     * @param templateName 模板名称
     * @param context Velocity上下文
     * @return 渲染后的代码内容
     */
    private fun renderTemplate(templateName: String, context: VelocityContext): String {
        val template = try {
            velocityEngine.getTemplate(templateName, config.encoding)
        } catch (e: Exception) {
            throw IllegalArgumentException("无法加载模板: $templateName", e)
        }

        val writer = StringWriter()
        template.merge(context, writer)
        return writer.toString()
    }

    /**
     * 使用KSP方式输出代码
     */
    private fun outputWithKsp(
        code: String,
        fileName: String,
        packageName: String,
        dependencies: Dependencies
    ) {
        requireNotNull(kspCodeGenerator) { "KSP代码生成器不能为空" }

        try {
            kspCodeGenerator.createNewFile(
                dependencies = dependencies,
                packageName = packageName,
                fileName = fileName.removeSuffix(".kt")
            ).use { stream ->
                stream.write(code.toByteArray(charset(config.encoding)))
            }
        } catch (e: Exception) {
            throw RuntimeException("KSP输出文件失败: $packageName.$fileName", e)
        }
    }

    /**
     * 使用文件IO方式输出代码
     *
     * @param shouldOverwrite 是否覆盖已存在的文件（模板级别控制）
     */
    private fun outputWithFileIO(
        code: String,
        fileName: String,
        packageName: String,
        shouldOverwrite: Boolean
    ) {
        // 构建输出路径
        val packagePath = packageName.replace('.', File.separatorChar)
        val outputDir = File(config.outputDirectory, packagePath)

        // 确保目录存在
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        // 检查文件是否已存在
        val outputFile = File(outputDir, fileName)
        if (outputFile.exists() && !shouldOverwrite) {
            logger.warn("文件已存在，跳过生成: ${outputFile.absolutePath}")
            return
        }

        // 写入文件
        try {
            outputFile.writeText(code, charset(config.encoding))
            val action = if (outputFile.exists()) "覆盖" else "新建"
            logger.info("文件${action}生成成功: ${outputFile.absolutePath}")
        } catch (e: Exception) {
            throw RuntimeException("文件写入失败: ${outputFile.absolutePath}", e)
        }
    }

    /**
     * 初始化Velocity引擎
     */
    private fun initializeVelocityEngine(): VelocityEngine {
        val engine = VelocityEngine()

        // 应用配置
        config.velocityProperties.forEach { (key, value) ->
            engine.setProperty(key, value)
        }

        // 设置模板目录
        if (config.templateDirectory.startsWith("classpath:")) {
            // 类路径资源
            engine.setProperty("resource.loader", "classpath")
            engine.setProperty("classpath.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader")
        } else {
            // 文件系统路径
            engine.setProperty("resource.loader", "file")
            engine.setProperty("file.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.FileResourceLoader")
            engine.setProperty("file.resource.loader.path", config.templateDirectory)
        }

        // 初始化引擎
        try {
            engine.init()
            logger.info("Velocity引擎初始化成功")
        } catch (e: Exception) {
            throw RuntimeException("Velocity引擎初始化失败", e)
        }

        return engine
    }

    /**
     * 字符串工具类，提供给模板使用
     */
    object StringUtils {
        @JvmStatic
        fun capitalize(str: String): String = str.capitalize()

        @JvmStatic
        fun uncapitalize(str: String): String = str.replaceFirstChar { it.lowercase() }

        @JvmStatic
        fun camelToSnake(str: String): String = str.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()

        @JvmStatic
        fun snakeToCamel(str: String): String = str.split("_").joinToString("") { it.capitalize() }

        @JvmStatic
        fun isEmpty(str: String?): Boolean = str.isNullOrEmpty()

        @JvmStatic
        fun isNotEmpty(str: String?): Boolean = !str.isNullOrEmpty()
    }

    /**
     * 文件工具类，提供给模板使用
     */
    object FileUtils {
        @JvmStatic
        fun getFileNameWithoutExtension(fileName: String): String {
            val lastDot = fileName.lastIndexOf('.')
            return if (lastDot > 0) fileName.substring(0, lastDot) else fileName
        }

        @JvmStatic
        fun getFileExtension(fileName: String): String {
            val lastDot = fileName.lastIndexOf('.')
            return if (lastDot > 0) fileName.substring(lastDot + 1) else ""
        }
    }
}
