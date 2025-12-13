package site.addzero.aop.dicttrans.monitoring

import java.time.Instant

/**
 * Memory monitor interface for tracking JVM memory usage and triggering cleanup
 *
 * @author zjarlin
 * @since 2025/01/12
 */
interface MemoryMonitor {
    
    /**
     * Start memory monitoring
     */
    fun startMonitoring()
    
    /**
     * Stop memory monitoring
     */
    fun stopMonitoring()
    
    /**
     * Get current memory usage
     * 
     * @return Current memory usage information
     */
    fun getCurrentMemoryUsage(): MemoryUsage
    
    /**
     * Check if memory pressure is high
     * 
     * @return true if memory pressure is high
     */
    fun isMemoryPressureHigh(): Boolean
    
    /**
     * Trigger cleanup operations
     */
    fun triggerCleanup()
    
    /**
     * Get memory monitoring statistics
     * 
     * @return Memory monitoring statistics
     */
    fun getStatistics(): MemoryMonitoringStatistics
    
    /**
     * Register a memory pressure listener
     * 
     * @param listener The listener to register
     */
    fun addMemoryPressureListener(listener: MemoryPressureListener)
    
    /**
     * Remove a memory pressure listener
     * 
     * @param listener The listener to remove
     */
    fun removeMemoryPressureListener(listener: MemoryPressureListener)
}



/**
 * Memory monitoring statistics
 */
data class MemoryMonitoringStatistics(
    val monitoringStartTime: Instant,
    val totalChecks: Long,
    val highPressureEvents: Long,
    val cleanupTriggered: Long,
    val averageHeapUsage: Double,
    val averageMetaspaceUsage: Double,
    val peakHeapUsage: Double,
    val peakMetaspaceUsage: Double,
    val lastCleanupTime: Instant?
)

/**
 * Memory pressure listener interface
 */
interface MemoryPressureListener {
    
    /**
     * Called when memory pressure is detected
     * 
     * @param memoryUsage Current memory usage
     * @param pressureLevel Pressure level (0.0 to 1.0)
     */
    fun onMemoryPressure(memoryUsage: MemoryUsage, pressureLevel: Double)
    
    /**
     * Called when memory pressure is relieved
     * 
     * @param memoryUsage Current memory usage
     */
    fun onMemoryPressureRelieved(memoryUsage: MemoryUsage)
}

/**
 * Memory pressure levels
 */
enum class MemoryPressureLevel {
    LOW,      // < 70%
    MEDIUM,   // 70% - 85%
    HIGH,     // 85% - 95%
    CRITICAL  // > 95%
}