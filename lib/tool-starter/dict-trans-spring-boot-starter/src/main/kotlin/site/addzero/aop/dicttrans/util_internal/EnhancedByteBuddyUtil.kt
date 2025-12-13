package site.addzero.aop.dicttrans.util_internal

import cn.hutool.core.bean.BeanUtil
import cn.hutool.core.collection.CollUtil
import cn.hutool.extra.spring.SpringUtil
import org.slf4j.LoggerFactory
import site.addzero.aop.dicttrans.cache.ByteBuddyCacheManager
import site.addzero.aop.dicttrans.config.MemoryManagementProperties
import site.addzero.aop.dicttrans.dictaop.entity.NeedAddInfo
import site.addzero.aop.dicttrans.processing.ProcessingContext
import site.addzero.util.RefUtil
import java.lang.reflect.Field
import java.util.*
import java.util.function.Function

/**
 * Enhanced ByteBuddy utility with caching and memory management
 *
 * @author zjarlin
 * @since 2025/01/12
 */
object EnhancedByteBuddyUtil {
    
    private val logger = LoggerFactory.getLogger(EnhancedByteBuddyUtil::class.java)
    
    // Lazy initialization to avoid circular dependencies
    private val cacheManager: ByteBuddyCacheManager by lazy {
        SpringUtil.getBean(ByteBuddyCacheManager::class.java)
    }
    
    private val properties: MemoryManagementProperties by lazy {
        SpringUtil.getBean(MemoryManagementProperties::class.java)
    }
    
    /**
     * Enhanced version of genChildObjectRecursion with caching and limits
     */
    fun genChildObjectRecursion(
        o: Any?, 
        getNeedAddInfoFun: Function<Any, MutableList<NeedAddInfo>>,
        context: ProcessingContext = ProcessingContext.create()
    ): Any? {
        o ?: return o
        
        // Check processing limits
        if (context.isTimeoutExceeded()) {
            logger.warn("Processing timeout exceeded for object: {}", o.javaClass.name)
            return o
        }
        
        if (context.isRecursionLimitExceeded()) {
            logger.warn("Recursion depth limit exceeded: {} for object: {}", 
                context.currentDepth, o.javaClass.name)
            return o
        }
        
        // Check if object is a business type
        val isT = RefUtil.isT(o)
        if (!isT) {
            return o
        }
        
        val needAddFields = try {
            getNeedAddInfoFun.apply(o)
        } catch (e: Exception) {
            logger.error("Failed to get needed fields for object: {}", o.javaClass.name, e)
            return o
        }
        
        // Early exit if no fields needed and can skip transformation
        if (CollUtil.isEmpty(needAddFields) && !canNotSkipTrans(o)) {
            return o
        }
        
        val aClass = o.javaClass
        
        // Use cached class generation
        val enhancedClass = try {
            cacheManager.getOrCreateClass(aClass, needAddFields)
        } catch (e: Exception) {
            logger.error("Failed to get or create enhanced class for: {}", aClass.name, e)
            return o
        }
        
        // If cache returned the same class, no enhancement was possible
        if (enhancedClass == aClass) {
            logger.debug("No enhancement possible for class: {}, returning original object", aClass.name)
            return o
        }
        
        // Create instance of enhanced class
        val enhancedInstance = try {
            enhancedClass.getDeclaredConstructor().newInstance()
        } catch (e: Exception) {
            logger.error("Failed to create instance of enhanced class: {}", enhancedClass.name, e)
            return o
        }
        
        // Process fields recursively with updated context
        val nextContext = context.incrementDepth()
        val fields = EnhancedRefUtil.getFields(aClass)
        
        Arrays.stream<Field>(fields).forEach { field: Field ->
            try {
                val fieldValue = EnhancedRefUtil.getFieldValue(o, field)
                
                when {
                    EnhancedRefUtil.isObjectField(o, field) -> {
                        val processedValue = genChildObjectRecursion(fieldValue, getNeedAddInfoFun, nextContext)
                        EnhancedRefUtil.setFieldValue(o, field, processedValue)
                    }
                    EnhancedRefUtil.isCollectionField(field) && fieldValue != null -> {
                        val collection = fieldValue as? MutableCollection<*>
                        if (CollUtil.isNotEmpty(collection)) {
                            // Check collection size limit
                            if (nextContext.isCollectionTooLarge(collection!!.size)) {
                                logger.warn("Collection too large ({} items), skipping processing for field: {} in class: {}", 
                                    collection.size, field.name, aClass.name)
                                return@forEach
                            }
                            
                            val processedCollection = collection.map { item ->
                                genChildObjectRecursion(item, getNeedAddInfoFun, nextContext)
                            }
                            EnhancedRefUtil.setFieldValue(o, field, processedCollection)
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error("Failed to process field: {} in class: {}", field.name, aClass.name, e)
                // Continue processing other fields
            }
        }
        
        // Copy properties from original to enhanced instance
        try {
            BeanUtil.copyProperties(o, enhancedInstance)
            logger.debug("Successfully created enhanced instance for class: {}", aClass.name)
            return enhancedInstance
        } catch (e: Exception) {
            logger.error("Failed to copy properties to enhanced instance for class: {}", aClass.name, e)
            return o
        }
    }
    
    /**
     * Check if transformation can be skipped (using cached reflection)
     */
    private fun canNotSkipTrans(o: Any): Boolean {
        val fields = EnhancedRefUtil.getFields(o.javaClass)
        return Arrays.stream<Field>(fields).anyMatch { field: Field ->
            val objectField = EnhancedRefUtil.isObjectField(o, field)
            val collectionField = EnhancedRefUtil.isCollectionField(field)
            objectField || collectionField
        }
    }
    
    /**
     * Enhanced version with class-based processing
     */
    fun genChildObjectRecursion(
        claz: Class<*>,
        getNeedAddInfoFun: (Any) -> MutableList<NeedAddInfo>
    ): Class<Any>? {
        return try {
            val instance = claz.getDeclaredConstructor().newInstance()
            val processedInstance = genChildObjectRecursion(instance, Function { obj ->
                getNeedAddInfoFun(obj).toMutableList()
            })
            processedInstance?.javaClass as? Class<Any>
        } catch (e: Exception) {
            logger.error("Failed to process class: {}", claz.name, e)
            null
        }
    }
    
    /**
     * Get cache statistics for monitoring
     */
    fun getCacheStatistics() = cacheManager.getStatistics()
    
    /**
     * Clear cache for memory management
     */
    fun clearCache() {
        cacheManager.evictAll()
    }
    
    /**
     * Trigger cache cleanup
     */
    fun cleanupCache(count: Int = 10) {
        logger.debug("Triggering ByteBuddy cache cleanup, evicting {} entries", count)
        cacheManager.evictLeastRecentlyUsed(count)
    }
}