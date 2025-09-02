package com.addzero.kaleidoscope.ksp

import com.addzero.kaleidoscope.core.*
import com.google.devtools.ksp.symbol.*

private val primitiveTypes = setOf(
    "kotlin.Boolean", "kotlin.Byte", "kotlin.Short", "kotlin.Int",
    "kotlin.Long", "kotlin.Char", "kotlin.Float", "kotlin.Double"
)

/**
 * 转换类型种类
 */
private fun convertTypeKind(type: KSType): KldTypeKind {
    val qualifiedName = type.declaration.qualifiedName?.asString()
    return when (qualifiedName) {
        "kotlin.Boolean" -> KldTypeKind.BOOLEAN
        "kotlin.Byte" -> KldTypeKind.BYTE
        "kotlin.Short" -> KldTypeKind.SHORT
        "kotlin.Int" -> KldTypeKind.INT
        "kotlin.Long" -> KldTypeKind.LONG
        "kotlin.Char" -> KldTypeKind.CHAR
        "kotlin.Float" -> KldTypeKind.FLOAT
        "kotlin.Double" -> KldTypeKind.DOUBLE
        "kotlin.Unit" -> KldTypeKind.UNIT
        "kotlin.Nothing" -> KldTypeKind.NONE
        "kotlin.Array" -> KldTypeKind.ARRAY
        else -> when (type.declaration) {
            is KSTypeParameter -> KldTypeKind.TYPE_VARIABLE
            is KSClassDeclaration -> KldTypeKind.DECLARED
            else -> KldTypeKind.OTHER
        }
    }
}

/**
 * 将KSType转换为KldType
 */
fun KSType.toKldType(): KldType {
    val type = this
    val qualifiedName = type.declaration.qualifiedName?.asString()

    return when {
        qualifiedName in primitiveTypes -> type.toKldPrimitiveType()
        qualifiedName == "kotlin.Array" -> type.toKldArrayType()
        type.declaration is KSTypeParameter -> type.toKldTypeVariableType()
        qualifiedName == "kotlin.Unit" -> type.toKldNoType()
        else -> type.toKldDeclaredType()
    }
}

/**
 * 将KSType转换为KldType（避免递归）
 */
fun KSType.toKldTypeWithoutRecursion(): KldType {
    val type = this
    val qualifiedName = type.declaration.qualifiedName?.asString()

    return when {
        qualifiedName in primitiveTypes -> type.toKldPrimitiveTypeWithoutRecursion()
        qualifiedName == "kotlin.Array" -> type.toKldArrayTypeWithoutRecursion()
        type.declaration is KSTypeParameter -> type.toKldTypeVariableTypeWithoutRecursion()
        qualifiedName == "kotlin.Unit" -> type.toKldNoTypeWithoutRecursion()
        else -> type.toKldDeclaredTypeWithoutRecursion()
    }
}

/**
 * 将KSType转换为KldPrimitiveType
 */
fun KSType.toKldPrimitiveType(): KldPrimitiveType {
    val type = this
    return object : KldPrimitiveType {
        override val typeName = type.toString()
        override val qualifiedName = type.declaration.qualifiedName?.asString()
        override val isNullable = type.isMarkedNullable
        override val isPrimitive = type.declaration.qualifiedName?.asString() in primitiveTypes
        override val isArray = type.declaration.qualifiedName?.asString() == "kotlin.Array"
        override val isGeneric = type.arguments.isNotEmpty()
        override val isWildcard = false
        override val isTypeVariable = type.declaration is KSTypeParameter
        override val kldTypeKind = convertTypeKind(type)
        override val typeArguments = type.arguments.map { it.type?.resolve()?.toKldType() ?: UnknownTypeAdapter() }
        override val upperBounds = when (val declaration = type.declaration) {
            is KSTypeParameter -> declaration.bounds.map { it.resolve().toKldType() }.toList()
            else -> emptyList()
        }
        override val lowerBounds = emptyList<KldType>()
        override val declaration = type.declaration.toKldElementWithoutRecursion()
        override val annotations = type.annotations.map { it.toKldAnnotationWithoutRecursion() }.toList()
        override fun getErasedType() = this
        override fun isAssignableFrom(other: KldType): Boolean {
            return type.isAssignableFrom(other as? KSType ?: return false)
        }
        override fun isAssignableTo(other: KldType): Boolean {
            return (other as? KSType ?: return false).isAssignableFrom(type)
        }

        override fun getPrimitiveType() = null as KldType?
        override fun getWrapperType() = null as KldType?
        override fun getArrayElementType() = if (isArray) typeArguments.firstOrNull() else null
        override fun substitute(substitutions: Map<String, KldType>) = this
    }
}

