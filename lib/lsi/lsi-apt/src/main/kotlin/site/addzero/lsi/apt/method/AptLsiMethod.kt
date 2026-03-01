package site.addzero.lsi.apt.method

import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.method.LsiMethod
import site.addzero.lsi.method.LsiParameter
import site.addzero.lsi.type.LsiType
import site.addzero.lsi.apt.anno.methodComment
import site.addzero.lsi.apt.anno.toLsiAnnotations
import site.addzero.lsi.apt.clazz.AptLsiClass
import site.addzero.lsi.apt.type.AptLsiType
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
        _root_ide_package_.site.addzero.lsi.apt.type.AptLsiType(elements, method.returnType)
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
        method.parameters.map { _root_ide_package_.site.addzero.lsi.apt.method.AptLsiParameter(elements, it) }
    }

    override val declaringClass: LsiClass? by lazy {
        (method.enclosingElement as? TypeElement)?.let {
            _root_ide_package_.site.addzero.lsi.apt.clazz.AptLsiClass(
                elements,
                it
            )
        }
    }
}

class AptLsiParameter(private val elements: Elements, private val param: VariableElement) : LsiParameter {

    override val name: String? by lazy {
        param.simpleName.toString()
    }

    override val type: LsiType? by lazy {
        _root_ide_package_.site.addzero.lsi.apt.type.AptLsiType(elements, param.asType())
    }

    override val typeName: String? by lazy {
        param.asType().toString()
    }

    override val annotations: List<LsiAnnotation> by lazy {
        param.annotationMirrors.toLsiAnnotations()
    }
}
