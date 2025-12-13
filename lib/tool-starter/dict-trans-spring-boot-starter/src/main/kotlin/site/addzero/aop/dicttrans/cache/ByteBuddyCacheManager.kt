package site.addzero.aop.dicttrans.cache

import site.addzero.aop.dicttrans.dictaop.entity.NeedAddInfo
import site.addzero.aop.dicttrans.monitoring.CacheStatistics

/**
 * ByteBuddy class cache manager interface
 *
 * @author zjarlin
 * @since 2025/01/12
 */
interface ByteBuddyCacheManager {
    
    /**
     * Get or create a dynamic class with the specified fields
     * 
     * @param baseClass The base class to extend
     * @param fields The fields to add to the dynamic class
     * @return The cached or newly created dynamic class
     */
    fun getOrCreateClass(baseClass: Class<*>, fields: List<NeedAddInfo>): Class<*>
    
    /**
     * Evict all cached classes
     */
    fun evictAll()
    
    /**
     * Get cache statistics
     * 
     * @return Current cache statistics
     */
    fun getStatistics(): CacheStatistics
    
    /**
     * Evict least recently used entries
     * 
     * @param count Number of entries to evict
     */
    fun evictLeastRecentlyUsed(count: Int)
    
    /**
     * Get current cache size
     * 
     * @return Number of cached classes
     */
    fun size(): Long
    
    /**
     * Check if cache contains a class for the given key
     * 
     * @param baseClass The base class
     * @param fields The fields
     * @return true if cached, false otherwise
     */
    fun contains(baseClass: Class<*>, fields: List<NeedAddInfo>): Boolean
}