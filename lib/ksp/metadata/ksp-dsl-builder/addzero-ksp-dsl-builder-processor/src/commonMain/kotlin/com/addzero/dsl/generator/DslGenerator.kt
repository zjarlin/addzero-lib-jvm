package com.addzero.dsl.generator

import com.addzero.dsl.generator.strategy.CollectionDslStrategy
import com.addzero.dsl.generator.strategy.DslStrategy
import com.addzero.dsl.generator.strategy.NonGenericCollectionDslStrategy
import com.addzero.dsl.generator.strategy.NonGenericSimpleDslStrategy
import com.addzero.dsl.model.DslMeta

/**
 * 生成简单的泛型参数列表（不带边界约束）
 */
fun generateSimpleTypeParameters(meta: DslMeta): String {
    if (meta.typeParameters.isEmpty()) return ""
    return "<${meta.typeParameters.joinToString(", ") { it.name }}>"
}

/**
 * 生成导入语句，避免导入自己的包
 */
fun generateImports(meta: DslMeta): String {
    // 如果是嵌套类，使用专门的嵌套类导入逻辑
    if (meta.isNested) {
        return generateNestedClassImports(meta)
    }

    // 如果类所在的包是其他包，才需要导入
    val outerClassImport = meta.qualifiedName.substringBeforeLast('.')

    return if (outerClassImport != meta.packageName) {
        "import $outerClassImport"
    } else {
        // 类在当前包中，不需要导入
        ""
    }
}

/**
 * 生成嵌套类的导入语句
 */
fun generateNestedClassImports(meta: DslMeta): String {
    // 获取完整的嵌套类限定名
    val fullNestedClassImport = meta.qualifiedName

    // 收集所有需要导入的类
    val imports = mutableSetOf(fullNestedClassImport)

    // 如果有父类，也需要导入它们
    meta.parentClasses.forEach { parentClass ->
        imports.add(parentClass.qualifiedName)
    }

    // 转换为导入语句
    return imports.joinToString("\n") { "import $it" }
}

/**
 * 合并并去重导入语句
 */
fun combineImports(vararg imports: String): String {
    // 将所有导入语句分割并去重
    val uniqueImports = imports
        .flatMap { it.lines() }
        .filter { it.startsWith("import ") }
        .distinct()
        .sorted()

    return uniqueImports.joinToString("\n")
}

/**
 * 生成剩余属性
 */
fun generateRemainingProperties(meta: DslMeta, excludedParams: Set<String>): String {
    return meta.constructor
        .filterNot { it.name in excludedParams }
        .joinToString("\n    ") { param ->
            val typeName = param.fullTypeName
            val isGenericType = meta.typeParameters.any { typeParam ->
                // 检查是否是泛型参数的包装类型（如 List<T>）
                typeName.contains("<") && typeName.contains(typeParam.name) ||
                // 或者直接是泛型参数
                typeName == typeParam.name
            }
            val isDirectGenericType = meta.typeParameters.any { typeParam ->
                typeName == typeParam.name
            }

            // 如果是直接的泛型参数，不生成属性，因为它会作为构造器参数
            if (isDirectGenericType) {
                return@joinToString ""
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
                    // 对于泛型类型，使用更精确的类型判断
                    else -> if (isGenericType) {
                        // 使用正则表达式匹配泛型类型
                        val listPattern = """^kotlin\.collections\.(Mutable)?List<.*>$""".toRegex()
                        val setPattern = """^kotlin\.collections\.(Mutable)?Set<.*>$""".toRegex()
                        val mapPattern = """^kotlin\.collections\.(Mutable)?Map<.*>$""".toRegex()

                        when {
                            listPattern.matches(typeName) -> "var ${param.name}: $processedTypeName = emptyList()"
                            setPattern.matches(typeName) -> "var ${param.name}: $processedTypeName = emptySet()"
                            mapPattern.matches(typeName) -> "var ${param.name}: $processedTypeName = emptyMap()"
                            else -> "lateinit var ${param.name}: $processedTypeName"
                        }
                    } else {
                        "lateinit var ${param.name}: $processedTypeName"
                    }
                }
            }
        }
}

/**
 * 获取外部类链
 */
