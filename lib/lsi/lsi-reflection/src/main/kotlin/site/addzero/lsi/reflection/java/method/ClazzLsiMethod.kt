package site.addzero.lsi.reflection.java.method

import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.method.LsiMethod
import site.addzero.lsi.method.LsiParameter
import site.addzero.lsi.type.LsiType
import site.addzero.lsi.reflection.clazz.ClazzLsiClass
import site.addzero.lsi.reflection.anno.ClazzLsiAnnotation
import site.addzero.lsi.reflection.type.ClazzLsiType
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Parameter

/**
 * 基于 Java Method 反射的 LsiMethod 实现
 */
class ClazzLsiMethod(private val clazzMethod: Method) : LsiMethod {
    override val name: String?
        get() = clazzMethod.name

    override val returnType: LsiType?
        get() = ClazzLsiType(clazzMethod.returnType)

    override val returnTypeName: String?
        get() = clazzMethod.returnType.simpleName

    override val comment: String?
        get() = clazzMethod.comment()

    override val annotations: List<LsiAnnotation>
        get() = clazzMethod.annotations.map { ClazzLsiAnnotation(it) }

    override val isStatic: Boolean
        get() = Modifier.isStatic(clazzMethod.modifiers)

    override val isAbstract: Boolean
        get() = Modifier.isAbstract(clazzMethod.modifiers)

    override val parameters: List<LsiParameter>
        get() = clazzMethod.parameters.map { ClazzLsiParameter(it) }

    override val declaringClass: LsiClass?
        get() = ClazzLsiClass(clazzMethod.declaringClass)
}

class ClazzLsiParameter(private val clazzParameter: Parameter) : LsiParameter {
    override val name: String?
        get() = clazzParameter.name

    override val type: LsiType
        get() = ClazzLsiType(clazzParameter.type)

    override val typeName: String?
        get() = clazzParameter.type.simpleName

    override val annotations: List<LsiAnnotation>
        get() = clazzParameter.annotations.map { ClazzLsiAnnotation(it) }
}

