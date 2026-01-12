package site.addzero.util.lsi_impl.impl.apt.method

import site.addzero.util.lsi.anno.LsiAnnotation
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.method.LsiMethod
import site.addzero.util.lsi.method.LsiParameter
import site.addzero.util.lsi.type.LsiType
import site.addzero.util.lsi_impl.impl.apt.anno.methodComment
import site.addzero.util.lsi_impl.impl.apt.anno.toLsiAnnotations
import site.addzero.util.lsi_impl.impl.apt.clazz.AptLsiClass
import site.addzero.util.lsi_impl.impl.apt.type.AptLsiType
import site.addzero.util.str.firstNotBlank
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements

class AptLsiMethod(
    private val elements: Elements,
    private val method: ExecutableElement
) : LsiMethod {

    override val name: String? by lazy {
        method.simpleName.toString()
    }

    override val returnType: LsiType? by lazy {
        AptLsiType(elements, method.returnType)
    }

    override val returnTypeName: String? by lazy {
        method.returnType.toString()
    }

    override val comment: String? by lazy {
        firstNotBlank(
            method.annotationMirrors.methodComment(),
            elements.getDocComment(method)
        )
    }

    override val annotations: List<LsiAnnotation> by lazy {
        method.annotationMirrors.toLsiAnnotations()
    }

    override val isStatic: Boolean by lazy {
        method.modifiers.contains(Modifier.STATIC)
    }

    override val isAbstract: Boolean by lazy {
        method.modifiers.contains(Modifier.ABSTRACT)
    }

    override val parameters: List<LsiParameter> by lazy {
        method.parameters.map { AptLsiParameter(elements, it) }
    }

    override val declaringClass: LsiClass? by lazy {
        (method.enclosingElement as? TypeElement)?.let { AptLsiClass(elements, it) }
    }
}

class AptLsiParameter(private val elements: Elements, private val param: VariableElement) : LsiParameter {

    override val name: String? by lazy {
        param.simpleName.toString()
    }

    override val type: LsiType? by lazy {
        AptLsiType(elements, param.asType())
    }

    override val typeName: String? by lazy {
        param.asType().toString()
    }

    override val annotations: List<LsiAnnotation> by lazy {
        param.annotationMirrors.toLsiAnnotations()
    }
}
