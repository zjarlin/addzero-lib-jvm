package site.addzero.dsl.generator.strategy

import site.addzero.dsl.generator.combineImports
import site.addzero.dsl.generator.generateImports
import site.addzero.dsl.generator.generateNestedClassImports
import site.addzero.dsl.generator.getOuterClassChain
import site.addzero.dsl.model.DslMeta

/**
 * 非泛型简单类型DSL生成策略
 */
class NonGenericSimpleDslStrategy : DslStrategy {
    override fun support(meta: DslMeta): Boolean {
        // 检查是否没有泛型参数
        val hasNoTypeParams = meta.typeParameters.isEmpty()
        // 检查是否不是集合类型
        val isNotCollectionType = meta.constructor.none { param ->
            val typeName = param.fullTypeName
            typeName == "kotlin.collections.List" ||
            typeName == "kotlin.collections.MutableList" ||
            typeName == "kotlin.collections.Set" ||
            typeName == "kotlin.collections.MutableSet" ||
            typeName == "kotlin.collections.Map" ||
            typeName == "kotlin.collections.MutableMap"
        }
        // 必须同时满足三个条件：
        // 1. 没有泛型参数
        // 2. 不是集合类型
        // 3. 没有指定genCollectionDslBuilder=true
        return hasNoTypeParams && isNotCollectionType && !meta.genCollectionDslBuilder
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
            |
            |$imports
            |
            |/**
            | * ${meta.simpleName} 的 DSL 构建器
            | */
            |class $builderClassName$typeParams(
            |    $constructorParams
            |) {
            |    $builderProperties
            |
            |    /**
            |     * 构建 ${meta.simpleName} 实例
            |     */
            |    fun build(): $returnType$typeParams {
            |        return $outerClassChain($constructorParamsList)
            |    }
            |}
            |
            |/**
            | * ${meta.simpleName} 的 DSL 函数
            | */
            |fun $dslFunctionName(
            |    init: $builderClassName$typeParams.() -> Unit = {}
            |): $returnType$typeParams {
            |    return $builderClassName$typeParams().apply(init).build()
            |}
            |""".trimMargin()
    }
}
