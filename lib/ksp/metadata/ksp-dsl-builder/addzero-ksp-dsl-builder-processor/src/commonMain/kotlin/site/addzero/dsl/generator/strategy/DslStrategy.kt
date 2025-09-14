package site.addzero.dsl.generator.strategy

import site.addzero.dsl.model.DslMeta

/**
 * DSL生成策略接口
 */
 interface DslStrategy {
    fun support(meta: DslMeta): Boolean
    fun generate(
        meta: DslMeta,
        builderProperties: String,
        buildParams: String,
        dslFunctionName: String,
        builderClassName: String,
        typeParams: String,
        constructorParams: String,
        functionParams: String,
        constructorArgs: String
    ): String
}