/**
 * 将KSType转换为KldPrimitiveType（避免递归）
 */
fun KSType.toKldPrimitiveTypeWithoutRecursion(): KldPrimitiveType {
    val type = this
    return object : KldPrimitiveType {
        override val typeName = type.toString()
        override val qualifiedName = type.declaration.qualifiedName?.asString()
        override val isNullable = type.isMarkedNullable
        override val isPrimitive = type.declaration.qualifiedName?.asString() in primitiveTypes
        override val isArray = type.declaration.qualifiedName?.asString() == "kotlin.Array"
        override val isGeneric = type.arguments.isNotEmpty()
        override val isWildcard = false
        override val isTypeVariable = type.declaration is KSTypeParameter
        override val kldTypeKind = convertTypeKind(type)
        override val typeArguments = type.arguments.map { it.type?.resolve()?.toKldTypeWithoutRecursion() ?: UnknownTypeAdapter() }
        override val upperBounds = when (val declaration = type.declaration) {
            is KSTypeParameter -> declaration.bounds.map { it.resolve().toKldTypeWithoutRecursion() }.toList()
            else -> emptyList()
        }
        override val lowerBounds = emptyList<KldType>()
        override val declaration = type.declaration.toKldElementWithoutRecursion()
        override val annotations = type.annotations.map { it.toKldAnnotationWithoutRecursion() }.toList()
        override fun getErasedType() = this
        override fun isAssignableFrom(other: KldType): Boolean {
            return type.isAssignableFrom(other as? KSType ?: return false)
        }
        override fun isAssignableTo(other: KldType): Boolean {
            return (other as? KSType ?: return false).isAssignableFrom(type)
        }

        override fun getPrimitiveType() = null as KldType?
        override fun getWrapperType() = null as KldType?
        override fun getArrayElementType() = if (isArray) typeArguments.firstOrNull() else null
        override fun substitute(substitutions: Map<String, KldType>) = this
    }
}

/**
 * 将KSType转换为KldNoType
 */
fun KSType.toKldNoType(): KldNoType {
    val type = this
    return object : KldNoType {
        override val typeName = type.toString()
        override val qualifiedName = type.declaration.qualifiedName?.asString()
        override val isNullable = type.isMarkedNullable
        override val isPrimitive = type.declaration.qualifiedName?.asString() in primitiveTypes
        override val isArray = type.declaration.qualifiedName?.asString() == "kotlin.Array"
        override val isGeneric = type.arguments.isNotEmpty()
        override val isWildcard = false
        override val isTypeVariable = type.declaration is KSTypeParameter
        override val kldTypeKind = convertTypeKind(type)
        override val typeArguments = type.arguments.map { it.type?.resolve()?.toKldType() ?: UnknownTypeAdapter() }
        override val upperBounds = when (val declaration = type.declaration) {
            is KSTypeParameter -> declaration.bounds.map { it.resolve().toKldType() }.toList()
            else -> emptyList()
        }
        override val lowerBounds = emptyList<KldType>()
        override val declaration = type.declaration.toKldElementWithoutRecursion()
        override val annotations = type.annotations.map { it.toKldAnnotationWithoutRecursion() }.toList()
        override fun getErasedType() = this
        override fun isAssignableFrom(other: KldType) = false
        override fun isAssignableTo(other: KldType) = false
        override fun getPrimitiveType() = null as KldType?
        override fun getWrapperType() = null as KldType?
        override fun getArrayElementType() = null as KldType?
        override fun substitute(substitutions: Map<String, KldType>) = this
    }
}

