package site.addzero.aop.dicttrans.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import site.addzero.aop.dicttrans.monitoring.CacheStatistics
import site.addzero.aop.dicttrans.monitoring.MemoryPressureLevel
import site.addzero.aop.dicttrans.monitoring.MemoryUsage
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for MemoryManagementLogger
 *
 * @author zjarlin
 * @since 2025/01/12
 */
class MemoryManagementLoggerTest {
    
    private lateinit var listAppender: ListAppender<ILoggingEvent>
    private lateinit var logger: ch.qos.logback.classic.Logger
    
    @BeforeEach
    fun setUp() {
        logger = LoggerFactory.getLogger("MemoryManagement") as ch.qos.logback.classic.Logger
        listAppender = ListAppender<ILoggingEvent>()
        listAppender.start()
        logger.addAppender(listAppender)
        logger.level = Level.DEBUG
    }
    
    @AfterEach
    fun tearDown() {
        logger.detachAppender(listAppender)
        listAppender.stop()
    }
    
    @Test
    fun `should log cache operation with hit`() {
        // When
        MemoryManagementLogger.logCacheOperation(
            cacheName = "TestCache",
            operation = "get",
            key = "test-key",
            hit = true,
            executionTimeMs = 5L
        )
        
        // Then
        val logEvents = listAppender.list
        assertTrue(logEvents.isNotEmpty())
        
        val logEvent = logEvents.first()
        assertEquals(Level.DEBUG, logEvent.level)
        assertTrue(logEvent.message.contains("Cache hit"))
        assertTrue(logEvent.message.contains("operation=get"))
        assertTrue(logEvent.message.contains("cache=TestCache"))
        assertTrue(logEvent.message.contains("key=test-key"))
        assertTrue(logEvent.message.contains("hit=true"))
        assertTrue(logEvent.message.contains("executionTime=5ms"))
    }
    
    @Test
    fun `should log cache operation with miss`() {
        // When
        MemoryManagementLogger.logCacheOperation(
            cacheName = "TestCache",
            operation = "get",
            key = "test-key",
            hit = false,
            executionTimeMs = 50L
        )
        
        // Then
        val logEvents = listAppender.list
        assertTrue(logEvents.isNotEmpty())
        
        val logEvent = logEvents.first()
        assertEquals(Level.INFO, logEvent.level)
        assertTrue(logEvent.message.contains("Cache miss"))
    }
    
    @Test
    fun `should log slow cache operation as warning`() {
        // When
        MemoryManagementLogger.logCacheOperation(
            cacheName = "TestCache",
            operation = "generate",
            key = "slow-key",
            hit = false,
            executionTimeMs = 150L
        )
        
        // Then
        val logEvents = listAppender.list
        assertTrue(logEvents.isNotEmpty())
        
        val logEvent = logEvents.first()
        assertEquals(Level.WARN, logEvent.level)
        assertTrue(logEvent.message.contains("Slow cache operation"))
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
        
        // When
        MemoryManagementLogger.logCacheStatistics("TestCache", statistics)
        
        // Then
        val logEvents = listAppender.list
        assertTrue(logEvents.isNotEmpty())
        
        val logEvent = logEvents.first()
        assertEquals(Level.INFO, logEvent.level)
        assertTrue(logEvent.message.contains("Cache statistics"))
        assertTrue(logEvent.message.contains("cache=TestCache"))
        assertTrue(logEvent.message.contains("size=100"))
        assertTrue(logEvent.message.contains("hitRate=80.00%"))
        assertTrue(logEvent.message.contains("requests=100"))
        assertTrue(logEvent.message.contains("hits=80"))
        assertTrue(logEvent.message.contains("misses=20"))
        assertTrue(logEvent.message.contains("evictions=5"))
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
        
        // When
        MemoryManagementLogger.logCacheStatistics("TestCache", statistics)
        
        // Then
        val logEvents = listAppender.list
        assertTrue(logEvents.size >= 2) // Info + Warning
        
        val warningEvent = logEvents.find { it.level == Level.WARN }
        assertTrue(warningEvent != null)
        assertTrue(warningEvent.message.contains("Low cache hit rate detected"))
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
        
        // Test different pressure levels
        val testCases = listOf(
            MemoryPressureLevel.LOW to Level.DEBUG,
            MemoryPressureLevel.MEDIUM to Level.INFO,
            MemoryPressureLevel.HIGH to Level.WARN,
            MemoryPressureLevel.CRITICAL to Level.ERROR
        )
        
        testCases.forEach { (pressureLevel, expectedLevel) ->
            listAppender.list.clear()
            
            // When
            MemoryManagementLogger.logMemoryUsage("TestComponent", memoryUsage, pressureLevel)
            
            // Then
            val logEvents = listAppender.list
            assertTrue(logEvents.isNotEmpty())
            
            val logEvent = logEvents.first()
            assertEquals(expectedLevel, logEvent.level)
            assertTrue(logEvent.message.contains("Memory usage"))
            assertTrue(logEvent.message.contains("component=TestComponent"))
            assertTrue(logEvent.message.contains("pressure=$pressureLevel"))
            assertTrue(logEvent.message.contains("heap=800MB/1000MB"))
            assertTrue(logEvent.message.contains("metaspace=200MB/256MB"))
        }
    }
    
