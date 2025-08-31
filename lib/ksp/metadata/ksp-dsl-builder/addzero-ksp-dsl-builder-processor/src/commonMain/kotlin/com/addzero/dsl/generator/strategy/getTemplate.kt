package com.addzero.dsl.generator.strategy

import com.addzero.dsl.generator.generateRemainingProperties
import com.addzero.dsl.generator.getOuterClassChain
import com.addzero.dsl.model.DslMeta

fun getTemplate(
    meta: DslMeta,
    imports: String,
    builderClassName: String,
    fullGenericTypeParams: String,
    constructorGenericParams: String,
    simpleGenericTypeParams: String,
    buildParams: String,
    dslFunctionName: String,
    functionParams: String,
    constructorArgs: String,
    simpleName: String
): String {
    val builderClassNameWithParams = "$builderClassName$fullGenericTypeParams"

    // 构建构造函数参数列表
    val constructorParamsList = meta.constructor.joinToString(", ") { it.name }

    // 获取外部类链
    val outerClassChain = getOuterClassChain(meta)

    // 返回类型使用完整的引用路径
    val returnType = if (meta.isNested) {
        outerClassChain
    } else {
        simpleName
    }

    return """
            |package ${meta.packageName}
            |$imports
            |class $builderClassNameWithParams${if (constructorGenericParams.isNotBlank()) "($constructorGenericParams)" else "()"} {
            |    ${generateRemainingProperties(meta, emptySet())}
            |
            |    fun build(): $returnType$simpleGenericTypeParams {
            |        return $outerClassChain($constructorParamsList)
            |    }
            |}
            |
            |fun $dslFunctionName$fullGenericTypeParams(
            |    init: $builderClassNameWithParams.() -> Unit
            |): $returnType$simpleGenericTypeParams {
            |    return $builderClassNameWithParams().apply(init).build()
            |}
            """.trimMargin()
}
