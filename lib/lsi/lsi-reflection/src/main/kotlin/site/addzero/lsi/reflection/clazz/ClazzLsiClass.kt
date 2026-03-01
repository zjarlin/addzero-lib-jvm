package site.addzero.lsi.reflection.clazz

import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.field.LsiField
import site.addzero.lsi.method.LsiMethod
import site.addzero.lsi.reflection.anno.ClazzLsiAnnotation
import site.addzero.lsi.reflection.field.ClazzLsiField
import site.addzero.lsi.reflection.java.method.ClazzLsiMethod

/**
 * 基于 Java Class 字节码的 LsiClass 实现
 */
class ClazzLsiClass(private val clazz: Class<*>) : LsiClass {
    override val simpleName: String?
        get() = clazz.simpleName

    override val qualifiedName: String?
        get() = clazz.name

    override val comment: String?
        get() = null // 字节码中不包含注释信息

    override val fields: List<LsiField>
        get() = clazz.declaredFields.map { ClazzLsiField(it) }

    override val annotations: List<LsiAnnotation>
        get() = clazz.annotations.map { ClazzLsiAnnotation(it) }

    override val isInterface: Boolean
        get() = clazz.isInterface

    override val isEnum: Boolean
        get() = clazz.isEnum

    override val isCollectionType: Boolean
        get() = clazz.isCollectionType()

    override val isPojo: Boolean
        get() = clazz.isPojo()

    override val superClasses: List<LsiClass>
        get() = clazz.superclass?.let { listOf(ClazzLsiClass(it)) } ?: emptyList()

    override val interfaces: List<LsiClass>
        get() = clazz.interfaces.map { ClazzLsiClass(it) }

    override val methods: List<LsiMethod>
        get() = clazz.declaredMethods.map { ClazzLsiMethod(it) }
}
