package site.addzero.aop.dicttrans.processing

import java.time.Duration
import java.time.Instant

/**
 * Processing context for tracking operation limits and timeouts
 *
 * @author zjarlin
 * @since 2025/01/12
 */
data class ProcessingContext(
    val maxCollectionSize: Int,
    val maxRecursionDepth: Int,
    val currentDepth: Int = 0,
    val processingStartTime: Instant = Instant.now(),
    val timeout: Duration,
    val circuitBreakerEnabled: Boolean = true
) {
    
    fun isTimeoutExceeded(): Boolean {
        return Duration.between(processingStartTime, Instant.now()) > timeout
    }
    
    fun isRecursionLimitExceeded(): Boolean {
        return currentDepth >= maxRecursionDepth
    }
    
    fun isCollectionTooLarge(collectionSize: Int): Boolean {
        return collectionSize > maxCollectionSize
    }
    
    fun incrementDepth(): ProcessingContext {
        return copy(currentDepth = currentDepth + 1)
    }
    
    fun getElapsedTime(): Duration {
        return Duration.between(processingStartTime, Instant.now())
    }
    
    companion object {
        fun create(
            maxCollectionSize: Int = 1000,
            maxRecursionDepth: Int = 10,
            timeout: Duration = Duration.ofSeconds(30),
            circuitBreakerEnabled: Boolean = true
        ): ProcessingContext {
            return ProcessingContext(
                maxCollectionSize = maxCollectionSize,
                maxRecursionDepth = maxRecursionDepth,
                timeout = timeout,
                circuitBreakerEnabled = circuitBreakerEnabled
            )
        }
    }
}