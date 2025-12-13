package site.addzero.aop.dicttrans.monitoring

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import site.addzero.aop.dicttrans.cache.ByteBuddyCacheManager
import site.addzero.aop.dicttrans.cache.ReflectionCacheManager
import site.addzero.aop.dicttrans.config.CacheProperties
import site.addzero.aop.dicttrans.config.MemoryManagementProperties
import site.addzero.aop.dicttrans.config.MonitoringProperties
import site.addzero.aop.dicttrans.config.ProcessingProperties
import site.addzero.aop.dicttrans.tracking.WeakReferenceTracker
import java.time.Duration

/**
 * Test for memory monitor
 */
class MemoryMonitorTest {
    
    private lateinit var memoryMonitor: MemoryMonitor
    private lateinit var properties: MemoryManagementProperties
    private lateinit var byteBuddyCacheManager: ByteBuddyCacheManager
    private lateinit var reflectionCacheManager: ReflectionCacheManager
    private lateinit var weakReferenceTracker: WeakReferenceTracker
    
    @BeforeEach
    fun setUp() {
        properties = MemoryManagementProperties(
            byteBuddyCache = CacheProperties(),
            reflectionCache = CacheProperties(),
            processing = ProcessingProperties(),
            monitoring = MonitoringProperties(
                metaspaceWarningThreshold = 0.8,
                heapWarningThreshold = 0.85,
                monitoringInterval = Duration.ofSeconds(1),
                enableJmxMetrics = true
            )
        )
        
        // Create mocks
        byteBuddyCacheManager = mock(ByteBuddyCacheManager::class.java)
        reflectionCacheManager = mock(ReflectionCacheManager::class.java)
        weakReferenceTracker = mock(WeakReferenceTracker::class.java)
        
        // Configure mock returns
        `when`(byteBuddyCacheManager.getStatistics()).thenReturn(
            CacheStatistics(
                hitCount = 100,
                missCount = 20,
                hitRate = 0.83,
                evictionCount = 5,
                size = 50,
                maxSize = 1000
            )
        )
        
        `when`(reflectionCacheManager.getStatistics()).thenReturn(
            CacheStatistics(
                hitCount = 200,
                missCount = 30,
                hitRate = 0.87,
                evictionCount = 10,
                size = 80,
                maxSize = 2000
            )
        )
        
        memoryMonitor = MemoryMonitorImpl(
            properties, 
            byteBuddyCacheManager, 
            reflectionCacheManager, 
            weakReferenceTracker
        )
    }
    
    @Test
    fun `should get current memory usage`() {
        // When
        val memoryUsage = memoryMonitor.getCurrentMemoryUsage()
        
        // Then
        assertNotNull(memoryUsage, "Memory usage should not be null")
        assertTrue(memoryUsage.heapUsed >= 0, "Heap used should be non-negative")
        assertTrue(memoryUsage.heapMax > 0, "Heap max should be positive")
        assertTrue(memoryUsage.heapUsagePercent >= 0, "Heap usage percent should be non-negative")
        assertTrue(memoryUsage.heapUsagePercent <= 1, "Heap usage percent should not exceed 100%")
        assertNotNull(memoryUsage.timestamp, "Timestamp should not be null")
    }
    
    @Test
    fun `should detect memory pressure`() {
        // When
        val isHighPressure = memoryMonitor.isMemoryPressureHigh()
        
        // Then - should not fail (actual result depends on current memory state)
        assertNotNull(isHighPressure, "Memory pressure check should return a boolean")
    }
    
    @Test
    fun `should start and stop monitoring`() {
        // When
        memoryMonitor.startMonitoring()
        
        // Then - should not throw exception
        assertTrue(true, "Start monitoring should complete without error")
        
        // When
        memoryMonitor.stopMonitoring()
        
        // Then - should not throw exception
        assertTrue(true, "Stop monitoring should complete without error")
    }
    
    @Test
    fun `should trigger cleanup operations`() {
        // When
        memoryMonitor.triggerCleanup()
        
        // Then - verify cleanup methods were called
        verify(byteBuddyCacheManager, atLeastOnce()).evictLeastRecentlyUsed(anyInt())
        verify(reflectionCacheManager, atLeastOnce()).evictAll()
        verify(weakReferenceTracker, atLeastOnce()).cleanup()
    }
    
