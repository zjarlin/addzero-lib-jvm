package site.addzero.aop.dicttrans.cache

import site.addzero.aop.dicttrans.monitoring.CacheStatistics
import java.lang.reflect.Field

/**
 * Reflection cache manager interface for caching field metadata and operations
 *
 * @author zjarlin
 * @since 2025/01/12
 */
interface ReflectionCacheManager {
    
    /**
     * Get cached fields for a class
     * 
     * @param clazz The class to get fields for
     * @return Array of fields for the class
     */
    fun getFields(clazz: Class<*>): Array<Field>
    
    /**
     * Get field value with caching of field access
     * 
     * @param obj The object to get field value from
     * @param field The field to get value from
     * @return The field value
     */
    fun getFieldValue(obj: Any, field: Field): Any?
    
    /**
     * Set field value with caching of field access
     * 
     * @param obj The object to set field value on
     * @param field The field to set value on
     * @param value The value to set
     */
    fun setFieldValue(obj: Any, field: Field, value: Any?)
    
    /**
     * Get field by name with caching
     * 
     * @param clazz The class to get field from
     * @param fieldName The name of the field
     * @return The field if found, null otherwise
     */
    fun getField(clazz: Class<*>, fieldName: String): Field?
    
    /**
     * Check if a field is a collection field (cached result)
     * 
     * @param field The field to check
     * @return true if field is a collection field
     */
    fun isCollectionField(field: Field): Boolean
    
    /**
     * Check if a field is an object field (cached result)
     * 
     * @param obj The object containing the field
     * @param field The field to check
     * @return true if field is an object field
     */
    fun isObjectField(obj: Any, field: Field): Boolean
    
    /**
     * Check if a field has non-null value (cached result)
     * 
     * @param obj The object containing the field
     * @param field The field to check
     * @return true if field has non-null value
     */
    fun isNonNullField(obj: Any, field: Field): Boolean
    
    /**
     * Invalidate cache entries for a specific class
     * 
     * @param clazz The class to invalidate cache for
     */
    fun invalidateClass(clazz: Class<*>)
    
    /**
     * Get cache statistics
     * 
     * @return Current cache statistics
     */
    fun getStatistics(): CacheStatistics
    
    /**
     * Clear all cache entries
     */
    fun evictAll()
    
    /**
     * Get current cache size
     * 
     * @return Number of cached entries
     */
    fun size(): Long
}