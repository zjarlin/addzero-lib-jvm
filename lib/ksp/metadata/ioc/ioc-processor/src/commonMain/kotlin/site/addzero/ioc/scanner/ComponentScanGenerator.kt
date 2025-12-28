package site.addzero.ioc.scanner

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.clazz.packageName

data class ComponentScanInfo(
    val targetClass: LsiClass,
    val packages: List<String>,
    val defaultNamespace: String
)

class ComponentScanGenerator(private val codeGenerator: CodeGenerator) {

    fun generate(scans: List<ComponentScanInfo>, allComponents: List<LsiClass>) {
        scans.forEach { scanInfo ->
            generateScanner(scanInfo, allComponents)
        }
    }

    private fun generateScanner(scanInfo: ComponentScanInfo, allComponents: List<LsiClass>) {
        val packagesToScan = scanInfo.packages.ifEmpty { listOf(scanInfo.defaultNamespace) }

        val matchedComponents = allComponents.filter { component ->
            val qualifiedName = component.qualifiedName ?: return@filter false
            packagesToScan.any { pkg -> qualifiedName.startsWith(pkg) }
        }

        if (matchedComponents.isEmpty()) return

        val targetPackage = scanInfo.targetClass.packageName ?: "site.addzero.ioc.generated"
        val targetClassName = scanInfo.targetClass.name ?: "Unknown"
        val scannerClassName = "${targetClassName}ComponentScanner"

        val code = generateScannerCode(
            packageName = targetPackage,
            className = scannerClassName,
            components = matchedComponents,
            scannedPackages = packagesToScan
        )

        codeGenerator.createNewFile(
            Dependencies.ALL_FILES,
            targetPackage,
            scannerClassName,
            "kt"
        ).use { it.write(code.toByteArray()) }
    }

    private fun generateScannerCode(
        packageName: String,
        className: String,
        components: List<LsiClass>,
        scannedPackages: List<String>
    ): String {
        val imports = components.mapNotNull { it.qualifiedName }.toSet() +
                components.flatMap { it.interfaces.mapNotNull { i -> i.qualifiedName } }.toSet()

        val registrationCode = buildString {
            components.forEach { component ->
                val constructor = component.methods.find { it.name == "<init>" }
                val params = constructor?.parameters ?: emptyList()

                val constructorCall = if (params.isEmpty()) {
                    "${component.qualifiedName}()"
                } else {
                    val paramCalls = params.joinToString(", ") { param ->
                        "registry.getRequiredBean(${param.type?.qualifiedName}::class)"
                    }
                    "${component.qualifiedName}($paramCalls)"
                }

                appendLine("            registry.registerProvider(${component.qualifiedName}::class) { $constructorCall }")
            }
            appendLine()

            val interfaceMap = mutableMapOf<String, MutableList<String>>()
            components.forEach { component ->
                component.interfaces.forEach { interfaceName ->
                    interfaceMap.getOrPut(interfaceName.qualifiedName ?: "") { mutableListOf() }
                        .add(component.qualifiedName ?: "")
                }
            }

            if (interfaceMap.isNotEmpty()) {
                appendLine("            // Register interface implementations")
                interfaceMap.forEach { (interfaceName, implementations) ->
                    implementations.forEach { implClass ->
                        appendLine("            registry.registerImplementation(${interfaceName}::class, ${implClass}::class)")
                    }
                }
            }
        }

        return """
            package $packageName

            ${imports.joinToString("\n") { "import $it" }}
            import site.addzero.ioc.registry.KmpBeanRegistry
            import site.addzero.ioc.registry.BeanRegistry
            import kotlin.reflect.KClass

            object $className {
                private val registry = KmpBeanRegistry()

                init {
$registrationCode
                }

                fun getRegistry(): BeanRegistry = registry

                inline fun <reified T : Any> getBean(): T? = registry.getBean(T::class)

                inline fun <reified T : Any> getRequiredBean(): T = registry.getRequiredBean(T::class)

                inline fun <reified T : Any> injectList(): List<T> = registry.injectList(T::class)

                fun getScannedPackages(): List<String> = listOf(${scannedPackages.joinToString(", ") { "\"$it\"" }})

                fun getComponentTypes(): Set<KClass<*>> = setOf(
                    ${components.mapNotNull { it.qualifiedName }.joinToString(",\n                    ") { "$it::class" }}
                )
            }
        """.trimIndent()
    }
}
