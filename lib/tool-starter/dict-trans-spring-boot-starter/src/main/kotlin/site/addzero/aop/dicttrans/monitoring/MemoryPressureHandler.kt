package site.addzero.aop.dicttrans.monitoring

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import site.addzero.aop.dicttrans.cache.ByteBuddyCacheManager
import site.addzero.aop.dicttrans.cache.ReflectionCacheManager
import site.addzero.aop.dicttrans.tracking.WeakReferenceTracker
import site.addzero.aop.dicttrans.tracking.WeakReferenceTrackerImpl
import javax.annotation.PostConstruct

/**
 * Memory pressure handler that responds to memory pressure events
 *
 * @author zjarlin
 * @since 2025/01/12
 */
@Component
class MemoryPressureHandler @Autowired constructor(
    private val memoryMonitor: MemoryMonitor,
    private val byteBuddyCacheManager: ByteBuddyCacheManager,
    private val reflectionCacheManager: ReflectionCacheManager,
    private val weakReferenceTracker: WeakReferenceTracker
) : MemoryPressureListener {
    
    private val logger = LoggerFactory.getLogger(MemoryPressureHandler::class.java)
    
    @PostConstruct
    fun initialize() {
        memoryMonitor.addMemoryPressureListener(this)
        logger.info("Memory pressure handler initialized and registered")
    }
    
    override fun onMemoryPressure(memoryUsage: MemoryUsage, pressureLevel: Double) {
        logger.warn("Memory pressure detected - Level: {:.1f}%, Heap: {:.1f}%, Metaspace: {:.1f}%", 
            pressureLevel * 100, memoryUsage.heapUsagePercent * 100, memoryUsage.metaspaceUsagePercent * 100)
        
        try {
            when {
                pressureLevel >= 0.95 -> handleCriticalPressure(memoryUsage)
                pressureLevel >= 0.85 -> handleHighPressure(memoryUsage)
                pressureLevel >= 0.70 -> handleModeratePressure(memoryUsage)
            }
        } catch (e: Exception) {
            logger.error("Error handling memory pressure", e)
        }
    }
    
    override fun onMemoryPressureRelieved(memoryUsage: MemoryUsage) {
        logger.info("Memory pressure relieved - Heap: {:.1f}%, Metaspace: {:.1f}%", 
            memoryUsage.heapUsagePercent * 100, memoryUsage.metaspaceUsagePercent * 100)
        
        // Log cache statistics after pressure relief
        logCacheStatistics()
    }
    
    /**
     * Handle critical memory pressure (>95%)
     */
    private fun handleCriticalPressure(memoryUsage: MemoryUsage) {
        logger.error("CRITICAL memory pressure detected! Taking aggressive cleanup actions.")
        
        // Aggressive cache cleanup
        byteBuddyCacheManager.evictAll()
        reflectionCacheManager.evictAll()
        
        // Trigger memory pressure cleanup for weak references
        if (weakReferenceTracker is WeakReferenceTrackerImpl) {
            weakReferenceTracker.triggerMemoryPressureCleanup()
        }
        
        // Force garbage collection
        System.gc()
        Thread.yield() // Give GC a chance to run
        System.gc() // Second GC call
        
        logger.error("Critical memory pressure cleanup completed")
    }
    
    /**
     * Handle high memory pressure (85-95%)
     */
    private fun handleHighPressure(memoryUsage: MemoryUsage) {
        logger.warn("HIGH memory pressure detected! Taking cleanup actions.")
        
        // Moderate cache cleanup
        val byteBuddyStats = byteBuddyCacheManager.getStatistics()
        val reflectionStats = reflectionCacheManager.getStatistics()
        
        // Evict 50% of cache entries
        val byteBuddyEvictCount = (byteBuddyStats.size * 0.5).toInt()
        val reflectionEvictCount = (reflectionStats.size * 0.5).toInt()
        
        if (byteBuddyEvictCount > 0) {
            byteBuddyCacheManager.evictLeastRecentlyUsed(byteBuddyEvictCount)
        }
        
        // For reflection cache, clear all as it's cheaper to rebuild
        reflectionCacheManager.evictAll()
        
        // Clean up weak references
        weakReferenceTracker.cleanup()
        
        // Suggest garbage collection
        System.gc()
        
        logger.warn("High memory pressure cleanup completed")
    }
    
    /**
     * Handle moderate memory pressure (70-85%)
     */
    private fun handleModeratePressure(memoryUsage: MemoryUsage) {
        logger.info("MODERATE memory pressure detected. Taking preventive actions.")
        
        // Light cache cleanup
        val byteBuddyStats = byteBuddyCacheManager.getStatistics()
        
        // Evict 25% of ByteBuddy cache entries
        val evictCount = (byteBuddyStats.size * 0.25).toInt()
        if (evictCount > 0) {
            byteBuddyCacheManager.evictLeastRecentlyUsed(evictCount)
        }
        
        // Clean up weak references
        weakReferenceTracker.cleanup()
        
        logger.info("Moderate memory pressure cleanup completed")
    }
    
    /**
     * Log current cache statistics
     */
    private fun logCacheStatistics() {
        try {
            val byteBuddyStats = byteBuddyCacheManager.getStatistics()
            val reflectionStats = reflectionCacheManager.getStatistics()
            val weakRefStats = weakReferenceTracker.getStatistics()
            
            logger.info("Cache Statistics - ByteBuddy: {}/{} (hit rate: {:.1f}%), " +
                       "Reflection: {}/{} (hit rate: {:.1f}%), " +
                       "WeakRef: {} tracked, {} cleaned", 
                byteBuddyStats.size, byteBuddyStats.maxSize, byteBuddyStats.hitRate * 100,
                reflectionStats.size, reflectionStats.maxSize, reflectionStats.hitRate * 100,
                weakRefStats.currentlyTracked, weakRefStats.cleanedUp)
                
        } catch (e: Exception) {
            logger.debug("Error logging cache statistics", e)
        }
    }
}