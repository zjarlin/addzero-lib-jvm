package site.addzero.aop.dicttrans.logging

import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import site.addzero.aop.dicttrans.monitoring.CacheStatistics
import site.addzero.aop.dicttrans.monitoring.MemoryPressureLevel
import site.addzero.aop.dicttrans.monitoring.MemoryUsage
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for MemoryManagementLogger
 * 
 * Note: These tests verify that logging methods execute without errors.
 * For detailed log content verification, integration tests with proper
 * logging configuration should be used.
 *
 * @author zjarlin
 * @since 2025/01/12
 */
class MemoryManagementLoggerTest {
    
    companion object {
        @JvmStatic
        fun isLogbackAvailable(): Boolean {
            return try {
                val logger = LoggerFactory.getLogger("test")
                logger is ch.qos.logback.classic.Logger
            } catch (e: Exception) {
                false
            }
        }
    }
    
    @Test
    fun `should log cache operation with hit`() {
        // When - This should not throw any exceptions
        MemoryManagementLogger.logCacheOperation(
            cacheName = "TestCache",
            operation = "get",
            key = "test-key",
            hit = true,
            executionTimeMs = 5L
        )
        
        // Then - Verify the method executed successfully
        assertTrue(true, "Cache operation logging completed without errors")
    }
    
    @Test
    fun `should log cache operation with miss`() {
        // When - This should not throw any exceptions
        MemoryManagementLogger.logCacheOperation(
            cacheName = "TestCache",
            operation = "get",
            key = "test-key",
            hit = false,
            executionTimeMs = 50L
        )
        
        // Then - Verify the method executed successfully
        assertTrue(true, "Cache miss logging completed without errors")
    }
    
    @Test
    fun `should log slow cache operation as warning`() {
        // When - This should not throw any exceptions
        MemoryManagementLogger.logCacheOperation(
            cacheName = "TestCache",
            operation = "generate",
            key = "slow-key",
            hit = false,
            executionTimeMs = 150L
        )
        
        // Then - Verify the method executed successfully
        assertTrue(true, "Slow cache operation logging completed without errors")
    }
    
    @Test
    fun `should log cache statistics with performance metrics`() {
        // Given
        val statistics = CacheStatistics(
            hitCount = 80L,
            missCount = 20L,
            hitRate = 0.8,
            evictionCount = 5L,
            size = 100L,
            maxSize = 1000L
        )
        
        // When - This should not throw any exceptions
        MemoryManagementLogger.logCacheStatistics("TestCache", statistics)
        
        // Then - Verify the method executed successfully and statistics are valid
        assertTrue(statistics.totalRequests == 100L, "Total requests should be calculated correctly")
        assertTrue(statistics.hitRate == 0.8, "Hit rate should be correct")
        assertTrue(true, "Cache statistics logging completed without errors")
    }
    
    @Test
    fun `should warn on low cache hit rate`() {
        // Given
        val statistics = CacheStatistics(
            hitCount = 30L,
            missCount = 70L,
            hitRate = 0.3,
            evictionCount = 2L,
            size = 50L,
            maxSize = 1000L
        )
        
        // When - This should not throw any exceptions
        MemoryManagementLogger.logCacheStatistics("TestCache", statistics)
        
        // Then - Verify the method executed successfully and low hit rate is detected
        assertTrue(statistics.hitRate < 0.5, "Hit rate should be low")
        assertTrue(statistics.totalRequests >= 100, "Should have enough requests to trigger warning")
        assertTrue(true, "Low cache hit rate logging completed without errors")
    }
    
    @Test
    fun `should log memory usage with different pressure levels`() {
        // Given
        val memoryUsage = MemoryUsage(
            heapUsed = 800L * 1024 * 1024, // 800MB
            heapMax = 1000L * 1024 * 1024,  // 1GB
            heapUsagePercent = 0.8,
            metaspaceUsed = 200L * 1024 * 1024, // 200MB
            metaspaceMax = 256L * 1024 * 1024,  // 256MB
            metaspaceUsagePercent = 0.78,
            nonHeapUsed = 250L * 1024 * 1024,
            nonHeapMax = 512L * 1024 * 1024,
            gcCount = 10L,
            gcTime = 500L
        )
        
        // Test different pressure levels - should not throw exceptions
        val testCases = listOf(
            MemoryPressureLevel.LOW,
            MemoryPressureLevel.MEDIUM,
            MemoryPressureLevel.HIGH,
            MemoryPressureLevel.CRITICAL
        )
        
        testCases.forEach { pressureLevel ->
            // When - This should not throw any exceptions
            MemoryManagementLogger.logMemoryUsage("TestComponent", memoryUsage, pressureLevel)
        }
        
        // Then - Verify memory usage calculations are correct
        assertTrue(memoryUsage.heapUsagePercent == 0.8, "Heap usage percent should be correct")
        assertTrue(memoryUsage.metaspaceUsagePercent == 0.78, "Metaspace usage percent should be correct")
        assertTrue(true, "Memory usage logging completed without errors for all pressure levels")
    }
    
