package site.addzero.ksp.metadata.jimmer.entity.external

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityCollector
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityMeta
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityProcessContext
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityProcessorIds
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityProcessorOptions
import site.addzero.lsi.processor.ProcessorSpi
import site.addzero.tool.coll.topoSort
import java.util.ServiceLoader

private val PROCESSOR_ENABLE_OPTION_BY_ID = mapOf(
    JimmerEntityProcessorIds.ENTITY2_ISO to JimmerEntityProcessorOptions.ENTITY2_ISO_ENABLED,
    JimmerEntityProcessorIds.ENTITY2_FORM to JimmerEntityProcessorOptions.ENTITY2_FORM_ENABLED,
    JimmerEntityProcessorIds.ENTITY2_MCP to JimmerEntityProcessorOptions.ENTITY2_MCP_ENABLED
)

internal object JimmerEntityExternalProcessorSupport {
    fun mergeCollectedEntities(
        target: MutableMap<String, JimmerEntityMeta>,
        roundEntities: Map<String, JimmerEntityMeta>
    ) {
        target.putAll(roundEntities)
    }

    fun filterEnabledProcessors(
        processors: List<ProcessorSpi<JimmerEntityProcessContext, Unit>>,
        options: Map<String, String>
    ): LinkedHashMap<String, ProcessorSpi<JimmerEntityProcessContext, Unit>> {
        return linkedMapOf<String, ProcessorSpi<JimmerEntityProcessContext, Unit>>().apply {
            processors.forEach { processor ->
                val previous = put(processor.id, processor)
                check(previous == null) {
                    "检测到重复处理器ID: ${processor.id}, 冲突处理器: ${previous?.id} / ${processor.id}"
                }
            }
        }.filterTo(linkedMapOf()) { (processorId, _) ->
            isProcessorEnabled(processorId, options)
        }
    }

    fun sortLayers(
        processorsById: LinkedHashMap<String, ProcessorSpi<JimmerEntityProcessContext, Unit>>
    ): List<List<ProcessorSpi<JimmerEntityProcessContext, Unit>>> {
        return topoSort(
            items = processorsById.values,
            idSelector = { it.id },
            dependsOnSelector = { it.dependsOn },
            ignoreMissingDependency = false
        )
    }

    fun executeRound(
        sortedLayers: List<List<ProcessorSpi<JimmerEntityProcessContext, Unit>>>,
        context: JimmerEntityProcessContext,
        logger: KSPLogger
    ) {
        val failures = mutableListOf<Pair<String, Throwable>>()

        sortedLayers.forEach { layer ->
            layer.forEach { processor ->
                try {
                    processor.ctx = context
                    processor.onRound()
                } catch (e: Throwable) {
                    failures += processor.id to e
                    logger.error("处理器 onRound 失败: ${processor.id}, 错误: ${e.message}")
                }
            }
        }

        check(failures.isEmpty()) {
            "处理器 onRound 失败: ${failures.joinToString { it.first }}"
        }
    }

    fun executeFinish(
        sortedLayers: List<List<ProcessorSpi<JimmerEntityProcessContext, Unit>>>,
        context: JimmerEntityProcessContext,
        logger: KSPLogger
    ) {
        val completed = mutableListOf<String>()

        runBlocking {
            sortedLayers.forEachIndexed { index, layer ->
                val layerNames = layer.joinToString(", ") { it.id }
                logger.warn("开始执行第 ${index + 1}/${sortedLayers.size} 层处理器: $layerNames")

                val results = supervisorScope {
                    layer.map { processor ->
                        async(Dispatchers.Default) {
                            try {
                                processor.ctx = context
                                processor.onFinish()
                                processor.id to null
                            } catch (e: Throwable) {
                                processor.id to e
                            }
                        }
                    }.awaitAll()
                }

                val failures = mutableListOf<Pair<String, Throwable>>()
                results.forEach { (processorId, error) ->
                    if (error == null) {
                        completed += processorId
                        logger.warn("处理器执行完成: $processorId")
                    } else {
                        failures += processorId to error
                        logger.error("处理器执行失败: $processorId, 错误: ${error.message}")
                    }
                }

                check(failures.isEmpty()) {
                    "处理器执行失败: ${failures.joinToString { it.first }}"
                }
            }
        }

        logger.warn("外部实体处理器执行完成，已完成: ${completed.joinToString(", ")}")
    }

