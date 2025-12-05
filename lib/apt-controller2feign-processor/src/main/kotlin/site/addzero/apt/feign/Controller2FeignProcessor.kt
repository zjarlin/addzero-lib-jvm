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

        log("Processing round started, annotations: ${annotations.map { it.qualifiedName }}")

        val restControllerType = processingEnv.elementUtils
            .getTypeElement("org.springframework.web.bind.annotation.RestController")
        
        if (restControllerType == null) {
            log("RestController annotation not found in classpath")
            return false
        }

        log("RestController type found: ${restControllerType.qualifiedName}")

        val controllers = roundEnv.getElementsAnnotatedWith(restControllerType)

        log("Found ${controllers.size} controller(s) in this round")

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
                log("Processing controller: ${controller.qualifiedName}")
                
                val docComment = processingEnv.elementUtils.getDocComment(controller)
                val lsiClass = AptLsiClass(controller, docComment)
                
                log("LsiClass created: name=${lsiClass.name}, qualifiedName=${lsiClass.qualifiedName}")
                log("LsiClass annotations: ${lsiClass.annotations.map { it.simpleName }}")
                log("LsiClass methods count: ${lsiClass.methods.size}")
                
                lsiClass.methods.forEach { method ->
                    log("  Method: ${method.name}, annotations=${method.annotations.map { it.simpleName }}")
                    log("    returnType=${method.returnTypeName}, params=${method.parameters.map { "${it.name}:${it.typeName}" }}")
                }
                
                val metadata = ControllerMetadataExtractor.extract(lsiClass)
                
                log("Extracted metadata: className=${metadata.className}, packageName=${metadata.packageName}")
                log("Extracted basePath=${metadata.basePath}, methods count=${metadata.methods.size}")
                
                metadata.methods.forEach { m ->
                    log("  Extracted method: ${m.name}, httpMethod=${m.httpMethod}, path=${m.path}")
                    log("    returnType=${m.returnType}, params=${m.parameters.map { "${it.name}:${it.type}@${it.annotation}" }}")
                }
                
                if (metadata.methods.isNotEmpty()) {
                    generator.generate(metadata)
                    log("Generated FeignClient for ${metadata.className}")
                } else {
                    log("Skipped ${metadata.className} (no HTTP methods found after extraction)")
                }
            } catch (e: Exception) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.WARNING,
                    "[Controller2Feign] Error processing ${controller.simpleName}: ${e.message}\n${e.stackTraceToString()}"
                )
            }
        }

        return false
    }

    private fun log(message: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "[Controller2Feign] $message")
    }
}
