package site.addzero.aop.dicttrans.util_internal

import cn.hutool.core.util.ReflectUtil
import cn.hutool.extra.spring.SpringUtil
import org.slf4j.LoggerFactory
import site.addzero.aop.dicttrans.cache.ReflectionCacheManager
import site.addzero.util.RefUtil
import java.lang.reflect.Field

/**
 * Enhanced RefUtil with reflection caching for improved performance
 *
 * @author zjarlin
 * @since 2025/01/12
 */
object EnhancedRefUtil {
    
    private val logger = LoggerFactory.getLogger(EnhancedRefUtil::class.java)
    
    // Lazy initialization to avoid circular dependencies
    private val reflectionCacheManager: ReflectionCacheManager by lazy {
        SpringUtil.getBean(ReflectionCacheManager::class.java)
    }
    
    /**
     * Get fields for a class with caching
     */
    fun getFields(clazz: Class<*>): Array<Field> {
        return try {
            reflectionCacheManager.getFields(clazz)
        } catch (e: Exception) {
            logger.error("Failed to get cached fields for class: {}, falling back to direct reflection", clazz.name, e)
            ReflectUtil.getFields(clazz) ?: emptyArray()
        }
    }
    
    /**
     * Get field value with caching optimizations
     */
    fun getFieldValue(obj: Any, field: Field): Any? {
        return try {
            reflectionCacheManager.getFieldValue(obj, field)
        } catch (e: Exception) {
            logger.debug("Failed to get cached field value, falling back to direct access", e)
            try {
                field.isAccessible = true
                field.get(obj)
            } catch (ex: Exception) {
                logger.debug("Direct field access also failed for field: {} in class: {}", 
                    field.name, obj.javaClass.name, ex)
                null
            }
        }
    }
    
    /**
     * Set field value with caching optimizations
     */
    fun setFieldValue(obj: Any, field: Field, value: Any?) {
        try {
            reflectionCacheManager.setFieldValue(obj, field, value)
        } catch (e: Exception) {
            logger.debug("Failed to set cached field value, falling back to direct access", e)
            try {
                field.isAccessible = true
                field.set(obj, value)
            } catch (ex: Exception) {
                logger.error("Failed to set field value for field: {} in class: {}", 
                    field.name, obj.javaClass.name, ex)
            }
        }
    }
    
    /**
     * Get field by name with caching
     */
    fun getField(clazz: Class<*>, fieldName: String): Field? {
        return try {
            reflectionCacheManager.getField(clazz, fieldName)
        } catch (e: Exception) {
            logger.debug("Failed to get cached field by name, falling back to direct lookup", e)
            try {
                clazz.getDeclaredField(fieldName).also { it.isAccessible = true }
            } catch (ex: Exception) {
                logger.debug("Field not found: {} in class: {}", fieldName, clazz.name)
                null
            }
        }
    }
    
    /**
     * Check if field is a collection field with caching
     */
    fun isCollectionField(field: Field): Boolean {
        return try {
            reflectionCacheManager.isCollectionField(field)
        } catch (e: Exception) {
            logger.debug("Failed to get cached collection field check, falling back to direct check", e)
            RefUtil.isCollectionField(field)
        }
    }
    
    /**
     * Check if field is an object field with caching
     */
    fun isObjectField(obj: Any, field: Field): Boolean {
        return try {
            reflectionCacheManager.isObjectField(obj, field)
        } catch (e: Exception) {
            logger.debug("Failed to get cached object field check, falling back to direct check", e)
            RefUtil.isObjectField(obj, field)
        }
    }
    
    /**
     * Check if field has non-null value with caching
     */
    fun isNonNullField(obj: Any, field: Field): Boolean {
        return try {
            reflectionCacheManager.isNonNullField(obj, field)
        } catch (e: Exception) {
            logger.debug("Failed to get cached non-null field check, falling back to direct check", e)
            RefUtil.isNonNullField(obj, field)
        }
    }
    
    /**
     * Delegate to original RefUtil for methods that don't need caching
     */
    fun isT(obj: Any, vararg blacklistClasses: Class<*>): Boolean {
        return RefUtil.isT(obj, *blacklistClasses)
    }
    
    fun isNew(obj: Any?): Boolean {
        return RefUtil.isNew(obj)
    }
    
    fun isCollection(obj: Any): Boolean {
        return RefUtil.isCollection(obj)
    }
    
    /**
     * Get cache statistics for monitoring
     */
    fun getCacheStatistics() = reflectionCacheManager.getStatistics()
    
    /**
     * Clear reflection cache
     */
    fun clearCache() {
        logger.info("Clearing reflection cache")
        reflectionCacheManager.evictAll()
    }
    
    /**
     * Invalidate cache for specific class
     */
    fun invalidateClass(clazz: Class<*>) {
        logger.debug("Invalidating reflection cache for class: {}", clazz.name)
        reflectionCacheManager.invalidateClass(clazz)
    }
}