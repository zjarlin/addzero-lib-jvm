package site.addzero.aop.dicttrans.limits

import site.addzero.aop.dicttrans.circuit.CircuitBreaker
import site.addzero.aop.dicttrans.processing.ProcessingContext
import java.time.Duration

/**
 * Processing limit manager interface for enforcing resource limits
 *
 * @author zjarlin
 * @since 2025/01/12
 */
interface ProcessingLimitManager {
    
    /**
     * Check if collection processing is allowed
     * 
     * @param collectionSize Size of the collection to process
     * @param context Processing context
     * @return ProcessingDecision indicating whether to proceed
     */
    fun checkCollectionLimit(collectionSize: Int, context: ProcessingContext): ProcessingDecision
    
    /**
     * Check if recursion is allowed
     * 
     * @param currentDepth Current recursion depth
     * @param context Processing context
     * @return ProcessingDecision indicating whether to proceed
     */
    fun checkRecursionLimit(currentDepth: Int, context: ProcessingContext): ProcessingDecision
    
    /**
     * Check if processing time limit is exceeded
     * 
     * @param context Processing context
     * @return ProcessingDecision indicating whether to proceed
     */
    fun checkTimeLimit(context: ProcessingContext): ProcessingDecision
    
    /**
     * Check if memory pressure requires limiting processing
     * 
     * @return ProcessingDecision indicating whether to proceed
     */
    fun checkMemoryPressure(): ProcessingDecision
    
    /**
     * Get circuit breaker for processing operations
     * 
     * @return Circuit breaker instance
     */
    fun getCircuitBreaker(): CircuitBreaker
    
    /**
     * Record successful processing
     * 
     * @param context Processing context
     */
    fun recordSuccess(context: ProcessingContext)
    
    /**
     * Record failed processing
     * 
     * @param context Processing context
     * @param exception The exception that caused failure
     */
    fun recordFailure(context: ProcessingContext, exception: Throwable)
    
    /**
     * Get processing statistics
     * 
     * @return Current processing statistics
     */
    fun getStatistics(): ProcessingStatistics
}

/**
 * Processing decision result
 */
data class ProcessingDecision(
    val allowed: Boolean,
    val reason: String,
    val suggestedAction: ProcessingAction = ProcessingAction.CONTINUE
)

/**
 * Suggested processing actions
 */
enum class ProcessingAction {
    CONTINUE,           // Continue normal processing
    SKIP,              // Skip this item/collection
    BATCH,             // Process in smaller batches
    ABORT,             // Abort entire operation
    RETRY_LATER        // Retry after some time
}

/**
 * Processing statistics
 */
data class ProcessingStatistics(
    val totalProcessed: Long,
    val successfulProcessed: Long,
    val failedProcessed: Long,
    val skippedDueToLimits: Long,
    val averageProcessingTime: Duration,
    val memoryPressureEvents: Long,
    val circuitBreakerTrips: Long
)