package site.addzero.apt.feign

import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedAnnotationTypes("org.springframework.web.bind.annotation.RestController")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(
    "feignOutputPackage",
    "feignEnabled"
)
class Controller2FeignProcessor : AbstractProcessor() {

    private var metadataExtractor: ControllerMetadataExtractor? = null
    private var codeGenerator: FeignCodeGenerator? = null
    private var processed = false

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)

        val options = processingEnv.options
        val enabled = options["feignEnabled"]?.toBoolean() ?: true

        if (!enabled) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.NOTE,
                "[Controller2Feign] Processor disabled via feignEnabled=false"
            )
            return
        }

        val outputPackage = options["feignOutputPackage"] ?: "site.addzero.generated.feign"

        metadataExtractor = ControllerMetadataExtractor(processingEnv)
        codeGenerator = FeignCodeGenerator(processingEnv.filer, outputPackage)

        processingEnv.messager.printMessage(
            Diagnostic.Kind.NOTE,
            "[Controller2Feign] Initialized with outputPackage=$outputPackage"
        )
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (processed || roundEnv.processingOver()) return false
        if (metadataExtractor == null || codeGenerator == null) return false

        processed = true

        val controllers = roundEnv.getElementsAnnotatedWith(
            processingEnv.elementUtils.getTypeElement("org.springframework.web.bind.annotation.RestController")
        )

        if (controllers.isEmpty()) {
            processingEnv.messager.printMessage(
                Diagnostic.Kind.NOTE,
                "[Controller2Feign] No @RestController found"
            )
            return false
        }

        processingEnv.messager.printMessage(
            Diagnostic.Kind.NOTE,
            "[Controller2Feign] Found ${controllers.size} controller(s)"
        )

        controllers.filterIsInstance<TypeElement>().forEach { controller ->
            try {
                val metadata = metadataExtractor!!.extract(controller)
                if (metadata.methods.isNotEmpty()) {
                    codeGenerator!!.generate(metadata)
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.NOTE,
                        "[Controller2Feign] Generated FeignClient for ${metadata.className}"
                    )
                }
            } catch (e: Exception) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "[Controller2Feign] Error processing ${controller.simpleName}: ${e.message}\n${e.stackTraceToString()}"
                )
            }
        }

        return false
    }
}
