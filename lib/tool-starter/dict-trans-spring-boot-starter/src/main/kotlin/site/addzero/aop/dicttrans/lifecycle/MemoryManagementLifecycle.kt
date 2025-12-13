package site.addzero.aop.dicttrans.lifecycle

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.event.ContextClosedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import site.addzero.aop.dicttrans.cache.ByteBuddyCacheManager
import site.addzero.aop.dicttrans.cache.ReflectionCacheManager
import site.addzero.aop.dicttrans.logging.MemoryManagementLogger
import site.addzero.aop.dicttrans.monitoring.MemoryMonitor
import site.addzero.aop.dicttrans.tracking.WeakReferenceTracker

/**
 * Memory management lifecycle handler for application shutdown cleanup
 *
 * @author zjarlin
 * @since 2025/01/12
 */
@Component
class MemoryManagementLifecycle @Autowired constructor(
    private val byteBuddyCacheManager: ByteBuddyCacheManager,
    private val reflectionCacheManager: ReflectionCacheManager,
    private val weakReferenceTracker: WeakReferenceTracker,
    private val memoryMonitor: MemoryMonitor
) {
    
    private val logger = LoggerFactory.getLogger(MemoryManagementLifecycle::class.java)
    
    @EventListener
    fun handleContextClosed(event: ContextClosedEvent) {
        MemoryManagementLogger.logSystemEvent("shutdown", mapOf(
            "event" to "application_shutdown",
            "source" to "ContextClosedEvent"
        ))
        
        try {
            // Generate final diagnostic dump
            generateFinalDiagnosticDump()
            
            // Stop memory monitoring
            memoryMonitor.stopMonitoring()
            
            // Log final statistics
            logFinalStatistics()
            
            // Clear all caches
            byteBuddyCacheManager.evictAll()
            reflectionCacheManager.evictAll()
            weakReferenceTracker.clear()
            
            MemoryManagementLogger.logSystemEvent("shutdown", mapOf(
                "event" to "cleanup_completed",
                "status" to "success"
            ))
            
        } catch (e: Exception) {
            MemoryManagementLogger.logSystemEvent("error", mapOf(
                "event" to "shutdown_cleanup_failed",
                "error" to (e.message ?: "Unknown error")
            ))
            logger.error("Error during memory management cleanup", e)
        }
    }
    
    private fun logFinalStatistics() {
        try {
            val byteBuddyStats = byteBuddyCacheManager.getStatistics()
            val reflectionStats = reflectionCacheManager.getStatistics()
            val weakRefStats = weakReferenceTracker.getStatistics()
            val memoryStats = memoryMonitor.getStatistics()
            
            // Log individual cache statistics
            MemoryManagementLogger.logCacheStatistics("ByteBuddy", byteBuddyStats)
            MemoryManagementLogger.logCacheStatistics("Reflection", reflectionStats)
            
            // Log system event with final statistics
            MemoryManagementLogger.logSystemEvent("final_statistics", mapOf(
                "byteBuddyCacheSize" to byteBuddyStats.size,
                "byteBuddyHitRate" to byteBuddyStats.hitRate,
                "reflectionCacheSize" to reflectionStats.size,
                "reflectionHitRate" to reflectionStats.hitRate,
                "weakReferencesTracked" to weakRefStats.totalTracked,
                "weakReferencesCleanedUp" to weakRefStats.cleanedUp,
                "memoryChecks" to memoryStats.totalChecks,
                "pressureEvents" to memoryStats.highPressureEvents,
                "cleanupTriggered" to memoryStats.cleanupTriggered,
                "peakHeapUsage" to memoryStats.peakHeapUsage,
                "peakMetaspaceUsage" to memoryStats.peakMetaspaceUsage
            ))
            
        } catch (e: Exception) {
            logger.error("Error logging final statistics", e)
        }
    }
    
    private fun generateFinalDiagnosticDump() {
        try {
            val byteBuddyStats = byteBuddyCacheManager.getStatistics()
            val reflectionStats = reflectionCacheManager.getStatistics()
            val memoryUsage = memoryMonitor.getCurrentMemoryUsage()
            val currentPressureLevel = if (memoryMonitor.isMemoryPressureHigh()) {
                site.addzero.aop.dicttrans.monitoring.MemoryPressureLevel.HIGH
            } else {
                site.addzero.aop.dicttrans.monitoring.MemoryPressureLevel.LOW
            }
            
            val cacheStatistics = mapOf(
                "ByteBuddy" to byteBuddyStats,
                "Reflection" to reflectionStats
            )
            
            val additionalInfo = mapOf(
                "shutdownReason" to "application_shutdown",
                "uptime" to System.currentTimeMillis(),
                "totalMemory" to Runtime.getRuntime().totalMemory(),
                "freeMemory" to Runtime.getRuntime().freeMemory(),
                "maxMemory" to Runtime.getRuntime().maxMemory()
            )
            
            MemoryManagementLogger.generateDiagnosticDump(
                cacheStatistics = cacheStatistics,
                memoryUsage = memoryUsage,
                pressureLevel = currentPressureLevel,
                additionalInfo = additionalInfo
            )
            
        } catch (e: Exception) {
            logger.error("Error generating final diagnostic dump", e)
        }
    }
}