/**
 * 将KSType转换为KldNoType（避免递归）
 */
fun KSType.toKldNoTypeWithoutRecursion(): KldNoType {
    val type = this
    return object : KldNoType {
        override val typeName = type.toString()
        override val qualifiedName = type.declaration.qualifiedName?.asString()
        override val isNullable = type.isMarkedNullable
        override val isPrimitive = type.declaration.qualifiedName?.asString() in primitiveTypes
        override val isArray = type.declaration.qualifiedName?.asString() == "kotlin.Array"
        override val isGeneric = type.arguments.isNotEmpty()
        override val isWildcard = false
        override val isTypeVariable = type.declaration is KSTypeParameter
        override val kldTypeKind = convertTypeKind(type)
        override val typeArguments = type.arguments.map { it.type?.resolve()?.toKldTypeWithoutRecursion() ?: UnknownTypeAdapter() }
        override val upperBounds = when (val declaration = type.declaration) {
            is KSTypeParameter -> declaration.bounds.map { it.resolve().toKldTypeWithoutRecursion() }.toList()
            else -> emptyList()
        }
        override val lowerBounds = emptyList<KldType>()
        override val declaration = type.declaration.toKldElementWithoutRecursion()
        override val annotations = type.annotations.map { it.toKldAnnotationWithoutRecursion() }.toList()
        override fun getErasedType() = this
        override fun isAssignableFrom(other: KldType) = false
        override fun isAssignableTo(other: KldType) = false
        override fun getPrimitiveType() = null as KldType?
        override fun getWrapperType() = null as KldType?
        override fun getArrayElementType() = null as KldType?
        override fun substitute(substitutions: Map<String, KldType>) = this
    }
}

/**
 * 将KSType转换为KldArrayType
 */
fun KSType.toKldArrayType(): KldArrayType {
    val type = this
    return object : KldArrayType {
        override val typeName = type.toString()
        override val qualifiedName = type.declaration.qualifiedName?.asString()
        override val isNullable = type.isMarkedNullable
        override val isPrimitive = type.declaration.qualifiedName?.asString() in primitiveTypes
        override val isArray = type.declaration.qualifiedName?.asString() == "kotlin.Array"
        override val isGeneric = type.arguments.isNotEmpty()
        override val isWildcard = false
        override val isTypeVariable = type.declaration is KSTypeParameter
        override val kldTypeKind = convertTypeKind(type)
        override val typeArguments = type.arguments.map { it.type?.resolve()?.toKldType() ?: UnknownTypeAdapter() }
        override val upperBounds = when (val declaration = type.declaration) {
            is KSTypeParameter -> declaration.bounds.map { it.resolve().toKldType() }.toList()
            else -> emptyList()
        }
        override val lowerBounds = emptyList<KldType>()
        override val declaration = type.declaration.toKldElementWithoutRecursion()
        override val annotations = type.annotations.map { it.toKldAnnotationWithoutRecursion() }.toList()
        override val kldComponentType = type.arguments.firstOrNull()?.type?.resolve()?.toKldType() ?: UnknownTypeAdapter()
        override fun getErasedType(): KldType = this
        override fun isAssignableFrom(other: KldType): Boolean = false
        override fun isAssignableTo(other: KldType): Boolean = false
        override fun getPrimitiveType(): KldType? = null
        override fun getWrapperType(): KldType? = null
        override fun getArrayElementType(): KldType? = kldComponentType
        override fun substitute(substitutions: Map<String, KldType>): KldType = this
    }
}

/**
 * 将KSType转换为KldArrayType（避免递归）
 */
