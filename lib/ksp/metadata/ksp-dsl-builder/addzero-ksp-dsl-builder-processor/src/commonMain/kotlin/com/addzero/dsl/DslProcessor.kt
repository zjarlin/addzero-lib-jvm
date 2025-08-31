package com.addzero.dsl

import com.addzero.AbsProcessor
import com.addzero.GenerationMode
import com.addzero.dsl.generator.DslGenerator
import com.addzero.dsl.generator.generateSimpleTypeParameters
import com.addzero.dsl.model.*
import com.addzero.getAnnoProperty
import com.addzero.getParentClasses
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.*

/**
 * DSL注解处理器的Provider
 */
class DslProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return DslProcessor(environment)
    }
}

/**
 * DSL注解处理器
 * 负责处理带有@Dsl注解的类，生成基于属性委托的DSL构建器代码
 */
class DslProcessor(
    environment: SymbolProcessorEnvironment
) : AbsProcessor<DslMeta, DslProcessor>(environment) {

    private val dslGenerator = DslGenerator()

    override val debug: Boolean = false


    // 保留原包名用于BULK模式
    override val PKG: String
        get() = "com.addzero.ksp.dslbuilder"

    // 使用即时生成模式
    override val generationMode: GenerationMode = GenerationMode.IMMEDIATE

    /**
     * 使用原类所在的包名
     * 这样生成的DSL构建器就会在与原类相同的包中
     */
    override fun getPackageName(meta: DslMeta, declaration: KSDeclaration): String {
        return meta.packageName
    }

    override fun getAnnotationName(): String = Dsl::class.qualifiedName!!

    override fun extractMetaData(declaration: KSDeclaration, annotation: KSAnnotation): DslMeta {
        val klass =
            declaration as? KSClassDeclaration ?: throw IllegalStateException("Only class declarations are supported")

        val constructor =
            klass.primaryConstructor ?: throw IllegalStateException("Class must have a primary constructor")
        return DslMeta(
            simpleName = klass.simpleName.asString(),
            packageName = klass.packageName.asString(),
            qualifiedName = klass.qualifiedName?.asString()
                ?: throw IllegalStateException("Class must have a qualified name"),
            isNested = klass.parentDeclaration is KSClassDeclaration,
            isPrimary = klass.primaryConstructor != null,
            dslFunctionName = klass.simpleName.asString().replaceFirstChar { it.lowercase() },
            genCollectionDslBuilder = getAnnoProperty<Boolean>(annotation, "genCollectionDslBuilder", Boolean::class),
            customDslName = getAnnoProperty<String>(annotation, "value", String::class),
            removePrefix = getAnnoProperty<String>(annotation, "removePrefix", String::class),
            removeSuffix = getAnnoProperty<String>(annotation, "removeSuffix", String::class),
            constructor = constructor.toConstructorMeta(),
            parentClasses = getParentClasses(klass).map { it.toParentClassMeta() },
            typeParameters = klass.typeParameters.map { it.toTypeParameter() },
            simpleTypeParameters = emptyList(),
            properties = emptyList(),
            classDeclaration = klass,
            imports = emptyList()
        )
    }

    // 为单个元数据生成代码
    override fun generateImmediateCode(resolver: Resolver, meta: DslMeta): String {
        val builderProperties = dslGenerator.generateBuilderProperties(meta)
        val buildParams = meta.constructor.joinToString(", ") { it.name }
        val dslFunctionName = dslGenerator.generateDslFunctionName(meta)
        val builderClassName = "${meta.simpleName}${"BuilderGen"}"
        val typeParams = dslGenerator.generateTypeParameters(meta)
        val simpleTypeParams = generateSimpleTypeParameters(meta)
        // 找出构造函数中的泛型参数
        val constructorGenericParams = meta.constructor
            .filter { param ->
                val typeName = param.fullTypeName
                meta.typeParameters.any { typeParam ->
                    // 检查是否是直接的泛型参数（非包装类型）
                    typeName == typeParam.name ||
                            // 或者是泛型参数的包装类型（如 List<T>）
                            typeName.contains("<${typeParam.name}>")
                } && !param.isNullable
            }
            .map { it.name }
            .toSet()
        // 找出直接的泛型参数（非包装类型）
        val directGenericParams = constructorGenericParams.filter { paramName ->
            val param = meta.constructor.first { it.name == paramName }
            meta.typeParameters.any { typeParam ->
                param.fullTypeName == typeParam.name
            }
        }
        // 生成构造函数参数
        val constructorParams = constructorGenericParams.joinToString(", ") { paramName ->
            val param = meta.constructor.first { it.name == paramName }
            val typeName = param.fullTypeName
            "val $paramName: $typeName"
        }
        // 生成函数参数列表
        val functionParams = if (constructorGenericParams.isEmpty()) {
            "block: $builderClassName$simpleTypeParams.() -> Unit"
        } else {
            // 将直接的泛型参数作为函数参数
            val directParams = directGenericParams.joinToString(", ") { paramName ->
                "$paramName: ${meta.typeParameters.first { it.name == paramName }.name}"
            }
            // 将包装类型的泛型参数作为构造函数参数
            val wrappedParams = (constructorGenericParams - directGenericParams.toSet())
                .joinToString(", ") { paramName ->
                    val param = meta.constructor.first { it.name == paramName }
                    val typeName = param.fullTypeName
                    "$paramName: $typeName"
                }
            // 组合所有参数
            val allParams = listOfNotNull(
                directParams.takeIf { it.isNotBlank() },
                wrappedParams.takeIf { it.isNotBlank() },
                "block: $builderClassName$simpleTypeParams.() -> Unit"
            ).joinToString(", ")
            allParams
        }
        // 生成构造函数调用参数（只包含参数名）
        val constructorArgs = if (constructorGenericParams.isEmpty()) {
            "()"
        } else {
            "(${constructorGenericParams.joinToString(", ")})"
        }
        val strategy = dslGenerator.strategies.first { it.support(meta) }
        return strategy.generate(
            meta,
            builderProperties,
            buildParams,
            dslFunctionName,
            builderClassName,
            typeParams,
            constructorParams,
            functionParams,
            constructorArgs
        )
    }

    // 获取单个元数据生成的文件名
    override fun getImmediateFileName(resolver: Resolver, meta: DslMeta): String {
        return "${meta.simpleName}DslBuilder"
    }




    private fun getTypeParameter(param: KSTypeParameter): TypeParameter {
        val name = param.name.asString()
        val variance = when (param.variance) {
            Variance.COVARIANT -> "out"
            Variance.CONTRAVARIANT -> "in"
            else -> ""
        }

        // 简化边界表示
        val bounds = param.bounds.map {
            val boundType = it.resolve()
            val boundDeclaration = boundType.declaration

            // 获取基础类型名称
            val baseName = boundDeclaration.simpleName.asString()

            // 检查是否有泛型参数
            val arguments = boundType.arguments
            if (arguments.isEmpty()) {
                return@map baseName
            }

            // 为泛型参数构建简化的表示
            val args = arguments.map { arg ->
                // 如果参数是当前类的泛型参数，简化为其名称
                val argType = arg.type?.resolve()
                if (argType?.declaration is KSTypeParameter) {
                    argType.declaration.simpleName.asString()
                } else {
                    arg.type?.resolve()?.declaration?.simpleName?.asString() ?: "Any"
                }
            }.joinToString(", ")

            "$baseName<$args>"
        }

        return TypeParameter(name, bounds.toList(), variance)
    }

    private fun getConstructorParameter(param: KSValueParameter): ConstructorParameter {
        val name = param.name?.asString() ?: ""
        val type = param.type.resolve()
        val fullTypeName = type.declaration.qualifiedName?.asString() ?: ""
        val isGeneric = type.declaration is KSTypeParameter
        val isNullable = type.isMarkedNullable

        return ConstructorParameter(
            name = name,
            fullTypeName = fullTypeName,
            isGeneric = isGeneric,
            isNullable = isNullable
        )
    }
}

private fun KSClassDeclaration.toParentClassMeta(): ParentClassMeta {
    val simpleName1 = this.simpleName.asString()?:""
    val simpleName2 = this.qualifiedName?.asString() ?:""
return    ParentClassMeta(simpleName1,simpleName2)
}

private fun String.removePrefixIfNotNull(prefix: String?): String {
    return if (prefix != null && this.startsWith(prefix)) {
        this.substring(prefix.length)
    } else {
        this
    }
}

private fun String.removeSuffixIfNotNull(suffix: String?): String {
    return if (suffix != null && this.endsWith(suffix)) {
        this.substring(0, this.length - suffix.length)
    } else {
        this
    }
}

