package com.addzero.codegen.core

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFile

/**
 * 元数据提取器接口
 *
 * 负责从KSP符号中提取元数据，与代码生成逻辑解耦
 *
 * @param T 元数据类型
 */
interface MetadataExtractor<T> {

    /**
     * 提取器标识，用于缓存键
     */
    val extractorId: String

    /**
     * 从解析器中提取元数据
     *
     * @param resolver KSP解析器
     * @return 提取的元数据列表
     */
    fun extract(resolver: Resolver): List<T>

    /**
     * 检查是否需要重新提取
     *
     * @param resolver 当前解析器
     * @return true表示需要重新提取，false表示可以使用缓存
     */
    fun shouldReextract(resolver: Resolver): Boolean = true

    /**
     * 获取依赖的文件列表
     * 用于KSP增量编译
     *
     * @param resolver 解析器
     * @param metadata 元数据列表
     * @return 依赖的源文件列表
     */
    fun getDependencies(resolver: Resolver, metadata: List<T>): List<KSFile> = emptyList()
}

/**
 * 抽象元数据提取器基类
 * 提供通用的实现逻辑
 */
abstract class AbstractMetadataExtractor<T> : MetadataExtractor<T> {

    override fun shouldReextract(resolver: Resolver): Boolean {
        // 默认总是重新提取，子类可以根据需要优化
        return true
    }

    override fun getDependencies(resolver: Resolver, metadata: List<T>): List<KSFile> {
        // 默认返回所有相关文件，子类可以精确指定
        return emptyList()
    }
}

/**
 * 复合元数据提取器
 * 可以组合多个提取器的结果
 */
class CompositeMetadataExtractor<T>(
    private val extractors: List<MetadataExtractor<T>>,
    override val extractorId: String
) : MetadataExtractor<T> {

    override fun extract(resolver: Resolver): List<T> {
        return extractors.flatMap { it.extract(resolver) }
    }

    override fun shouldReextract(resolver: Resolver): Boolean {
        return extractors.any { it.shouldReextract(resolver) }
    }

    override fun getDependencies(resolver: Resolver, metadata: List<T>): List<KSFile> {
        return extractors.flatMap { it.getDependencies(resolver, metadata) }.distinct()
    }
}
