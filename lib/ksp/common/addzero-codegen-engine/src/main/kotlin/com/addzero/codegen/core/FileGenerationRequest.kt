package com.addzero.codegen.core

/**
 * 文件生成请求
 *
 * 表示一个要生成的文件及其相关信息
 */
data class FileGenerationRequest(
    /**
     * 输出文件名（包含扩展名）
     */
    val fileName: String,

    /**
     * 输出包名
     */
    val packageName: String,

    /**
     * 模板名称
     */
    val templateName: String,

    /**
     * 模板上下文数据
     */
    val context: Map<String, Any>,

    /**
     * 是否覆盖已存在的文件
     */
    val shouldOverwrite: Boolean = false,

    /**
     * 生成前回调
     */
    val beforeGenerate: () -> Unit = {},

    /**
     * 生成后回调
     */
    val afterGenerate: (String) -> Unit = {}
)

/**
 * 文件生成器接口
 *
 * 负责将元数据转换为文件生成请求
 * 支持一对多的文件生成模式
 *
 * @param T 元数据类型
 */
interface FileGenerator<T> {

    /**
     * 生成器标识
     */
    val generatorId: String

    /**
     * 将元数据转换为文件生成请求列表
     *
     * @param metadata 单个元数据对象
     * @return 文件生成请求列表（可以是多个文件）
     */
    fun generateFiles(metadata: T): List<FileGenerationRequest>

    /**
     * 批量生成文件请求
     *
     * @param metadataList 元数据列表
     * @return 所有的文件生成请求
     */
    fun generateFiles(metadataList: List<T>): List<FileGenerationRequest> {
        return metadataList.flatMap { generateFiles(it) }
    }

    /**
     * 是否应该跳过生成
     *
     * @param metadata 元数据对象
     * @return true表示跳过生成
     */
    fun shouldSkip(metadata: T): Boolean = false
}

/**
 * 抽象文件生成器基类
 *
 * 提供模板上下文构建的通用逻辑
 */
abstract class AbstractFileGenerator<T> : FileGenerator<T> {

    /**
     * 构建模板上下文
     * 子类实现具体的上下文构建逻辑
     */
    protected abstract fun buildContext(metadata: T): Map<String, Any>

    /**
     * 获取文件生成配置
     * 子类实现具体的文件配置逻辑
     */
    protected abstract fun getFileConfigs(metadata: T): List<FileConfig>

    override fun generateFiles(metadata: T): List<FileGenerationRequest> {
        if (shouldSkip(metadata)) {
            return emptyList()
        }

        val context = buildContext(metadata)
        val configs = getFileConfigs(metadata)

        return configs.map { config ->
            FileGenerationRequest(
                fileName = config.fileName,
                packageName = config.packageName,
                templateName = config.templateName,
                context = context + config.additionalContext,
                shouldOverwrite = config.shouldOverwrite,
                beforeGenerate = { beforeGenerate(metadata, config) },
                afterGenerate = { code -> afterGenerate(metadata, config, code) }
            )
        }
    }

    /**
     * 生成前回调
     */
    protected open fun beforeGenerate(metadata: T, config: FileConfig) {}

    /**
     * 生成后回调
     */
    protected open fun afterGenerate(metadata: T, config: FileConfig, generatedCode: String) {}

    /**
     * 文件配置
     */
    protected data class FileConfig(
        val fileName: String,
        val packageName: String,
        val templateName: String,
        val shouldOverwrite: Boolean = false,
        val additionalContext: Map<String, Any> = emptyMap()
    )
}

/**
 * 单文件生成器
 *
 * 适用于一个元数据生成一个文件的简单场景
 */
abstract class SingleFileGenerator<T> : AbstractFileGenerator<T>() {

    /**
     * 获取输出文件名
     */
    protected abstract fun getOutputFileName(metadata: T): String

    /**
     * 获取输出包名
     */
    protected abstract fun getOutputPackage(metadata: T): String

    /**
     * 获取模板名称
     */
    protected abstract fun getTemplateName(metadata: T): String

    /**
     * 是否覆盖已存在文件
     */
    protected open fun shouldOverwriteExisting(metadata: T): Boolean = false

    override fun getFileConfigs(metadata: T): List<FileConfig> {
        return listOf(
            FileConfig(
                fileName = getOutputFileName(metadata),
                packageName = getOutputPackage(metadata),
                templateName = getTemplateName(metadata),
                shouldOverwrite = shouldOverwriteExisting(metadata)
            )
        )
    }
}

/**
 * 多文件生成器
 *
 * 适用于一个元数据生成多个文件的复杂场景
 */
abstract class MultiFileGenerator<T> : AbstractFileGenerator<T>() {

    /**
     * 获取多个文件的配置
     * 子类实现具体的多文件生成逻辑
     */
    abstract override fun getFileConfigs(metadata: T): List<FileConfig>
}

/**
 * 复合文件生成器
 *
 * 可以组合多个生成器
 */
class CompositeFileGenerator<T>(
    private val generators: List<FileGenerator<T>>,
    override val generatorId: String
) : FileGenerator<T> {

    override fun generateFiles(metadata: T): List<FileGenerationRequest> {
        return generators.flatMap { generator ->
            if (!generator.shouldSkip(metadata)) {
                generator.generateFiles(metadata)
            } else {
                emptyList()
            }
        }
    }

    override fun shouldSkip(metadata: T): Boolean {
        // 如果所有生成器都跳过，则跳过
        return generators.all { it.shouldSkip(metadata) }
    }
}
