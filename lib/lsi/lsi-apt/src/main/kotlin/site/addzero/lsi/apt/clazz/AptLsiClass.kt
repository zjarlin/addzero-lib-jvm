package site.addzero.lsi.apt.clazz

import site.addzero.lsi.anno.LsiAnnotation
import site.addzero.lsi.assist.checkIsPojo
import site.addzero.lsi.clazz.LsiClass
import site.addzero.lsi.field.LsiField
import site.addzero.lsi.method.LsiMethod
import site.addzero.lsi.apt.anno.classComment
import site.addzero.lsi.apt.anno.toLsiAnnotations
import site.addzero.lsi.apt.element.getDocComment
import site.addzero.lsi.apt.field.AptLsiField
import site.addzero.lsi.apt.method.AptLsiMethod
import site.addzero.util.str.firstNotBlank
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.util.Elements

class AptLsiClass(
    private val elements: Elements,
    private val typeElement: TypeElement
) : LsiClass {

    override val simpleName: String? by lazy {
        typeElement.simpleName.toString()
    }

    override val qualifiedName: String? by lazy {
        typeElement.qualifiedName.toString()
    }

    override val comment: String? by lazy {
        val docComment = typeElement.getDocComment(elements)

        firstNotBlank(
            docComment,
            typeElement.annotationMirrors.classComment()
        )
    }

    override val fields: List<LsiField> by lazy {
        typeElement.enclosedElements
            .filterIsInstance<VariableElement>()
            .filter { it.kind == ElementKind.FIELD }
            .map { _root_ide_package_.site.addzero.lsi.apt.field.AptLsiField(elements, it) }
    }

    override val annotations: List<LsiAnnotation> by lazy {
        typeElement.annotationMirrors.toLsiAnnotations()
    }

    override val isInterface: Boolean by lazy {
        typeElement.kind == ElementKind.INTERFACE
    }

    override val isEnum: Boolean by lazy {
        typeElement.kind == ElementKind.ENUM
    }

    override val isCollectionType: Boolean by lazy {
        val name = qualifiedName ?: ""
        name.startsWith("java.util.") &&
            (name.contains("List") || name.contains("Set") || name.contains("Collection"))
    }

    override val isPojo: Boolean by lazy {
        checkIsPojo(
            isInterface = isInterface,
            isEnum = isEnum,
            isAbstract = typeElement.modifiers.contains(Modifier.ABSTRACT),
            isDataClass = false,
            annotationNames = annotations.mapNotNull { it.qualifiedName },
            isShortName = false
        )
    }

    override val superClasses: List<LsiClass> by lazy {
        val superclass = typeElement.superclass
        if (superclass is DeclaredType) {
            val element = superclass.asElement() as? TypeElement
            element?.let { listOf(_root_ide_package_.site.addzero.lsi.apt.clazz.AptLsiClass(elements, it)) } ?: emptyList()
        } else {
            emptyList()
        }
    }

    override val interfaces: List<LsiClass> by lazy {
        typeElement.interfaces.mapNotNull { interfaceType ->
            (interfaceType as? DeclaredType)?.asElement()?.let {
                _root_ide_package_.site.addzero.lsi.apt.clazz.AptLsiClass(elements, it as TypeElement)
            }
        }
    }

    override val methods: List<LsiMethod> by lazy {
        typeElement.enclosedElements
            .filterIsInstance<ExecutableElement>()
            .filter { it.kind == ElementKind.METHOD }
            .map { _root_ide_package_.site.addzero.lsi.apt.method.AptLsiMethod(elements, it) }
    }
}