fun KSType.toKldArrayTypeWithoutRecursion(): KldArrayType {
    val type = this
    return object : KldArrayType {
        override val typeName = type.toString()
        override val qualifiedName = type.declaration.qualifiedName?.asString()
        override val isNullable = type.isMarkedNullable
        override val isPrimitive = type.declaration.qualifiedName?.asString() in primitiveTypes
        override val isArray = type.declaration.qualifiedName?.asString() == "kotlin.Array"
        override val isGeneric = type.arguments.isNotEmpty()
        override val isWildcard = false
        override val isTypeVariable = type.declaration is KSTypeParameter
        override val kldTypeKind = convertTypeKind(type)
        override val typeArguments = type.arguments.map { it.type?.resolve()?.toKldTypeWithoutRecursion() ?: UnknownTypeAdapter() }
        override val upperBounds = when (val declaration = type.declaration) {
            is KSTypeParameter -> declaration.bounds.map { it.resolve().toKldTypeWithoutRecursion() }.toList()
            else -> emptyList()
        }
        override val lowerBounds = emptyList<KldType>()
        override val declaration = type.declaration.toKldElementWithoutRecursion()
        override val annotations = type.annotations.map { it.toKldAnnotationWithoutRecursion() }.toList()
        override val kldComponentType = type.arguments.firstOrNull()?.type?.resolve()?.toKldTypeWithoutRecursion() ?: UnknownTypeAdapter()
        override fun getErasedType(): KldType = this
        override fun isAssignableFrom(other: KldType): Boolean = false
        override fun isAssignableTo(other: KldType): Boolean = false
        override fun getPrimitiveType(): KldType? = null
        override fun getWrapperType(): KldType? = null
        override fun getArrayElementType(): KldType? = kldComponentType
        override fun substitute(substitutions: Map<String, KldType>): KldType = this
    }
}

/**
 * 将KSType转换为KldDeclaredType
 */
fun KSType.toKldDeclaredType(): KldDeclaredType {
    val type = this
    return object : KldDeclaredType {
        override val typeName= type.toString()
        override val qualifiedName= type.declaration.qualifiedName?.asString()
        override val isNullable= type.isMarkedNullable
        override val isPrimitive= type.declaration.qualifiedName?.asString() in primitiveTypes
        override val isArray= type.declaration.qualifiedName?.asString() == "kotlin.Array"
        override val isGeneric= type.arguments.isNotEmpty()
        override val isWildcard= false
        override val isTypeVariable= type.declaration is KSTypeParameter
        override val kldTypeKind= convertTypeKind(type)
        override val typeArguments= type.arguments.map { it.type?.resolve()?.toKldType() ?: UnknownTypeAdapter() }
        override val upperBounds= when (val declaration = type.declaration) {
            is KSTypeParameter -> declaration.bounds.map { it.resolve().toKldType() }.toList()
            else -> emptyList()
        }
        override val lowerBounds= emptyList<KldType>()
        override val declaration= type.declaration.toKldElementWithoutRecursion()
        override val annotations= type.annotations.map { it.toKldAnnotation() }.toList()
        override val kldTypeElement= (type.declaration as KSClassDeclaration).toKldTypeElement()
        override val kldEnclosingType= (type.declaration as? KSClassDeclaration)?.parentDeclaration?.let { parent ->
            if (parent is KSClassDeclaration) {
                parent.asType(emptyList()).toKldDeclaredType()
            } else null
        }
        override fun getErasedType()= this
        override fun isAssignableFrom(other: KldType) = false
        override fun isAssignableTo(other: KldType) = false
        override fun getPrimitiveType(): KldType? = null
        override fun getWrapperType(): KldType? = null
        override fun getArrayElementType(): KldType? = null
        override fun substitute(substitutions: Map<String, KldType>) = this
    }
}

/**
 * 将KSType转换为KldDeclaredType（避免递归）
 */
