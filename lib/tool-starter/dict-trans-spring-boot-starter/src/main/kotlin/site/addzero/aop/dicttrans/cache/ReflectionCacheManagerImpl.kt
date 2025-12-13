package site.addzero.aop.dicttrans.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalCause
import cn.hutool.core.util.ReflectUtil
import org.slf4j.LoggerFactory
import site.addzero.aop.dicttrans.config.MemoryManagementProperties
import site.addzero.aop.dicttrans.logging.MemoryManagementLogger
import site.addzero.aop.dicttrans.monitoring.CacheStatistics
import site.addzero.util.RefUtil
import java.lang.reflect.Field
import java.util.concurrent.ConcurrentHashMap

/**
 * Reflection cache manager implementation using Caffeine cache
 *
 * @author zjarlin
 * @since 2025/01/12
 */
class ReflectionCacheManagerImpl(
    private val properties: MemoryManagementProperties
) : ReflectionCacheManager {
    
    private val logger = LoggerFactory.getLogger(ReflectionCacheManagerImpl::class.java)
    
    // Cache for class fields
    private val fieldCache: Cache<Class<*>, Array<Field>> = Caffeine.newBuilder()
        .maximumSize(properties.reflectionCache.maxSize)
        .expireAfterAccess(properties.reflectionCache.expireAfterAccess)
        .expireAfterWrite(properties.reflectionCache.expireAfterWrite)
        .recordStats()
        .removalListener { key: Class<*>?, value: Array<Field>?, cause: RemovalCause ->
            logger.debug("Evicted field cache for class: {} (cause: {})", key?.name, cause)
        }
        .build()
    
    // Cache for field by name lookups
    private val fieldByNameCache: Cache<String, Field?> = Caffeine.newBuilder()
        .maximumSize(properties.reflectionCache.maxSize * 2) // More field name lookups
        .expireAfterAccess(properties.reflectionCache.expireAfterAccess)
        .recordStats()
        .build()
    
    // Cache for field type checks
    private val fieldTypeCache: Cache<Field, FieldTypeInfo> = Caffeine.newBuilder()
        .maximumSize(properties.reflectionCache.maxSize * 3) // Many field type checks
        .expireAfterAccess(properties.reflectionCache.expireAfterAccess)
        .recordStats()
        .build()
    
    // Cache for field access patterns (performance optimization)
    private val fieldAccessCache = ConcurrentHashMap<String, Boolean>()
    
    override fun getFields(clazz: Class<*>): Array<Field> {
        val startTime = System.currentTimeMillis()
        val cacheKey = clazz.name
        
        val existingFields = fieldCache.getIfPresent(clazz)
        if (existingFields != null) {
            val executionTime = System.currentTimeMillis() - startTime
            MemoryManagementLogger.logCacheOperation(
                cacheName = "Reflection-Fields",
                operation = "get",
                key = cacheKey,
                hit = true,
                executionTimeMs = executionTime
            )
            return existingFields
        }
        
        return fieldCache.get(clazz) { cls ->
            MemoryManagementLogger.logCacheOperation(
                cacheName = "Reflection-Fields",
                operation = "load",
                key = cls.name,
                hit = false
            )
            
            try {
                val fields = ReflectUtil.getFields(cls)
                val executionTime = System.currentTimeMillis() - startTime
                MemoryManagementLogger.logCacheOperation(
                    cacheName = "Reflection-Fields",
                    operation = "get_or_load",
                    key = cacheKey,
                    hit = false,
                    executionTimeMs = executionTime,
                    additionalContext = mapOf("fieldsCount" to fields.size)
                )
                fields
            } catch (e: Exception) {
                val executionTime = System.currentTimeMillis() - startTime
                MemoryManagementLogger.logCacheOperation(
                    cacheName = "Reflection-Fields",
                    operation = "load_failed",
                    key = cacheKey,
                    hit = false,
                    executionTimeMs = executionTime,
                    additionalContext = mapOf("error" to (e.message ?: "Unknown error"))
                )
                logger.error("Failed to get fields for class: {}", cls.name, e)
                emptyArray()
            }
        } ?: emptyArray()
    }
    
    override fun getFieldValue(obj: Any, field: Field): Any? {
        return try {
            // Ensure field is accessible
            if (!field.isAccessible) {
                field.isAccessible = true
            }
            ReflectUtil.getFieldValue(obj, field)
        } catch (e: Exception) {
            logger.debug("Failed to get field value for field: {} in class: {}", 
                field.name, obj.javaClass.name, e)
            null
        }
    }
    
    override fun setFieldValue(obj: Any, field: Field, value: Any?) {
        try {
            // Ensure field is accessible
            if (!field.isAccessible) {
                field.isAccessible = true
            }
            ReflectUtil.setFieldValue(obj, field, value)
        } catch (e: Exception) {
            logger.error("Failed to set field value for field: {} in class: {}", 
                field.name, obj.javaClass.name, e)
        }
    }
    
    override fun getField(clazz: Class<*>, fieldName: String): Field? {
        val cacheKey = "${clazz.name}#$fieldName"
        return fieldByNameCache.get(cacheKey) { key ->
            logger.debug("Cache miss for field lookup: {}", key)
            try {
                val fields = getFields(clazz)
                fields.find { it.name == fieldName }
            } catch (e: Exception) {
                logger.error("Failed to find field: {} in class: {}", fieldName, clazz.name, e)
                null
            }
        }
    }
    
    override fun isCollectionField(field: Field): Boolean {
        val typeInfo = getFieldTypeInfo(field)
        return typeInfo.isCollection
    }
    
    override fun isObjectField(obj: Any, field: Field): Boolean {
        val fieldValue = getFieldValue(obj, field) ?: return false
        return RefUtil.isT(fieldValue)
    }
    
    override fun isNonNullField(obj: Any, field: Field): Boolean {
        return getFieldValue(obj, field) != null
    }
    
    override fun invalidateClass(clazz: Class<*>) {
        logger.debug("Invalidating cache for class: {}", clazz.name)
        fieldCache.invalidate(clazz)
        
        // Invalidate related field by name cache entries
        fieldByNameCache.asMap().keys.removeIf { key ->
            key.startsWith("${clazz.name}#")
        }
        
        // Invalidate field type cache entries for this class
        fieldTypeCache.asMap().keys.removeIf { field ->
            field.declaringClass == clazz
        }
        
        // Clear field access cache for this class
        fieldAccessCache.keys.removeIf { key ->
            key.startsWith("${clazz.name}#")
        }
    }
    
    override fun getStatistics(): CacheStatistics {
        val fieldStats = fieldCache.stats()
        val fieldByNameStats = fieldByNameCache.stats()
        val fieldTypeStats = fieldTypeCache.stats()
        
        // Combine statistics from all caches
        val totalHits = fieldStats.hitCount() + fieldByNameStats.hitCount() + fieldTypeStats.hitCount()
        val totalMisses = fieldStats.missCount() + fieldByNameStats.missCount() + fieldTypeStats.missCount()
        val totalRequests = totalHits + totalMisses
        val hitRate = if (totalRequests > 0) totalHits.toDouble() / totalRequests else 0.0
        
        return CacheStatistics(
            hitCount = totalHits,
            missCount = totalMisses,
            hitRate = hitRate,
            evictionCount = fieldStats.evictionCount() + fieldByNameStats.evictionCount() + fieldTypeStats.evictionCount(),
            size = fieldCache.estimatedSize() + fieldByNameCache.estimatedSize() + fieldTypeCache.estimatedSize(),
            maxSize = properties.reflectionCache.maxSize * 6 // Total capacity across all caches
        )
    }
    
    override fun evictAll() {
        logger.info("Evicting all reflection cache entries")
        fieldCache.invalidateAll()
        fieldByNameCache.invalidateAll()
        fieldTypeCache.invalidateAll()
        fieldAccessCache.clear()
        logger.info("Reflection cache cleared successfully")
    }
    
    override fun size(): Long {
        return fieldCache.estimatedSize() + fieldByNameCache.estimatedSize() + fieldTypeCache.estimatedSize()
    }
    
    /**
     * Get cached field type information
     */
    private fun getFieldTypeInfo(field: Field): FieldTypeInfo {
        return fieldTypeCache.get(field) { f ->
            logger.trace("Analyzing field type for: {}.{}", f.declaringClass.name, f.name)
            FieldTypeInfo(
                isCollection = RefUtil.isCollectionField(f),
                isPrimitive = f.type.isPrimitive,
                isWrapper = isPrimitiveWrapper(f.type),
                typeName = f.type.name
            )
        } ?: FieldTypeInfo(false, false, false, "unknown")
    }
    
    /**
     * Check if type is a primitive wrapper
     */
    private fun isPrimitiveWrapper(type: Class<*>): Boolean {
        return type in setOf(
            Boolean::class.java, Byte::class.java, Character::class.java,
            Short::class.java, Integer::class.java, Long::class.java,
            Float::class.java, Double::class.java
        )
    }
    
    /**
     * Field type information for caching
     */
    private data class FieldTypeInfo(
        val isCollection: Boolean,
        val isPrimitive: Boolean,
        val isWrapper: Boolean,
        val typeName: String
    )
}