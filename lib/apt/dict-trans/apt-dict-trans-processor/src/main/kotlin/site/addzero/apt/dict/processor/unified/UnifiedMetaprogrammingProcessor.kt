package site.addzero.apt.dict.processor.unified

import site.addzero.apt.dict.processor.unified.adapter.AptLsiAdapter
import site.addzero.apt.dict.processor.unified.context.MetaprogrammingContext
import site.addzero.apt.dict.processor.unified.pipeline.ProcessingPipeline
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.context.LsiContext
import site.addzero.util.lsi_impl.impl.apt.environment.toLsiEnvironment
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement

/**
 * 统一元编程处理器基类
 *
 * 基于LSI抽象体系，提供统一的元编程处理框架
 * 支持多种注解处理器的统一抽象和代码生成
 */
abstract class UnifiedMetaprogrammingProcessor : AbstractProcessor() {

    protected lateinit var lsiAdapter: AptLsiAdapter
    protected lateinit var processingPipeline: ProcessingPipeline
    protected lateinit var metaprogrammingContext: MetaprogrammingContext

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        val elementUtils = processingEnv.elementUtils
        val typeUtils = processingEnv.typeUtils

        // 初始化LSI适配器
        val toLsiEnvironment = processingEnv.toLsiEnvironment()


        lsiAdapter = AptLsiAdapter(elementUtils, typeUtils)

        // 初始化元编程上下文
        metaprogrammingContext = MetaprogrammingContext(processingEnv, lsiAdapter)

        // 初始化处理管道
        processingPipeline = ProcessingPipeline(metaprogrammingContext)

        // 子类特定初始化
        onInit(metaprogrammingContext)
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        if (roundEnv == null || annotations.isNullOrEmpty()) return false

        return try {
            // 使用统一处理管道
            processingPipeline.process(annotations, roundEnv) { typeElement, lsiClass, lsiContext ->
                // 委托给子类处理具体的注解逻辑
                processLsiClass(typeElement, lsiClass, lsiContext)
            }
        } catch (e: Exception) {
            metaprogrammingContext.reportError("Processing failed: ${e.message}", e)
            false
        }
    }

    /**
     * 子类初始化回调
     */
    protected abstract fun onInit(context: MetaprogrammingContext)

    /**
     * 处理LSI类抽象
     *
     * @param originalElement 原始APT元素
     * @param lsiClass LSI类抽象
     * @param lsiContext LSI上下文
     */
    protected abstract fun processLsiClass(
        originalElement: TypeElement,
        lsiClass: LsiClass,
        lsiContext: LsiContext
    ): Boolean
}