fun KSType.toKldDeclaredTypeWithoutRecursion(): KldDeclaredType {
    val type = this
    return object : KldDeclaredType {
        override val typeName= type.toString()
        override val qualifiedName= type.declaration.qualifiedName?.asString()
        override val isNullable= type.isMarkedNullable
        override val isPrimitive= type.declaration.qualifiedName?.asString() in primitiveTypes
        override val isArray= type.declaration.qualifiedName?.asString() == "kotlin.Array"
        override val isGeneric= type.arguments.isNotEmpty()
        override val isWildcard= false
        override val isTypeVariable= type.declaration is KSTypeParameter
        override val kldTypeKind= convertTypeKind(type)
        override val typeArguments= type.arguments.map { it.type?.resolve()?.toKldTypeWithoutRecursion() ?: UnknownTypeAdapter() }
        override val upperBounds= when (val declaration = type.declaration) {
            is KSTypeParameter -> declaration.bounds.map { it.resolve().toKldTypeWithoutRecursion() }.toList()
            else -> emptyList()
        }
        override val lowerBounds= emptyList<KldType>()
        override val declaration= type.declaration.toKldElementWithoutRecursion()
        override val annotations= type.annotations.map { it.toKldAnnotationWithoutRecursion() }.toList()
        override val kldTypeElement= (type.declaration as KSClassDeclaration).toKldTypeElementWithoutRecursion()
        override val kldEnclosingType= (type.declaration as? KSClassDeclaration)?.parentDeclaration?.let { parent ->
            if (parent is KSClassDeclaration) {
                // 修复递归调用问题，使用简化版本
                object : KldDeclaredType {
                    override val typeName = "unknown"
                    override val qualifiedName = null
                    override val isNullable = false
                    override val isPrimitive = false
                    override val isArray = false
                    override val isGeneric = false
                    override val isWildcard = false
                    override val isTypeVariable = false
                    override val kldTypeKind = KldTypeKind.OTHER
                    override val typeArguments = emptyList<KldType>()
                    override val upperBounds = emptyList<KldType>()
                    override val lowerBounds = emptyList<KldType>()
                    override val declaration = null
                    override val annotations = emptyList<KldAnnotation>()
                    override val kldTypeElement = UnknownTypeElementAdapter()
                    override val kldEnclosingType = null
                    
                    override fun getErasedType() = this
                    override fun isAssignableFrom(other: KldType) = false
                    override fun isAssignableTo(other: KldType) = false
                    override fun getPrimitiveType(): KldType? = null
                    override fun getWrapperType(): KldType? = null
                    override fun getArrayElementType(): KldType? = null
                    override fun substitute(substitutions: Map<String, KldType>) = this
                }
            } else null
        }
        override fun getErasedType()= this
        override fun isAssignableFrom(other: KldType) = false
        override fun isAssignableTo(other: KldType) = false
        override fun getPrimitiveType(): KldType? = null
        override fun getWrapperType(): KldType? = null
        override fun getArrayElementType(): KldType? = null
        override fun substitute(substitutions: Map<String, KldType>) = this
    }
}

/**
 * 将KSType转换为KldTypeVariableType
 */
fun KSType.toKldTypeVariableType(): KldTypeVariableType {
    val type = this
    return object : KldTypeVariableType {
        override val typeName= type.toString()
        override val qualifiedName= type.declaration.qualifiedName?.asString()
        override val isNullable = type.isMarkedNullable
        override val isPrimitive= type.declaration.qualifiedName?.asString() in primitiveTypes
        override val isArray= type.declaration.qualifiedName?.asString() == "kotlin.Array"
        override val isGeneric= type.arguments.isNotEmpty()
        override val isWildcard= false
        override val isTypeVariable= type.declaration is KSTypeParameter
        override val kldTypeKind= convertTypeKind(type)
        override val typeArguments= type.arguments.map { it.type?.resolve()?.toKldType() ?: UnknownTypeAdapter() }
        override val upperBounds= when (val declaration = type.declaration) {
            is KSTypeParameter -> declaration.bounds.map { it.resolve().toKldType() }.toList()
            else -> emptyList()
        }
        override val lowerBounds= emptyList<KldType>()
        override val declaration= type.declaration.toKldElementWithoutRecursion()
        override val annotations= type.annotations.map { it.toKldAnnotation() }.toList()
        override val kldTypeParameter= (type.declaration as KSTypeParameter).toKldTypeParameter()
        override fun getErasedType()= this
        override fun isAssignableFrom(other: KldType) = false
        override fun isAssignableTo(other: KldType) = false
        override fun getPrimitiveType()= null
        override fun getWrapperType()= null
        override fun getArrayElementType()= null
        override fun substitute(substitutions: Map<String, KldType>) = this
    }
}

