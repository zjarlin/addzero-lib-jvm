package site.addzero.dsl.generator.strategy

import site.addzero.dsl.generator.combineImports
import site.addzero.dsl.generator.generateImports
import site.addzero.dsl.generator.generateNestedClassImports
import site.addzero.dsl.generator.generatePluralDslFunctionName
import site.addzero.dsl.generator.getOuterClassChain
import site.addzero.dsl.model.DslMeta

/**
 * 非泛型集合类型DSL生成策略
 */
class NonGenericCollectionDslStrategy : DslStrategy {
    override fun support(meta: DslMeta): Boolean {
        // 检查是否是集合类型
        val isCollectionType = meta.constructor.any { param ->
            val typeName = param.fullTypeName
            typeName == "kotlin.collections.List" ||
            typeName == "kotlin.collections.MutableList" ||
            typeName == "kotlin.collections.Set" ||
            typeName == "kotlin.collections.MutableSet" ||
            typeName == "kotlin.collections.Map" ||
            typeName == "kotlin.collections.MutableMap"
        }
        // 检查是否没有泛型参数
        val hasNoTypeParams = meta.typeParameters.isEmpty()

        // 必须没有泛型参数且满足两个条件之一：
        // 1. 是集合类型
        // 2. 指定了genCollectionDslBuilder=true
        return hasNoTypeParams && (isCollectionType || meta.genCollectionDslBuilder)
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

        // 生成复数形式的DSL函数名
        val pluralFunctionName = generatePluralDslFunctionName(meta)

        return """
        |package ${meta.packageName}
        |$imports
        |class $builderClassName {
        |    $builderProperties
        |
        |    fun build(): ${outerClassChain} {
        |        return $outerClassChain($constructorParamsList)
        |    }
        |}
        |
        |class ${meta.simpleName}CollectionBuilderGen {
        |     val items = mutableListOf<${outerClassChain}>()
        |
        |    fun $dslFunctionName(init: $builderClassName.() -> Unit = {}) {
        |        items.add($builderClassName().apply(init).build())
        |    }
        |
        |    fun build(): List<${outerClassChain}> = items.toList()
        |}
        |
        |fun $dslFunctionName(init: $builderClassName.() -> Unit = {}): ${outerClassChain} {
        |    return $builderClassName().apply(init).build()
        |}
        |
        |fun $pluralFunctionName(init: ${meta.simpleName}CollectionBuilderGen.() -> Unit = {}): List<${outerClassChain}> {
        |    return ${meta.simpleName}CollectionBuilderGen().apply(init).build()
        |}
        |""".trimMargin()
    }
}
