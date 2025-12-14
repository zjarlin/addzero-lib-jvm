package site.addzero.apt.dict.processor.unified.context

import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.field.LsiField
import javax.annotation.processing.ProcessingEnvironment

/**
 * 元编程上下文
 *
 * 封装APT处理环境和LSI适配器，提供统一的元编程操作接口
 */
class MetaprogrammingContext(
    val processingEnv: ProcessingEnvironment,
) {

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
