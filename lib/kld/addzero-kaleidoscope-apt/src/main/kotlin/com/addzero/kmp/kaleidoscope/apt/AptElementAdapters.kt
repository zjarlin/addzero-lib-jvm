package com.addzero.kmp.kaleidoscope.apt

import com.addzero.kmp.kaleidoscope.core.*
import javax.lang.model.element.*
import javax.lang.model.type.TypeKind

/**
 * 将Element转换为KldElement
 */
fun Element.toKldElement(): KldElement {
    return when (this.kind) {
        ElementKind.PACKAGE -> this.toKldPackageElement()
        ElementKind.CLASS, ElementKind.INTERFACE, ElementKind.ENUM, ElementKind.ANNOTATION_TYPE -> this.toKldTypeElement()

        ElementKind.METHOD, ElementKind.CONSTRUCTOR -> this.toKldExecutableElement()

        ElementKind.FIELD, ElementKind.ENUM_CONSTANT, ElementKind.PARAMETER, ElementKind.LOCAL_VARIABLE, ElementKind.EXCEPTION_PARAMETER, ElementKind.RESOURCE_VARIABLE -> this.toVariableElement()

        ElementKind.TYPE_PARAMETER -> this.toTypeParameter()
        else -> this.toUnknownElement()
    }
}

/**
 * 将Element转换为KldPackageElement
 */
fun Element.toKldPackageElement(): KldPackageElement {
    val element = this as PackageElement
    return object : KldPackageElement {
        override val simpleName = element.simpleName.toString()
        override val qualifiedName = element.qualifiedName?.toString()
        override val kldElementType = KldElementType.PACKAGE
        override val packageName = element.qualifiedName?.toString()
        override val enclosingElement = null
        override val enclosedElements = element.enclosedElements.map { it.toKldElement() }
        override val annotations = emptyList<KldAnnotation>()
        override val kldModifiers = emptySet<KldModifier>()
        override val documentation = null
        override val kldSourceFile = null
        override val isUnnamed = element.isUnnamed
        override fun getAnnotation(qualifiedName: String) = null as KldAnnotation?
        override fun hasAnnotation(simpleName: String) = false
        override fun hasAnnotationByQualifiedName(qualifiedName: String) = false
        override fun getAnnotations(qualifiedName: String) = emptyList<KldAnnotation>()
        override fun isValid() = true
        override fun getSubpackages() = emptyList<KldPackageElement>()
        override fun getTypes() = element.enclosedElements.filterIsInstance<TypeElement>().map { it.toKldTypeElement() }
    }
}


/**
 * 将Element转换为KldTypeElement
 */
fun Element.toKldTypeElement(): KldTypeElement {
    val element = this as TypeElement
    return object : KldTypeElement {
        override val simpleName = element.simpleName.toString()
        override val qualifiedName = element.qualifiedName?.toString() ?: ""
        override val kldElementType = convertElementKind(element.kind)
        override val packageName = getPackageName(element)
        override val enclosingElement = element.enclosingElement?.toKldElement()
        override val enclosedElements = element.enclosedElements.map { it.toKldElement() }
        override val annotations = element.annotationMirrors.map { it.toKldAnnotation() }
        override val kldModifiers = element.modifiers.map { convertModifier(it) }.toSet()
        override val documentation = null
        override val kldSourceFile = null
        override val typeKind = when (element.kind) {
            ElementKind.CLASS -> KldTypeElementKind.CLASS
            ElementKind.INTERFACE -> KldTypeElementKind.INTERFACE
            ElementKind.ENUM -> KldTypeElementKind.ENUM
            ElementKind.ANNOTATION_TYPE -> KldTypeElementKind.ANNOTATION_TYPE
            else -> KldTypeElementKind.CLASS
        }
        override val superclass = if (element.superclass.kind != TypeKind.NONE) {
            element.superclass.toKldType()
        } else null
        override val interfaces = element.interfaces.map { it.toKldType() }
        override val kldTypeParameters = element.typeParameters.map { it.toTypeParameter() }
        override val nestedTypes = element.enclosedElements.filterIsInstance<TypeElement>().map { it.toKldTypeElement() }
        override val fields = element.enclosedElements.filter { it.kind == ElementKind.FIELD }.map { it.toVariableElement() }.toList()
        override val methods = element.enclosedElements.filter { it.kind == ElementKind.METHOD }.map { it.toKldExecutableElement() }.toList()
        override val constructors = element.enclosedElements.filter { it.kind == ElementKind.CONSTRUCTOR }.map { it.toKldExecutableElement() }.toList()
        override val properties = emptyList<KldPropertyElement>()
        override val companionObject = null
        override val isInner = element.nestingKind == NestingKind.MEMBER
        override val isLocal = element.nestingKind == NestingKind.LOCAL
        override val isAnonymous = element.nestingKind == NestingKind.ANONYMOUS
        override fun getMethod(name: String, parameterTypes: List<KldType>): KldExecutableElement? {
            return methods.find { method ->
                method.simpleName == name && method.parameters.map { it.type } == parameterTypes
            }
        }

        override fun getMethods(name: String): List<KldExecutableElement> {
            return methods.filter { it.simpleName == name }
        }

        override fun getField(name: String): KldVariableElement? {
            return fields.find { it.simpleName == name }
        }

        override fun getProperty(name: String): KldPropertyElement? = null
        override fun getDefaultConstructor(): KldExecutableElement? {
            return constructors.find { it.parameters.isEmpty() }
        }

        override fun getAnnotation(qualifiedName: String): KldAnnotation? {
            val mirror = element.annotationMirrors.find {
                it.annotationType.toString() == qualifiedName
            } ?: return null
            return mirror.toKldAnnotation()
        }

        override fun hasAnnotation(simpleName: String): Boolean {
            return element.annotationMirrors.any {
                val typeElement = it.annotationType.asElement() as TypeElement
                typeElement.simpleName.toString() == simpleName
            }
        }

        override fun hasAnnotationByQualifiedName(qualifiedName: String): Boolean {
            return element.annotationMirrors.any {
                it.annotationType.toString() == qualifiedName
            }
        }

        override fun getAnnotations(qualifiedName: String): List<KldAnnotation> {
            return element.annotationMirrors.filter { it.annotationType.toString() == qualifiedName }.map { it.toKldAnnotation() }
        }

        override fun isValid(): Boolean = true
    }
}

