package com.addzero.kaleidoscope.apt

import com.addzero.kaleidoscope.core.*
import javax.lang.model.element.TypeElement
import javax.lang.model.element.TypeParameterElement
import javax.lang.model.type.*
import javax.lang.model.type.TypeKind.*

/**
 * 将TypeMirror转换为KldType
 */
fun TypeMirror.toKldType(): KldType {
    return when (this.kind) {
        BOOLEAN, BYTE, SHORT, INT, LONG, CHAR, FLOAT, DOUBLE -> (this as PrimitiveType).toKldPrimitiveType()

        VOID, NONE -> (this as NoType).toKldNoType()

        ARRAY -> (this as ArrayType).toKldArrayType()

        DECLARED -> (this as DeclaredType).toKldDeclaredType()

        ERROR -> (this as ErrorType).toKldErrorType()

        TYPEVAR -> (this as TypeVariable).toKldTypeVariableType()

        WILDCARD -> (this as WildcardType).toKldWildcardType()

        EXECUTABLE -> (this as ExecutableType).toKldExecutableType()

        UNION -> (this as UnionType).toKldUnionType()

        INTERSECTION -> (this as IntersectionType).toKldIntersectionType()

        else -> this.toKldUnknownType()
    }
}

/**
 * 将PrimitiveType转换为KldPrimitiveType
 */
fun PrimitiveType.toKldPrimitiveType(): KldPrimitiveType {
    val type = this
    return object : KldPrimitiveType {
        override val typeName= type.toString()
        override val qualifiedName= when (type) {
            is DeclaredType -> (type.asElement() as? TypeElement)?.qualifiedName?.toString()
            else -> null
        }
        override val isNullable= false
        override val isPrimitive= type.kind.isPrimitive
        override val isArray= type.kind == ARRAY
        override val isGeneric= when (type) {
            is DeclaredType -> type.typeArguments.isNotEmpty()
            else -> false
        }
        override val isWildcard= type.kind == WILDCARD
        override val isTypeVariable= type.kind == TYPEVAR
        override val kldTypeKind= convertTypeKind(type.kind)
        override val typeArguments= when (type) {
            is DeclaredType -> type.typeArguments.map { it.toKldType() }
            else -> emptyList()
        }
        override val upperBounds= when (type) {
            is TypeVariable -> type.upperBound?.let { listOf(it.toKldType()) } ?: emptyList()
            is WildcardType -> type.extendsBound?.let { listOf(it.toKldType()) } ?: emptyList()
            else -> emptyList()
        }
        override val lowerBounds= when (type) {
            is WildcardType -> type.superBound?.let { listOf(it.toKldType()) } ?: emptyList()
            else -> emptyList()
        }
        override val declaration= when (type) {
            is DeclaredType -> type.asElement()?.toKldElement()
            else -> null
        }
        override val annotations= type.annotationMirrors.map { it.toKldAnnotation() }
        override fun getErasedType()= this
        override fun isAssignableFrom(other: KldType) = false
        override fun isAssignableTo(other: KldType) = false
        override fun getPrimitiveType() = null
        override fun getWrapperType() = null
        override fun getArrayElementType() = null
        override fun substitute(substitutions: Map<String, KldType>) = this
    }
}

/**
 * 将NoType转换为KldNoType
 */
