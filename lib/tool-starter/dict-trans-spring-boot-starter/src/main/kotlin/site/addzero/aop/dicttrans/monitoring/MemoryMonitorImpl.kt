package site.addzero.aop.dicttrans.monitoring

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import site.addzero.aop.dicttrans.cache.ByteBuddyCacheManager
import site.addzero.aop.dicttrans.cache.ReflectionCacheManager
import site.addzero.aop.dicttrans.config.MemoryManagementProperties
import site.addzero.aop.dicttrans.logging.MemoryManagementLogger
import site.addzero.aop.dicttrans.tracking.WeakReferenceTracker
import java.lang.management.GarbageCollectorMXBean
import java.lang.management.ManagementFactory
import java.lang.management.MemoryMXBean
import java.lang.management.MemoryPoolMXBean
import java.time.Instant
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * Memory monitor implementation with comprehensive JVM memory tracking
 *
 * @author zjarlin
 * @since 2025/01/12
 */
@Component
class MemoryMonitorImpl @Autowired constructor(
    private val properties: MemoryManagementProperties,
    private val byteBuddyCacheManager: ByteBuddyCacheManager,
    private val reflectionCacheManager: ReflectionCacheManager,
    private val weakReferenceTracker: WeakReferenceTracker
) : MemoryMonitor {
    
    private val logger = LoggerFactory.getLogger(MemoryMonitorImpl::class.java)
    
    // JVM management beans
    private val memoryBean: MemoryMXBean = ManagementFactory.getMemoryMXBean()
    private val gcBeans: List<GarbageCollectorMXBean> = ManagementFactory.getGarbageCollectorMXBeans()
    private val memoryPoolBeans: List<MemoryPoolMXBean> = ManagementFactory.getMemoryPoolMXBeans()
    
    // Monitoring state
    private val isMonitoring = AtomicBoolean(false)
    private val monitoringStartTime = AtomicLong(0)
    private var scheduler: ScheduledExecutorService? = null
    
    // Statistics
    private val totalChecks = AtomicLong(0)
    private val highPressureEvents = AtomicLong(0)
    private val cleanupTriggered = AtomicLong(0)
    private val lastCleanupTime = AtomicLong(0)
    
    // Memory usage tracking
    private val heapUsageHistory = mutableListOf<Double>()
    private val metaspaceUsageHistory = mutableListOf<Double>()
    private val maxHistorySize = 100
    
    // Peak usage tracking
    private var peakHeapUsage = 0.0
    private var peakMetaspaceUsage = 0.0
    
    // Listeners
    private val memoryPressureListeners = CopyOnWriteArrayList<MemoryPressureListener>()
    
    // Current pressure state
    private var currentPressureLevel = MemoryPressureLevel.LOW
    
    @PostConstruct
    fun initialize() {
        if (properties.monitoring.enableJmxMetrics) {
            logger.info("Initializing memory monitor with interval: {}", properties.monitoring.monitoringInterval)
            startMonitoring()
        } else {
            logger.info("Memory monitoring disabled in configuration")
        }
    }
    
    override fun startMonitoring() {
        if (isMonitoring.compareAndSet(false, true)) {
            monitoringStartTime.set(System.currentTimeMillis())
            
            scheduler = Executors.newScheduledThreadPool(1) { r ->
                Thread(r, "MemoryMonitor").apply { isDaemon = true }
            }
            
            scheduler?.scheduleAtFixedRate(
                this::performMemoryCheck,
                0,
                properties.monitoring.monitoringInterval.toMillis(),
                TimeUnit.MILLISECONDS
            )
            
            logger.info("Memory monitoring started with interval: {}", properties.monitoring.monitoringInterval)
        }
    }
    
    override fun stopMonitoring() {
        if (isMonitoring.compareAndSet(true, false)) {
            scheduler?.shutdown()
            try {
                if (scheduler?.awaitTermination(5, TimeUnit.SECONDS) == false) {
                    scheduler?.shutdownNow()
                }
            } catch (e: InterruptedException) {
                scheduler?.shutdownNow()
                Thread.currentThread().interrupt()
            }
            logger.info("Memory monitoring stopped")
        }
    }
    
    override fun getCurrentMemoryUsage(): MemoryUsage {
        val heapMemory = memoryBean.heapMemoryUsage
        val nonHeapMemory = memoryBean.nonHeapMemoryUsage
        
        // Get metaspace usage
        val metaspacePool = memoryPoolBeans.find { 
            it.name.contains("Metaspace", ignoreCase = true) 
        }
        val metaspaceUsage = metaspacePool?.usage
        
        // Get GC statistics
        val totalGcCount = gcBeans.sumOf { it.collectionCount }
        val totalGcTime = gcBeans.sumOf { it.collectionTime }
        
        val heapUsagePercent = if (heapMemory.max > 0) {
            heapMemory.used.toDouble() / heapMemory.max
        } else {
            0.0
        }
        
        val metaspaceUsagePercent = if (metaspaceUsage != null && metaspaceUsage.max > 0) {
            metaspaceUsage.used.toDouble() / metaspaceUsage.max
        } else {
            0.0
        }
        
        return MemoryUsage(
            heapUsed = heapMemory.used,
            heapMax = heapMemory.max,
            heapUsagePercent = heapUsagePercent,
            metaspaceUsed = metaspaceUsage?.used ?: 0,
            metaspaceMax = metaspaceUsage?.max ?: 0,
            metaspaceUsagePercent = metaspaceUsagePercent,
            nonHeapUsed = nonHeapMemory.used,
            nonHeapMax = nonHeapMemory.max,
            gcCount = totalGcCount,
            gcTime = totalGcTime
        )
    }
    
    override fun isMemoryPressureHigh(): Boolean {
        val memoryUsage = getCurrentMemoryUsage()
        return memoryUsage.isHeapPressureHigh(properties.monitoring.heapWarningThreshold) ||
               memoryUsage.isMetaspacePressureHigh(properties.monitoring.metaspaceWarningThreshold)
    }
    
    override fun triggerCleanup() {
        logger.info("Triggering memory cleanup operations")
        
        try {
            // Clean up caches
            val byteBuddyStats = byteBuddyCacheManager.getStatistics()
            val reflectionStats = reflectionCacheManager.getStatistics()
            
            logger.debug("Before cleanup - ByteBuddy cache: {}, Reflection cache: {}", 
                byteBuddyStats.size, reflectionStats.size)
            
            // Trigger cache cleanup
            byteBuddyCacheManager.evictLeastRecentlyUsed(10)
            reflectionCacheManager.evictAll() // More aggressive for reflection cache
            
            // Clean up weak references
            weakReferenceTracker.cleanup()
            
            // Suggest GC
            System.gc()
            
            cleanupTriggered.incrementAndGet()
            lastCleanupTime.set(System.currentTimeMillis())
            
            logger.info("Memory cleanup completed")
            
        } catch (e: Exception) {
            logger.error("Error during memory cleanup", e)
        }
    }
    
    override fun getStatistics(): MemoryMonitoringStatistics {
        val avgHeapUsage = synchronized(heapUsageHistory) {
            if (heapUsageHistory.isEmpty()) 0.0 else heapUsageHistory.average()
        }
        
        val avgMetaspaceUsage = synchronized(metaspaceUsageHistory) {
            if (metaspaceUsageHistory.isEmpty()) 0.0 else metaspaceUsageHistory.average()
        }
        
        return MemoryMonitoringStatistics(
            monitoringStartTime = Instant.ofEpochMilli(monitoringStartTime.get()),
            totalChecks = totalChecks.get(),
            highPressureEvents = highPressureEvents.get(),
            cleanupTriggered = cleanupTriggered.get(),
            averageHeapUsage = avgHeapUsage,
            averageMetaspaceUsage = avgMetaspaceUsage,
            peakHeapUsage = peakHeapUsage,
            peakMetaspaceUsage = peakMetaspaceUsage,
            lastCleanupTime = if (lastCleanupTime.get() > 0) {
                Instant.ofEpochMilli(lastCleanupTime.get())
            } else null
        )
    }
    
    override fun addMemoryPressureListener(listener: MemoryPressureListener) {
        memoryPressureListeners.add(listener)
        logger.debug("Added memory pressure listener: {}", listener.javaClass.simpleName)
    }
    
    override fun removeMemoryPressureListener(listener: MemoryPressureListener) {
        memoryPressureListeners.remove(listener)
        logger.debug("Removed memory pressure listener: {}", listener.javaClass.simpleName)
    }
    
    /**
     * Perform periodic memory check
     */
    private fun performMemoryCheck() {
        try {
            totalChecks.incrementAndGet()
            
            val memoryUsage = getCurrentMemoryUsage()
            
            // Update usage history
            synchronized(heapUsageHistory) {
                heapUsageHistory.add(memoryUsage.heapUsagePercent)
                if (heapUsageHistory.size > maxHistorySize) {
                    heapUsageHistory.removeAt(0)
                }
            }
            
            synchronized(metaspaceUsageHistory) {
                metaspaceUsageHistory.add(memoryUsage.metaspaceUsagePercent)
                if (metaspaceUsageHistory.size > maxHistorySize) {
                    metaspaceUsageHistory.removeAt(0)
                }
            }
            
            // Update peak usage
            if (memoryUsage.heapUsagePercent > peakHeapUsage) {
                peakHeapUsage = memoryUsage.heapUsagePercent
            }
            if (memoryUsage.metaspaceUsagePercent > peakMetaspaceUsage) {
                peakMetaspaceUsage = memoryUsage.metaspaceUsagePercent
            }
            
            // Determine pressure level
            val newPressureLevel = determinePressureLevel(memoryUsage)
            
            // Log memory usage with comprehensive logging
            MemoryManagementLogger.logMemoryUsage("MemoryMonitor", memoryUsage, newPressureLevel)
            
            // Check for pressure changes
            if (newPressureLevel != currentPressureLevel) {
                handlePressureLevelChange(memoryUsage, currentPressureLevel, newPressureLevel)
                currentPressureLevel = newPressureLevel
            }
            
        } catch (e: Exception) {
            logger.error("Error during memory check", e)
        }
    }
    
    /**
     * Determine memory pressure level
     */
    private fun determinePressureLevel(memoryUsage: MemoryUsage): MemoryPressureLevel {
        val maxUsage = maxOf(memoryUsage.heapUsagePercent, memoryUsage.metaspaceUsagePercent)
        
        return when {
            maxUsage >= 0.95 -> MemoryPressureLevel.CRITICAL
            maxUsage >= 0.85 -> MemoryPressureLevel.HIGH
            maxUsage >= 0.70 -> MemoryPressureLevel.MEDIUM
            else -> MemoryPressureLevel.LOW
        }
    }
    
    /**
     * Handle pressure level changes
     */
    private fun handlePressureLevelChange(
        memoryUsage: MemoryUsage, 
        oldLevel: MemoryPressureLevel, 
        newLevel: MemoryPressureLevel
    ) {
        when (newLevel) {
            MemoryPressureLevel.HIGH, MemoryPressureLevel.CRITICAL -> {
                highPressureEvents.incrementAndGet()
                
                MemoryManagementLogger.logSystemEvent("memory_pressure_increase", mapOf(
                    "oldLevel" to oldLevel,
                    "newLevel" to newLevel,
                    "heapUsage" to memoryUsage.heapUsagePercent,
                    "metaspaceUsage" to memoryUsage.metaspaceUsagePercent,
                    "listenerCount" to memoryPressureListeners.size
                ))
                
                // Notify listeners
                val pressureLevel = maxOf(memoryUsage.heapUsagePercent, memoryUsage.metaspaceUsagePercent)
                memoryPressureListeners.forEach { listener ->
                    try {
                        listener.onMemoryPressure(memoryUsage, pressureLevel)
                    } catch (e: Exception) {
                        logger.error("Error notifying memory pressure listener", e)
                    }
                }
                
                // Trigger cleanup for high pressure
                if (newLevel == MemoryPressureLevel.CRITICAL) {
                    MemoryManagementLogger.logSystemEvent("memory_pressure_response", mapOf(
                        "action" to "trigger_cleanup",
                        "pressureLevel" to newLevel
                    ))
                    triggerCleanup()
                }
            }
            
            MemoryPressureLevel.LOW, MemoryPressureLevel.MEDIUM -> {
                if (oldLevel == MemoryPressureLevel.HIGH || oldLevel == MemoryPressureLevel.CRITICAL) {
                    logger.info("Memory pressure relieved to {} - Heap: {:.1f}%, Metaspace: {:.1f}%", 
                        newLevel, memoryUsage.heapUsagePercent * 100, memoryUsage.metaspaceUsagePercent * 100)
                    
                    // Notify listeners
                    memoryPressureListeners.forEach { listener ->
                        try {
                            listener.onMemoryPressureRelieved(memoryUsage)
                        } catch (e: Exception) {
                            logger.error("Error notifying memory pressure relief listener", e)
                        }
                    }
                }
            }
        }
    }
    
    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down memory monitor")
        stopMonitoring()
    }
}