fun getOuterClassChain(meta: DslMeta): String {
    if (!meta.isNested) {
        // 非嵌套类，返回简单类名
        return meta.simpleName
    }

    // 对于嵌套类，返回完整的引用路径（不包含包名）
    // 例如：Http.Body.Json
    return meta.qualifiedName.substringAfter("${meta.packageName}.")
}

/**
 * 生成复数形式的DSL函数名
 * 例如：http -> https, box -> boxes
 */
fun generatePluralDslFunctionName(meta: DslMeta): String {
    val dslGenerator = DslGenerator()
    val singular = dslGenerator.generateDslFunctionName(meta)
    // 处理特殊情况
    return when {
        singular.endsWith("s") -> "${singular}es" // bus -> buses, class -> classes
        singular.endsWith("y") && !isVowel(singular[singular.length - 2]) ->
            "${singular.substring(0, singular.length - 1)}ies" // city -> cities
        singular.endsWith("x") || singular.endsWith("ch") ||
                singular.endsWith("sh") || singular.endsWith("z") -> "${singular}es"
        else -> "${singular}s" // 一般情况直接加s
    }
}

/**
 * 判断字符是否是元音字母
 */
private fun isVowel(c: Char): Boolean {
    return c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u' ||
           c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U'
}

/**
 * DSL代码生成器
 */
class DslGenerator() {
    val strategies = listOf(
        GenericCollectionDslStrategy(),
        GenericSimpleDslStrategy(),
        CollectionDslStrategy(),
        NonGenericCollectionDslStrategy(),
        NonGenericSimpleDslStrategy()
    )

    /**
     * 生成构建器属性
     */
    fun generateBuilderProperties(meta: DslMeta): String {
        return meta.constructor.joinToString("\n    ") { param ->
            // 处理泛型参数引用
            val typeName = param.fullTypeName
            val isGenericType = meta.typeParameters.any { typeParam ->
                // 检查是否是泛型参数的包装类型（如 List<T>）
                typeName.contains("<") && typeName.contains(typeParam.name) ||
                // 或者直接是泛型参数
                typeName == typeParam.name
            }
            val isDirectGenericType = meta.typeParameters.any { typeParam ->
                typeName == typeParam.name
            }

            // 如果是泛型参数自引用类型，不生成属性，因为它会作为构造器参数
            if (isDirectGenericType) {
                return@joinToString ""
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
    }

    /**
     * 生成DSL函数名
     */
    fun generateDslFunctionName(meta: DslMeta): String {
        return when {
            meta.customDslName.isNotBlank() -> meta.customDslName
            else -> {
                var name = meta.simpleName
                if (meta.removePrefix.isNotBlank() && name.startsWith(meta.removePrefix)) {
                    name = name.substring(meta.removePrefix.length)
                }
                if (meta.removeSuffix.isNotBlank() && name.endsWith(meta.removeSuffix)) {
                    name = name.substring(0, name.length - meta.removeSuffix.length)
                }
                name.replaceFirstChar { it.lowercase() }
            }
        }
    }

    /**
     * 生成泛型参数列表
     */
    fun generateTypeParameters(meta: DslMeta): String {
        if (meta.typeParameters.isEmpty()) return ""

        return "<${
            meta.typeParameters.joinToString(", ") { param ->
                val bounds = if (param.bounds.isNotEmpty()) {
                    " : ${
                        param.bounds.joinToString(" & ") { bound ->
                            // 简化泛型边界中的类型引用
                            bound.replace("${meta.qualifiedName}.", "")
                                .replace("kotlin.", "")
                                .replace("java.lang.", "")
                        }
                    }"
                } else ""
                "${param.name}$bounds"
            }
        }>"
    }

    /**
     * 生成构造函数参数
     */
    fun generateConstructorParams(meta: DslMeta): String {
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
        return constructorGenericParams.joinToString(", ") { paramName ->
            val param = meta.constructor.first { it.name == paramName }
            val typeName = param.fullTypeName
            "val $paramName: $typeName"
        }
    }

    /**
     * 生成函数参数列表
     */
    fun generateFunctionParams(meta: DslMeta, builderClassName: String, simpleTypeParams: String): String {
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

        if (constructorGenericParams.isEmpty()) {
            return "block: $builderClassName$simpleTypeParams.() -> Unit"
        }

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
        return allParams
    }

