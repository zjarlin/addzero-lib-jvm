package site.addzero.aop.dicttrans.config

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import site.addzero.aop.dicttrans.cache.ByteBuddyCacheManager
import site.addzero.aop.dicttrans.cache.ByteBuddyCacheManagerImpl
import site.addzero.aop.dicttrans.cache.ReflectionCacheManager
import site.addzero.aop.dicttrans.cache.ReflectionCacheManagerImpl
import site.addzero.aop.dicttrans.limits.ProcessingLimitManager
import site.addzero.aop.dicttrans.limits.ProcessingLimitManagerImpl
import site.addzero.aop.dicttrans.monitoring.MemoryMonitor
import site.addzero.aop.dicttrans.monitoring.MemoryMonitorImpl
import site.addzero.aop.dicttrans.tracking.WeakReferenceTracker
import site.addzero.aop.dicttrans.tracking.WeakReferenceTrackerImpl
import site.addzero.aop.dicttrans.logging.MemoryManagementLogger
import javax.annotation.PostConstruct

/**
 * Auto-configuration for memory management components
 *
 * @author zjarlin
 * @since 2025/01/12
 */
@Configuration
@EnableConfigurationProperties(MemoryManagementProperties::class)
@ComponentScan(basePackages = [
    "site.addzero.aop.dicttrans.inter",
    "site.addzero.aop.dicttrans.lifecycle"
])
@ConditionalOnProperty(
    prefix = "addzero.dict.memory",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true
)
open class MemoryManagementAutoConfiguration(
    private val properties: MemoryManagementProperties
) {
    
    private val logger = LoggerFactory.getLogger(MemoryManagementAutoConfiguration::class.java)
    
    @PostConstruct
    fun validateConfiguration() {
        MemoryManagementLogger.logSystemEvent("startup", mapOf(
            "event" to "configuration_loading",
            "properties" to properties.toString()
        ))
        
        // Validate cache properties
        validateCacheProperties("ByteBuddy", properties.byteBuddyCache)
        validateCacheProperties("Reflection", properties.reflectionCache)
        
        // Validate processing properties
        validateProcessingProperties(properties.processing)
        
        // Validate monitoring properties
        validateMonitoringProperties(properties.monitoring)
        
        MemoryManagementLogger.logSystemEvent("configuration_loaded", mapOf(
            "status" to "success",
            "byteBuddyCacheMaxSize" to properties.byteBuddyCache.maxSize,
            "reflectionCacheMaxSize" to properties.reflectionCache.maxSize,
            "maxCollectionSize" to properties.processing.maxCollectionSize,
            "maxRecursionDepth" to properties.processing.maxRecursionDepth,
            "monitoringEnabled" to properties.monitoring.enableJmxMetrics
        ))
    }
    
    private fun validateCacheProperties(cacheName: String, cacheProps: CacheProperties) {
        require(cacheProps.maxSize > 0) { 
            "$cacheName cache maxSize must be positive, got: ${cacheProps.maxSize}" 
        }
        require(!cacheProps.expireAfterAccess.isNegative) { 
            "$cacheName cache expireAfterAccess must not be negative, got: ${cacheProps.expireAfterAccess}" 
        }
        require(!cacheProps.expireAfterWrite.isNegative) { 
            "$cacheName cache expireAfterWrite must not be negative, got: ${cacheProps.expireAfterWrite}" 
        }
        logger.debug("$cacheName cache configuration validated: maxSize={}, expireAfterAccess={}, expireAfterWrite={}", 
            cacheProps.maxSize, cacheProps.expireAfterAccess, cacheProps.expireAfterWrite)
    }
    
    private fun validateProcessingProperties(processingProps: ProcessingProperties) {
        require(processingProps.maxCollectionSize > 0) { 
            "Processing maxCollectionSize must be positive, got: ${processingProps.maxCollectionSize}" 
        }
        require(processingProps.maxRecursionDepth > 0) { 
            "Processing maxRecursionDepth must be positive, got: ${processingProps.maxRecursionDepth}" 
        }
        require(!processingProps.processingTimeout.isNegative) { 
            "Processing timeout must not be negative, got: ${processingProps.processingTimeout}" 
        }
        logger.debug("Processing configuration validated: maxCollectionSize={}, maxRecursionDepth={}, timeout={}", 
            processingProps.maxCollectionSize, processingProps.maxRecursionDepth, processingProps.processingTimeout)
    }
    
    private fun validateMonitoringProperties(monitoringProps: MonitoringProperties) {
        require(monitoringProps.metaspaceWarningThreshold in 0.0..1.0) { 
            "Metaspace warning threshold must be between 0.0 and 1.0, got: ${monitoringProps.metaspaceWarningThreshold}" 
        }
        require(monitoringProps.heapWarningThreshold in 0.0..1.0) { 
            "Heap warning threshold must be between 0.0 and 1.0, got: ${monitoringProps.heapWarningThreshold}" 
        }
        require(!monitoringProps.monitoringInterval.isNegative) { 
            "Monitoring interval must not be negative, got: ${monitoringProps.monitoringInterval}" 
        }
        logger.debug("Monitoring configuration validated: metaspaceThreshold={}, heapThreshold={}, interval={}", 
            monitoringProps.metaspaceWarningThreshold, monitoringProps.heapWarningThreshold, monitoringProps.monitoringInterval)
    }
    
    @Bean
    fun byteBuddyCacheManager(): ByteBuddyCacheManager {
        logger.info("Creating ByteBuddyCacheManager with configuration: {}", properties.byteBuddyCache)
        return ByteBuddyCacheManagerImpl(properties)
    }
    
    @Bean
    fun reflectionCacheManager(): ReflectionCacheManager {
        logger.info("Creating ReflectionCacheManager with configuration: {}", properties.reflectionCache)
        return ReflectionCacheManagerImpl(properties)
    }
    
    @Bean
    fun weakReferenceTracker(): WeakReferenceTracker {
        logger.info("Creating WeakReferenceTracker for memory-safe object tracking")
        return WeakReferenceTrackerImpl()
    }
    
    @Bean
    fun processingLimitManager(): ProcessingLimitManager {
        logger.info("Creating ProcessingLimitManager with configuration: {}", properties.processing)
        return ProcessingLimitManagerImpl(properties)
    }
    
    @Bean
    fun memoryMonitor(): MemoryMonitor {
        logger.info("Creating MemoryMonitor with configuration: {}", properties.monitoring)
        return MemoryMonitorImpl(properties, byteBuddyCacheManager(), reflectionCacheManager(), weakReferenceTracker())
    }
}