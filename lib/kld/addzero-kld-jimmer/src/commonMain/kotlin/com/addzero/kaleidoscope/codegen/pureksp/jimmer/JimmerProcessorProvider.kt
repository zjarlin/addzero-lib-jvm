package com.addzero.kaleidoscope.codegen.pureksp.jimmer

import com.addzero.kaleidoscope.ksp.toKldResolver
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated

class JimmerProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val processor = object : SymbolProcessor {
            override fun process(resolver: Resolver): List<KSAnnotated> {
                val collectRet = JimmerKldCodeGenerator.collectRet(resolver.toKldResolver(environment))
                return emptyList()
            }
            override fun finish() {
               JimmerKldCodeGenerator.finish()
            }
        }
        return processor
    }
}
