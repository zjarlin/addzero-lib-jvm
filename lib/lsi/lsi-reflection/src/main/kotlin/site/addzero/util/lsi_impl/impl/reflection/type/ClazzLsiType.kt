package site.addzero.util.lsi_impl.impl.reflection.type

import site.addzero.util.lsi.anno.LsiAnnotation
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.type.LsiType
import site.addzero.util.lsi_impl.impl.reflection.anno.ClazzLsiAnnotation
import site.addzero.util.lsi_impl.impl.reflection.clazz.ClazzLsiClass
import site.addzero.util.lsi_impl.impl.reflection.clazz.isCollectionType
import site.addzero.util.lsi_impl.impl.reflection.clazz.isNullable

/**
 * 基于 Java Class 的 LsiType 实现
 */
class ClazzLsiType(private val clazz: Class<*>) : LsiType {
    override val name: String?
        get() = clazz.simpleName

    override val qualifiedName: String?
        get() = clazz.name

    override val presentableText: String?
        get() = clazz.simpleName

    override val annotations: List<LsiAnnotation>
        get() = clazz.annotations.map { ClazzLsiAnnotation(it) }

    override val isCollectionType: Boolean
        get() = clazz.isCollectionType()

    override val isNullable: Boolean
        get() = clazz.isNullable()

    override val typeParameters: List<LsiType>
        get() = clazz.typeParameters
            .map {
                val bound = it.bounds.firstOrNull()
                ClazzLsiType(bound as? Class<*> ?: Any::class.java)
            }

    override val isPrimitive: Boolean
        get() = clazz.isPrimitive

    override val componentType: LsiType?
        get() = if (clazz.isArray) {
            clazz.componentType?.let { ClazzLsiType(it) }
        } else {
            null
        }

    override val isArray: Boolean
        get() = clazz.isArray

    override val lsiClass: LsiClass?
        get() = ClazzLsiClass(clazz)
}