fun NoType.toKldNoType(): KldNoType {
    val type = this
    return object : KldNoType {
        override val typeName= type.toString()
        override val qualifiedName= when (type) {
            is DeclaredType -> (type.asElement() as? TypeElement)?.qualifiedName?.toString()
            else -> null
        }
        override val isNullable= false
        override val isPrimitive= type.kind.isPrimitive
        override val isArray= type.kind == ARRAY
        override val isGeneric= when (type) {
            is DeclaredType -> type.typeArguments.isNotEmpty()
            else -> false
        }
        override val isWildcard= type.kind == WILDCARD
        override val isTypeVariable= type.kind == TYPEVAR
        override val kldTypeKind= convertTypeKind(type.kind)
        override val typeArguments= when (type) {
            is DeclaredType -> type.typeArguments.map { it.toKldType() }
            else -> emptyList()
        }
        override val upperBounds= when (type) {
            is TypeVariable -> type.upperBound?.let { listOf(it.toKldType()) } ?: emptyList()
            is WildcardType -> type.extendsBound?.let { listOf(it.toKldType()) } ?: emptyList()
            else -> emptyList()
        }
        override val lowerBounds= when (type) {
            is WildcardType -> type.superBound?.let { listOf(it.toKldType()) } ?: emptyList()
            else -> emptyList()
        }
        override val declaration= when (type) {
            is DeclaredType -> type.asElement()?.toKldElement()
            else -> null
        }
        override val annotations= type.annotationMirrors.map { it.toKldAnnotation() }
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
 * 将ArrayType转换为KldArrayType
 */
fun ArrayType.toKldArrayType(): KldArrayType {
    val type = this
    return object : KldArrayType {
        override val typeName= type.toString()
        override val qualifiedName= when (type) {
            is DeclaredType -> (type.asElement() as? TypeElement)?.qualifiedName?.toString()
            else -> null
        }
        override val isNullable= false
        override val isPrimitive= type.kind.isPrimitive
        override val isArray= type.kind == ARRAY
        override val isGeneric= when (type) {
            is DeclaredType -> type.typeArguments.isNotEmpty()
            else -> false
        }
        override val isWildcard= type.kind == WILDCARD
        override val isTypeVariable= type.kind == TYPEVAR
        override val kldTypeKind= convertTypeKind(type.kind)
        override val typeArguments= when (type) {
            is DeclaredType -> type.typeArguments.map { it.toKldType() }
            else -> emptyList()
        }
        override val upperBounds= when (type) {
            is TypeVariable -> type.upperBound?.let { listOf(it.toKldType()) } ?: emptyList()
            is WildcardType -> type.extendsBound?.let { listOf(it.toKldType()) } ?: emptyList()
            else -> emptyList()
        }
        override val lowerBounds= when (type) {
            is WildcardType -> type.superBound?.let { listOf(it.toKldType()) } ?: emptyList()
            else -> emptyList()
        }
        override val declaration= when (type) {
            is DeclaredType -> type.asElement()?.toKldElement()
            else -> null
        }
        override val annotations= type.annotationMirrors.map { it.toKldAnnotation() }
        override fun getErasedType()= this
        override fun isAssignableFrom(other: KldType) = false
        override fun isAssignableTo(other: KldType) = false
        override fun getPrimitiveType(): KldType? = null
        override fun getWrapperType(): KldType? = null
        override fun getArrayElementType() = kldComponentType
        override fun substitute(substitutions: Map<String, KldType>) = this
        override val kldComponentType
            get() = type.componentType.toKldType()
    }
}

/**
 * 将DeclaredType转换为KldDeclaredType
 */
fun DeclaredType.toKldDeclaredType(): KldDeclaredType {
    val type = this
    return object : KldDeclaredType {
        override val typeName= type.toString()
        override val qualifiedName= when (type) {
            else -> (type.asElement() as? TypeElement)?.qualifiedName?.toString()
        }
        override val isNullable= false
        override val isPrimitive= type.kind.isPrimitive
        override val isArray= type.kind == ARRAY
        override val isGeneric= when (type) {
            else -> type.typeArguments.isNotEmpty()
        }
        override val isWildcard= type.kind == WILDCARD
        override val isTypeVariable= type.kind == TYPEVAR
        override val kldTypeKind= convertTypeKind(type.kind)
        override val typeArguments= when (type) {
            else -> type.typeArguments.map { it.toKldType() }
        }
        override val upperBounds= when (type) {
            is TypeVariable -> type.upperBound?.let { listOf(it.toKldType()) } ?: emptyList()
            is WildcardType -> type.extendsBound?.let { listOf(it.toKldType()) } ?: emptyList()
            else -> emptyList()
        }
        override val lowerBounds= when (type) {
            is WildcardType -> type.superBound?.let { listOf(it.toKldType()) } ?: emptyList()
            else -> emptyList()
        }
        override val declaration= when (type) {
            else -> type.asElement()?.toKldElement()
        }
        override val annotations= type.annotationMirrors.map { it.toKldAnnotation() }
        override val kldTypeElement= (type.asElement() as TypeElement).toKldTypeElement()
        override val kldEnclosingType
            get() = (type.enclosingType as? DeclaredType)?.toKldDeclaredType()

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
 * 将ErrorType转换为KldErrorType
 */
fun ErrorType.toKldErrorType(): KldErrorType {
    val type = this
    return object : KldErrorType {
        override val typeName = type.toString()
        override val qualifiedName= when (type) {
            else -> (type.asElement() as? TypeElement)?.qualifiedName?.toString()
        }
        override val isNullable= false
        override val isPrimitive= type.kind.isPrimitive
        override val isArray= type.kind == ARRAY
        override val isGeneric= when (type) {
            else -> type.typeArguments.isNotEmpty()
        }
        override val isWildcard= type.kind == WILDCARD
        override val isTypeVariable= type.kind == TYPEVAR
        override val kldTypeKind= convertTypeKind(type.kind)
        override val typeArguments= when (type) {
            else -> type.typeArguments.map { it.toKldType() }
        }
        override val upperBounds= when (type) {
            is TypeVariable -> type.upperBound?.let { listOf(it.toKldType()) } ?: emptyList()
            is WildcardType -> type.extendsBound?.let { listOf(it.toKldType()) } ?: emptyList()
            else -> emptyList()
        }
        override val lowerBounds= when (type) {
            is WildcardType -> type.superBound?.let { listOf(it.toKldType()) } ?: emptyList()
            else -> emptyList()
        }
        override val declaration= when (type) {
            else -> type.asElement()?.toKldElement()
        }
        override val annotations= type.annotationMirrors.map { it.toKldAnnotation() }
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
 * 将TypeVariable转换为KldTypeVariableType
 */
fun TypeVariable.toKldTypeVariableType(): KldTypeVariableType {
    val type = this
    return object : KldTypeVariableType {
        override val typeName= type.toString()
        override val qualifiedName= when (type) {
            is DeclaredType -> (type.asElement() as? TypeElement)?.qualifiedName?.toString()
            else -> null
        }
        override val isNullable= false
        override val isPrimitive= type.kind.isPrimitive
        override val isArray= type.kind == ARRAY
        override val isGeneric= when (type) {
            is DeclaredType -> type.typeArguments.isNotEmpty()
            else -> false
        }
        override val isWildcard= type.kind == WILDCARD
        override val isTypeVariable= type.kind == TYPEVAR
        override val kldTypeKind= convertTypeKind(type.kind)
        override val typeArguments= when (type) {
            is DeclaredType -> type.typeArguments.map { it.toKldType() }
            else -> emptyList()
        }
        override val upperBounds= when (type) {
            is TypeVariable -> type.upperBound?.let { listOf(it.toKldType()) } ?: emptyList()
            is WildcardType -> type.extendsBound?.let { listOf(it.toKldType()) } ?: emptyList()
        }
        override val lowerBounds= when (type) {
            is WildcardType -> type.superBound?.let { listOf(it.toKldType()) } ?: emptyList()
            else -> emptyList()
        }
        override val declaration= when (type) {
            is DeclaredType -> type.asElement()?.toKldElement()
            else -> null
        }
        override val annotations= type.annotationMirrors.map { it.toKldAnnotation() }
        override val kldTypeParameter= (type.asElement() as TypeParameterElement).toTypeParameter()
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
 *
 * 通配符类型
 * @return [KldWildcardType]
 */
fun WildcardType.toKldWildcardType(): KldWildcardType {
    val type = this
    return object : KldWildcardType {
        override val typeName= type.toString()
        override val qualifiedName= when (type) {
            is DeclaredType -> (type.asElement() as? TypeElement)?.qualifiedName?.toString()
            else -> null
        }
        override val isNullable= false
        override val isPrimitive= type.kind.isPrimitive
        override val isArray= type.kind == ARRAY
        override val isGeneric= when (type) {
            is DeclaredType -> type.typeArguments.isNotEmpty()
            else -> false
        }
        override val isWildcard= type.kind == WILDCARD
        override val isTypeVariable= type.kind == TYPEVAR
        override val kldTypeKind= convertTypeKind(type.kind)
        override val typeArguments= when (type) {
            is DeclaredType -> type.typeArguments.map { it.toKldType() }
            else -> emptyList()
        }
        override val upperBounds= when (type) {
            is TypeVariable -> type.upperBound?.let { listOf(it.toKldType()) } ?: emptyList()
            is WildcardType -> type.extendsBound?.let { listOf(it.toKldType()) } ?: emptyList()
            else -> emptyList()
        }
        override val lowerBounds= when (type) {
            is WildcardType -> type.superBound?.let { listOf(it.toKldType()) } ?: emptyList()
            else -> emptyList()
        }
        override val declaration= when (type) {
            is DeclaredType -> type.asElement()?.toKldElement()
            else -> null
        }
        override val annotations= type.annotationMirrors.map { it.toKldAnnotation() }
        override val extendsBound= type.extendsBound?.let { it.toKldType() }
        override val superBound= type.superBound?.let { it.toKldType() }
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
 * 将ExecutableType转换为KldExecutableType
 */
fun ExecutableType.toKldExecutableType(): KldExecutableType {
    val type = this
    return object : KldExecutableType {
        override val typeName= type.toString()
        override val qualifiedName= when (type) {
            is DeclaredType -> (type.asElement() as? TypeElement)?.qualifiedName?.toString()
            else -> null
        }
        override val isNullable= false
        override val isPrimitive= type.kind.isPrimitive
        override val isArray= type.kind == ARRAY
        override val isGeneric= when (type) {
            is DeclaredType -> type.typeArguments.isNotEmpty()
            else -> false
        }
        override val isWildcard= type.kind == WILDCARD
        override val isTypeVariable= type.kind == TYPEVAR
        override val kldTypeKind= convertTypeKind(type.kind)
        override val typeArguments= when (type) {
            is DeclaredType -> type.typeArguments.map { it.toKldType() }
            else -> emptyList()
        }
        override val upperBounds= when (type) {
            is TypeVariable -> type.upperBound?.let { listOf(it.toKldType()) } ?: emptyList()
            is WildcardType -> type.extendsBound?.let { listOf(it.toKldType()) } ?: emptyList()
            else -> emptyList()
        }
        override val lowerBounds= when (type) {
            is WildcardType -> type.superBound?.let { listOf(it.toKldType()) } ?: emptyList()
            else -> emptyList()
        }
        override val declaration= when (type) {
            is DeclaredType -> type.asElement()?.toKldElement()
            else -> null
        }
        override val annotations= type.annotationMirrors.map { it.toKldAnnotation() }
        override val parameterTypes= type.parameterTypes.map { it.toKldType() }
        override val returnType= type.returnType.toKldType()
        override val thrownTypes= type.thrownTypes.map { it.toKldType() }
        override val typeVariables= type.typeVariables.map { it.toKldTypeVariableType() }
        override val receiverType= type.receiverType?.let { it.toKldType() }
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
 * 将UnionType转换为KldUnionType
 */
fun UnionType.toKldUnionType(): KldUnionType {
    val type = this
    return object : KldUnionType {
        override val typeName= type.toString()
        override val qualifiedName= when (type) {
            is DeclaredType -> (type.asElement() as? TypeElement)?.qualifiedName?.toString()
            else -> null
        }
        override val isNullable= false
        override val isPrimitive= type.kind.isPrimitive
        override val isArray= type.kind == ARRAY
        override val isGeneric= when (type) {
            is DeclaredType -> type.typeArguments.isNotEmpty()
            else -> false
        }
        override val isWildcard= type.kind == WILDCARD
        override val isTypeVariable= type.kind == TYPEVAR
        override val kldTypeKind= convertTypeKind(type.kind)
        override val typeArguments= when (type) {
            is DeclaredType -> type.typeArguments.map { it.toKldType() }
            else -> emptyList()
        }
        override val upperBounds= when (type) {
            is TypeVariable -> type.upperBound?.let { listOf(it.toKldType()) } ?: emptyList()
            is WildcardType -> type.extendsBound?.let { listOf(it.toKldType()) } ?: emptyList()
            else -> emptyList()
        }
        override val lowerBounds= when (type) {
            is WildcardType -> type.superBound?.let { listOf(it.toKldType()) } ?: emptyList()
            else -> emptyList()
        }
        override val declaration= when (type) {
            is DeclaredType -> type.asElement()?.toKldElement()
            else -> null
        }
        override val annotations= type.annotationMirrors.map { it.toKldAnnotation() }
        override val alternatives= type.alternatives.map { it.toKldType() }
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
 * 将IntersectionType转换为KldIntersectionType
 */
fun IntersectionType.toKldIntersectionType(): KldIntersectionType {
    val type = this
    return object : KldIntersectionType {
        override val typeName= type.toString()
        override val qualifiedName= when (type) {
            is DeclaredType -> (type.asElement() as? TypeElement)?.qualifiedName?.toString()
            else -> null
        }
        override val isNullable= false
        override val isPrimitive= type.kind.isPrimitive
        override val isArray= type.kind == ARRAY
        override val isGeneric= when (type) {
            is DeclaredType -> type.typeArguments.isNotEmpty()
            else -> false
        }
        override val isWildcard= type.kind == WILDCARD
        override val isTypeVariable= type.kind == TYPEVAR
        override val kldTypeKind= convertTypeKind(type.kind)
        override val typeArguments= when (type) {
            is DeclaredType -> type.typeArguments.map { it.toKldType() }
            else -> emptyList()
        }
        override val upperBounds= when (type) {
            is TypeVariable -> type.upperBound?.let { listOf(it.toKldType()) } ?: emptyList()
            is WildcardType -> type.extendsBound?.let { listOf(it.toKldType()) } ?: emptyList()
            else -> emptyList()
        }
        override val lowerBounds= when (type) {
            is WildcardType -> type.superBound?.let { listOf(it.toKldType()) } ?: emptyList()
            else -> emptyList()
        }
        override val declaration= when (type) {
            is DeclaredType -> type.asElement()?.toKldElement()
            else -> null
        }
        override val annotations= type.annotationMirrors.map { it.toKldAnnotation() }
        override val bounds= type.bounds.map { it.toKldType() }
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
 * 将TypeMirror转换为KldUnknownType
 * 类型抽象接口
 */
fun TypeMirror.toKldUnknownType(): KldType {
    val type = this
    return object : KldType {
        override val typeName= type.toString()
        override val qualifiedName= when (type) {
            is DeclaredType -> (type.asElement() as? TypeElement)?.qualifiedName?.toString()
            else -> null
        }
        override val isNullable= false
        override val isPrimitive= type.kind.isPrimitive
        override val isArray= type.kind == ARRAY
        override val isGeneric= when (type) {
            is DeclaredType -> type.typeArguments.isNotEmpty()
            else -> false
        }
        override val isWildcard= type.kind == WILDCARD
        override val isTypeVariable= type.kind == TYPEVAR
        override val kldTypeKind= convertTypeKind(type.kind)
        override val typeArguments= when (type) {
            is DeclaredType -> type.typeArguments.map { it.toKldType() }
            else -> emptyList()
        }
        override val upperBounds= when (type) {
            is TypeVariable -> type.upperBound?.let { listOf(it.toKldType()) } ?: emptyList()
            is WildcardType -> type.extendsBound?.let { listOf(it.toKldType()) } ?: emptyList()
            else -> emptyList()
        }
        override val lowerBounds= when (type) {
            is WildcardType -> type.superBound?.let { listOf(it.toKldType()) } ?: emptyList()
            else -> emptyList()
        }
        override val declaration= when (type) {
            is DeclaredType -> type.asElement()?.toKldElement()
            else -> null
        }
        override val annotations= type.annotationMirrors.map { it.toKldAnnotation() }
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
 * 创建String类型的简化实现
 */
fun createStringType(): KldType {
    return object : KldType {
        override val typeName = "String"
        override val qualifiedName = "java.lang.String"
        override val isNullable = false
        override val isPrimitive = false
        override val isArray = false
        override val isGeneric = false
        override val isWildcard = false
        override val isTypeVariable = false
        override val kldTypeKind = KldTypeKind.DECLARED
        override val typeArguments = emptyList<KldType>()
        override val upperBounds = emptyList<KldType>()
        override val lowerBounds = emptyList<KldType>()
        override val declaration = null as KldElement?
        override val annotations = emptyList<KldAnnotation>()
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
 * 转换类型种类
 */
private fun convertTypeKind(kind: TypeKind): KldTypeKind {
    return when (kind) {
        BOOLEAN -> KldTypeKind.BOOLEAN
        BYTE -> KldTypeKind.BYTE
        SHORT -> KldTypeKind.SHORT
        INT -> KldTypeKind.INT
        LONG -> KldTypeKind.LONG
        CHAR -> KldTypeKind.CHAR
        FLOAT -> KldTypeKind.FLOAT
        DOUBLE -> KldTypeKind.DOUBLE
        VOID -> KldTypeKind.VOID
        NONE -> KldTypeKind.NONE
        NULL -> KldTypeKind.NULL
        ARRAY -> KldTypeKind.ARRAY
        DECLARED -> KldTypeKind.DECLARED
        ERROR -> KldTypeKind.ERROR
        TYPEVAR -> KldTypeKind.TYPE_VARIABLE
        WILDCARD -> KldTypeKind.WILDCARD
        EXECUTABLE -> KldTypeKind.EXECUTABLE
        UNION -> KldTypeKind.UNION
        INTERSECTION -> KldTypeKind.INTERSECTION
        else -> KldTypeKind.OTHER
    }
}