/**
 * 将Element转换为KldExecutableElement
 */
fun Element.toKldExecutableElement(): KldExecutableElement {
    val element = this as ExecutableElement
    return object : KldExecutableElement {
        override val simpleName = element.simpleName.toString()
        override val qualifiedName = when (element) {
            is QualifiedNameable -> element.qualifiedName?.toString()
            else -> null
        }
        override val kldElementType = convertElementKind(element.kind)
        override val packageName = getPackageName(element)
        override val enclosingElement = element.enclosingElement?.toKldElement()
        override val enclosedElements = emptyList<KldElement>()
        override val annotations = element.annotationMirrors.map { it.toKldAnnotation() }
        override val kldModifiers = element.modifiers.map { convertModifier(it) }.toSet()
        override val documentation = null
        override val kldSourceFile = null
        override val returnType = element.returnType.toKldType()
        override val parameters = element.parameters.map { it.toVariableElement() }
        override val kldTypeParameters = element.typeParameters.map { it.toTypeParameter() }
        override val thrownTypes = element.thrownTypes.map { it.toKldType() }
        override val receiverType = null
        override val isVarArgs = element.isVarArgs
        override val isAbstract = element.modifiers.contains(Modifier.ABSTRACT)
        override val isDefault = element.modifiers.contains(Modifier.DEFAULT)
        override val isSuspend = false
        override val isInline = false
        override val isOperator = false
        override val isInfix = false
        override val signature = "${element.simpleName}(${parameters.joinToString { it.type.typeName }})"
        override fun getAnnotation(qualifiedName: String): KldAnnotation? {
            val mirror = element.annotationMirrors.find {
                it.annotationType.toString() == qualifiedName
            } ?: return null
            return mirror.toKldAnnotation()
        }

        override fun hasAnnotation(simpleName: String): Boolean {
            return element.annotationMirrors.any {
                val typeElement = it.annotationType.asElement() as TypeElement
                typeElement.simpleName.toString() == simpleName
            }
        }

        override fun hasAnnotationByQualifiedName(qualifiedName: String): Boolean {
            return element.annotationMirrors.any {
                it.annotationType.toString() == qualifiedName
            }
        }

        override fun getAnnotations(qualifiedName: String): List<KldAnnotation> {
            return element.annotationMirrors.filter { it.annotationType.toString() == qualifiedName }.map { it.toKldAnnotation() }
        }

        override fun isValid(): Boolean = true
    }
}

/**
 * 将Element转换为VariableElement
 */