/**
 * 将KSType转换为KldTypeVariableType（避免递归）
 */
fun KSType.toKldTypeVariableTypeWithoutRecursion(): KldTypeVariableType {
    val type = this
    return object : KldTypeVariableType {
        override val typeName= type.toString()
        override val qualifiedName= type.declaration.qualifiedName?.asString()
        override val isNullable = type.isMarkedNullable
        override val isPrimitive= type.declaration.qualifiedName?.asString() in primitiveTypes
        override val isArray= type.declaration.qualifiedName?.asString() == "kotlin.Array"
        override val isGeneric= type.arguments.isNotEmpty()
        override val isWildcard= false
        override val isTypeVariable= type.declaration is KSTypeParameter
        override val kldTypeKind= convertTypeKind(type)
        override val typeArguments= type.arguments.map { it.type?.resolve()?.toKldTypeWithoutRecursion() ?: UnknownTypeAdapter() }
        override val upperBounds= when (val declaration = type.declaration) {
            is KSTypeParameter -> declaration.bounds.map { it.resolve().toKldTypeWithoutRecursion() }.toList()
            else -> emptyList()
        }
        override val lowerBounds= emptyList<KldType>()
        override val declaration= type.declaration.toKldElementWithoutRecursion()
        override val annotations= type.annotations.map { it.toKldAnnotationWithoutRecursion() }.toList()
        override val kldTypeParameter= (type.declaration as KSTypeParameter).toKldTypeParameterWithoutRecursion()
        override fun getErasedType()= this
        override fun isAssignableFrom(other: KldType) = false
        override fun isAssignableTo(other: KldType) = false
        override fun getPrimitiveType()= null
        override fun getWrapperType()= null
        override fun getArrayElementType()= null
        override fun substitute(substitutions: Map<String, KldType>) = this
    }
}

/**
 * 创建Unit类型的简化实现
 */
fun createUnitType(): KldType {
    return UnknownTypeAdapter() // 简化实现，返回Unit类型适配器
}

/**
 * 未知类型适配器
 */
class UnknownTypeAdapter : KldType {
    override val typeName= "unknown"
    override val qualifiedName= null
    override val isNullable= false
    override val isPrimitive= false
    override val isArray= false
    override val isGeneric= false
    override val isWildcard= false
    override val isTypeVariable= false
    override val kldTypeKind= KldTypeKind.OTHER
    override val typeArguments= emptyList<KldType>()
    override val upperBounds= emptyList<KldType>()
    override val lowerBounds= emptyList<KldType>()
    override val declaration= null
    override val annotations= emptyList<KldAnnotation>()

    override fun getErasedType()= this
    override fun isAssignableFrom(other: KldType) = false
    override fun isAssignableTo(other: KldType) = false
    override fun getPrimitiveType()= null
    override fun getWrapperType()= null
    override fun getArrayElementType()= null
    override fun substitute(substitutions: Map<String, KldType>) = this
}

/**
 * 未知类型元素适配器
 */
