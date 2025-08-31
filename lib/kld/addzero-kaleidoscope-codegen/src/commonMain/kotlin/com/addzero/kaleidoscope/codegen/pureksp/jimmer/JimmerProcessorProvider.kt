package com.addzero.kaleidoscope.codegen.pureksp.jimmer

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Jimmer代码生成器提供者（纯KSP版本）
 */
class JimmerProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return JimmerCodeGenerator(environment)
    }
}
