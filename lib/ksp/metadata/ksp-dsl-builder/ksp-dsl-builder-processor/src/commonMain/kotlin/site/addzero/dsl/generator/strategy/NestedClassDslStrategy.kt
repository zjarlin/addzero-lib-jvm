//package site.addzero.dsl.generator.strategy
//
//import site.addzero.dsl.generator.generateImports
//import site.addzero.dsl.generator.generateSimpleTypeParameters
//import site.addzero.dsl.generator.getOuterClassChain
//import site.addzero.dsl.model.DslMeta
//
///**
// * 嵌套类 DSL 生成策略
// */
//class NestedClassDslStrategy : DslStrategy {
//    override fun support(meta: DslMeta): Boolean {
//        return meta.isNested
//    }
//
//    override fun generate(
//        meta: DslMeta,
//        builderProperties: String,
//        buildParams: String,
//        dslFunctionName: String,
//        builderClassName: String,
//        typeParams: String,
//        constructorParams: String,
//        functionParams: String,
//        constructorArgs: String
//    ): String {
//        val outerClassChain = getOuterClassChain(meta)
//        val imports = generateImports(meta)
//        val nestedImports = generateNestedClassImports(meta)
//
//        // 构建构造函数参数列表（用于构建原始类）
//        val constructorParamsList = meta.constructor.joinToString(", ") { it.name }
//
//        return """
//            |package ${meta.packageName}
//            |
//            |$imports
//            |$nestedImports
//            |
//            |/**
//            | * ${meta.simpleName} 的 DSL 构建器
//            | */
//            |class $builderClassName {
//            |    $builderProperties
//            |
//            |    /**
//            |     * 构建 ${meta.simpleName} 实例
//            |     */
//            |    fun build(): ${outerClassChain} {
//            |        return $outerClassChain($constructorParamsList)
//            |    }
//            |}
//            |
//            |/**
//            | * ${meta.simpleName} 的 DSL 函数
//            | */
//            |fun $dslFunctionName(init: $builderClassName.() -> Unit = {}): ${outerClassChain} {
//            |    return $builderClassName().apply(init).build()
//            |}
//            |""".trimMargin()
//    }
//}
