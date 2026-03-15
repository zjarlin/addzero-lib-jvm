package site.addzero.springktor.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated

class SpringKtorProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return object : SymbolProcessor {
            private val topLevelRoutes = linkedSetOf<TopLevelRouteMeta>()
            private val controllerRoutes = linkedSetOf<ControllerRouteMeta>()
            private var hasErrors = false

            override fun process(resolver: Resolver): List<KSAnnotated> {
                val collector = SpringKtorCollector(resolver, environment.logger)
                val result = collector.collect()

                topLevelRoutes += result.model.topLevelRoutes
                controllerRoutes += result.model.controllerRoutes
                hasErrors = hasErrors || result.hasErrors

                return result.deferred
            }

            override fun finish() {
                if (hasErrors) {
                    return
                }

                val model = SpringKtorModel(
                    topLevelRoutes = topLevelRoutes,
                    controllerRoutes = controllerRoutes,
                )
                if (model.topLevelRoutes.isEmpty() && model.controllerRoutes.isEmpty()) {
                    return
                }

                val generatedPackage = environment.options["springKtor.generatedPackage"]
                    ?.takeIf { it.isNotBlank() }
                    ?: defaultGeneratedPackage(model)

                SpringKtorGenerator(
                    codeGenerator = environment.codeGenerator,
                ).generate(model, generatedPackage)
            }

            private fun defaultGeneratedPackage(model: SpringKtorModel): String {
                val basePackage = sequenceOf(
                    model.topLevelRoutes.map { it.packageName },
                    model.controllerRoutes.map { it.controllerPackageName },
                )
                    .flatten()
                    .filter { it.isNotBlank() }
                    .sorted()
                    .firstOrNull()

                return if (basePackage.isNullOrBlank()) {
                    "site.addzero.generated.springktor"
                } else {
                    "$basePackage.generated.springktor"
                }
            }
        }
    }
}
