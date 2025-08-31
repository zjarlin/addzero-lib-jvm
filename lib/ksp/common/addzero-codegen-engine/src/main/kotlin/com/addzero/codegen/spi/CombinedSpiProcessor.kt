package com.addzero.codegen.spi

import com.addzero.codegen.core.*
import com.addzero.codegen.ksp.AbstractTemplateProcessor
import com.google.devtools.ksp.processing.*

/**
 * 组合型SPI处理器
 *
 * 支持同时使用多个枚举来聚合代码生成提供者
 * 这样用户可以将核心提供者和自定义提供者组合使用
 *
 * @param T 元数据类型
 */
abstract class CombinedSpiTemplateProcessor<T>(
    codeGenerator: CodeGenerator,
    logger: KSPLogger,
    options: Map<String, String>
) : AbstractTemplateProcessor<T>(codeGenerator, logger, options) {

    /**
     * 获取所有的提供者枚举类
     * 子类需要返回所有要聚合的枚举类
     */
    protected abstract fun getProviderEnumClasses(): List<Class<out Enum<*>>>

    /**
     * 从枚举实例获取提供者
     * 子类需要实现此方法来处理不同类型的枚举
     */
    protected abstract fun getProviderFromEnum(enumConstant: Enum<*>): CodeGenProvider<T>?

    /**
     * 聚合的代码生成提供者列表
     */
    private val aggregatedProviders: List<CodeGenProvider<T>> by lazy {
        val allProviders = mutableListOf<CodeGenProvider<T>>()

        getProviderEnumClasses().forEach { enumClass ->
            logger.info("扫描枚举类: ${enumClass.simpleName}")

            val enumConstants = enumClass.enumConstants
            enumConstants.forEach { enumConstant ->
                val provider = getProviderFromEnum(enumConstant)
                if (provider != null && provider.shouldProcess(options)) {
                    allProviders.add(provider)
                    logger.info("  添加提供者: ${provider.providerId} (${provider.description})")
                } else if (provider != null) {
                    logger.info("  跳过提供者: ${provider.providerId} (已禁用或不满足条件)")
                }
            }
        }

        // 按优先级排序
        val sortedProviders = allProviders.sortedBy { it.priority }
        logger.info("共聚合了 ${sortedProviders.size} 个代码生成提供者")

        sortedProviders
    }

    override fun createMetadataExtractors(): List<MetadataExtractor<T>> {
        return aggregatedProviders.map { it.createMetadataExtractor() }
    }

    override fun createFileGenerators(): List<FileGenerator<T>> {
        return aggregatedProviders.flatMap { it.createFileGenerators() }
    }
}

/**
 * 通用的组合型SPI处理器提供者
 */
abstract class CombinedSpiProcessorProvider<T> : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return createCombinedSpiProcessor(
            environment.codeGenerator,
            environment.logger,
            environment.options
        )
    }

    protected abstract fun createCombinedSpiProcessor(
        codeGenerator: CodeGenerator,
        logger: KSPLogger,
        options: Map<String, String>
    ): CombinedSpiTemplateProcessor<T>
}

/**
 * 智能提供者发现器
 *
 * 自动发现classpath中的CodeGenProviderEnum实现
 */
object SmartProviderDiscovery {

    /**
     * 自动发现指定包下的所有提供者枚举
     *
     * @param basePackage 基础包名
     * @param classLoader 类加载器
     * @return 发现的提供者列表
     */
    fun <T> discoverProviders(
        basePackage: String,
        classLoader: ClassLoader = Thread.currentThread().contextClassLoader
    ): List<CodeGenProvider<T>> {
        // 注意：这是一个编译时工具，实际实现可能需要结合具体的类扫描机制
        // 在KSP环境中，通常通过注解处理器来发现相关类
        return emptyList()
    }

    /**
     * 验证枚举是否为有效的提供者枚举
     */
    fun isValidProviderEnum(enumClass: Class<*>): Boolean {
        return enumClass.isEnum &&
               CodeGenProviderEnum::class.java.isAssignableFrom(enumClass)
    }
}

/**
 * 提供者冲突检测器
 *
 * 检测不同提供者之间的潜在冲突
 */
object ProviderConflictDetector {

    /**
     * 检测提供者之间的冲突
     *
     * @param providers 提供者列表
     * @return 冲突报告列表
     */
    fun <T> detectConflicts(providers: List<CodeGenProvider<T>>): List<ConflictReport> {
        val conflicts = mutableListOf<ConflictReport>()

        // 检测ID冲突
        val providerIds = providers.groupBy { it.providerId }
        providerIds.forEach { (id, providerList) ->
            if (providerList.size > 1) {
                conflicts.add(
                    ConflictReport(
                        type = ConflictType.DUPLICATE_ID,
                        message = "发现重复的提供者ID: $id",
                        affectedProviders = providerList.map { it.providerId }
                    )
                )
            }
        }

        // 检测优先级冲突（相同优先级可能导致不确定的执行顺序）
        val priorityGroups = providers.groupBy { it.priority }
        priorityGroups.forEach { (priority, providerList) ->
            if (providerList.size > 1) {
                conflicts.add(
                    ConflictReport(
                        type = ConflictType.PRIORITY_CONFLICT,
                        message = "发现相同优先级($priority)的提供者，执行顺序不确定",
                        affectedProviders = providerList.map { it.providerId }
                    )
                )
            }
        }

        return conflicts
    }

    /**
     * 冲突报告
     */
    data class ConflictReport(
        val type: ConflictType,
        val message: String,
        val affectedProviders: List<String>
    )

    /**
     * 冲突类型
     */
    enum class ConflictType {
        DUPLICATE_ID,
        PRIORITY_CONFLICT,
        OUTPUT_FILE_CONFLICT,
        TEMPLATE_CONFLICT
    }
}

/**
 * 提供者性能监控器
 *
 * 监控各个提供者的执行性能
 */
class ProviderPerformanceMonitor {

    private val metrics = mutableMapOf<String, ProviderMetrics>()

    /**
     * 记录提供者开始执行
     */
    fun startProvider(providerId: String) {
        metrics[providerId] = ProviderMetrics(
            providerId = providerId,
            startTime = System.currentTimeMillis()
        )
    }

    /**
     * 记录提供者执行完成
     */
    fun endProvider(providerId: String, generatedFiles: Int) {
        metrics[providerId]?.let { metric ->
            metric.endTime = System.currentTimeMillis()
            metric.generatedFiles = generatedFiles
        }
    }

    /**
     * 获取性能报告
     */
    fun getPerformanceReport(): List<ProviderMetrics> {
        return metrics.values
            .filter { it.endTime > 0 }
            .sortedByDescending { it.executionTime }
    }

    /**
     * 提供者性能指标
     */
    data class ProviderMetrics(
        val providerId: String,
        val startTime: Long,
        var endTime: Long = 0,
        var generatedFiles: Int = 0
    ) {
        val executionTime: Long
            get() = if (endTime > 0) endTime - startTime else 0
    }
}

/**
 * 提供者依赖解析器
 *
 * 处理提供者之间的依赖关系
 */
object ProviderDependencyResolver {

    /**
     * 根据依赖关系对提供者进行拓扑排序
     *
     * @param providers 提供者列表
     * @return 按依赖顺序排序的提供者列表
     */
    fun <T> resolveDependencies(providers: List<CodeGenProvider<T>>): List<CodeGenProvider<T>> {
        // 简单实现：按优先级排序
        // 复杂的依赖解析可以在这里实现
        return providers.sortedWith(
            compareBy<CodeGenProvider<T>> { it.priority }
                .thenBy { it.providerId }
        )
    }
}