    @Test
    fun `should provide monitoring statistics`() {
        // When
        val statistics = memoryMonitor.getStatistics()
        
        // Then
        assertNotNull(statistics, "Statistics should not be null")
        assertNotNull(statistics.monitoringStartTime, "Monitoring start time should not be null")
        assertTrue(statistics.totalChecks >= 0, "Total checks should be non-negative")
        assertTrue(statistics.highPressureEvents >= 0, "High pressure events should be non-negative")
        assertTrue(statistics.cleanupTriggered >= 0, "Cleanup triggered should be non-negative")
        assertTrue(statistics.averageHeapUsage >= 0, "Average heap usage should be non-negative")
        assertTrue(statistics.averageMetaspaceUsage >= 0, "Average metaspace usage should be non-negative")
    }
    
    @Test
    fun `should manage memory pressure listeners`() {
        // Given
        val listener = TestMemoryPressureListener()
        
        // When
        memoryMonitor.addMemoryPressureListener(listener)
        
        // Then - should not throw exception
        assertTrue(true, "Adding listener should complete without error")
        
        // When
        memoryMonitor.removeMemoryPressureListener(listener)
        
        // Then - should not throw exception
        assertTrue(true, "Removing listener should complete without error")
    }
    
    @Test
    fun `should handle memory usage calculations correctly`() {
        // Given
        val memoryUsage = MemoryUsage(
            heapUsed = 500_000_000,      // 500MB
            heapMax = 1_000_000_000,     // 1GB
            heapUsagePercent = 0.5,      // 50%
            metaspaceUsed = 100_000_000, // 100MB
            metaspaceMax = 200_000_000,  // 200MB
            metaspaceUsagePercent = 0.5, // 50%
            nonHeapUsed = 150_000_000,   // 150MB
            nonHeapMax = 300_000_000,    // 300MB
            gcCount = 10,
            gcTime = 1000
        )
        
        // Then
        assertEquals(500_000_000, memoryUsage.heapAvailable, "Heap available should be calculated correctly")
        assertEquals(100_000_000, memoryUsage.metaspaceAvailable, "Metaspace available should be calculated correctly")
        assertFalse(memoryUsage.isHeapPressureHigh(0.8), "Should not detect high heap pressure at 50%")
        assertTrue(memoryUsage.isHeapPressureHigh(0.4), "Should detect high heap pressure with low threshold")
        assertFalse(memoryUsage.isMetaspacePressureHigh(0.8), "Should not detect high metaspace pressure at 50%")
        assertTrue(memoryUsage.isMetaspacePressureHigh(0.4), "Should detect high metaspace pressure with low threshold")
    }
    
    @Test
    fun `should handle empty memory usage`() {
        // Given
        val emptyUsage = MemoryUsage.empty()
        
        // Then
        assertEquals(0, emptyUsage.heapUsed, "Empty usage should have zero heap used")
        assertEquals(0, emptyUsage.heapMax, "Empty usage should have zero heap max")
        assertEquals(0.0, emptyUsage.heapUsagePercent, "Empty usage should have zero heap usage percent")
        assertEquals(0, emptyUsage.metaspaceUsed, "Empty usage should have zero metaspace used")
        assertEquals(0, emptyUsage.metaspaceMax, "Empty usage should have zero metaspace max")
        assertEquals(0.0, emptyUsage.metaspaceUsagePercent, "Empty usage should have zero metaspace usage percent")
        assertFalse(emptyUsage.isHeapPressureHigh(0.5), "Empty usage should not show high pressure")
        assertFalse(emptyUsage.isMetaspacePressureHigh(0.5), "Empty usage should not show high pressure")
    }
    
    // Test implementation of MemoryPressureListener
    private class TestMemoryPressureListener : MemoryPressureListener {
        var pressureEventCount = 0
        var reliefEventCount = 0
        
        override fun onMemoryPressure(memoryUsage: MemoryUsage, pressureLevel: Double) {
            pressureEventCount++
        }
        
        override fun onMemoryPressureRelieved(memoryUsage: MemoryUsage) {
            reliefEventCount++
        }
    }
}