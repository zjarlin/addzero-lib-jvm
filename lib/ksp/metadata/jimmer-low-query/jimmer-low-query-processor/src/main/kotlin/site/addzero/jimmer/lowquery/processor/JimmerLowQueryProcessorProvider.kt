package site.addzero.jimmer.lowquery.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import site.addzero.jimmer.lowquery.processor.context.Settings

class JimmerLowQueryProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        Settings.fromOptions(environment.options)
        return object : SymbolProcessor {
            private val entities = linkedSetOf<LowQueryEntityMeta>()
            private var hasErrors = false

            override fun process(resolver: Resolver): List<KSAnnotated> {
                val collector = JimmerLowQueryCollector(resolver, environment.logger)
                val result = collector.collect()
                entities += result.entities
                hasErrors = hasErrors || result.hasErrors
                return result.deferred
            }

            override fun finish() {
                if (hasErrors || entities.isEmpty()) {
                    return
                }
                JimmerLowQueryGenerator(environment.codeGenerator)
                    .generate(entities, Settings.jimmerLowQueryGeneratedPackage)
            }
        }
    }
}
