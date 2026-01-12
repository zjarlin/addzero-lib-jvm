package site.addzero.util.lsi_impl.impl.apt.type

import site.addzero.util.lsi.anno.LsiAnnotation
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.type.LsiType
import site.addzero.util.lsi_impl.impl.apt.anno.toLsiAnnotations
import site.addzero.util.lsi_impl.impl.apt.clazz.AptLsiClass
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.PrimitiveType
import javax.lang.model.type.TypeMirror
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

class AptLsiType(private val elements: Elements, private val typeMirror: TypeMirror) : LsiType {

    override val name: String? by lazy {
        typeMirror.toString().substringAfterLast('.')
    }

    override val qualifiedName: String? by lazy {
        typeMirror.toString()
    }

    override val presentableText: String? by lazy {
        typeMirror.toString()
    }

    override val annotations: List<LsiAnnotation> by lazy {
        typeMirror.annotationMirrors.toLsiAnnotations()
    }

    override val isCollectionType: Boolean by lazy {
        val qName = qualifiedName ?: ""
        qName.startsWith("java.util.") &&
            (qName.contains("List") || qName.contains("Set") || qName.contains("Collection") || qName.contains("Map"))
    }

    override val typeParameters: List<LsiType> by lazy {
        when (typeMirror) {
            is DeclaredType -> typeMirror.typeArguments.map { AptLsiType(elements, it) }
            else -> emptyList()
        }
    }

    override val isPrimitive: Boolean by lazy {
        typeMirror is PrimitiveType
    }

    override val componentType: LsiType? by lazy {
        when (typeMirror) {
            is ArrayType -> AptLsiType(elements, typeMirror.componentType)
            else -> null
        }
    }

    override val isArray: Boolean by lazy {
        typeMirror is ArrayType
    }

    override val lsiClass: LsiClass? by lazy {
        when (typeMirror) {
            is DeclaredType -> {
                val element = typeMirror.asElement()
                if (element is TypeElement) AptLsiClass(elements, element) else null
            }
            else -> null
        }
    }
}

// Removed unused extension function - AptLsiType now requires Elements
