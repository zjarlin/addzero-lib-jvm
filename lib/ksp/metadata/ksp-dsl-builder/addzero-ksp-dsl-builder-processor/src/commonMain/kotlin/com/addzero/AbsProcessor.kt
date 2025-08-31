package com.addzero

import cn.hutool.json.JSONUtil.toJsonStr
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*

/**
 * 代码生成模式
 */
enum class GenerationMode {
    // 批量收集所有元数据，生成单个文件
    BULK,
    // 每个元数据单独生成一个文件
    IMMEDIATE
}

/**
 * 抽象注解处理器基类
 * @param Meta 注解元数据类型
 * @param P 处理器自身类型，用于构建泛型类型系统
 */
abstract class AbsProcessor<Meta, P: AbsProcessor<Meta, P>>(
    environment: SymbolProcessorEnvironment
) : SymbolProcessor {
    protected val codeGenerator = environment.codeGenerator
    protected val logger = environment.logger

    // 存储收集到的元数据
    protected val metaList = mutableListOf<Meta>()
    
    // 默认包名，子类可以覆盖或使用动态包名方法
    open val PKG: String = "com.addzero.ksp.generated"

    // 默认包名，子类可以覆盖或使用动态包名方法
    open val debug: Boolean = false
    // 指定代码生成模式
    protected abstract val generationMode: GenerationMode
    
    /**
     * 动态获取生成代码的包名
     * 默认使用 PKG 属性，子类可覆盖此方法实现动态包名
     * @param meta 元数据对象
     * @param declaration 声明对象
     */
    protected open fun getPackageName(meta: Meta, declaration: KSDeclaration): String {
        return PKG
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // 获取所有带有指定注解的类声明
        val symbols = resolver.getSymbolsWithAnnotation(getAnnotationName())
        
        when (generationMode) {
            GenerationMode.BULK -> {
                // 处理类声明上的注解，收集元数据
                symbols.filterIsInstance<KSClassDeclaration>().forEach { processDeclaration(it) }
                // 处理函数声明上的注解，收集元数据
                symbols.filterIsInstance<KSFunctionDeclaration>().forEach { processDeclaration(it) }
                
                // 批量生成代码
                if (metaList.isNotEmpty()) {
                    generateBulkFile(resolver, metaList)
                }
            }
            
            GenerationMode.IMMEDIATE -> {
                // 立即处理每个声明并生成对应文件
                symbols.filterIsInstance<KSClassDeclaration>().forEach { declaration ->
                    val meta = extractMetaDataSafe(declaration)
                    meta?.let { metaData -> generateImmediateFile(resolver, metaData, declaration) }
                }
                
                symbols.filterIsInstance<KSFunctionDeclaration>().forEach { declaration ->
                    val meta = extractMetaDataSafe(declaration)
                    meta?.let { metaData -> generateImmediateFile(resolver, metaData, declaration) }
                }
            }
        }
        
        return emptyList()
    }
    
    private fun extractMetaDataSafe(declaration: KSDeclaration): Meta? {
        val annotation = declaration.annotations.find {
            it.shortName.asString() == getAnnotationName().substringAfterLast('.')
        }
        return annotation?.let { extractMetaData(declaration, it) }
    }

    private fun processDeclaration(declaration: KSDeclaration) {
        val meta = extractMetaDataSafe(declaration)
        meta?.let { metaList.add(it) }
    }
    
    // 批量生成单个文件
    private fun generateBulkFile(resolver: Resolver, metaList: List<Meta>) {
        try {
            val code = generateCode(resolver, metaList)

            val dependencies = Dependencies(
                aggregating = true,
                sources = resolver.getAllFiles().toList().toTypedArray()
            )

            if (code.isNotBlank()) {

                codeGenerator.createNewFile(
                    dependencies = dependencies,
                    packageName = PKG,
                    fileName = FILE_NAME(resolver, metaList)
                ).use { output ->
                    output.write(code.toByteArray())
                }
            }


            if (debug) {
                val toJsonStr = toJsonStr(metaList)

                codeGenerator.createNewFile(
                    dependencies = dependencies,
                    packageName = PKG,
                    fileName = FILE_NAME(resolver, metaList),
                    extensionName = "json"
                ).use { output ->
                    output.write(toJsonStr.toByteArray())
                }

            }

        } catch (e: Exception) {
            logger.warn(e.stackTraceToString())
        }
    }

    // 为单个元数据生成文件
    private fun generateImmediateFile(resolver: Resolver, meta: Meta, declaration: KSDeclaration) {
        try {
            val dependencies = Dependencies(
                aggregating = false,
                sources = listOf(declaration.containingFile!!).toTypedArray()
            )
            val code = generateImmediateCode(resolver, meta)
            if (code.isNotBlank()) {

                // 使用动态获取的包名
                val packageName = getPackageName(meta, declaration)

                codeGenerator.createNewFile(
                    dependencies = dependencies,
                    packageName = packageName,
                    fileName = getImmediateFileName(resolver, meta)
                ).use { output ->
                    output.write(code.toByteArray())
                }
            }


            if (debug) {
                val toJsonStr = toJsonStr(meta)
                codeGenerator.createNewFile(
                    dependencies = dependencies,
                    packageName = PKG,
                    fileName = getImmediateFileName(resolver, meta),
                    extensionName = "json"
                ).use { output ->
                    output.write(toJsonStr.toByteArray())
                }

            }

        } catch (e: Exception) {
            logger.warn(e.stackTraceToString())
        }
    }

    /**
     * 获取要处理的注解全限定名
     */
    protected abstract fun getAnnotationName(): String

    /**
     * 从注解中提取元数据
     */
    protected abstract fun extractMetaData(declaration: KSDeclaration, annotation: KSAnnotation): Meta

    /**
     * 根据收集的元数据生成代码（用于BULK模式）
     */
    protected open fun generateCode(resolver: Resolver, metaList: List<Meta>): String = ""

    /**
     * 为单个元数据生成代码（用于IMMEDIATE模式）
     */
    protected open fun generateImmediateCode(resolver: Resolver, meta: Meta): String = ""

    /**
     * 获取批量生成的文件名（用于BULK模式）
     */
    protected open fun FILE_NAME(resolver: Resolver, metaList: List<Meta>): String = "Generated"

    /**
     * 获取单个元数据生成的文件名（用于IMMEDIATE模式）
     */
    protected open fun getImmediateFileName(resolver: Resolver, meta: Meta): String = "Generated_${meta.hashCode()}"

    override fun onError() {
        logger.error("Error occurred during processing")
    }
}
