package site.addzero.aop.dicttrans.strategy

import cn.hutool.core.collection.CollUtil
import cn.hutool.core.util.ObjectUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import site.addzero.aop.dicttrans.config.MemoryManagementProperties
import site.addzero.aop.dicttrans.inter.TransStrategy
import site.addzero.aop.dicttrans.limits.ProcessingAction
import site.addzero.aop.dicttrans.limits.ProcessingLimitManager
import site.addzero.aop.dicttrans.processing.ProcessingContext
import site.addzero.aop.dicttrans.util_internal.EnhancedByteBuddyUtil
import site.addzero.util.RefUtil
import site.addzero.aop.dicttrans.util_internal.TransInternalUtil
import org.springframework.stereotype.Component
import java.util.*

/**
 * Enhanced collection strategy with memory management and caching
 * 
 * @author zjarlin
 * @since 2023/11/8 10:31
 */
@Component
class CollectionStrategy @Autowired constructor(
    private val properties: MemoryManagementProperties,
    private val limitManager: ProcessingLimitManager
) : TransStrategy<Collection<*>> {
    
    private val logger = LoggerFactory.getLogger(CollectionStrategy::class.java)
    
    override fun trans(t: Collection<*>): Collection<*> {
        var inVOs = t
        if (CollUtil.isEmpty(inVOs)) {
            return inVOs
        }

        // Create processing context
        val context = ProcessingContext.create(
            maxCollectionSize = properties.processing.maxCollectionSize,
            maxRecursionDepth = properties.processing.maxRecursionDepth,
            timeout = properties.processing.processingTimeout,
            circuitBreakerEnabled = properties.processing.enableCircuitBreaker
        )

        // Check memory pressure first
        val memoryDecision = limitManager.checkMemoryPressure()
        if (!memoryDecision.allowed) {
            logger.warn("Skipping collection processing due to memory pressure: {}", memoryDecision.reason)
            limitManager.recordFailure(context, RuntimeException("Memory pressure: ${memoryDecision.reason}"))
            return when (memoryDecision.suggestedAction) {
                ProcessingAction.ABORT -> emptyList()
                else -> inVOs // Return original collection
            }
        }

        // Check collection size limits
        val collectionDecision = limitManager.checkCollectionLimit(inVOs.size, context)
        if (!collectionDecision.allowed) {
            logger.warn("Collection processing limited: {}", collectionDecision.reason)
            
            when (collectionDecision.suggestedAction) {
                ProcessingAction.BATCH -> {
                    // Process in batches
                    inVOs = inVOs.take(properties.processing.maxCollectionSize)
                    logger.info("Processing collection in batch of {} items", inVOs.size)
                }
                ProcessingAction.SKIP -> {
                    limitManager.recordFailure(context, RuntimeException("Collection too large: ${inVOs.size}"))
                    return inVOs // Return original collection
                }
                ProcessingAction.ABORT -> {
                    limitManager.recordFailure(context, RuntimeException("Collection size exceeds limits"))
                    return emptyList()
                }
                ProcessingAction.RETRY_LATER -> {
                    logger.info("Circuit breaker open, returning original collection")
                    return inVOs
                }
                else -> {
                    // Continue with original size
                }
            }
        }

        inVOs = inVOs.filter { e -> Objects.nonNull(e) && !RefUtil.isNew(e) }
        if (CollUtil.isEmpty(inVOs)) {
            return inVOs
        }

        logger.debug("Processing collection of {} items with context: {}", inVOs.size, context)

        // Use enhanced ByteBuddy utility with caching and limits
        val collect = inVOs.mapNotNull { e ->
            try {
                // Check time limit for each item
                val timeDecision = limitManager.checkTimeLimit(context)
                if (!timeDecision.allowed) {
                    logger.warn("Aborting collection processing due to timeout: {}", timeDecision.reason)
                    limitManager.recordFailure(context, RuntimeException("Processing timeout"))
                    return inVOs.take(inVOs.indexOf(e)) // Return processed items so far
                }
                
                EnhancedByteBuddyUtil.genChildObjectRecursion(e, {
                    val needAddFields = TransInternalUtil.getNeedAddFields(it)
                    needAddFields.toMutableList()
                }, context)
            } catch (ex: Exception) {
                logger.error("Failed to process object of type: {}", e?.javaClass?.name, ex)
                limitManager.recordFailure(context, ex)
                e // Return original object on failure
            }
        }

        // Process translation information
        val collect1 = collect.filter { ObjectUtil.isNotEmpty(it) }.flatMap {
            try {
                TransInternalUtil.process(it!!)
            } catch (ex: Exception) {
                logger.error("Failed to extract translation info from object: {}", it?.javaClass?.name, ex)
                emptyList()
            }
        }.groupBy { it.classificationOfTranslation }

        // Log cache statistics periodically
        if (logger.isDebugEnabled) {
            val stats = EnhancedByteBuddyUtil.getCacheStatistics()
            logger.debug("ByteBuddy cache stats: hits={}, misses={}, hitRate={:.2f}%, size={}/{}", 
                stats.hitCount, stats.missCount, stats.hitRate * 100, stats.size, stats.maxSize)
        }

        try {
            /** Process built-in dictionary translation */
            TransInternalUtil.processBuiltInDictionaryTranslation(collect1)
            /** Process arbitrary table translation */
            TransInternalUtil.processAnyTableTranslation(collect1)
            /** Process SPEL expressions */
            // TransUtil.processingSpelExpressions(collect1)
            
            // Record successful processing
            limitManager.recordSuccess(context)
            
        } catch (ex: Exception) {
            logger.error("Failed to process translations", ex)
            limitManager.recordFailure(context, ex)
        }

        // Log processing statistics periodically
        if (logger.isInfoEnabled && inVOs.size > 100) {
            val stats = limitManager.getStatistics()
            logger.info("Processing stats: total={}, success={}, failed={}, skipped={}, avgTime={}ms", 
                stats.totalProcessed, stats.successfulProcessed, stats.failedProcessed, 
                stats.skippedDueToLimits, stats.averageProcessingTime.toMillis())
        }

        return collect
    }

    override fun support(t: Any): Boolean {
        return Collection::class.java.isAssignableFrom(t.javaClass)
    }
}
