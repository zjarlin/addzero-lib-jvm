package com.addzero.dsl.model

import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.symbol.Variance.*

/**
 * DSL生成器的元数据模型
 */
data class DslMeta(
    // 类信息
    val simpleName: String,
    val packageName: String,
    val qualifiedName: String,
    val isNested: Boolean,
    val isPrimary: Boolean,
    val dslFunctionName: String,

    // 注解配置
    val genCollectionDslBuilder: Boolean,
    val customDslName: String,
    val removePrefix: String,
    val removeSuffix: String,

    // 构造函数信息
    val constructor: List<ConstructorParameter>,

    // 父类信息
    val parentClasses: List<ParentClassMeta> = emptyList(),

    // 泛型参数信息
    val typeParameters: List<TypeParameter>,
    val simpleTypeParameters: List<String>,  // 新添加字段，存储简单泛型参数

    // 属性信息
    val properties: List<KSPropertyDeclaration>,

    // 类声明信息
    val classDeclaration: KSClassDeclaration,

    // 导入信息
    val imports: List<String>
)

/**
 * 构造函数元数据
 */
data class ConstructorMeta(
    val parameters: List<ParameterMeta>
)

/**
 * 参数元数据
 */
data class ParameterMeta(
    val name: String,
    val type: String,
    val fullTypeName: String, // 完整类型名称，包括泛型信息
    val isRequired: Boolean,
    val hasDefault: Boolean,
    val defaultValue: String? = null,
    val isNullable: Boolean
)

/**
 * 父类元数据
 */
data class ParentClassMeta(
    val className: String,
    val qualifiedName: String
)

/**
 * 泛型参数元数据
 */
data class TypeParameter(
    val name: String,
    val bounds: List<String>,
    val variance: String
)

/**
 * 构造函数参数元数据
 */
data class ConstructorParameter(
    val name: String,
    val fullTypeName: String,
    val isGeneric: Boolean,
    val isNullable: Boolean
)

/**
 * 扩展函数，用于从KSFunctionDeclaration创建ConstructorMeta
 */
internal fun KSFunctionDeclaration.toConstructorMeta(): List<ConstructorParameter> {
    return parameters.map { it.toConstructorParameter() }
}

/**
 * 获取类型的完整字符串表示，包括泛型参数
 */
private fun KSType.getFullTypeName(): String {
    val baseType = declaration.qualifiedName?.asString() ?: "Any"
    val nullableSuffix = if (isMarkedNullable) "?" else ""

    // 如果没有泛型参数，直接返回基本类型
    if (arguments.isEmpty()) {
        return "$baseType$nullableSuffix"
    }

    // 处理泛型参数
    val genericArgs = arguments.joinToString(", ") { arg ->
        arg.type?.resolve()?.getFullTypeName() ?: "Any"
    }

    return "$baseType<$genericArgs>$nullableSuffix"
}

/**
 * 扩展函数，用于从KSValueParameter创建ParameterMeta
 */
private fun KSValueParameter.toParameterMeta(): ParameterMeta {
    val resolvedType = type.resolve()
    val baseTypeName = resolvedType.declaration.qualifiedName?.asString()
        ?: throw IllegalStateException("Parameter type must have a qualified name")
    val fullTypeName = resolvedType.getFullTypeName()

    return ParameterMeta(
        name = name?.asString() ?: throw IllegalStateException("Parameter must have a name"),
        type = baseTypeName,
        fullTypeName = fullTypeName,
        isRequired = !hasDefault,
        hasDefault = hasDefault,
        defaultValue = null, // KSP不支持直接获取默认值
        isNullable = resolvedType.isMarkedNullable
    )
}

/**
 * 扩展函数，用于从KSTypeParameter创建TypeParameter
 */
internal fun KSTypeParameter.toTypeParameter(): TypeParameter {
    val bounds = bounds.map { it.resolve().getFullTypeName() }.toList()
    val variance = when (variance) {
        COVARIANT -> "out"
        CONTRAVARIANT -> "in"
        INVARIANT -> ""
        STAR -> "*"
    }

    // 处理边界约束中的自引用类型参数
    val processedBounds = bounds.map { bound ->
        bound.replace("${name.asString()}.T", "T")
    }.filter { it != "kotlin.Any?" } // 移除 Any? 边界约束

    return TypeParameter(
        name = name.asString(),
        bounds = processedBounds,
        variance = variance
    )
}

/**
 * 扩展函数，用于从KSValueParameter创建ConstructorParameter
 */
private fun KSValueParameter.toConstructorParameter(): ConstructorParameter {
    val resolvedType = type.resolve()
    val fullTypeName = resolvedType.getFullTypeName()
    val isGeneric = resolvedType.declaration is KSTypeParameter
    val isNullable = resolvedType.isMarkedNullable

    return ConstructorParameter(
        name = name?.asString() ?: throw IllegalStateException("Parameter must have a name"),
        fullTypeName = fullTypeName,
        isGeneric = isGeneric,
        isNullable = isNullable
    )
}
