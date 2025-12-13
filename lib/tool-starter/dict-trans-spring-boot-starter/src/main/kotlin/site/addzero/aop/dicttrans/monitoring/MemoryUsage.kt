package site.addzero.aop.dicttrans.monitoring

import java.time.Instant

/**
 * Memory usage data class for monitoring JVM memory consumption
 *
 * @author zjarlin
 * @since 2025/01/12
 */
data class MemoryUsage(
    val heapUsed: Long,
    val heapMax: Long,
    val heapUsagePercent: Double,
    val metaspaceUsed: Long,
    val metaspaceMax: Long,
    val metaspaceUsagePercent: Double,
    val nonHeapUsed: Long = 0L,
    val nonHeapMax: Long = 0L,
    val gcCount: Long = 0L,
    val gcTime: Long = 0L,
    val timestamp: Instant = Instant.now()
) {
    val heapAvailable: Long = heapMax - heapUsed
    val metaspaceAvailable: Long = if (metaspaceMax > 0) metaspaceMax - metaspaceUsed else Long.MAX_VALUE
    
    fun isHeapPressureHigh(threshold: Double): Boolean = heapUsagePercent >= threshold
    fun isMetaspacePressureHigh(threshold: Double): Boolean = metaspaceUsagePercent >= threshold
    
    companion object {
        fun current(): MemoryUsage {
            val runtime = Runtime.getRuntime()
            val heapUsed = runtime.totalMemory() - runtime.freeMemory()
            val heapMax = runtime.maxMemory()
            val heapPercent = if (heapMax > 0) heapUsed.toDouble() / heapMax else 0.0
            
            // For metaspace, we'll use management beans in the actual implementation
            // This is a simplified version for now
            return MemoryUsage(
                heapUsed = heapUsed,
                heapMax = heapMax,
                heapUsagePercent = heapPercent,
                metaspaceUsed = 0L, // Will be implemented with MemoryMXBean
                metaspaceMax = 0L,  // Will be implemented with MemoryMXBean
                metaspaceUsagePercent = 0.0
            )
        }
        
        fun empty(): MemoryUsage {
            return MemoryUsage(
                heapUsed = 0L,
                heapMax = 0L,
                heapUsagePercent = 0.0,
                metaspaceUsed = 0L,
                metaspaceMax = 0L,
                metaspaceUsagePercent = 0.0,
                nonHeapUsed = 0L,
                nonHeapMax = 0L,
                gcCount = 0L,
                gcTime = 0L
            )
        }
    }
}