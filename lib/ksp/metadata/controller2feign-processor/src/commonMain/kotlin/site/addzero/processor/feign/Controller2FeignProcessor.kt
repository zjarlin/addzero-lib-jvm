package site.addzero.processor.feign

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate

class Controller2FeignProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    private val collectedControllers = mutableListOf<ControllerMeta>()
    private val outputPackage = options["feignOutputPackage"] ?: "site.addzero.generated.feign"
    private val outputDir = options["feignOutputDir"]
    private val enabled = options["feignEnabled"]?.toBoolean() ?: true

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (!enabled) {
            logger.info("[Controller2Feign] Processor disabled via feignEnabled=false")
            return emptyList()
        }

        val controllerSymbols = resolver
            .getSymbolsWithAnnotation("org.springframework.web.bind.annotation.RestController")
            .filterIsInstance<KSClassDeclaration>()

        if (!controllerSymbols.iterator().hasNext()) {
            return emptyList()
        }

        val extractor = ControllerMetadataExtractor(logger)
        val invalidSymbols = mutableListOf<KSClassDeclaration>()

        controllerSymbols.forEach { controller ->
            if (controller.validate()) {
                try {
                    val metadata = extractor.extract(controller)
                    if (metadata.methods.isNotEmpty()) {
                        collectedControllers.add(metadata)
                        logger.info("[Controller2Feign] Collected: ${metadata.className}")
                    }
                } catch (e: Exception) {
                    logger.error("[Controller2Feign] Error extracting ${controller.simpleName.asString()}: ${e.message}")
                }
            } else {
                invalidSymbols.add(controller)
            }
        }

        return invalidSymbols
    }

    override fun finish() {
        if (collectedControllers.isEmpty()) return

        val generator = FeignCodeGenerator(codeGenerator, logger, outputPackage, outputDir)
        
        collectedControllers.forEach { metadata ->
            try {
                generator.generate(metadata)
            } catch (e: Exception) {
                logger.error("[Controller2Feign] Error generating ${metadata.className}: ${e.message}")
            }
        }

        logger.info("[Controller2Feign] Generated ${collectedControllers.size} FeignClient(s)")
        collectedControllers.clear()
    }
}

class Controller2FeignProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return Controller2FeignProcessor(
            environment.codeGenerator,
            environment.logger,
            environment.options
        )
    }
}
