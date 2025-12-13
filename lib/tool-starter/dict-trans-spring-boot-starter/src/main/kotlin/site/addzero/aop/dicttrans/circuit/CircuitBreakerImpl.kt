package site.addzero.aop.dicttrans.circuit

import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/**
 * Circuit breaker implementation with sliding window failure detection
 *
 * @author zjarlin
 * @since 2025/01/12
 */
class CircuitBreakerImpl(
    private val name: String,
    private val config: CircuitBreakerConfig
) : CircuitBreaker {
    
    private val logger = LoggerFactory.getLogger(CircuitBreakerImpl::class.java)
    
    private val state = AtomicReference(CircuitBreakerState.CLOSED)
    private val lastFailureTime = AtomicLong(0)
    private val lastSuccessTime = AtomicLong(0)
    private val consecutiveFailures = AtomicLong(0)
    private val consecutiveSuccesses = AtomicLong(0)
    
    // Sliding window for tracking calls
    private val callWindow = mutableListOf<CallResultWithTime>()
    private val lock = ReentrantReadWriteLock()
    
    // Statistics
    private val totalCalls = AtomicLong(0)
    private val successfulCalls = AtomicLong(0)
    private val failedCalls = AtomicLong(0)
    private val timeoutCalls = AtomicLong(0)
    
    override fun allowExecution(): Boolean {
        val currentState = state.get()
        
        return when (currentState) {
            CircuitBreakerState.CLOSED -> true
            CircuitBreakerState.OPEN -> {
                // Check if timeout has elapsed to try half-open
                val timeSinceLastFailure = System.currentTimeMillis() - lastFailureTime.get()
                if (timeSinceLastFailure >= config.timeout.toMillis()) {
                    // Try to transition to half-open
                    if (state.compareAndSet(CircuitBreakerState.OPEN, CircuitBreakerState.HALF_OPEN)) {
                        logger.info("Circuit breaker '{}' transitioning from OPEN to HALF_OPEN", name)
                        return true
                    }
                }
                false
            }
            CircuitBreakerState.HALF_OPEN -> {
                // Allow limited calls to test if service has recovered
                true
            }
        }
    }
    
    override fun recordSuccess() {
        totalCalls.incrementAndGet()
        successfulCalls.incrementAndGet()
        lastSuccessTime.set(System.currentTimeMillis())
        
        lock.write {
            addCallResult(CallResult.SUCCESS)
        }
        
        val currentState = state.get()
        when (currentState) {
            CircuitBreakerState.HALF_OPEN -> {
                val successes = consecutiveSuccesses.incrementAndGet()
                if (successes >= config.successThreshold) {
                    // Transition back to closed
                    if (state.compareAndSet(CircuitBreakerState.HALF_OPEN, CircuitBreakerState.CLOSED)) {
                        logger.info("Circuit breaker '{}' transitioning from HALF_OPEN to CLOSED after {} successes", 
                            name, successes)
                        consecutiveSuccesses.set(0)
                        consecutiveFailures.set(0)
                    }
                }
            }
            CircuitBreakerState.CLOSED -> {
                consecutiveFailures.set(0)
            }
            else -> { /* No action needed for OPEN state */ }
        }
    }
    
    override fun recordFailure(exception: Throwable) {
        totalCalls.incrementAndGet()
        failedCalls.incrementAndGet()
        lastFailureTime.set(System.currentTimeMillis())
        
        lock.write {
            addCallResult(CallResult.FAILURE)
        }
        
        logger.debug("Circuit breaker '{}' recorded failure: {}", name, exception.message)
        
        val currentState = state.get()
        when (currentState) {
            CircuitBreakerState.CLOSED -> {
                val failures = consecutiveFailures.incrementAndGet()
                if (shouldOpenCircuit()) {
                    if (state.compareAndSet(CircuitBreakerState.CLOSED, CircuitBreakerState.OPEN)) {
                        logger.warn("Circuit breaker '{}' transitioning from CLOSED to OPEN after {} failures", 
                            name, failures)
                    }
                }
            }
            CircuitBreakerState.HALF_OPEN -> {
                // Any failure in half-open state should open the circuit
                if (state.compareAndSet(CircuitBreakerState.HALF_OPEN, CircuitBreakerState.OPEN)) {
                    logger.warn("Circuit breaker '{}' transitioning from HALF_OPEN to OPEN due to failure", name)
                    consecutiveSuccesses.set(0)
                }
            }
            else -> { /* Already open */ }
        }
    }
    
    override fun recordTimeout() {
        totalCalls.incrementAndGet()
        timeoutCalls.incrementAndGet()
        
        lock.write {
            addCallResult(CallResult.TIMEOUT)
        }
        
        // Treat timeout as failure
        recordFailure(RuntimeException("Operation timeout"))
    }
    
    override fun getState(): CircuitBreakerState = state.get()
    
    override fun getStatistics(): CircuitBreakerStatistics {
        val total = totalCalls.get()
        val successful = successfulCalls.get()
        val failed = failedCalls.get()
        val timeout = timeoutCalls.get()
        
        val failureRate = if (total > 0) {
            (failed + timeout).toDouble() / total
        } else {
            0.0
        }
        
        return CircuitBreakerStatistics(
            state = state.get(),
            totalCalls = total,
            successfulCalls = successful,
            failedCalls = failed,
            timeoutCalls = timeout,
            failureRate = failureRate,
            lastFailureTime = lastFailureTime.get().takeIf { it > 0 },
            lastSuccessTime = lastSuccessTime.get().takeIf { it > 0 }
        )
    }
    
    override fun reset() {
        logger.info("Resetting circuit breaker '{}'", name)
        state.set(CircuitBreakerState.CLOSED)
        consecutiveFailures.set(0)
        consecutiveSuccesses.set(0)
        
        lock.write {
            callWindow.clear()
        }
    }
    
    override fun forceOpen() {
        logger.warn("Forcing circuit breaker '{}' to OPEN state", name)
        state.set(CircuitBreakerState.OPEN)
        lastFailureTime.set(System.currentTimeMillis())
    }
    
    /**
     * Add call result to sliding window
     */
    private fun addCallResult(result: CallResult) {
        val now = Instant.now()
        callWindow.add(CallResultWithTime(result, now))
        
        // Remove old entries outside the sliding window
        if (callWindow.size > config.slidingWindowSize) {
            callWindow.removeAt(0)
        }
    }
    
    /**
     * Check if circuit should be opened based on failure rate
     */
    private fun shouldOpenCircuit(): Boolean {
        lock.read {
            if (callWindow.size < config.minimumThroughput) {
                return false
            }
            
            val failures = callWindow.takeLast(config.slidingWindowSize).count { 
                it.result == CallResult.FAILURE || it.result == CallResult.TIMEOUT 
            }
            
            val failureRate = failures.toDouble() / minOf(callWindow.size, config.slidingWindowSize)
            val threshold = config.failureThreshold.toDouble() / config.slidingWindowSize
            
            return failureRate >= threshold
        }
    }
    
    /**
     * Call result enumeration
     */
    private enum class CallResult {
        SUCCESS, FAILURE, TIMEOUT
    }
    
    /**
     * Call result with timestamp
     */
    private data class CallResultWithTime(
        val result: CallResult,
        val timestamp: Instant
    )
}