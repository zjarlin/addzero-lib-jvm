package site.addzero.ioc.scanner

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import site.addzero.util.lsi.clazz.LsiClass
import site.addzero.util.lsi.clazz.packageName

data class ComponentScanInfo(
    val targetClass: LsiClass,
    val packages: List<String>,
    val excludePackages: List<String>,
    val defaultNamespace: String
)

class ComponentScanGenerator(private val codeGenerator: CodeGenerator) {

    fun generate(scans: List<ComponentScanInfo>, allComponents: List<LsiClass>) {
        scans.forEach { scanInfo ->
            generateScanner(scanInfo, allComponents)
        }
    }

    private fun generateScanner(scanInfo: ComponentScanInfo, allComponents: List<LsiClass>) {
        // packages 为空时，回退到声明类自身的包名，再回退到 defaultNamespace
        val packagesToScan = scanInfo.packages.ifEmpty {
            val selfPackage = scanInfo.targetClass.packageName
            if (!selfPackage.isNullOrEmpty()) listOf(selfPackage) else listOf(scanInfo.defaultNamespace)
        }

        val matchedComponents = allComponents.filter { component ->
            val qualifiedName = component.qualifiedName ?: return@filter false
            val inScanScope = packagesToScan.any { pkg -> qualifiedName.startsWith(pkg) }
            val excluded = scanInfo.excludePackages.any { pkg -> qualifiedName.startsWith(pkg) }
            inScanScope && !excluded
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
                        "delegate.getRequiredBean(${param.type?.qualifiedName}::class)"
                    }
                    "${component.qualifiedName}($paramCalls)"
                }

                appendLine("            delegate.registerProvider(${component.qualifiedName}::class) { $constructorCall }")
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
                        appendLine("            delegate.registerImplementation(${interfaceName}::class, ${implClass}::class)")
                    }
                }
            }
        }

        return """
            package $packageName

            ${imports.joinToString("\n") { "import $it" }}
            import site.addzero.ioc.registry.KmpBeanRegistry
            import site.addzero.ioc.registry.BeanRegistry
            import site.addzero.ioc.registry.TypeKey
            import kotlin.reflect.KClass

            object $className : BeanRegistry {
                private val delegate = KmpBeanRegistry()

                init {
$registrationCode
                }

                fun getRegistry(): BeanRegistry = delegate

                // BeanRegistry delegation
                override fun <T : Any> getBean(clazz: KClass<T>): T? = delegate.getBean(clazz)
                override fun getBean(name: String): Any? = delegate.getBean(name)
                override fun <T : Any> registerBean(clazz: KClass<T>, instance: T) = delegate.registerBean(clazz, instance)
                override fun <T : Any> registerProvider(clazz: KClass<T>, provider: () -> T) = delegate.registerProvider(clazz, provider)
                override fun <T : Any> registerProvider(name: String, clazz: KClass<T>, provider: () -> T) = delegate.registerProvider(name, clazz, provider)
                override fun <R : Any> registerExtension(receiverClass: KClass<R>, name: String, extension: R.() -> Any?) = delegate.registerExtension(receiverClass, name, extension)
                override fun <R : Any> getExtensions(receiverClass: KClass<R>) = delegate.getExtensions(receiverClass)
                override fun <R : Any> getExtension(receiverClass: KClass<R>, name: String) = delegate.getExtension(receiverClass, name)
                override fun containsBean(clazz: KClass<*>): Boolean = delegate.containsBean(clazz)
                override fun getBeanTypes(): Set<KClass<*>> = delegate.getBeanTypes()
                override fun <T : Any> injectList(clazz: KClass<T>): List<T> = delegate.injectList(clazz)
                override fun <T : Any> getBean(typeKey: TypeKey): T? = delegate.getBean(typeKey)
                override fun <T : Any> registerBean(typeKey: TypeKey, instance: T) = delegate.registerBean(typeKey, instance)
                override fun <T : Any> registerProvider(typeKey: TypeKey, provider: () -> T) = delegate.registerProvider(typeKey, provider)

                inline fun <reified T : Any> getBean(): T? = delegate.getBean(T::class)
                inline fun <reified T : Any> getRequiredBean(): T = delegate.getRequiredBean(T::class)
                inline fun <reified T : Any> injectList(): List<T> = delegate.injectList(T::class)

                fun getScannedPackages(): List<String> = listOf(${scannedPackages.joinToString(", ") { "\"$it\"" }})

                fun getComponentTypes(): Set<KClass<*>> = setOf(
                    ${components.mapNotNull { it.qualifiedName }.joinToString(",\n                    ") { "$it::class" }}
                )
            }
        """.trimIndent()
    }
}
