package com.addzero.codegen.core

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFile
import java.util.concurrent.ConcurrentHashMap

/**
 * 元数据缓存接口
 */
interface MetadataCache {

    /**
     * 获取或提取元数据
     *
     * @param extractor 元数据提取器
     * @param resolver KSP解析器
     * @return 缓存或新提取的元数据
     */
    fun <T> getOrExtract(extractor: MetadataExtractor<T>, resolver: Resolver): List<T>

    /**
     * 清空所有缓存
     */
    fun clear()

    /**
     * 清空特定提取器的缓存
     */
    fun clear(extractorId: String)

    /**
     * 检查是否有缓存
     */
    fun hasCache(extractorId: String): Boolean
}

/**
 * 默认的内存缓存实现
 *
 * 在整个编译过程中共享元数据
 */
class InMemoryMetadataCache : MetadataCache {

    // 使用线程安全的Map存储缓存
    private val cache = ConcurrentHashMap<String, CacheEntry<*>>()

    @Suppress("UNCHECKED_CAST")
    override fun <T> getOrExtract(extractor: MetadataExtractor<T>, resolver: Resolver): List<T> {
        val extractorId = extractor.extractorId

        // 检查是否有缓存且不需要重新提取
        val existing = cache[extractorId] as? CacheEntry<T>
        if (existing != null && !extractor.shouldReextract(resolver)) {
            return existing.data
        }

        // 提取新数据
        val newData = extractor.extract(resolver)

        // 更新缓存
        cache[extractorId] = CacheEntry(
            data = newData,
            timestamp = System.currentTimeMillis(),
            dependencies = extractor.getDependencies(resolver, newData)
        )

        return newData
    }

    override fun clear() {
        cache.clear()
    }

    override fun clear(extractorId: String) {
        cache.remove(extractorId)
    }

    override fun hasCache(extractorId: String): Boolean {
        return cache.containsKey(extractorId)
    }

    /**
     * 获取缓存统计信息
     */
    fun getCacheStats(): Map<String, CacheStats> {
        return cache.mapValues { (_, entry) ->
            CacheStats(
                itemCount = entry.data.size,
                timestamp = entry.timestamp,
                dependencyCount = entry.dependencies.size
            )
        }
    }

    /**
     * 缓存条目
     */
    private data class CacheEntry<T>(
        val data: List<T>,
        val timestamp: Long,
        val dependencies: List<KSFile>
    )

    /**
     * 缓存统计信息
     */
    data class CacheStats(
        val itemCount: Int,
        val timestamp: Long,
        val dependencyCount: Int
    )
}

/**
 * 全局缓存管理器
 *
 * 提供全局单例缓存，供所有KSP处理器共享
 */
object GlobalMetadataCache {

    // 全局缓存实例
    private val globalCache: MetadataCache = InMemoryMetadataCache()

    /**
     * 获取全局缓存实例
     */
    fun getInstance(): MetadataCache = globalCache

    /**
     * 重置全局缓存（通常在编译开始时调用）
     */
    fun reset() {
        globalCache.clear()
    }

    /**
     * 获取缓存状态（用于调试）
     */
    fun getStats(): Map<String, InMemoryMetadataCache.CacheStats> {
        return if (globalCache is InMemoryMetadataCache) {
            globalCache.getCacheStats()
        } else {
            emptyMap()
        }
    }
}

/**
 * 缓存感知的元数据提取器
 *
 * 自动集成缓存机制的提取器包装类
 */
class CachedMetadataExtractor<T>(
    private val delegate: MetadataExtractor<T>,
    private val cache: MetadataCache = GlobalMetadataCache.getInstance()
) : MetadataExtractor<T> by delegate {

    /**
     * 自动使用缓存的提取方法
     */
    fun extractWithCache(resolver: Resolver): List<T> {
        return cache.getOrExtract(delegate, resolver)
    }
}
