package site.addzero.aop.dicttrans.circuit

import java.time.Duration

/**
 * Circuit breaker interface for protecting against resource exhaustion
 *
 * @author zjarlin
 * @since 2025/01/12
 */
interface CircuitBreaker {
    
    /**
     * Check if circuit breaker allows execution
     * 
     * @return true if execution is allowed, false if circuit is open
     */
    fun allowExecution(): Boolean
    
    /**
     * Record a successful execution
     */
    fun recordSuccess()
    
    /**
     * Record a failed execution
     * 
     * @param exception The exception that caused the failure
     */
    fun recordFailure(exception: Throwable)
    
    /**
     * Record a timeout
     */
    fun recordTimeout()
    
    /**
     * Get current circuit breaker state
     * 
     * @return Current state (CLOSED, OPEN, HALF_OPEN)
     */
    fun getState(): CircuitBreakerState
    
    /**
     * Get circuit breaker statistics
     * 
     * @return Current statistics
     */
    fun getStatistics(): CircuitBreakerStatistics
    
    /**
     * Reset circuit breaker to closed state
     */
    fun reset()
    
    /**
     * Force circuit breaker to open state
     */
    fun forceOpen()
}

/**
 * Circuit breaker states
 */
enum class CircuitBreakerState {
    CLOSED,    // Normal operation
    OPEN,      // Circuit is open, rejecting calls
    HALF_OPEN  // Testing if service has recovered
}

/**
 * Circuit breaker statistics
 */
data class CircuitBreakerStatistics(
    val state: CircuitBreakerState,
    val totalCalls: Long,
    val successfulCalls: Long,
    val failedCalls: Long,
    val timeoutCalls: Long,
    val failureRate: Double,
    val lastFailureTime: Long?,
    val lastSuccessTime: Long?
)

/**
 * Circuit breaker configuration
 */
data class CircuitBreakerConfig(
    val failureThreshold: Int = 5,           // Number of failures to open circuit
    val successThreshold: Int = 3,           // Number of successes to close circuit in half-open state
    val timeout: Duration = Duration.ofSeconds(60), // Time to wait before trying half-open
    val slidingWindowSize: Int = 10,         // Size of sliding window for failure rate calculation
    val minimumThroughput: Int = 5           // Minimum calls before calculating failure rate
)