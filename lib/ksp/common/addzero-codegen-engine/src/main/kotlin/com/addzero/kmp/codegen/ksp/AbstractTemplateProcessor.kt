package com.addzero.kmp.codegen.ksp

import com.addzero.kmp.codegen.core.*
import com.addzero.kmp.codegen.engine.VelocityCodeGenerator
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated

/**
 * 新架构的抽象KSP处理器基类
 * 
 * 特性：
 * - 支持一个元数据生成多个文件
 * - 解耦元数据解析和代码生成逻辑
 * - 多个KSP处理器共享缓存
 * - 基于MetadataExtractor和FileGenerator的设计
 * 
 * @param T 元数据类型
 */
abstract class AbstractTemplateProcessor<T>(
    protected val codeGenerator: CodeGenerator,
    protected val logger: KSPLogger,
    protected val options: Map<String, String>
) : SymbolProcessor {
    
    protected val config: CodeGenConfig = createConfig(options)
    protected val velocityGenerator: VelocityCodeGenerator = VelocityCodeGenerator(config, logger, codeGenerator)
    protected val metadataCache: MetadataCache = GlobalMetadataCache.getInstance()
    
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("开始处理 ${getProcessorName()}")
        
        try {
            // 1. 通过缓存提取元数据（解耦逻辑）
            val metadataList = extractMetadataWithCache(resolver)
            
            if (metadataList.isEmpty()) {
                logger.info("没有找到需要处理的元数据")
                return emptyList()
            }
            
            // 2. 生成文件（支持一对多）
            generateFiles(resolver, metadataList)
            
            logger.info("${getProcessorName()} 处理完成，共处理 ${metadataList.size} 项元数据")
            
        } catch (e: Exception) {
            logger.error("${getProcessorName()} 处理失败: ${e.message}")
            if (config.debugMode) {
                e.printStackTrace()
            }
        }
        
        return emptyList()
    }
    
    /**
     * 通过缓存提取元数据
     * 
     * 解耦了元数据解析逻辑，支持多处理器共享缓存
     */
    private fun extractMetadataWithCache(resolver: Resolver): List<T> {
        val extractors = createMetadataExtractors()
        val allMetadata = mutableListOf<T>()
        
        extractors.forEach { extractor ->
            val cached = metadataCache.getOrExtract(extractor, resolver)
            allMetadata.addAll(cached)
            
            logger.info("提取器 ${extractor.extractorId} 提取到 ${cached.size} 项元数据")
        }
        
        return allMetadata
    }
    
    /**
     * 生成文件（支持一对多）
     */
    private fun generateFiles(resolver: Resolver, metadataList: List<T>) {
        val fileGenerators = createFileGenerators()
        
        fileGenerators.forEach { generator ->
            logger.info("开始使用生成器 ${generator.generatorId} 生成文件")
            
            val requests = generator.generateFiles(metadataList)
            val dependencies = collectDependencies(resolver, metadataList)
            
            requests.forEach { request ->
                try {
                    val templateContext = createTemporaryTemplateContext(request)
                    velocityGenerator.generateCode(
                        metadataList = listOf(request),
                        templateContext = templateContext,
                        dependencies = dependencies
                    )
                } catch (e: Exception) {
                    logger.error("文件生成失败: ${request.fileName}, 错误: ${e.message}")
                    if (config.debugMode) {
                        e.printStackTrace()
                    }
                }
            }
            
            logger.info("生成器 ${generator.generatorId} 完成，生成了 ${requests.size} 个文件")
        }
    }
    
    /**
     * 创建配置
     * 
     * 子类可以重写此方法来自定义配置
     */
    protected open fun createConfig(options: Map<String, String>): CodeGenConfig {
        return CodeGenConfig.fromKspOptions(options)
    }
    
    /**
     * 获取处理器名称
     * 
     * 用于日志输出
     */
    protected abstract fun getProcessorName(): String
    
    /**
     * 创建元数据提取器列表
     * 
     * 子类实现此方法来定义需要的元数据提取器
     * 支持多个提取器协同工作，共享缓存
     * 
     * @return 元数据提取器列表
     */
    protected abstract fun createMetadataExtractors(): List<MetadataExtractor<T>>
    
    /**
     * 创建文件生成器列表
     * 
     * 子类实现此方法来定义文件生成策略
     * 支持一个元数据生成多个文件
     * 
     * @return 文件生成器列表
     */
    protected abstract fun createFileGenerators(): List<FileGenerator<T>>
    
    /**
     * 收集依赖文件
     * 
     * 子类可以重写此方法来自定义依赖文件收集逻辑
     * 
     * @param resolver KSP解析器
     * @param metadataList 元数据列表
     * @return KSP依赖对象
     */
    protected open fun collectDependencies(resolver: Resolver, metadataList: List<T>): Dependencies {
        return Dependencies.ALL_FILES
    }
    
    /**
     * 为文件生成请求创建临时的模板上下文
     * 
     * 这是一个内部工具方法，用于桥接FileGenerationRequest和VelocityCodeGenerator
     */
    private fun createTemporaryTemplateContext(request: FileGenerationRequest): TemplateContext<FileGenerationRequest> {
        return object : TemplateContext<FileGenerationRequest> {
            override fun buildContext(metadata: FileGenerationRequest): Map<String, Any> {
                return metadata.context
            }
            
            override fun getOutputFileName(metadata: FileGenerationRequest): String {
                return metadata.fileName
            }
            
            override fun getOutputPackage(metadata: FileGenerationRequest): String {
                return metadata.packageName
            }
            
            override fun getTemplateName(metadata: FileGenerationRequest): String {
                return metadata.templateName
            }
            
            override fun shouldOverwriteExisting(metadata: FileGenerationRequest): Boolean {
                return metadata.shouldOverwrite
            }
            
            override fun beforeGenerate(metadata: FileGenerationRequest) {
                metadata.beforeGenerate()
            }
            
            override fun afterGenerate(metadata: FileGenerationRequest, generatedCode: String) {
                metadata.afterGenerate(generatedCode)
            }
        }
    }
}
/**
 * 抽象的KSP处理器提供者基类
 * 
 * 简化处理器提供者的创建
 */
abstract class AbstractTemplateProcessorProvider<T> : SymbolProcessorProvider {
    
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return createProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            options = environment.options
        )
    }
    
    /**
     * 创建处理器实例
     * 
     * 子类实现此方法来创建具体的处理器
     */
    protected abstract fun createProcessor(
        codeGenerator: CodeGenerator,
        logger: KSPLogger,
        options: Map<String, String>
    ): AbstractTemplateProcessor<T>
}