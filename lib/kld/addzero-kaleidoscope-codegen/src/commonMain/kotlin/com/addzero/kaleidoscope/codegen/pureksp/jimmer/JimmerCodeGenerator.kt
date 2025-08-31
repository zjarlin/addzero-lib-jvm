package com.addzero.kaleidoscope.codegen.pureksp.jimmer

import com.addzero.kaleidoscope.codegen.pureksp.VlProcessor
import com.addzero.kaleidoscope.codegen.pureksp.Ret
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment

/**
 * Jimmer代码生成器（纯KSP版本）
 *
 * 提供Jimmer相关的代码生成模板组：
 * 1. Jimmer元数据组
 * 2. DTO生成组
 * 3. API接口生成组
 * 4. Service层生成组
 */
class JimmerCodeGenerator(environment: SymbolProcessorEnvironment) : VlProcessor(environment) {

    /**
     * 要处理的注解全限定名（向后兼容）
     */


    override fun collectRet(resolver: Resolver): Sequence<Ret> {
        TODO("Not yet implemented")
    }

    override fun getTemplates(): List<TemlateContext> {
        TODO("Not yet implemented")
    }
}
