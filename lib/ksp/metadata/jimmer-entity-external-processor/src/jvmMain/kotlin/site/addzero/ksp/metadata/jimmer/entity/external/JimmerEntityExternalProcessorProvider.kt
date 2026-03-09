package site.addzero.ksp.metadata.jimmer.entity.external

import androidx.room.compiler.processing.*
import androidx.room.compiler.processing.XTypeElement
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import kotlinx.coroutines.*
import site.addzero.ksp.metadata.jimmer.entity.spi.JIMMER_ENTITY_ANNOTATION
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityCollectionResult
import site.addzero.ksp.metadata.jimmer.entity.spi.JimmerEntityProcessContext
import site.addzero.lsi.processor.ProcessorSpi
import site.addzero.tool.coll.topoSort
import java.util.*

class JimmerEntityExternalProcessorProvider : SymbolProcessorProvider {
  override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
    val logger = environment.logger

    return object : SymbolProcessor {
      private val collectedEntitiesByQualifiedName = linkedMapOf<String, XTypeElement>()
      private var noProcessorLogged = false
      private val processorsById: LinkedHashMap<String, ProcessorSpi<JimmerEntityProcessContext, Unit>> by lazy {
        val processors = loadProcessors(javaClass.classLoader)
        if (processors.isEmpty()) {
          logNoProcessorOnce()
          return@lazy linkedMapOf()
        }

        linkedMapOf<String, ProcessorSpi<JimmerEntityProcessContext, Unit>>().apply {
          processors.forEach { processor ->
            val previous = put(processor.id, processor)
            check(previous == null) {
              "检测到重复处理器ID: ${processor.id}, 冲突处理器: ${previous?.id} / ${processor.id}"
            }
          }
        }
      }
      private val sortedLayers by lazy {
        topoSort(
          items = processorsById.values,
          idSelector = { it.id },
          dependsOnSelector = { it.dependsOn },
          ignoreMissingDependency = false
        )
      }

      @OptIn(ExperimentalProcessingApi::class)
      override fun process(resolver: Resolver): List<KSAnnotated> {
        val xProcessingEnv = XProcessingEnv.create(environment, resolver)
        val symbols = resolver.getSymbolsWithAnnotation(JIMMER_ENTITY_ANNOTATION).toList()
        val deferred = symbols.filterNot { it.validate() }
        val entitiesByQualifiedName = linkedMapOf<String, XTypeElement>()
        symbols
          .asSequence()
          .filter { it.validate() }
          .filterIsInstance<KSClassDeclaration>()
          .forEach { declaration ->
            val qualifiedName = declaration.qualifiedName?.asString() ?: return@forEach
            val typeElement = xProcessingEnv.findTypeElement(qualifiedName)
            if (typeElement == null) {
              logger.error("收集Jimmer实体失败: 无法解析 XTypeElement: $qualifiedName")
              return@forEach
            }
            entitiesByQualifiedName[qualifiedName] = typeElement
          }
        val result = JimmerEntityCollectionResult(
          deferred = deferred,
          entitiesByQualifiedName = entitiesByQualifiedName
        )
        collectedEntitiesByQualifiedName.putAll(result.entitiesByQualifiedName)
        if (result.entitiesByQualifiedName.isNotEmpty() && sortedLayers.isNotEmpty()) {
          val roundContext = JimmerEntityProcessContext(
            logger = logger,
            options = environment.options,
            entities = result.entities
          )
          executeRoundProcessors(roundContext)
        }

        return result.deferred
      }

      override fun finish() {
        if (collectedEntitiesByQualifiedName.isEmpty()) {
          logger.warn("未收集到Jimmer实体，跳过外部处理器执行")
          return
        }

        if (sortedLayers.isEmpty()) {
          return
        }

        logger.warn("加载到 ${processorsById.size} 个外部实体处理器，按 ${sortedLayers.size} 层执行（onFinish 同层并发）...")

        val context = JimmerEntityProcessContext(
          logger = logger,
          options = environment.options,
          entities = collectedEntitiesByQualifiedName.values.toSet()
        )
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

      private fun executeRoundProcessors(context: JimmerEntityProcessContext) {
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
