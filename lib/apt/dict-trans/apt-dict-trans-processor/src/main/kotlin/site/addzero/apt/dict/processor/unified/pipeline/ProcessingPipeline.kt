package site.addzero.apt.dict.processor.unified.pipeline

import site.addzero.apt.dict.processor.unified.context.MetaprogrammingContext
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.context.LsiContext
import site.addzero.util.lsi_impl.impl.apt.clazz.AptLsiClass
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement

/**
 * 处理管道
 *
 * 统一的注解处理流程，负责协调APT处理和LSI转换
 */
class ProcessingPipeline(
    private val context: MetaprogrammingContext
) {

    /**
     * 处理注解
     *
     * @param annotations 注解类型集合
     * @param roundEnv 处理环境
     * @param processor 具体的处理逻辑
     */
    fun process(
        annotations: Set<TypeElement>,
        roundEnv: RoundEnvironment,
        processor: (TypeElement, LsiClass, LsiContext) -> Boolean
    ): Boolean {
        var success = true

        // 收集所有被注解的类
        val annotatedClasses = mutableSetOf<TypeElement>()

        for (annotation in annotations) {
            val elements = roundEnv.getElementsAnnotatedWith(annotation)

            for (element in elements) {
                // 找到包含注解字段的类
                val enclosingClass = when (element.kind) {
                    ElementKind.FIELD -> element.enclosingElement as? TypeElement
                    ElementKind.CLASS -> element as? TypeElement
                    else -> null
                }

                enclosingClass?.let { annotatedClasses.add(it) }
            }
        }

        // 处理每个类
        for (typeElement in annotatedClasses) {
            try {
                // 转换为LSI抽象
                val lsiClass = AptLsiClass(context.lsiAdapter.elements, typeElement)
                val lsiContext = context.lsiAdapter.createLsiContext(lsiClass)

                // 执行具体处理逻辑
                val result = processor(typeElement, lsiClass, lsiContext)
                if (!result) {
                    success = false
                    context.reportWarning("Processing failed for class: ${typeElement.qualifiedName}")
                }

            } catch (e: Exception) {
                success = false
                context.reportError(
                    "Failed to process class ${typeElement.qualifiedName}: ${e.message}",
                    e
                )
            }
        }

        return success
    }
}