    /**
     * 生成构造函数调用参数（只包含参数名）
     */
    fun generateConstructorArgs(meta: DslMeta): String {
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

        return if (constructorGenericParams.isEmpty()) {
            "()"
        } else {
            "(${constructorGenericParams.joinToString(", ")})"
        }
    }
}

/**
 * 泛型集合DSL生成策略
 */
class GenericCollectionDslStrategy : DslStrategy {

    override fun support(meta: DslMeta): Boolean {
        return meta.typeParameters.isNotEmpty() && meta.genCollectionDslBuilder
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

        // 构建构造函数参数列表（用于构建原始类）
        val constructorParamsList = meta.constructor.joinToString(", ") { it.name }

        // 返回类型使用完整的引用路径
        val returnType = if (meta.isNested) {
            outerClassChain
        } else {
            meta.simpleName
        }

        // 生成复数形式的DSL函数名
        val pluralFunctionName = generatePluralDslFunctionName(meta)

        return """
        |package ${meta.packageName}
        |$imports
        |class $builderClassName$typeParams(${if (builderConstructorParams.isNotBlank()) builderConstructorParams else ""}) {
        |    ${generateRemainingProperties(meta, excludedParamNames)}
        |
        |    fun build(): $returnType$simpleTypeParams {
        |        return $outerClassChain($constructorParamsList)
        |    }
        |}
        |
        |class ${meta.simpleName}CollectionBuilderGen$simpleTypeParams {
        |     val items = mutableListOf<$returnType$simpleTypeParams>()
        |
        |    fun $dslFunctionName(
        |        ${if (dslFunctionParams.isNotBlank()) dslFunctionParams + ", " else ""}init: $builderClassName$simpleTypeParams.() -> Unit = {}
        |    ) {
        |        items.add($builderClassName$simpleTypeParams(${builderConstructorArgs}).apply(init).build())
        |    }
        |
        |    fun build(): List<$returnType$simpleTypeParams> = items.toList()
        |}
        |
        |fun $typeParams $dslFunctionName(
        |    ${if (dslFunctionParams.isNotBlank()) dslFunctionParams + ", " else ""}init: $builderClassName$simpleTypeParams.() -> Unit = {}
        |): $returnType$simpleTypeParams {
        |    return $builderClassName$simpleTypeParams(${builderConstructorArgs}).apply(init).build()
        |}
        |
        |fun $typeParams $pluralFunctionName(
        |    init: ${meta.simpleName}CollectionBuilderGen$simpleTypeParams.() -> Unit = {}
        |): List<$returnType$simpleTypeParams> {
        |    return ${meta.simpleName}CollectionBuilderGen$simpleTypeParams().apply(init).build()
        |}
        |""".trimMargin()
    }
}

/**
 * 泛型简单DSL生成策略
 */
class GenericSimpleDslStrategy : DslStrategy {
    override fun support(meta: DslMeta): Boolean {
        return meta.typeParameters.isNotEmpty() && !meta.genCollectionDslBuilder
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

        // 构建构造函数参数列表（用于构建原始类）
        val constructorParamsList = meta.constructor.joinToString(", ") { it.name }

        // 返回类型使用完整的引用路径
        val returnType = if (meta.isNested) {
            outerClassChain
        } else {
            meta.simpleName
        }

        return """
        |package ${meta.packageName}
        |$imports
        |class $builderClassName$typeParams(${if (builderConstructorParams.isNotBlank()) builderConstructorParams else ""}) {
        |    ${generateRemainingProperties(meta, excludedParamNames)}
        |
        |    fun build(): $returnType$simpleTypeParams {
        |        return $outerClassChain($constructorParamsList)
        |    }
        |}
        |
        |fun $typeParams $dslFunctionName(
        |    ${if (dslFunctionParams.isNotBlank()) dslFunctionParams + ", " else ""}init: $builderClassName$simpleTypeParams.() -> Unit = {}
        |): $returnType$simpleTypeParams {
        |    return $builderClassName$simpleTypeParams(${builderConstructorArgs}).apply(init).build()
        |}
        |""".trimMargin()
    }
}

