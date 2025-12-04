package site.addzero.apt.feign

import site.addzero.util.lsi_impl.impl.apt.clazz.AptLsiClass
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class Controller2FeignProcessor : AbstractProcessor() {

    private var outputPackage: String = "site.addzero.generated.feign"
    private var serviceName: String? = null
    private var enabled = true
    private var processed = false

    override fun getSupportedOptions(): Set<String> = setOf(
        "feignOutputPackage",
        "feignEnabled",
        "feignServiceName"
    )

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)

        val options = processingEnv.options
        enabled = options["feignEnabled"]?.toBoolean() ?: true
        outputPackage = options["feignOutputPackage"] ?: "site.addzero.generated.feign"
        serviceName = options["feignServiceName"]

        processingEnv.messager.printMessage(
            Diagnostic.Kind.NOTE,
            "[Controller2Feign] Initialized with outputPackage=$outputPackage, enabled=$enabled, serviceName=${serviceName ?: "(auto)"}"
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

        val generator = FeignCodeGenerator(processingEnv.filer, outputPackage, serviceName)

        controllers.filterIsInstance<TypeElement>().forEach { controller ->
            try {
                val docComment = processingEnv.elementUtils.getDocComment(controller)
                val lsiClass = AptLsiClass(controller, docComment)
                val metadata = ControllerMetadataExtractor.extract(lsiClass)
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