class UnknownTypeElementAdapter : KldTypeElement {
    override val simpleName = "unknown"
    override val qualifiedName = ""  // 修改为String类型而不是null
    override val kldElementType = KldElementType.OTHER
    override val packageName = null
    override val enclosingElement = null
    override val enclosedElements = emptyList<KldElement>()
    override val annotations = emptyList<KldAnnotation>()
    override val kldModifiers = emptySet<KldModifier>()
    override val documentation = null
    override val kldSourceFile = null
    override val typeKind = KldTypeElementKind.CLASS
    override val superclass = null
    override val interfaces = emptyList<KldType>()
    override val kldTypeParameters = emptyList<KldTypeParameter>()
    override val nestedTypes = emptyList<KldTypeElement>()
    override val fields = emptyList<KldVariableElement>()
    override val methods = emptyList<KldExecutableElement>()
    override val constructors = emptyList<KldExecutableElement>()
    override val properties = emptyList<KldPropertyElement>()
    override val companionObject = null
    override val isInner = false
    override val isLocal = false
    override val isAnonymous = false
    
    override fun getMethod(name: String, parameterTypes: List<KldType>): KldExecutableElement? = null
    override fun getMethods(name: String): List<KldExecutableElement> = emptyList()
    override fun getField(name: String): KldVariableElement? = null
    override fun getProperty(name: String): KldPropertyElement? = null
    override fun getDefaultConstructor(): KldExecutableElement? = null
    override fun getAnnotation(qualifiedName: String): KldAnnotation? = null
    override fun hasAnnotation(simpleName: String): Boolean = false
    override fun hasAnnotationByQualifiedName(qualifiedName: String): Boolean = false
    override fun getAnnotations(qualifiedName: String): List<KldAnnotation> = emptyList()
    override fun isValid(): Boolean = true
}

/**
 * 将KSAnnotation转换为KldAnnotation
 */
fun KSAnnotation.toKldAnnotation(): KldAnnotation {
    val annotation = this
    // 预先解析注解类型以避免在属性访问时重复解析
    val resolvedType = annotation.annotationType.resolve()
    val declaration = resolvedType.declaration
    
    return object : KldAnnotation {
        override val annotationType= resolvedType.toKldTypeWithoutRecursion()
        override val simpleName= declaration.simpleName.asString()
        override val qualifiedName= declaration.qualifiedName?.asString()
        override val arguments by lazy {
            annotation.arguments.associate { arg ->
                (arg.name?.asString() ?: "value") to arg.value.toAnnotationValueWithoutRecursion()
            }
        }
        override val argumentList by lazy {
            annotation.arguments.map { arg ->
                KldAnnotationArgument(
                    name = arg.name?.asString() ?: "value",
                    value = arg.value.toAnnotationValueWithoutRecursion()
                )
            }
        }
        override fun getArgument(name: String): KldAnnotationValue? {
            return arguments[name]
        }
        override fun getArgumentOrDefault(name: String, defaultValue: KldAnnotationValue): KldAnnotationValue {
            return getArgument(name) ?: defaultValue
        }
        override fun hasArgument(name: String): Boolean {
            return arguments.containsKey(name)
        }
    }
}

/**
 * 将KSAnnotation转换为KldAnnotation（避免递归）
 */
fun KSAnnotation.toKldAnnotationWithoutRecursion(): KldAnnotation {
    val annotation = this
    // 预先解析注解类型以避免在属性访问时重复解析
    val resolvedType = annotation.annotationType.resolve()
    val declaration = resolvedType.declaration
    
    return object : KldAnnotation {
        override val annotationType= resolvedType.toKldTypeWithoutRecursion()
        override val simpleName= declaration.simpleName.asString()
        override val qualifiedName= declaration.qualifiedName?.asString()
        override val arguments = annotation.arguments.associate { arg ->
            (arg.name?.asString() ?: "value") to arg.value.toAnnotationValueWithoutRecursion()
        }
        override val argumentList = annotation.arguments.map { arg ->
            KldAnnotationArgument(
                name = arg.name?.asString() ?: "value",
                value = arg.value.toAnnotationValueWithoutRecursion()
            )
        }
        override fun getArgument(name: String): KldAnnotationValue? {
            return arguments[name]
        }
        override fun getArgumentOrDefault(name: String, defaultValue: KldAnnotationValue): KldAnnotationValue {
            return getArgument(name) ?: defaultValue
        }
        override fun hasArgument(name: String): Boolean {
            return arguments.containsKey(name)
        }
    }
}