    @Test
    fun `should log processing limits with context`() {
        // When
        MemoryManagementLogger.logProcessingLimits(
            context = "collection_processing",
            collectionSize = 1500,
            recursionDepth = 8,
            processingTimeMs = 250L,
            limitExceeded = "maxCollectionSize",
            action = "BATCH"
        )
        
        // Then
        val logEvents = listAppender.list
        assertTrue(logEvents.isNotEmpty())
        
        val logEvent = logEvents.first()
        assertEquals(Level.WARN, logEvent.level)
        assertTrue(logEvent.message.contains("Processing limit exceeded"))
        assertTrue(logEvent.message.contains("context=collection_processing"))
        assertTrue(logEvent.message.contains("collectionSize=1500"))
        assertTrue(logEvent.message.contains("recursionDepth=8"))
        assertTrue(logEvent.message.contains("processingTime=250ms"))
        assertTrue(logEvent.message.contains("limitExceeded=maxCollectionSize"))
        assertTrue(logEvent.message.contains("action=BATCH"))
    }
    
    @Test
    fun `should log system events with appropriate levels`() {
        val testCases = listOf(
            "startup" to Level.INFO,
            "shutdown" to Level.INFO,
            "configuration_loaded" to Level.INFO,
            "cache_cleanup" to Level.WARN,
            "memory_pressure_response" to Level.WARN,
            "error" to Level.ERROR,
            "failure" to Level.ERROR,
            "debug_event" to Level.DEBUG
        )
        
        testCases.forEach { (event, expectedLevel) ->
            listAppender.list.clear()
            
            // When
            MemoryManagementLogger.logSystemEvent(event, mapOf("detail" to "test"))
            
            // Then
            val logEvents = listAppender.list
            assertTrue(logEvents.isNotEmpty())
            
            val logEvent = logEvents.first()
            assertEquals(expectedLevel, logEvent.level)
            assertTrue(logEvent.message.contains("System event"))
            assertTrue(logEvent.message.contains("event=$event"))
            assertTrue(logEvent.message.contains("detail=test"))
        }
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
        
        // Then
        assertTrue(dump.contains("Memory Management Diagnostic Dump"))
        assertTrue(dump.contains("Memory Pressure Level: MEDIUM"))
        assertTrue(dump.contains("Heap: 500MB / 1000MB (50.00%)"))
        assertTrue(dump.contains("Metaspace: 100MB / 256MB (39.00%)"))
        assertTrue(dump.contains("ByteBuddy: size=50, hitRate=83.00%"))
        assertTrue(dump.contains("Reflection: size=80, hitRate=87.00%"))
        assertTrue(dump.contains("uptime: 3600000"))
        assertTrue(dump.contains("totalThreads: 25"))
        
        // Check that diagnostic logger was used
        val diagnosticLogger = LoggerFactory.getLogger("MemoryManagement.Diagnostic") as Logger
        val diagnosticAppender = ListAppender<ILoggingEvent>()
        diagnosticAppender.start()
        diagnosticLogger.addAppender(diagnosticAppender)
        
        // Generate another dump to test diagnostic logging
        MemoryManagementLogger.generateDiagnosticDump(
            cacheStatistics = cacheStatistics,
            memoryUsage = memoryUsage,
            pressureLevel = MemoryPressureLevel.LOW
        )
        
        val diagnosticEvents = diagnosticAppender.list
        assertTrue(diagnosticEvents.isNotEmpty())
        assertTrue(diagnosticEvents.first().message.contains("Diagnostic dump generated"))
        
        diagnosticLogger.detachAppender(diagnosticAppender)
        diagnosticAppender.stop()
    }
}