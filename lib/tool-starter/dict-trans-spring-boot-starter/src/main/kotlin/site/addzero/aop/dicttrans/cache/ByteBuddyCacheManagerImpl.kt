package site.addzero.aop.dicttrans.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalCause
import net.bytebuddy.ByteBuddy
import org.slf4j.LoggerFactory
import site.addzero.aop.dicttrans.config.MemoryManagementProperties
import site.addzero.aop.dicttrans.dictaop.entity.NeedAddInfo
import site.addzero.aop.dicttrans.logging.MemoryManagementLogger
import site.addzero.aop.dicttrans.monitoring.CacheStatistics
import java.util.concurrent.ConcurrentHashMap

/**
 * ByteBuddy cache manager implementation using Caffeine cache
 *
 * @author zjarlin
 * @since 2025/01/12
 */
class ByteBuddyCacheManagerImpl(
    private val properties: MemoryManagementProperties
) : ByteBuddyCacheManager {
    
    private val logger = LoggerFactory.getLogger(ByteBuddyCacheManagerImpl::class.java)
    
    private val cache: Cache<String, Class<*>> = Caffeine.newBuilder()
        .maximumSize(properties.byteBuddyCache.maxSize)
        .expireAfterAccess(properties.byteBuddyCache.expireAfterAccess)
        .expireAfterWrite(properties.byteBuddyCache.expireAfterWrite)
        .recordStats()
        .removalListener { key: String?, value: Class<*>?, cause: RemovalCause ->
            logger.debug("Evicted ByteBuddy class: {} (cause: {})", key, cause)
            if (cause == RemovalCause.SIZE) {
                logger.info("ByteBuddy cache size limit reached, evicted class: {}", key)
            }
        }
        .build()
    
    // Keep track of failed generations to avoid repeated failures
    private val failedGenerations = ConcurrentHashMap<String, Long>()
    private val failureRetryDelay = 60000L // 1 minute
    
    override fun getOrCreateClass(baseClass: Class<*>, fields: List<NeedAddInfo>): Class<*> {
        val cacheKey = generateCacheKey(baseClass, fields)
        val startTime = System.currentTimeMillis()
        
        // Check if this generation failed recently
        val lastFailure = failedGenerations[cacheKey]
        if (lastFailure != null && System.currentTimeMillis() - lastFailure < failureRetryDelay) {
            MemoryManagementLogger.logCacheOperation(
                cacheName = "ByteBuddy",
                operation = "skip_recent_failure",
                key = cacheKey,
                additionalContext = mapOf("lastFailure" to lastFailure)
            )
            return baseClass
        }
        
        return try {
            val existingClass = cache.getIfPresent(cacheKey)
            if (existingClass != null) {
                val executionTime = System.currentTimeMillis() - startTime
                MemoryManagementLogger.logCacheOperation(
                    cacheName = "ByteBuddy",
                    operation = "get",
                    key = cacheKey,
                    hit = true,
                    executionTimeMs = executionTime
                )
                existingClass
            } else {
                val generatedClass = cache.get(cacheKey) { key ->
                    MemoryManagementLogger.logCacheOperation(
                        cacheName = "ByteBuddy",
                        operation = "generate",
                        key = key,
                        hit = false
                    )
                    generateDynamicClass(baseClass, fields)
                }
                
                val executionTime = System.currentTimeMillis() - startTime
                MemoryManagementLogger.logCacheOperation(
                    cacheName = "ByteBuddy",
                    operation = "get_or_create",
                    key = cacheKey,
                    hit = false,
                    executionTimeMs = executionTime,
                    additionalContext = mapOf("fieldsCount" to fields.size)
                )
                
                generatedClass ?: baseClass
            }
        } catch (e: Exception) {
            val executionTime = System.currentTimeMillis() - startTime
            MemoryManagementLogger.logCacheOperation(
                cacheName = "ByteBuddy",
                operation = "get_or_create_failed",
                key = cacheKey,
                hit = false,
                executionTimeMs = executionTime,
                additionalContext = mapOf("error" to (e.message ?: "Unknown error"))
            )
            
            logger.error("Failed to generate or retrieve ByteBuddy class for key: {}", cacheKey, e)
            failedGenerations[cacheKey] = System.currentTimeMillis()
            baseClass
        }
    }
    
    override fun evictAll() {
        val currentSize = cache.estimatedSize()
        MemoryManagementLogger.logCacheOperation(
            cacheName = "ByteBuddy",
            operation = "evict_all",
            additionalContext = mapOf("currentSize" to currentSize)
        )
        
        cache.invalidateAll()
        failedGenerations.clear()
        
        MemoryManagementLogger.logSystemEvent("cache_cleanup", mapOf(
            "cache" to "ByteBuddy",
            "evictedEntries" to currentSize,
            "action" to "evict_all"
        ))
    }
    
    override fun getStatistics(): CacheStatistics {
        val stats = cache.stats()
        val cacheStats = CacheStatistics(
            hitCount = stats.hitCount(),
            missCount = stats.missCount(),
            hitRate = stats.hitRate(),
            evictionCount = stats.evictionCount(),
            size = cache.estimatedSize(),
            maxSize = properties.byteBuddyCache.maxSize
        )
        
        // Log statistics periodically
        MemoryManagementLogger.logCacheStatistics("ByteBuddy", cacheStats)
        
        return cacheStats
    }
    
    override fun evictLeastRecentlyUsed(count: Int) {
        logger.debug("Evicting {} least recently used ByteBuddy classes", count)
        // Caffeine doesn't provide direct LRU eviction, but we can trigger cleanup
        cache.cleanUp()
        
        // If we need more aggressive eviction, we can reduce the cache size temporarily
        if (cache.estimatedSize() > properties.byteBuddyCache.maxSize - count) {
            logger.info("Triggering aggressive cache cleanup due to memory pressure")
            cache.policy().eviction().ifPresent { eviction ->
                // This will trigger eviction of excess entries
                eviction.setMaximum(cache.estimatedSize() - count)
                cache.cleanUp()
                // Restore original size
                eviction.setMaximum(properties.byteBuddyCache.maxSize)
            }
        }
    }
    
    override fun size(): Long = cache.estimatedSize()
    
    override fun contains(baseClass: Class<*>, fields: List<NeedAddInfo>): Boolean {
        val cacheKey = generateCacheKey(baseClass, fields)
        return cache.getIfPresent(cacheKey) != null
    }
    
    /**
     * Generate a unique cache key based on base class and fields
     */
    private fun generateCacheKey(baseClass: Class<*>, fields: List<NeedAddInfo>): String {
        val fieldsSignature = fields.sortedBy { it.fieldName }
            .joinToString("|") { "${it.fieldName}:${it.type.name}" }
        return "${baseClass.name}#$fieldsSignature"
    }
    
    /**
     * Generate a dynamic class using ByteBuddy
     */
    private fun generateDynamicClass(baseClass: Class<*>, fields: List<NeedAddInfo>): Class<*> {
        logger.debug("Generating ByteBuddy class for base: {} with {} fields", baseClass.name, fields.size)
        
        try {
            var subclass = ByteBuddy().subclass(baseClass)
            
            for (field in fields) {
                val fieldName = field.fieldName
                val type = field.type
                logger.trace("Adding field: {} of type: {}", fieldName, type.name)
                subclass = subclass.defineProperty(fieldName, type)
            }
            
            val generatedClass = subclass
                .make()
                .load(baseClass.classLoader)
                .loaded
            
            logger.debug("Successfully generated ByteBuddy class: {}", generatedClass.name)
            return generatedClass
            
        } catch (e: Exception) {
            logger.error("Failed to generate ByteBuddy class for base: {} with fields: {}", 
                baseClass.name, fields.map { it.fieldName }, e)
            
            val errorMessage = """
                ByteBuddy class generation failed for ${baseClass.name}
                Fields: ${fields.map { "${it.fieldName}:${it.type.name}" }}
                Error: ${e.message}
                
                Possible causes:
                1. Base class is not open (Kotlin classes need 'open' keyword)
                2. Base class lacks no-arg constructor
                3. Field type is not accessible
                4. ClassLoader issues
                
                Falling back to original class.
            """.trimIndent()
            
            logger.warn(errorMessage)
            throw RuntimeException("ByteBuddy generation failed: ${e.message}", e)
        }
    }
}