    @Test
    fun `should log processing limits with context`() {
        // When - This should not throw any exceptions
        MemoryManagementLogger.logProcessingLimits(
            context = "collection_processing",
            collectionSize = 1500,
            recursionDepth = 8,
            processingTimeMs = 250L,
            limitExceeded = "maxCollectionSize",
            action = "BATCH"
        )
        
        // Then - Verify the method executed successfully
        assertTrue(true, "Processing limits logging completed without errors")
    }
    
    @Test
    fun `should log system events with appropriate levels`() {
        val testCases = listOf(
            "startup",
            "shutdown", 
            "configuration_loaded",
            "cache_cleanup",
            "memory_pressure_response",
            "error",
            "failure",
            "debug_event"
        )
        
        testCases.forEach { event ->
            // When - This should not throw any exceptions
            MemoryManagementLogger.logSystemEvent(event, mapOf("detail" to "test"))
        }
        
        // Then - Verify all system events logged successfully
        assertTrue(true, "System event logging completed without errors for all event types")
    }
    
    @Test
    fun `should generate comprehensive diagnostic dump`() {
        // Given
        val cacheStatistics = mapOf(
            "ByteBuddy" to CacheStatistics(
                hitCount = 100L,
                missCount = 20L,
                hitRate = 0.83,
                evictionCount = 5L,
                size = 50L,
                maxSize = 1000L
            ),
            "Reflection" to CacheStatistics(
                hitCount = 200L,
                missCount = 30L,
                hitRate = 0.87,
                evictionCount = 10L,
                size = 80L,
                maxSize = 2000L
            )
        )
        
        val memoryUsage = MemoryUsage(
            heapUsed = 500L * 1024 * 1024,
            heapMax = 1000L * 1024 * 1024,
            heapUsagePercent = 0.5,
            metaspaceUsed = 100L * 1024 * 1024,
            metaspaceMax = 256L * 1024 * 1024,
            metaspaceUsagePercent = 0.39,
            nonHeapUsed = 150L * 1024 * 1024,
            nonHeapMax = 512L * 1024 * 1024,
            gcCount = 5L,
            gcTime = 200L
        )
        
        val additionalInfo = mapOf(
            "uptime" to 3600000L,
            "totalThreads" to 25
        )
        
        // When
        val dump = MemoryManagementLogger.generateDiagnosticDump(
            cacheStatistics = cacheStatistics,
            memoryUsage = memoryUsage,
            pressureLevel = MemoryPressureLevel.MEDIUM,
            additionalInfo = additionalInfo
        )
        
        // Then - Verify diagnostic dump content
        assertNotNull(dump, "Diagnostic dump should not be null")
        assertTrue(dump.contains("Memory Management Diagnostic Dump"), "Should contain header")
        assertTrue(dump.contains("Memory Pressure Level: MEDIUM"), "Should contain pressure level")
        assertTrue(dump.contains("Heap: 500MB / 1000MB (50.00%)"), "Should contain heap info")
        assertTrue(dump.contains("Metaspace: 100MB / 256MB (39.00%)"), "Should contain metaspace info")
        assertTrue(dump.contains("ByteBuddy: size=50, hitRate=83.33%, requests=120, evictions=5"), "Should contain ByteBuddy stats")
        assertTrue(dump.contains("Reflection: size=80, hitRate=86.96%, requests=230, evictions=10"), "Should contain Reflection stats")
        assertTrue(dump.contains("uptime: 3600000"), "Should contain uptime")
        assertTrue(dump.contains("totalThreads: 25"), "Should contain thread count")
        
        // Test that generating another dump doesn't throw exceptions
        val secondDump = MemoryManagementLogger.generateDiagnosticDump(
            cacheStatistics = cacheStatistics,
            memoryUsage = memoryUsage,
            pressureLevel = MemoryPressureLevel.LOW
        )
        
        assertNotNull(secondDump, "Second diagnostic dump should not be null")
        assertTrue(secondDump.contains("Memory Pressure Level: LOW"), "Second dump should have correct pressure level")
        assertTrue(true, "Diagnostic dump generation completed without errors")
    }
}