fun Element.toVariableElement(): KldVariableElement {
    val element = this as VariableElement
    return object : KldVariableElement {
        override val simpleName = element.simpleName.toString()
        override val qualifiedName = when (element) {
            is QualifiedNameable -> element.qualifiedName?.toString()
            else -> null
        }
        override val kldElementType = convertElementKind(element.kind)
        override val packageName = getPackageName(element)
        override val enclosingElement = element.enclosingElement?.toKldElement()
        override val enclosedElements = emptyList<KldElement>()
        override val annotations = element.annotationMirrors.map { it.toKldAnnotation() }
        override val kldModifiers = element.modifiers.map { convertModifier(it) }.toSet()
        override val documentation = null
        override val kldSourceFile = null
        override val type = element.asType().toKldType()
        override val constantValue = element.constantValue
        override val isConstant = element.constantValue != null
        override val isMutable = !element.modifiers.contains(Modifier.FINAL)
        override val isLateinit = false
        override val kldVariableKind = when (element.kind) {
            ElementKind.FIELD -> KldVariableKind.FIELD
            ElementKind.PARAMETER -> KldVariableKind.PARAMETER
            ElementKind.LOCAL_VARIABLE -> KldVariableKind.LOCAL_VARIABLE
            ElementKind.EXCEPTION_PARAMETER -> KldVariableKind.EXCEPTION_PARAMETER
            ElementKind.RESOURCE_VARIABLE -> KldVariableKind.RESOURCE_VARIABLE
            ElementKind.ENUM_CONSTANT -> KldVariableKind.ENUM_CONSTANT
            else -> KldVariableKind.FIELD
        }

        override fun getAnnotation(qualifiedName: String): KldAnnotation? {
            val mirror = element.annotationMirrors.find {
                it.annotationType.toString() == qualifiedName
            } ?: return null
            return mirror.toKldAnnotation()
        }

        override fun hasAnnotation(simpleName: String): Boolean {
            return element.annotationMirrors.any {
                val typeElement = it.annotationType.asElement() as TypeElement
                typeElement.simpleName.toString() == simpleName
            }
        }

        override fun hasAnnotationByQualifiedName(qualifiedName: String): Boolean {
            return element.annotationMirrors.any {
                it.annotationType.toString() == qualifiedName
            }
        }

        override fun getAnnotations(qualifiedName: String): List<KldAnnotation> {
            return element.annotationMirrors.filter { it.annotationType.toString() == qualifiedName }.map { it.toKldAnnotation() }
        }

        override fun isValid(): Boolean = true
    }
}

/**
 * 将Element转换为TypeParameter
 */
fun Element.toTypeParameter(): KldTypeParameter {
    val element = this as TypeParameterElement
    return object : KldTypeParameter {
        override val simpleName = element.simpleName.toString()
        override val qualifiedName = when (element) {
            is QualifiedNameable -> element.qualifiedName?.toString()
            else -> null
        }
        override val kldElementType = convertElementKind(element.kind)
        override val packageName = getPackageName(element)
        override val enclosingElement = element.enclosingElement?.toKldElement()
        override val enclosedElements = emptyList<KldElement>()
        override val annotations = element.annotationMirrors.map { it.toKldAnnotation() }
        override val kldModifiers = element.modifiers.map { convertModifier(it) }.toSet()
        override val documentation = null
        override val kldSourceFile = null
        override val name = element.simpleName.toString()
        override val bounds = element.bounds.map { it.toKldType() }
        override val kldVariance = KldVariance.INVARIANT
        override val isReified = false
        override fun getAnnotation(qualifiedName: String): KldAnnotation? {
            val mirror = element.annotationMirrors.find {
                it.annotationType.toString() == qualifiedName
            } ?: return null
            return mirror.toKldAnnotation()
        }

        override fun hasAnnotation(simpleName: String): Boolean {
            return element.annotationMirrors.any {
                val typeElement = it.annotationType.asElement() as TypeElement
                typeElement.simpleName.toString() == simpleName
            }
        }

        override fun hasAnnotationByQualifiedName(qualifiedName: String): Boolean {
            return element.annotationMirrors.any {
                it.annotationType.toString() == qualifiedName
            }
        }

        override fun getAnnotations(qualifiedName: String): List<KldAnnotation> {
            return element.annotationMirrors.filter { it.annotationType.toString() == qualifiedName }.map { it.toKldAnnotation() }
        }

        override fun isValid(): Boolean = true
    }
}

/**
 * 将Element转换为UnknownElement
 */