    fun isProcessorEnabled(
        processorId: String,
        options: Map<String, String>
    ): Boolean {
        val optionKey = PROCESSOR_ENABLE_OPTION_BY_ID[processorId] ?: return true
        return options[optionKey]?.toBooleanStrictOrNull() ?: true
    }
}

class JimmerEntityExternalProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val logger = environment.logger

        return object : SymbolProcessor {
            private val collectedEntitiesByQualifiedName = linkedMapOf<String, JimmerEntityMeta>()
            private var noProcessorLogged = false
            private val processorsById: LinkedHashMap<String, ProcessorSpi<JimmerEntityProcessContext, Unit>> by lazy {
                val processors = loadProcessors(javaClass.classLoader)
                if (processors.isEmpty()) {
                    logNoProcessorOnce()
                    return@lazy linkedMapOf()
                }

                val enabledProcessors = JimmerEntityExternalProcessorSupport.filterEnabledProcessors(
                    processors = processors,
                    options = environment.options
                )
                if (enabledProcessors.isEmpty()) {
                    logger.warn("外部实体子处理器全部被禁用，当前不会执行任何生成逻辑")
                }
                enabledProcessors
            }
            private val sortedLayers by lazy {
                if (processorsById.isEmpty()) {
                    emptyList()
                } else {
                    JimmerEntityExternalProcessorSupport.sortLayers(processorsById)
                }
            }

            override fun process(resolver: Resolver): List<KSAnnotated> {
                val result = JimmerEntityCollector.collect(environment, resolver)
                JimmerEntityExternalProcessorSupport.mergeCollectedEntities(
                    target = collectedEntitiesByQualifiedName,
                    roundEntities = result.entitiesByQualifiedName
                )
                if (result.entities.isNotEmpty() && sortedLayers.isNotEmpty()) {
                    val roundContext = JimmerEntityProcessContext(
                        logger = logger,
                        options = environment.options,
                        entitiesByQualifiedName = result.entitiesByQualifiedName
                    )
                    JimmerEntityExternalProcessorSupport.executeRound(
                        sortedLayers = sortedLayers,
                        context = roundContext,
                        logger = logger
                    )
                }
                return result.deferred
            }

            override fun finish() {
                if (collectedEntitiesByQualifiedName.isEmpty()) {
                    logger.warn("未收集到 Jimmer 实体，跳过外部处理器执行")
                    return
                }
                if (sortedLayers.isEmpty()) {
                    return
                }

                logger.warn("加载到 ${processorsById.size} 个启用的外部实体处理器，按 ${sortedLayers.size} 层执行（onFinish 同层并发）...")
                val context = JimmerEntityProcessContext(
                    logger = logger,
                    options = environment.options,
                    entitiesByQualifiedName = collectedEntitiesByQualifiedName
                )
                JimmerEntityExternalProcessorSupport.executeFinish(
                    sortedLayers = sortedLayers,
                    context = context,
                    logger = logger
                )
            }

            private fun logNoProcessorOnce() {
                if (noProcessorLogged) {
                    return
                }
                logger.warn("未通过 ServiceLoader 加载到任何 ProcessorSpi")
                noProcessorLogged = true
            }

            @Suppress("UNCHECKED_CAST")
            private fun loadProcessors(classLoader: ClassLoader): List<ProcessorSpi<JimmerEntityProcessContext, Unit>> {
                return ServiceLoader
                    .load(ProcessorSpi::class.java, classLoader)
                    .map { it as ProcessorSpi<JimmerEntityProcessContext, Unit> }
                    .toList()
            }
        }
    }
}
