package com.addzero.dsl.generator.strategy

import com.addzero.dsl.generator.combineImports
import com.addzero.dsl.generator.generateImports
import com.addzero.dsl.generator.generateSimpleTypeParameters
import com.addzero.dsl.generator.getOuterClassChain
import com.addzero.dsl.model.DslMeta

/**
 * 泛型类 DSL 生成策略
 */
class GenericClassDslStrategy : DslStrategy {
    override fun support(meta: DslMeta): Boolean {
        return meta.typeParameters.isNotEmpty()
    }

    override fun generate(
        meta: DslMeta,
        builderProperties: String,
        buildParams: String,
        dslFunctionName: String,
        builderClassName: String,
        typeParams: String,
        constructorParams: String,
        functionParams: String,
        constructorArgs: String
    ): String {
        val outerClassChain = getOuterClassChain(meta)
        
        // 获取并去重导入语句
        val imports = if (meta.isNested) {
            generateImports(meta) // 对于嵌套类，generateImports已经返回了嵌套类的导入
        } else {
            generateImports(meta)
        }
        
        val simpleTypeParams = generateSimpleTypeParameters(meta)

        // 查找所有泛型参数名称
        val typeParamNames = meta.typeParameters.map { it.name }.toSet()
        
        // 查找直接泛型类型参数（如 K、V 这样的参数类型）
        val directGenericParams = meta.constructor.filter { param ->
            // 检查参数类型是否是某个直接泛型类型（如 K，而不是 List<K>）
            typeParamNames.contains(param.fullTypeName.split('.').lastOrNull()) && !param.isNullable
        }
        
        // 构建 Builder 类的构造函数参数列表
        val builderConstructorParams = directGenericParams.joinToString(", ") { param ->
            val simpleName = param.fullTypeName.split('.').lastOrNull() ?: param.fullTypeName
            "val ${param.name}: $simpleName"
        }
        
        // 构建 DSL 函数参数列表（不带 val 关键字）
        val dslFunctionParams = directGenericParams.joinToString(", ") { param ->
            val simpleName = param.fullTypeName.split('.').lastOrNull() ?: param.fullTypeName
            "${param.name}: $simpleName"
        }
        
        // 需要在 Builder 构造函数中传递的参数
        val builderConstructorArgs = directGenericParams.joinToString(", ") { it.name }
        
        // 排除已在构造函数中的参数
        val excludedParamNames = directGenericParams.map { it.name }.toSet()
        
        // 生成剩余属性（排除已放入构造函数的属性）
        val remainingProperties = meta.constructor
            .filterNot { it.name in excludedParamNames }
            .joinToString("\n    ") { param ->
                val typeName = param.fullTypeName
                val isGenericType = meta.typeParameters.any { typeParam ->
                    // 检查是否是泛型参数的包装类型（如 List<T>）
                    typeName.contains("<") && typeName.contains(typeParam.name) ||
                    // 或者直接是泛型参数
                    typeName == typeParam.name
                }
                
                // 处理泛型参数的类型名称
                val processedTypeName = if (isGenericType) {
                    // 先处理全限定的泛型参数（如 com.example.Box.T -> T）
                    var processed = typeName
                    meta.typeParameters.forEach { typeParam ->
                        // 替换所有的全限定泛型参数为简单形式
                        val pattern = "${meta.qualifiedName}.${typeParam.name}"
                        processed = processed.replace(pattern, typeParam.name)
                        
                        // 替换其他可能的全限定形式（针对嵌套泛型）
                        val simpleName = typeParam.name
                        val regex = """([a-zA-Z0-9_.<>]+\.)$simpleName""".toRegex()
                        processed = processed.replace(regex, simpleName)
                    }
                    processed
                } else {
                    typeName
                }
                
                if (param.isNullable) {
                    // 可空类型使用null默认值
                    "var ${param.name}: $processedTypeName = null"
                } else {
                    // 处理原始类型不能使用lateinit的问题
                    when (param.fullTypeName) {
                        // 原始类型需要默认值
                        "kotlin.Int" -> "var ${param.name}: $processedTypeName = 0"
                        "kotlin.Long" -> "var ${param.name}: $processedTypeName = 0L"
                        "kotlin.Double" -> "var ${param.name}: $processedTypeName = 0.0"
                        "kotlin.Float" -> "var ${param.name}: $processedTypeName = 0.0f"
                        "kotlin.Boolean" -> "var ${param.name}: $processedTypeName = false"
                        "kotlin.Char" -> "var ${param.name}: $processedTypeName = ' '"
                        "kotlin.Byte" -> "var ${param.name}: $processedTypeName = 0"
                        "kotlin.Short" -> "var ${param.name}: $processedTypeName = 0"
                        // 字符串类型
                        "kotlin.String" -> "var ${param.name}: $processedTypeName = \"\""
                        // 集合类型使用空集合
                        "kotlin.collections.List" -> "var ${param.name}: $processedTypeName = emptyList()"
                        "kotlin.collections.MutableList" -> "var ${param.name}: $processedTypeName = mutableListOf()"
                        "kotlin.collections.Set" -> "var ${param.name}: $processedTypeName = emptySet()"
                        "kotlin.collections.MutableSet" -> "var ${param.name}: $processedTypeName = mutableSetOf()"
                        "kotlin.collections.Map" -> "var ${param.name}: $processedTypeName = emptyMap()"
                        "kotlin.collections.MutableMap" -> "var ${param.name}: $processedTypeName = mutableMapOf()"
                        // 对于泛型类型，包装类型使用对应的空集合
                        else -> if (isGenericType) {
                            when {
                                processedTypeName.contains("List<") -> "var ${param.name}: $processedTypeName = emptyList()"
                                processedTypeName.contains("MutableList<") -> "var ${param.name}: $processedTypeName = mutableListOf()"
                                processedTypeName.contains("Set<") -> "var ${param.name}: $processedTypeName = emptySet()"
                                processedTypeName.contains("MutableSet<") -> "var ${param.name}: $processedTypeName = mutableSetOf()"
                                processedTypeName.contains("Map<") -> "var ${param.name}: $processedTypeName = emptyMap()"
                                processedTypeName.contains("MutableMap<") -> "var ${param.name}: $processedTypeName = mutableMapOf()"
                                else -> "lateinit var ${param.name}: $processedTypeName"
                            }
                        } else {
                            "lateinit var ${param.name}: $processedTypeName"
                        }
                    }
                }
            }

        // 构建构造函数参数列表（用于构建原始类）
        val constructorParamsList = meta.constructor.joinToString(", ") { it.name }

        // 生成泛型参数列表
        val typeParamList = meta.typeParameters.joinToString(", ") { param ->
            val bounds = if (param.bounds.isNotEmpty()) {
                " : ${param.bounds.joinToString(" & ")}"
            } else ""
            "${param.variance}${param.name}$bounds"
        }

        return """
            |package ${meta.packageName}
            |
            |$imports
            |
            |/**
            | * ${meta.simpleName} 的 DSL 构建器
            | */
            |class ${builderClassName}<$typeParamList>(${if (builderConstructorParams.isNotBlank()) builderConstructorParams else ""}) {
            |    $remainingProperties
            |
            |    /**
            |     * 构建 ${meta.simpleName} 实例
            |     */
            |    fun build(): $outerClassChain<$typeParamList> {
            |        return $outerClassChain($constructorParamsList)
            |    }
            |}
            |
            |/**
            | * ${meta.simpleName} 的 DSL 函数
            | */
            |fun <$typeParamList> $dslFunctionName(
            |    ${if (dslFunctionParams.isNotBlank()) dslFunctionParams + ", " else ""}init: ${builderClassName}<$typeParamList>.() -> Unit = {}
            |): $outerClassChain<$typeParamList> {
            |    return ${builderClassName}<$typeParamList>(${builderConstructorArgs}).apply(init).build()
            |}
            |""".trimMargin()
    }
} 