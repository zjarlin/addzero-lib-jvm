package site.addzero.lsi.apt.field

import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.assist.getColumnName
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.field.LsiField
import site.addzero.lsi.type.LsiType
import site.addzero.lsi.apt.anno.fieldComment
import site.addzero.lsi.apt.anno.toLsiAnnotations
import site.addzero.lsi.apt.clazz.AptLsiClass
import site.addzero.lsi.apt.field.getDocComment
import site.addzero.lsi.apt.type.AptLsiType
import site.addzero.util.str.toUnderLineCase
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.util.Elements

class AptLsiField(
    private val elements: Elements,
    private val field: VariableElement,
    ) : LsiField {

    override val name: String? by lazy {
        field.simpleName.toString()
    }

    override val type: LsiType? by lazy {
        val aptLsiType = _root_ide_package_.site.addzero.lsi.apt.type.AptLsiType(elements, field.asType())
        aptLsiType
    }

    override val typeName: String? by lazy {
        val toString = field.asType().toString()
        toString
    }

    override val comment: String? by lazy {
        val annotationMirrors = field.annotationMirrors
        val fieldComment = annotationMirrors.fieldComment()
        val docComment = field.getDocComment(elements)
        val string = fieldComment ?: docComment
        string
    }
    override val annotations: List<LsiAnnotation> by lazy {
        field.annotationMirrors.toLsiAnnotations()
    }

    override val isStatic: Boolean by lazy {
        field.modifiers.contains(Modifier.STATIC)
    }

    override val isConstant: Boolean by lazy {
        field.modifiers.contains(Modifier.STATIC) && field.modifiers.contains(Modifier.FINAL)
    }

    override val isVar: Boolean by lazy {
        !field.modifiers.contains(Modifier.FINAL)
    }

    override val isLateInit: Boolean
        get() = false

    override val isCollectionType: Boolean by lazy {
        type?.isCollectionType ?: false
    }

    override val defaultValue: String? by lazy {
        field.constantValue?.toString()
    }

    override val columnName: String? by lazy {
        val simpleName = field.simpleName.toString()
        val string = annotations.getColumnName() ?: simpleName
        string.toUnderLineCase()
    }

    override val declaringClass: LsiClass? by lazy {
        (field.enclosingElement as? TypeElement)?.let {
            _root_ide_package_.site.addzero.lsi.apt.clazz.AptLsiClass(
                elements,
                it
            )
        }
    }

    override val fieldTypeClass: LsiClass? by lazy {
        val typeMirror = field.asType()
        if (typeMirror is DeclaredType) {
            val element = typeMirror.asElement()
            if (element is TypeElement) _root_ide_package_.site.addzero.lsi.apt.clazz.AptLsiClass(
                elements,
                element
            ) else null
        } else null
    }

    override val isNestedObject: Boolean by lazy {
        !isCollectionType && fieldTypeClass?.isPojo == true
    }

    override val children: List<LsiField> by lazy {
        if (isNestedObject) fieldTypeClass?.fields ?: emptyList() else emptyList()
    }
}
