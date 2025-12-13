package site.addzero.aop.dicttrans.limits

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import site.addzero.aop.dicttrans.config.CacheProperties
import site.addzero.aop.dicttrans.config.MemoryManagementProperties
import site.addzero.aop.dicttrans.config.MonitoringProperties
import site.addzero.aop.dicttrans.config.ProcessingProperties
import site.addzero.aop.dicttrans.processing.ProcessingContext
import java.time.Duration

/**
 * Test for processing limit manager
 */
class ProcessingLimitManagerTest {
    
    private lateinit var limitManager: ProcessingLimitManager
    private lateinit var properties: MemoryManagementProperties
    
    @BeforeEach
    fun setUp() {
        properties = MemoryManagementProperties(
            byteBuddyCache = CacheProperties(),
            reflectionCache = CacheProperties(),
            processing = ProcessingProperties(
                maxCollectionSize = 100,
                maxRecursionDepth = 5,
                processingTimeout = Duration.ofSeconds(2),
                enableCircuitBreaker = true
            ),
            monitoring = MonitoringProperties(
                metaspaceWarningThreshold = 0.8,
                heapWarningThreshold = 0.85,
                monitoringInterval = Duration.ofMinutes(1),
                enableJmxMetrics = true
            )
        )
        limitManager = ProcessingLimitManagerImpl(properties)
    }
    
    @Test
    fun `should allow processing within collection size limits`() {
        // Given
        val context = ProcessingContext.create(
            maxCollectionSize = 100,
            maxRecursionDepth = 5,
            timeout = Duration.ofSeconds(10)
        )
        
        // When
        val decision = limitManager.checkCollectionLimit(50, context)
        
        // Then
        assertTrue(decision.allowed, "Should allow processing within limits")
        assertEquals(ProcessingAction.CONTINUE, decision.suggestedAction)
    }
    
    @Test
    fun `should reject processing when collection size exceeds limits`() {
        // Given
        val context = ProcessingContext.create(
            maxCollectionSize = 100,
            maxRecursionDepth = 5,
            timeout = Duration.ofSeconds(10)
        )
        
        // When
        val decision = limitManager.checkCollectionLimit(150, context)
        
        // Then
        assertFalse(decision.allowed, "Should reject processing when exceeding limits")
        assertEquals(ProcessingAction.BATCH, decision.suggestedAction)
        assertTrue(decision.reason.contains("exceeds limit"))
    }
    
    @Test
    fun `should suggest skipping for very large collections`() {
        // Given
        val context = ProcessingContext.create(
            maxCollectionSize = 100,
            maxRecursionDepth = 5,
            timeout = Duration.ofSeconds(10)
        )
        
        // When - collection size is more than 2x the limit
        val decision = limitManager.checkCollectionLimit(250, context)
        
        // Then
        assertFalse(decision.allowed, "Should reject very large collections")
        assertEquals(ProcessingAction.SKIP, decision.suggestedAction)
    }
    
    @Test
    fun `should allow processing within recursion depth limits`() {
        // Given
        val context = ProcessingContext.create(
            maxCollectionSize = 100,
            maxRecursionDepth = 5,
            timeout = Duration.ofSeconds(10)
        )
        
        // When
        val decision = limitManager.checkRecursionLimit(3, context)
        
        // Then
        assertTrue(decision.allowed, "Should allow processing within recursion limits")
        assertEquals(ProcessingAction.CONTINUE, decision.suggestedAction)
    }
    
    @Test
    fun `should reject processing when recursion depth exceeds limits`() {
        // Given
        val context = ProcessingContext.create(
            maxCollectionSize = 100,
            maxRecursionDepth = 5,
            timeout = Duration.ofSeconds(10)
        )
        
        // When
        val decision = limitManager.checkRecursionLimit(6, context)
        
        // Then
        assertFalse(decision.allowed, "Should reject processing when exceeding recursion limits")
        assertEquals(ProcessingAction.SKIP, decision.suggestedAction)
        assertTrue(decision.reason.contains("exceeds limit"))
    }
    
    @Test
    fun `should allow processing within time limits`() {
        // Given
        val context = ProcessingContext.create(
            maxCollectionSize = 100,
            maxRecursionDepth = 5,
            timeout = Duration.ofSeconds(10)
        )
        
        // When - check immediately after creation
        val decision = limitManager.checkTimeLimit(context)
        
        // Then
        assertTrue(decision.allowed, "Should allow processing within time limits")
        assertEquals(ProcessingAction.CONTINUE, decision.suggestedAction)
    }
    
    @Test
    fun `should reject processing when time limit is exceeded`() {
        // Given
        val context = ProcessingContext.create(
            maxCollectionSize = 100,
            maxRecursionDepth = 5,
            timeout = Duration.ofMillis(1) // Very short timeout
        )
        
        // When - wait for timeout to be exceeded
        Thread.sleep(10)
        val decision = limitManager.checkTimeLimit(context)
        
        // Then
        assertFalse(decision.allowed, "Should reject processing when time limit exceeded")
        assertEquals(ProcessingAction.ABORT, decision.suggestedAction)
        assertTrue(decision.reason.contains("exceeds timeout"))
    }
    
    @Test
    fun `should check memory pressure`() {
        // When
        val decision = limitManager.checkMemoryPressure()
        
        // Then - should not fail (actual memory pressure depends on system state)
        assertNotNull(decision, "Should return a decision")
        assertNotNull(decision.reason, "Should provide a reason")
    }
    
    @Test
    fun `should record success and failure statistics`() {
        // Given
        val context = ProcessingContext.create()
        
        // When
        limitManager.recordSuccess(context)
        limitManager.recordSuccess(context)
        limitManager.recordFailure(context, RuntimeException("Test failure"))
        
        val stats = limitManager.getStatistics()
        
        // Then
        assertEquals(3, stats.totalProcessed, "Should record total processed")
        assertEquals(2, stats.successfulProcessed, "Should record successful processed")
        assertEquals(1, stats.failedProcessed, "Should record failed processed")
        assertTrue(stats.averageProcessingTime.toMillis() >= 0, "Should have non-negative average time")
    }
    
    @Test
    fun `should provide circuit breaker`() {
        // When
        val circuitBreaker = limitManager.getCircuitBreaker()
        
        // Then
        assertNotNull(circuitBreaker, "Should provide circuit breaker")
        assertTrue(circuitBreaker.allowExecution(), "Circuit breaker should initially allow execution")
    }
    
    @Test
    fun `should handle circuit breaker state changes`() {
        // Given
        val context = ProcessingContext.create()
        val circuitBreaker = limitManager.getCircuitBreaker()
        
        // When - record multiple failures to potentially open circuit
        repeat(10) {
            limitManager.recordFailure(context, RuntimeException("Test failure $it"))
        }
        
        // Then - circuit breaker state might change (depends on configuration)
        val stats = limitManager.getStatistics()
        assertTrue(stats.failedProcessed >= 10, "Should record all failures")
    }
}