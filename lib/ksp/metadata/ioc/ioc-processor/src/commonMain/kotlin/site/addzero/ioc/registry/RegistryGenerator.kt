package site.addzero.ioc.registry

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import site.addzero.util.lsi.clazz.LsiClass

class RegistryGenerator(private val codeGenerator: CodeGenerator) {
    fun generate(components: List<LsiClass>) {
        if (components.isEmpty()) return

        val imports = (components.mapNotNull { it.qualifiedName } +
                components.flatMap { it.interfaces.mapNotNull { i -> i.qualifiedName } }).toSet()

        val registrationCode = buildString {
            components.forEach { component ->
                appendLine("        delegate.registerProvider(${component.qualifiedName}::class) { ${component.qualifiedName}() }")
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
                appendLine("        // 注册接口实现关系")
                interfaceMap.forEach { (interfaceName, implementations) ->
                    implementations.forEach { implClass ->
                        appendLine("        delegate.registerImplementation(${interfaceName}::class, ${implClass}::class)")
                    }
                }
            }
        }

        val componentNames = components.mapNotNull { comp ->
            comp.annotations.find { it.qualifiedName == "site.addzero.ioc.annotation.Component" }
                ?.getAttribute("value")?.toString()?.takeIf { it.isNotEmpty() }
                ?: comp.name?.replaceFirstChar { it.lowercase() }
        }

        val code = """
            package site.addzero.ioc.generated

            ${imports.joinToString("\n") { "import $it" }}
            import site.addzero.ioc.registry.KmpBeanRegistry
            import site.addzero.ioc.registry.BeanRegistry
            import site.addzero.ioc.registry.MetadataLoader
            import site.addzero.ioc.registry.TypeKey
            import kotlin.reflect.KClass

            public object AutoBeanRegistry : BeanRegistry {
                private val delegate = KmpBeanRegistry()

                override fun <T : Any> getBean(clazz: KClass<T>): T? = delegate.getBean(clazz)
                override fun <T : Any> getRequiredBean(clazz: KClass<T>): T = delegate.getRequiredBean(clazz)
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

                init {
$registrationCode
                    loadComponentsFromMetadata()
                }

                private fun loadComponentsFromMetadata() {
                    MetadataLoader.loadComponentMetadata().forEach { metadata ->
                        val javaClass = Class.forName(metadata.className)
                        val clazz = javaClass.kotlin
                        val constructor = javaClass.getDeclaredConstructor()
                        constructor.isAccessible = true
                        @Suppress("UNCHECKED_CAST")
                        delegate.registerProvider(clazz as KClass<Any>) { constructor.newInstance() }
                        javaClass.interfaces.forEach { interfaceClass ->
                            val interfaceKClass = interfaceClass.kotlin
                            @Suppress("UNCHECKED_CAST")
                            delegate.registerImplementation(interfaceKClass as KClass<Any>, clazz as KClass<Any>)
                        }
                    }
                }

                fun getComponentNames(): Set<String> = setOf(${componentNames.joinToString(", ") { "\"$it\"" }})

                fun getComponentType(name: String): KClass<*>? = when (name) {
${components.mapIndexed { i, comp -> "                    \"${componentNames[i]}\" -> ${comp.qualifiedName}::class" }.joinToString("\n")}
                    else -> null
                }
            }
        """.trimIndent()

        codeGenerator.createNewFile(
            Dependencies.ALL_FILES,
            "site.addzero.ioc.generated",
            "AutoBeanRegistry",
            "kt"
        ).use { it.write(code.toByteArray()) }
    }

    fun generateMetadata(components: List<LsiClass>) {
        if (components.isEmpty()) return

        val propertiesContent = buildString {
            appendLine("# Auto-generated by IOC Processor")
            appendLine("# Components discovered in this module")
            appendLine()
            components.forEach { component ->
                val name = component.annotations.find { it.qualifiedName == "site.addzero.ioc.annotation.Component" }
                    ?.getAttribute("value")?.toString()?.takeIf { it.isNotEmpty() }
                    ?: component.name?.replaceFirstChar { it.lowercase() }
                appendLine("$name=${component.qualifiedName}")
            }
        }

        codeGenerator.createNewFile(
            Dependencies.ALL_FILES,
            "META-INF",
            "ioc-components",
            "properties"
        ).use { it.write(propertiesContent.toByteArray()) }
    }
}