fun Element.toUnknownElement(): KldElement {
    val element = this
    return object : KldElement {
        override val simpleName= element.simpleName.toString()
        override val qualifiedName= when (element) {
            is QualifiedNameable -> element.qualifiedName?.toString()
            else -> null
        }
        override val kldElementType = convertElementKind(element.kind)
        override val packageName = getPackageName(element)
        override val enclosingElement = element.enclosingElement?.toKldElement()
        override val enclosedElements = element.enclosedElements.map { it.toKldElement() }
        override val annotations = element.annotationMirrors.map { it.toKldAnnotation() }
        override val kldModifiers = element.modifiers.map { convertModifier(it) }.toSet()
        override val documentation = null as String?
        override val kldSourceFile = null as KldSourceFile?
        override fun getAnnotation(qualifiedName: String): KldAnnotation? {
            val mirror = element.annotationMirrors.find {
                it.annotationType.toString() == qualifiedName
            } ?: return null
            return mirror.toKldAnnotation()
        }

        override fun hasAnnotation(simpleName: String): Boolean {
            return element.annotationMirrors.any {
                val typeElement = it.annotationType.asElement() as TypeElement
                typeElement.simpleName.toString() == simpleName
            }
        }

        override fun hasAnnotationByQualifiedName(qualifiedName: String): Boolean {
            return element.annotationMirrors.any {
                it.annotationType.toString() == qualifiedName
            }
        }

        override fun getAnnotations(qualifiedName: String): List<KldAnnotation> {
            return element.annotationMirrors.filter { it.annotationType.toString() == qualifiedName }.map { it.toKldAnnotation() }
        }

        override fun isValid(): Boolean = true
    }
}

/**
 * 获取元素的包名
 */
private fun getPackageName(element: Element): String? {
    return when (element) {
        is PackageElement -> element.qualifiedName.toString()
        else -> {
            // 获取包含此元素的包
            var enclosing = element.enclosingElement
            while (enclosing != null && enclosing.kind != ElementKind.PACKAGE) {
                enclosing = enclosing.enclosingElement
            }
            (enclosing as? PackageElement)?.qualifiedName?.toString()
        }
    }
}

/**
 * 转换元素类型
 */
private fun convertElementKind(kind: ElementKind): KldElementType {
    return when (kind) {
        ElementKind.PACKAGE -> KldElementType.PACKAGE
        ElementKind.ENUM -> KldElementType.ENUM
        ElementKind.CLASS -> KldElementType.CLASS
        ElementKind.ANNOTATION_TYPE -> KldElementType.ANNOTATION_TYPE
        ElementKind.INTERFACE -> KldElementType.INTERFACE
        ElementKind.ENUM_CONSTANT -> KldElementType.ENUM_CONSTANT
        ElementKind.FIELD -> KldElementType.FIELD
        ElementKind.PARAMETER -> KldElementType.PARAMETER
        ElementKind.LOCAL_VARIABLE -> KldElementType.LOCAL_VARIABLE
        ElementKind.EXCEPTION_PARAMETER -> KldElementType.EXCEPTION_PARAMETER
        ElementKind.METHOD -> KldElementType.METHOD
        ElementKind.CONSTRUCTOR -> KldElementType.CONSTRUCTOR
        ElementKind.TYPE_PARAMETER -> KldElementType.TYPE_PARAMETER
        ElementKind.RESOURCE_VARIABLE -> KldElementType.RESOURCE_VARIABLE
        else -> KldElementType.OTHER
    }
}

/**
 * 转换修饰符
 */
private fun convertModifier(modifier: Modifier): KldModifier {
    return when (modifier) {
        Modifier.PUBLIC -> KldModifier.PUBLIC
        Modifier.PROTECTED -> KldModifier.PROTECTED
        Modifier.PRIVATE -> KldModifier.PRIVATE
        Modifier.ABSTRACT -> KldModifier.ABSTRACT
        Modifier.DEFAULT -> KldModifier.DEFAULT
        Modifier.STATIC -> KldModifier.STATIC
        Modifier.FINAL -> KldModifier.FINAL
        Modifier.TRANSIENT -> KldModifier.TRANSIENT
        Modifier.VOLATILE -> KldModifier.VOLATILE
        Modifier.SYNCHRONIZED -> KldModifier.SYNCHRONIZED
        Modifier.NATIVE -> KldModifier.NATIVE
        Modifier.STRICTFP -> KldModifier.STRICTFP
        Modifier.SEALED -> KldModifier.SEALED
        Modifier.NON_SEALED -> KldModifier.OPEN // 映射为OPEN
        else -> KldModifier.PUBLIC
    }
}