/**
 * 将注解值转换为AnnotationValue
 */
fun Any?.toAnnotationValue(): KldAnnotationValue {
    val value = this
    return when (value) {
        is String -> KldAnnotationValue.StringValueKld(
            value,
            UnknownTypeAdapter()
        )

        is Boolean, is Byte, is Short, is Int, is Long, is Char, is Float, is Double ->
            KldAnnotationValue.Primitive(
                value,
                UnknownTypeAdapter()
            )

        is KSType -> KldAnnotationValue.KldClassValue(
            type = UnknownTypeAdapter(),  // 避免递归调用
            qualifiedName = value.declaration.qualifiedName?.asString() ?: "unknown",
            simpleName = value.declaration.simpleName.asString()
        )

        is KSName -> KldAnnotationValue.KldEnumValue(
            value.asString(),
            UnknownTypeAdapter(),
            UnknownTypeAdapter(),
            object : KldElement {
                override val simpleName= value.asString()
                override val qualifiedName= null
                override val kldElementType = KldElementType.ENUM_CONSTANT
                override val packageName = null
                override val enclosingElement = null
                override val enclosedElements= emptyList<KldElement>()
                override val annotations= emptyList<KldAnnotation>()
                override val kldModifiers= emptySet<KldModifier>()
                override val documentation= null
                override val kldSourceFile= null
                override fun getAnnotation(qualifiedName: String) = null
                override fun hasAnnotation(simpleName: String) = false
                override fun hasAnnotationByQualifiedName(qualifiedName: String) = false
                override fun getAnnotations(qualifiedName: String) = emptyList<KldAnnotation>()
                override fun isValid(): Boolean = true
            }
        )

        is List<*> -> KldAnnotationValue.KldArrayValue(
            value.map { it.toAnnotationValueWithoutRecursion() },
            UnknownTypeAdapter()
        )

        else -> KldAnnotationValue.KldUnknown(
            value,
            UnknownTypeAdapter()
        )
    }
}

/**
 * 将注解值转换为AnnotationValue（避免递归）
 */
fun Any?.toAnnotationValueWithoutRecursion(): KldAnnotationValue {
    val value = this
    return when (value) {
        is String -> KldAnnotationValue.StringValueKld(
            value,
            UnknownTypeAdapter()
        )

        is Boolean, is Byte, is Short, is Int, is Long, is Char, is Float, is Double ->
            KldAnnotationValue.Primitive(
                value,
                UnknownTypeAdapter()
            )

        is KSType -> KldAnnotationValue.KldClassValue(
            type = UnknownTypeAdapter(),
            qualifiedName = value.declaration.qualifiedName?.asString() ?: "unknown",
            simpleName = value.declaration.simpleName.asString()
        )

        is KSName -> KldAnnotationValue.KldEnumValue(
            value.asString(),
            UnknownTypeAdapter(),
            UnknownTypeAdapter(),
            object : KldElement {
                override val simpleName= value.asString()
                override val qualifiedName= null
                override val kldElementType = KldElementType.ENUM_CONSTANT
                override val packageName = null
                override val enclosingElement = null
                override val enclosedElements= emptyList<KldElement>()
                override val annotations= emptyList<KldAnnotation>()
                override val kldModifiers= emptySet<KldModifier>()
                override val documentation= null
                override val kldSourceFile= null
                override fun getAnnotation(qualifiedName: String) = null
                override fun hasAnnotation(simpleName: String) = false
                override fun hasAnnotationByQualifiedName(qualifiedName: String) = false
                override fun getAnnotations(qualifiedName: String) = emptyList<KldAnnotation>()
                override fun isValid(): Boolean = true
            }
        )

        is List<*> -> KldAnnotationValue.KldArrayValue(
            emptyList(), // 避免递归调用
            UnknownTypeAdapter()
        )

        else -> KldAnnotationValue.KldUnknown(
            value,
            UnknownTypeAdapter()
        )
    }
}

