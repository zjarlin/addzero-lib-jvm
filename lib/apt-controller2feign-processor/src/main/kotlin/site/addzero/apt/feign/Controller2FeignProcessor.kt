package site.addzero.apt.feign

import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class Controller2FeignProcessor : AbstractProcessor() {

    private var outputPackage: String = "site.addzero.generated.feign"
    private var enabled = true
    private var processed = false

    override fun getSupportedOptions(): Set<String> = setOf(
        "feignOutputPackage",
        "feignEnabled"
    )

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)

        val options = processingEnv.options
        enabled = options["feignEnabled"]?.toBoolean() ?: true
        outputPackage = options["feignOutputPackage"] ?: "site.addzero.generated.feign"

        processingEnv.messager.printMessage(
            Diagnostic.Kind.NOTE,
            "[Controller2Feign] Initialized with outputPackage=$outputPackage, enabled=$enabled"
        )
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (!enabled || processed || roundEnv.processingOver()) return false

        val restControllerType = processingEnv.elementUtils
            .getTypeElement("org.springframework.web.bind.annotation.RestController")
        
        if (restControllerType == null) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.NOTE,
                "[Controller2Feign] RestController annotation not found in classpath"
            )
            return false
        }

        val controllers = roundEnv.getElementsAnnotatedWith(restControllerType)

        if (controllers.isEmpty()) {
            return false
        }

        processed = true
        
        processingEnv.messager.printMessage(
            Diagnostic.Kind.NOTE,
            "[Controller2Feign] Found ${controllers.size} controller(s)"
        )

        val extractor = ControllerMetadataExtractor(processingEnv)
        val generator = FeignCodeGenerator(processingEnv.filer, outputPackage)

        controllers.filterIsInstance<TypeElement>().forEach { controller ->
            try {
                val metadata = extractor.extract(controller)
                if (metadata.methods.isNotEmpty()) {
                    generator.generate(metadata)
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.NOTE,
                        "[Controller2Feign] Generated FeignClient for ${metadata.className}"
                    )
                } else {
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.NOTE,
                        "[Controller2Feign] Skipped ${metadata.className} (no HTTP methods)"
                    )
                }
            } catch (e: Exception) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.WARNING,
                    "[Controller2Feign] Error processing ${controller.simpleName}: ${e.message}"
                )
            }
        }

        return false
    }
}
