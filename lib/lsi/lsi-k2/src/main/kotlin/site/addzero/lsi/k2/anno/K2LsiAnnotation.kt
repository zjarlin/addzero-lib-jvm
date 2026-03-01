package site.addzero.lsi.k2.anno

import org.jetbrains.kotlin.analysis.api.annotations.KaAnnotation
import org.jetbrains.kotlin.analysis.api.annotations.KaAnnotationValue
import site.addzero.lsi.anno.LsiAnnotation

/**
 * 基于 K2 Analysis API 的 LsiAnnotation 实现
 *
 * 注意：所有依赖 KaSession 的属性在构造时预先计算（eager evaluation），
 * 因为 KaSession 在 analyze 块结束后会失效。
 */
class K2LsiAnnotation(
    kaAnnotation: KaAnnotation
) : LsiAnnotation {

    // Eager evaluation - 在构造时计算所有需要 session 的值
    override val qualifiedName: String? = kaAnnotation.classId?.asFqNameString()
    override val simpleName: String? = kaAnnotation.classId?.shortClassName?.asString()
    override val attributes: Map<String, Any?>

    init {
        attributes = buildMap {
            kaAnnotation.arguments.forEach { arg ->
                val name = arg.name.asString()
                val value = extractAnnotationValue(arg.expression)
                put(name, value)
            }
        }
    }

    override fun getAttribute(name: String): Any? = attributes[name]

    override fun hasAttribute(name: String): Boolean = attributes.containsKey(name)

    private fun extractAnnotationValue(value: KaAnnotationValue): Any? {
        return when (value) {
            is KaAnnotationValue.ConstantValue -> value.value.value
            is KaAnnotationValue.EnumEntryValue -> value.callableId?.callableName?.asString()
            is KaAnnotationValue.ClassLiteralValue -> value.classId?.asFqNameString()
            is KaAnnotationValue.ArrayValue -> value.values.map { extractAnnotationValue(it) }
            is KaAnnotationValue.NestedAnnotationValue -> value.annotation.classId?.asFqNameString()
            is KaAnnotationValue.UnsupportedValue -> null
        }
    }
}
