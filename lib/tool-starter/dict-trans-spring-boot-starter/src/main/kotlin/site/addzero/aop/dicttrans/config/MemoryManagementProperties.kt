package site.addzero.aop.dicttrans.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * Memory management configuration properties for dict translation AOP
 *
 * @author zjarlin
 * @since 2025/01/12
 */
@ConfigurationProperties(prefix = "addzero.dict.memory")
data class MemoryManagementProperties(
    val byteBuddyCache: CacheProperties = CacheProperties(),
    val reflectionCache: CacheProperties = CacheProperties(),
    val processing: ProcessingProperties = ProcessingProperties(),
    val monitoring: MonitoringProperties = MonitoringProperties()
)

/**
 * Cache configuration properties
 */
data class CacheProperties(
    val maxSize: Long = 1000,
    val expireAfterAccess: Duration = Duration.ofHours(1),
    val expireAfterWrite: Duration = Duration.ofHours(2),
    val enableMetrics: Boolean = true
)

/**
 * Processing limits and circuit breaker configuration
 */
data class ProcessingProperties(
    val maxCollectionSize: Int = 1000,
    val maxRecursionDepth: Int = 10,
    val processingTimeout: Duration = Duration.ofSeconds(30),
    val enableCircuitBreaker: Boolean = true
)

/**
 * Memory monitoring configuration
 */
data class MonitoringProperties(
    val metaspaceWarningThreshold: Double = 0.8,
    val heapWarningThreshold: Double = 0.85,
    val monitoringInterval: Duration = Duration.ofMinutes(1),
    val enableJmxMetrics: Boolean = true
)