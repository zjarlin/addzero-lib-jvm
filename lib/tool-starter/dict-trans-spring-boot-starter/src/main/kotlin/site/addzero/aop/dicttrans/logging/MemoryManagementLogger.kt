package site.addzero.aop.dicttrans.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import site.addzero.aop.dicttrans.monitoring.CacheStatistics
import site.addzero.aop.dicttrans.monitoring.MemoryUsage
import site.addzero.aop.dicttrans.monitoring.MemoryPressureLevel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Centralized logging system for memory management operations
 * Provides structured logging with consistent formatting and context
 *
 * @author zjarlin
 * @since 2025/01/12
 */
class MemoryManagementLogger private constructor() {
    
    companion object {
        private val logger: Logger = LoggerFactory.getLogger("MemoryManagement")
        private val performanceLogger: Logger = LoggerFactory.getLogger("MemoryManagement.Performance")
        private val diagnosticLogger: Logger = LoggerFactory.getLogger("MemoryManagement.Diagnostic")
        
        private const val CACHE_OPERATION_KEY = "cache.operation"
        private const val CACHE_NAME_KEY = "cache.name"
        private const val MEMORY_COMPONENT_KEY = "memory.component"
        private const val PROCESSING_CONTEXT_KEY = "processing.context"
        
        /**
         * Log cache operation with structured context
         */
        fun logCacheOperation(
            cacheName: String,
            operation: String,
            key: String? = null,
            hit: Boolean? = null,
            executionTimeMs: Long? = null,
            additionalContext: Map<String, Any> = emptyMap()
        ) {
            try {
                MDC.put(CACHE_OPERATION_KEY, operation)
                MDC.put(CACHE_NAME_KEY, cacheName)
                
                val contextBuilder = StringBuilder()
                contextBuilder.append("operation=$operation, cache=$cacheName")
                
                key?.let { contextBuilder.append(", key=$it") }
                hit?.let { contextBuilder.append(", hit=$it") }
                executionTimeMs?.let { contextBuilder.append(", executionTime=${it}ms") }
                
                additionalContext.forEach { (k, v) ->
                    contextBuilder.append(", $k=$v")
                }
                
                when {
                    hit == true && executionTimeMs != null && executionTimeMs < 1 -> {
                        logger.debug("Cache hit: {}", contextBuilder.toString())
                    }
                    hit == false -> {
                        logger.info("Cache miss: {}", contextBuilder.toString())
                    }
                    executionTimeMs != null && executionTimeMs > 100 -> {
                        logger.warn("Slow cache operation: {}", contextBuilder.toString())
                    }
                    else -> {
                        logger.debug("Cache operation: {}", contextBuilder.toString())
                    }
                }
                
                // Performance logging
                if (executionTimeMs != null) {
                    performanceLogger.info("cache_operation_time,cache={},operation={},time_ms={}", 
                        cacheName, operation, executionTimeMs)
                }
                
            } finally {
                MDC.remove(CACHE_OPERATION_KEY)
                MDC.remove(CACHE_NAME_KEY)
            }
        }
        
        /**
         * Log cache statistics with performance metrics
         */
        fun logCacheStatistics(cacheName: String, statistics: CacheStatistics) {
            try {
                MDC.put(CACHE_NAME_KEY, cacheName)
                
                val hitRate = if (statistics.totalRequests > 0) {
                    (statistics.hitCount.toDouble() / statistics.totalRequests * 100).let { "%.2f%%".format(it) }
                } else "N/A"
                
                logger.info("Cache statistics: cache={}, size={}, hitRate={}, requests={}, hits={}, misses={}, evictions={}", 
                    cacheName, statistics.size, hitRate, statistics.totalRequests, 
                    statistics.hitCount, statistics.missCount, statistics.evictionCount)
                
                // Performance metrics logging
                performanceLogger.info("cache_statistics,cache={},size={},hit_rate={},requests={},hits={},misses={},evictions={}", 
                    cacheName, statistics.size, hitRate, statistics.totalRequests, 
                    statistics.hitCount, statistics.missCount, statistics.evictionCount)
                
                // Warn on poor performance
                val actualHitRate = if (statistics.totalRequests > 0) {
                    statistics.hitCount.toDouble() / statistics.totalRequests
                } else 0.0
                
                when {
                    statistics.totalRequests > 100 && actualHitRate < 0.5 -> {
                        logger.warn("Low cache hit rate detected: cache={}, hitRate={}, consider reviewing cache configuration", 
                            cacheName, hitRate)
                    }
                    statistics.evictionCount > statistics.hitCount -> {
                        logger.warn("High eviction rate detected: cache={}, evictions={}, hits={}, consider increasing cache size", 
                            cacheName, statistics.evictionCount, statistics.hitCount)
                    }
                }
                
            } finally {
                MDC.remove(CACHE_NAME_KEY)
            }
        }
        
        /**
         * Log memory usage and pressure information
         */
        fun logMemoryUsage(component: String, memoryUsage: MemoryUsage, pressureLevel: MemoryPressureLevel) {
            try {
                MDC.put(MEMORY_COMPONENT_KEY, component)
                
                val heapUsedMB = memoryUsage.heapUsed / (1024 * 1024)
                val heapMaxMB = memoryUsage.heapMax / (1024 * 1024)
                val heapUsagePercent = "%.2f%%".format(memoryUsage.heapUsagePercent * 100)
                
                val metaspaceUsedMB = memoryUsage.metaspaceUsed / (1024 * 1024)
                val metaspaceMaxMB = if (memoryUsage.metaspaceMax > 0) memoryUsage.metaspaceMax / (1024 * 1024) else -1
                val metaspaceUsagePercent = "%.2f%%".format(memoryUsage.metaspaceUsagePercent * 100)
                
                val logMessage = "Memory usage: component={}, pressure={}, heap={}MB/{}MB ({}), metaspace={}MB/{}MB ({})"
                val logArgs = arrayOf(component, pressureLevel, heapUsedMB, heapMaxMB, heapUsagePercent, 
                    metaspaceUsedMB, if (metaspaceMaxMB > 0) "${metaspaceMaxMB}MB" else "unlimited", metaspaceUsagePercent)
                
                when (pressureLevel) {
                    MemoryPressureLevel.LOW -> logger.debug(logMessage, *logArgs)
                    MemoryPressureLevel.MEDIUM -> logger.info(logMessage, *logArgs)
                    MemoryPressureLevel.HIGH -> logger.warn(logMessage, *logArgs)
                    MemoryPressureLevel.CRITICAL -> logger.error(logMessage, *logArgs)
                }
                
                // Performance metrics logging
                performanceLogger.info("memory_usage,component={},pressure={},heap_used_mb={},heap_max_mb={},heap_usage_percent={},metaspace_used_mb={},metaspace_max_mb={},metaspace_usage_percent={}", 
                    component, pressureLevel, heapUsedMB, heapMaxMB, memoryUsage.heapUsagePercent * 100,
                    metaspaceUsedMB, metaspaceMaxMB, memoryUsage.metaspaceUsagePercent * 100)
                
            } finally {
                MDC.remove(MEMORY_COMPONENT_KEY)
            }
        }
        
        /**
         * Log processing context and limits
         */
        fun logProcessingLimits(
            context: String,
            collectionSize: Int? = null,
            recursionDepth: Int? = null,
            processingTimeMs: Long? = null,
            limitExceeded: String? = null,
            action: String? = null
        ) {
            try {
                MDC.put(PROCESSING_CONTEXT_KEY, context)
                
                val contextBuilder = StringBuilder()
                contextBuilder.append("context=$context")
                
                collectionSize?.let { contextBuilder.append(", collectionSize=$it") }
                recursionDepth?.let { contextBuilder.append(", recursionDepth=$it") }
                processingTimeMs?.let { contextBuilder.append(", processingTime=${it}ms") }
                limitExceeded?.let { contextBuilder.append(", limitExceeded=$it") }
                action?.let { contextBuilder.append(", action=$it") }
                
                when {
                    limitExceeded != null -> {
                        logger.warn("Processing limit exceeded: {}", contextBuilder.toString())
                    }
                    processingTimeMs != null && processingTimeMs > 1000 -> {
                        logger.warn("Slow processing detected: {}", contextBuilder.toString())
                    }
                    else -> {
                        logger.debug("Processing limits: {}", contextBuilder.toString())
                    }
                }
                
                // Performance logging
                if (processingTimeMs != null) {
                    performanceLogger.info("processing_time,context={},time_ms={}", context, processingTimeMs)
                }
                
            } finally {
                MDC.remove(PROCESSING_CONTEXT_KEY)
            }
        }
        
        /**
         * Log system events and lifecycle operations
         */
        fun logSystemEvent(event: String, details: Map<String, Any> = emptyMap()) {
            val contextBuilder = StringBuilder()
            contextBuilder.append("event=$event")
            
            details.forEach { (k, v) ->
                contextBuilder.append(", $k=$v")
            }
            
            when (event) {
                "startup", "shutdown", "configuration_loaded" -> {
                    logger.info("System event: {}", contextBuilder.toString())
                }
                "cache_cleanup", "memory_pressure_response", "limit_enforcement" -> {
                    logger.warn("System event: {}", contextBuilder.toString())
                }
                "error", "failure", "exception" -> {
                    logger.error("System event: {}", contextBuilder.toString())
                }
                else -> {
                    logger.debug("System event: {}", contextBuilder.toString())
                }
            }
        }
        
        /**
         * Generate diagnostic dump with current system state
         */
        fun generateDiagnosticDump(
            cacheStatistics: Map<String, CacheStatistics>,
            memoryUsage: MemoryUsage,
            pressureLevel: MemoryPressureLevel,
            additionalInfo: Map<String, Any> = emptyMap()
        ): String {
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val dump = StringBuilder()
            
            dump.appendLine("=== Memory Management Diagnostic Dump ===")
            dump.appendLine("Timestamp: $timestamp")
            dump.appendLine("Memory Pressure Level: $pressureLevel")
            dump.appendLine()
            
            // Memory usage
            dump.appendLine("--- Memory Usage ---")
            dump.appendLine("Heap: ${memoryUsage.heapUsed / (1024 * 1024)}MB / ${memoryUsage.heapMax / (1024 * 1024)}MB (${String.format("%.2f%%", memoryUsage.heapUsagePercent * 100)})")
            dump.appendLine("Metaspace: ${memoryUsage.metaspaceUsed / (1024 * 1024)}MB / ${if (memoryUsage.metaspaceMax > 0) "${memoryUsage.metaspaceMax / (1024 * 1024)}MB" else "unlimited"} (${String.format("%.2f%%", memoryUsage.metaspaceUsagePercent * 100)})")
            dump.appendLine()
            
            // Cache statistics
            dump.appendLine("--- Cache Statistics ---")
            cacheStatistics.forEach { (cacheName, stats) ->
                val hitRate = if (stats.totalRequests > 0) {
                    String.format("%.2f%%", stats.hitCount.toDouble() / stats.totalRequests * 100)
                } else "N/A"
                
                dump.appendLine("$cacheName: size=${stats.size}, hitRate=$hitRate, requests=${stats.totalRequests}, evictions=${stats.evictionCount}")
            }
            dump.appendLine()
            
            // Additional information
            if (additionalInfo.isNotEmpty()) {
                dump.appendLine("--- Additional Information ---")
                additionalInfo.forEach { (key, value) ->
                    dump.appendLine("$key: $value")
                }
                dump.appendLine()
            }
            
            dump.appendLine("=== End Diagnostic Dump ===")
            
            val dumpContent = dump.toString()
            diagnosticLogger.info("Diagnostic dump generated:\n{}", dumpContent)
            
            return dumpContent
        }
    }
}