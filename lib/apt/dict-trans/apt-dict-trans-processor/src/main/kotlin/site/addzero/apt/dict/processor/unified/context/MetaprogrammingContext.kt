package site.addzero.apt.dict.processor.unified.context

import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.field.LsiField
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic

/**
 * 元编程上下文
 *
 * 封装APT处理环境和LSI适配器，提供统一的元编程操作接口
 */
class MetaprogrammingContext(
    val processingEnv: ProcessingEnvironment,
    val lsiAdapter: AptLsiAdapter
) {

    /**
     * 报告错误信息
     */
    fun reportError(message: String, throwable: Throwable? = null) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)
        throwable?.printStackTrace()
    }

    /**
     * 报告警告信息
     */
    fun reportWarning(message: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.WARNING, message)
    }

    /**
     * 报告信息
     */
    fun reportInfo(message: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, message)
    }

    /**
     * 创建源文件
     */
    fun createSourceFile(qualifiedName: String, content: String) {
        try {
            val sourceFile = processingEnv.filer.createSourceFile(qualifiedName)
            sourceFile.openWriter().use { writer ->
                writer.write(content)
            }
        } catch (e: Exception) {
            reportError("Failed to create source file $qualifiedName: ${e.message}", e)
        }
    }

    /**
     * 获取处理器选项
     */
    fun getOption(key: String): String? {
        return processingEnv.options[key]
    }

    /**
     * 检查是否有指定注解的字段
     */
    fun hasAnnotatedFields(lsiClass: LsiClass, annotationName: String): Boolean {
        return lsiClass.fields.any { field ->
            field.annotations.any { it.simpleName == annotationName }
        }
    }

    /**
     * 获取指定注解的字段
     */
    fun getAnnotatedFields(lsiClass: LsiClass, annotationName: String): List<LsiField> {
        return lsiClass.fields.filter { field ->
            field.annotations.any { it.simpleName == annotationName }
        }
    }

    /**
     * 递归获取所有字段（包括嵌套字段）
     */
    fun getAllFieldsRecursively(lsiClass: LsiClass, maxDepth: Int = 5): List<LsiField> {
        val result = mutableListOf<LsiField>()
        collectFieldsRecursively(lsiClass.fields, result, 0, maxDepth)
        return result
    }

    private fun collectFieldsRecursively(
        fields: List<LsiField>,
        result: MutableList<LsiField>,
        currentDepth: Int,
        maxDepth: Int
    ) {
        if (currentDepth >= maxDepth) return

        for (field in fields) {
            result.add(field)
            if (field.isNestedObject && field.children.isNotEmpty()) {
                collectFieldsRecursively(field.children, result, currentDepth + 1, maxDepth)
            }
        }
    }
}
