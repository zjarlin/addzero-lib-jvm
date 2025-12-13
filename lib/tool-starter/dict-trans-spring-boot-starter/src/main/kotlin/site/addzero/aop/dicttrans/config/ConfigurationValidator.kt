package site.addzero.aop.dicttrans.config

import org.slf4j.LoggerFactory
import java.time.Duration

/**
 * Configuration validation utility for memory management settings
 *
 * @author zjarlin
 * @since 2025/01/12
 */
object ConfigurationValidator {
    
    private val logger = LoggerFactory.getLogger(ConfigurationValidator::class.java)
    
    /**
     * Apply safe defaults and validate memory management properties
     */
    fun validateAndApplyDefaults(properties: MemoryManagementProperties): MemoryManagementProperties {
        logger.debug("Validating and applying defaults to memory management properties")
        
        val validatedByteBuddyCache = validateCacheProperties(
            "ByteBuddy", 
            properties.byteBuddyCache,
            defaultMaxSize = 1000L,
            defaultExpireAfterAccess = Duration.ofHours(1),
            defaultExpireAfterWrite = Duration.ofHours(2)
        )
        
        val validatedReflectionCache = validateCacheProperties(
            "Reflection", 
            properties.reflectionCache,
            defaultMaxSize = 500L,
            defaultExpireAfterAccess = Duration.ofMinutes(30),
            defaultExpireAfterWrite = Duration.ofHours(1)
        )
        
        val validatedProcessing = validateProcessingProperties(properties.processing)
        val validatedMonitoring = validateMonitoringProperties(properties.monitoring)
        
        return properties.copy(
            byteBuddyCache = validatedByteBuddyCache,
            reflectionCache = validatedReflectionCache,
            processing = validatedProcessing,
            monitoring = validatedMonitoring
        )
    }
    
    private fun validateCacheProperties(
        cacheName: String,
        cacheProps: CacheProperties,
        defaultMaxSize: Long,
        defaultExpireAfterAccess: Duration,
        defaultExpireAfterWrite: Duration
    ): CacheProperties {
        val maxSize = if (cacheProps.maxSize <= 0) {
            logger.warn("Invalid {} cache maxSize: {}, using default: {}", cacheName, cacheProps.maxSize, defaultMaxSize)
            defaultMaxSize
        } else {
            cacheProps.maxSize
        }
        
        val expireAfterAccess = if (cacheProps.expireAfterAccess.isNegative) {
            logger.warn("Invalid {} cache expireAfterAccess: {}, using default: {}", cacheName, cacheProps.expireAfterAccess, defaultExpireAfterAccess)
            defaultExpireAfterAccess
        } else {
            cacheProps.expireAfterAccess
        }
        
        val expireAfterWrite = if (cacheProps.expireAfterWrite.isNegative) {
            logger.warn("Invalid {} cache expireAfterWrite: {}, using default: {}", cacheName, cacheProps.expireAfterWrite, defaultExpireAfterWrite)
            defaultExpireAfterWrite
        } else {
            cacheProps.expireAfterWrite
        }
        
        return cacheProps.copy(
            maxSize = maxSize,
            expireAfterAccess = expireAfterAccess,
            expireAfterWrite = expireAfterWrite
        )
    }
    
    private fun validateProcessingProperties(processingProps: ProcessingProperties): ProcessingProperties {
        val maxCollectionSize = if (processingProps.maxCollectionSize <= 0) {
            logger.warn("Invalid maxCollectionSize: {}, using default: 1000", processingProps.maxCollectionSize)
            1000
        } else {
            processingProps.maxCollectionSize
        }
        
        val maxRecursionDepth = if (processingProps.maxRecursionDepth <= 0) {
            logger.warn("Invalid maxRecursionDepth: {}, using default: 10", processingProps.maxRecursionDepth)
            10
        } else {
            processingProps.maxRecursionDepth
        }
        
        val processingTimeout = if (processingProps.processingTimeout.isNegative) {
            logger.warn("Invalid processingTimeout: {}, using default: 30s", processingProps.processingTimeout)
            Duration.ofSeconds(30)
        } else {
            processingProps.processingTimeout
        }
        
        return processingProps.copy(
            maxCollectionSize = maxCollectionSize,
            maxRecursionDepth = maxRecursionDepth,
            processingTimeout = processingTimeout
        )
    }
    
    private fun validateMonitoringProperties(monitoringProps: MonitoringProperties): MonitoringProperties {
        val metaspaceThreshold = if (monitoringProps.metaspaceWarningThreshold !in 0.0..1.0) {
            logger.warn("Invalid metaspaceWarningThreshold: {}, using default: 0.8", monitoringProps.metaspaceWarningThreshold)
            0.8
        } else {
            monitoringProps.metaspaceWarningThreshold
        }
        
        val heapThreshold = if (monitoringProps.heapWarningThreshold !in 0.0..1.0) {
            logger.warn("Invalid heapWarningThreshold: {}, using default: 0.85", monitoringProps.heapWarningThreshold)
            0.85
        } else {
            monitoringProps.heapWarningThreshold
        }
        
        val monitoringInterval = if (monitoringProps.monitoringInterval.isNegative) {
            logger.warn("Invalid monitoringInterval: {}, using default: 1m", monitoringProps.monitoringInterval)
            Duration.ofMinutes(1)
        } else {
            monitoringProps.monitoringInterval
        }
        
        return monitoringProps.copy(
            metaspaceWarningThreshold = metaspaceThreshold,
            heapWarningThreshold = heapThreshold,
            monitoringInterval = monitoringInterval
        )
    }
}