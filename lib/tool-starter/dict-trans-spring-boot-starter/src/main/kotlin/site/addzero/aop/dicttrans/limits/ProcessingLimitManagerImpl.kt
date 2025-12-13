package site.addzero.aop.dicttrans.limits

import org.slf4j.LoggerFactory
import site.addzero.aop.dicttrans.circuit.CircuitBreaker
import site.addzero.aop.dicttrans.circuit.CircuitBreakerConfig
import site.addzero.aop.dicttrans.circuit.CircuitBreakerImpl
import site.addzero.aop.dicttrans.config.MemoryManagementProperties
import site.addzero.aop.dicttrans.logging.MemoryManagementLogger
import site.addzero.aop.dicttrans.processing.ProcessingContext
import java.lang.management.ManagementFactory
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

/**
 * Processing limit manager implementation
 *
 * @author zjarlin
 * @since 2025/01/12
 */
class ProcessingLimitManagerImpl(
    private val properties: MemoryManagementProperties
) : ProcessingLimitManager {
    
    private val logger = LoggerFactory.getLogger(ProcessingLimitManagerImpl::class.java)
    
    // Circuit breaker for processing operations
    private val circuitBreaker: CircuitBreaker = CircuitBreakerImpl(
        "ProcessingCircuitBreaker",
        CircuitBreakerConfig(
            failureThreshold = 5,
            successThreshold = 3,
            timeout = Duration.ofMinutes(2),
            slidingWindowSize = 20,
            minimumThroughput = 10
        )
    )
    
    // Statistics
    private val totalProcessed = AtomicLong(0)
    private val successfulProcessed = AtomicLong(0)
    private val failedProcessed = AtomicLong(0)
    private val skippedDueToLimits = AtomicLong(0)
    private val memoryPressureEvents = AtomicLong(0)
    private val circuitBreakerTrips = AtomicLong(0)
    
    // Processing time tracking
    private val processingTimes = mutableListOf<Long>()
    private val maxProcessingTimeHistory = 100
    
    // Memory management
    private val memoryBean = ManagementFactory.getMemoryMXBean()
    
    override fun checkCollectionLimit(collectionSize: Int, context: ProcessingContext): ProcessingDecision {
        if (!properties.processing.enableCircuitBreaker) {
            return ProcessingDecision(true, "Circuit breaker disabled")
        }
        
        // Check circuit breaker first
        if (!circuitBreaker.allowExecution()) {
            circuitBreakerTrips.incrementAndGet()
            return ProcessingDecision(
                false, 
                "Circuit breaker is open",
                ProcessingAction.RETRY_LATER
            )
        }
        
        // Check collection size limit
        if (collectionSize > context.maxCollectionSize) {
            skippedDueToLimits.incrementAndGet()
            
            val action = if (collectionSize > context.maxCollectionSize * 2) {
                ProcessingAction.SKIP
            } else {
                ProcessingAction.BATCH
            }
            
            MemoryManagementLogger.logProcessingLimits(
                context = "collection_size_check",
                collectionSize = collectionSize,
                limitExceeded = "maxCollectionSize",
                action = action.toString()
            )
            
            return ProcessingDecision(
                false,
                "Collection size $collectionSize exceeds limit ${context.maxCollectionSize}",
                action
            )
        }
        
        return ProcessingDecision(true, "Collection size within limits")
    }
    
    override fun checkRecursionLimit(currentDepth: Int, context: ProcessingContext): ProcessingDecision {
        if (currentDepth >= context.maxRecursionDepth) {
            skippedDueToLimits.incrementAndGet()
            
            MemoryManagementLogger.logProcessingLimits(
                context = "recursion_depth_check",
                recursionDepth = currentDepth,
                limitExceeded = "maxRecursionDepth",
                action = ProcessingAction.SKIP.toString()
            )
            
            return ProcessingDecision(
                false,
                "Recursion depth $currentDepth exceeds limit ${context.maxRecursionDepth}",
                ProcessingAction.SKIP
            )
        }
        
        // Warn when approaching limit
        if (currentDepth > context.maxRecursionDepth * 0.8) {
            logger.debug("Recursion depth {} approaching limit {}", currentDepth, context.maxRecursionDepth)
        }
        
        return ProcessingDecision(true, "Recursion depth within limits")
    }
    
    override fun checkTimeLimit(context: ProcessingContext): ProcessingDecision {
        if (context.isTimeoutExceeded()) {
            skippedDueToLimits.incrementAndGet()
            val elapsed = context.getElapsedTime()
            logger.warn("Processing time {} exceeds timeout {}", elapsed, context.timeout)
            
            return ProcessingDecision(
                false,
                "Processing time $elapsed exceeds timeout ${context.timeout}",
                ProcessingAction.ABORT
            )
        }
        
        // Warn when approaching timeout
        val elapsed = context.getElapsedTime()
        if (elapsed > Duration.ofMillis((context.timeout.toMillis() * 0.8).toLong())) {
            logger.debug("Processing time {} approaching timeout {}", elapsed, context.timeout)
        }
        
        return ProcessingDecision(true, "Processing time within limits")
    }
    
    override fun checkMemoryPressure(): ProcessingDecision {
        val heapMemory = memoryBean.heapMemoryUsage
        val heapUsagePercent = heapMemory.used.toDouble() / heapMemory.max
        
        // Check heap pressure
        if (heapUsagePercent > properties.monitoring.heapWarningThreshold) {
            memoryPressureEvents.incrementAndGet()
            logger.warn("High heap memory usage: {:.2f}%", heapUsagePercent * 100)
            
            return if (heapUsagePercent > 0.95) {
                ProcessingDecision(
                    false,
                    "Critical heap memory usage: ${String.format("%.2f", heapUsagePercent * 100)}%",
                    ProcessingAction.ABORT
                )
            } else {
                ProcessingDecision(
                    false,
                    "High heap memory usage: ${String.format("%.2f", heapUsagePercent * 100)}%",
                    ProcessingAction.SKIP
                )
            }
        }
        
        // Check metaspace if available
        try {
            val metaspaceUsage = ManagementFactory.getMemoryPoolMXBeans()
                .find { it.name.contains("Metaspace") }
                ?.usage
            
            if (metaspaceUsage != null && metaspaceUsage.max > 0) {
                val metaspacePercent = metaspaceUsage.used.toDouble() / metaspaceUsage.max
                if (metaspacePercent > properties.monitoring.metaspaceWarningThreshold) {
                    memoryPressureEvents.incrementAndGet()
                    logger.warn("High metaspace usage: {:.2f}%", metaspacePercent * 100)
                    
                    return ProcessingDecision(
                        false,
                        "High metaspace usage: ${String.format("%.2f", metaspacePercent * 100)}%",
                        ProcessingAction.SKIP
                    )
                }
            }
        } catch (e: Exception) {
            logger.debug("Could not check metaspace usage", e)
        }
        
        return ProcessingDecision(true, "Memory usage within acceptable limits")
    }
    
    override fun getCircuitBreaker(): CircuitBreaker = circuitBreaker
    
    override fun recordSuccess(context: ProcessingContext) {
        totalProcessed.incrementAndGet()
        successfulProcessed.incrementAndGet()
        
        // Record processing time
        val processingTime = context.getElapsedTime().toMillis()
        synchronized(processingTimes) {
            processingTimes.add(processingTime)
            if (processingTimes.size > maxProcessingTimeHistory) {
                processingTimes.removeAt(0)
            }
        }
        
        circuitBreaker.recordSuccess()
        logger.trace("Recorded successful processing in {}ms", processingTime)
    }
    
    override fun recordFailure(context: ProcessingContext, exception: Throwable) {
        totalProcessed.incrementAndGet()
        failedProcessed.incrementAndGet()
        
        circuitBreaker.recordFailure(exception)
        logger.debug("Recorded processing failure: {}", exception.message)
    }
    
    override fun getStatistics(): ProcessingStatistics {
        val avgProcessingTime = synchronized(processingTimes) {
            if (processingTimes.isEmpty()) {
                Duration.ZERO
            } else {
                Duration.ofMillis(processingTimes.average().toLong())
            }
        }
        
        return ProcessingStatistics(
            totalProcessed = totalProcessed.get(),
            successfulProcessed = successfulProcessed.get(),
            failedProcessed = failedProcessed.get(),
            skippedDueToLimits = skippedDueToLimits.get(),
            averageProcessingTime = avgProcessingTime,
            memoryPressureEvents = memoryPressureEvents.get(),
            circuitBreakerTrips = circuitBreakerTrips.get()
        )
    }
}