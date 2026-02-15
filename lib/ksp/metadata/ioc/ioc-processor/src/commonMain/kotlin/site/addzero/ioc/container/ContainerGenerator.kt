package site.addzero.ioc.container

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import site.addzero.ioc.strategy.BeanInfo
import site.addzero.ioc.strategy.InitType
import site.addzero.util.lsi.clazz.LsiClass

/**
 * @param generatedPackage package name for generated files (module-specific, no conflicts)
 * @param isApp true = generate Ioc object + SPI; false = generate SPI only
 */
class ContainerGenerator(
    private val codeGenerator: CodeGenerator,
    private val generatedPackage: String,
    private val isApp: Boolean
) {

    fun generate(beans: List<BeanInfo>, classComponents: List<LsiClass>) {
        if (beans.isEmpty()) return

        val regularFunctions = beans.filter {
            it.initType == InitType.TOP_LEVEL_FUNCTION || it.initType == InitType.COMPANION_OBJECT
        }
        val classInstances = beans.filter { it.initType == InitType.CLASS_INSTANCE }
        val objectInstances = beans.filter { it.initType == InitType.OBJECT_INSTANCE }
        val extensionFunctions = beans.filter { it.initType == InitType.EXTENSION_FUNCTION }
        val composableFunctions = beans.filter { it.initType == InitType.COMPOSABLE_FUNCTION }
        val suspendFunctions = beans.filter { it.initType == InitType.SUSPEND_FUNCTION }

        // always generate SPI module
        generateSpiModule(classInstances, objectInstances, classComponents)

        // only app module generates the Ioc entry point
        if (isApp) {
            generateIoc(regularFunctions, classInstances, objectInstances, classComponents)
        }

        if (extensionFunctions.isNotEmpty()) generateExtensionModules(extensionFunctions)
        if (composableFunctions.isNotEmpty()) generateComposableModule(composableFunctions)
        if (suspendFunctions.isNotEmpty()) generateSuspendModule(suspendFunctions)
    }

    // ============================================================
    // Ioc object (app module only)
    // ============================================================

    private fun generateIoc(
        regularFunctions: List<BeanInfo>,
        classInstances: List<BeanInfo>,
        objectInstances: List<BeanInfo>,
        classComponents: List<LsiClass>,
        allBeans: List<BeanInfo> = classInstances + objectInstances
    ) {
        val beanImports = (classInstances + objectInstances).map { it.name }.toSet()
        val interfaceMap = buildInterfaceMap(classComponents)
        val interfaceImports = interfaceMap.keys + interfaceMap.values.flatten()

        val code = buildString {
            appendLine("package $generatedPackage")
            appendLine()
            (beanImports + interfaceImports).toSet().forEach { appendLine("import $it") }
            appendLine("import site.addzero.ioc.registry.KmpBeanRegistry")
            appendLine("import site.addzero.ioc.registry.BeanRegistry")
            appendLine("import site.addzero.ioc.registry.MutableBeanRegistry")
            appendLine("import site.addzero.ioc.spi.IocModuleRegistry")
            appendLine()
            appendLine("/**")
            appendLine(" * Generated IoC container.")
            appendLine(" *")
            appendLine(" * Usage:")
            appendLine(" *   val service = Ioc.get<MyService>()")
            appendLine(" *   val required = Ioc.require<MyService>()")
            appendLine(" *   val all = Ioc.getAll<MyInterface>()")
            appendLine(" */")
            appendLine("object Ioc {")
            appendLine("    private val _registry = KmpBeanRegistry()")
            appendLine("    val registry: BeanRegistry get() = _registry")
            appendLine("    private var _initialized = false")
            appendLine()
            appendLine("    init {")

            // register this module's own beans
            classInstances.forEach { bean ->
                appendLine("        _registry.registerProvider(\"${deriveName(bean)}\", ${bean.name}::class) { ${bean.name}() }")
            }
            objectInstances.forEach { bean ->
                appendLine("        _registry.register(${bean.name}::class, ${bean.name})")
                appendLine("        _registry.registerProvider(\"${deriveName(bean)}\", ${bean.name}::class) { ${bean.name} }")
            }
            if (interfaceMap.isNotEmpty()) {
                appendLine()
                appendLine("        // interface -> implementation")
                interfaceMap.forEach { (ifaceFqn, impls) ->
                    impls.forEach { implFqn ->
                        appendLine("        _registry.registerImplementation(${ifaceFqn}::class, ${implFqn}::class)")
                    }
                }
            }

            // tags
            val taggedBeans = allBeans.filter { it.tags.isNotEmpty() }
            if (taggedBeans.isNotEmpty()) {
                appendLine()
                appendLine("        // tags")
                taggedBeans.forEach { bean ->
                    val simpleName = bean.name.substringAfterLast(".")
                    val tagsLiteral = bean.tags.joinToString(", ") { "\"$it\"" }
                    appendLine("        _registry.tagBean(${simpleName}::class, listOf($tagsLiteral))")
                }
            }

            appendLine("    }")
            appendLine()

            // initialize() — call after all library modules have registered
            appendLine("    /**")
            appendLine("     * Call after all library modules have called registerThisModule().")
            appendLine("     * Applies cross-module SPI providers to the registry.")
            appendLine("     */")
            appendLine("    fun initialize() {")
            appendLine("        if (_initialized) return")
            appendLine("        _initialized = true")
            appendLine("        IocModuleRegistry.getProviders().forEach { it.register(_registry) }")
            appendLine("    }")
            appendLine()

            // convenience methods
            appendLine("    inline fun <reified T : Any> get(): T? { initialize(); return registry.get(T::class) }")
            appendLine("    inline fun <reified T : Any> get(vararg generics: kotlin.reflect.KClass<*>): T? { initialize(); return registry.get(site.addzero.ioc.registry.TypeKey.of(T::class, *generics)) }")
            appendLine("    inline fun <reified T : Any> require(): T = get<T>() ?: throw IllegalArgumentException(\"No bean: \${T::class.simpleName}\")")
            appendLine("    inline fun <reified T : Any> require(vararg generics: kotlin.reflect.KClass<*>): T = get<T>(*generics) ?: throw IllegalArgumentException(\"No bean: \${T::class.simpleName}\")")
            appendLine("    inline fun <reified T : Any> getAll(): List<T> { initialize(); return registry.getAll(T::class) }")
            appendLine("    inline fun <reified T : Any> getAll(tag: String): List<T> { initialize(); return registry.getAll(T::class, tag) }")
            appendLine("    fun get(name: String): Any? { initialize(); return registry.get(name) }")

            // batch execution
            if (regularFunctions.isNotEmpty()) {
                appendLine()
                appendLine(generateBatchCode("Regular", regularFunctions) { "{ ${it.name}() }" })
            }
            if (classInstances.isNotEmpty()) {
                appendLine()
                appendLine(generateBatchCode("ClassInstance", classInstances) { "{ ${it.name}() }" })
            }
            if (objectInstances.isNotEmpty()) {
                appendLine()
                appendLine(generateBatchCode("ObjectInstance", objectInstances) { "{ ${it.name} }" })
            }

            val startMethods = mutableListOf<String>()
            if (regularFunctions.isNotEmpty()) startMethods.add("startRegular()")
            if (classInstances.isNotEmpty()) startMethods.add("startClassInstance()")
            if (objectInstances.isNotEmpty()) startMethods.add("startObjectInstance()")

            appendLine()
            appendLine("    fun startAll() {")
            appendLine("        initialize()")
            startMethods.forEach { appendLine("        $it") }
            appendLine("    }")
            appendLine()
            appendLine("    /** Start only beans matching the given tag */")
            appendLine("    fun startAll(tag: String) {")
            appendLine("        initialize()")
            if (regularFunctions.any { it.tags.isNotEmpty() }) appendLine("        startRegular(tag)")
            if (classInstances.any { it.tags.isNotEmpty() }) appendLine("        startClassInstance(tag)")
            if (objectInstances.any { it.tags.isNotEmpty() }) appendLine("        startObjectInstance(tag)")
            appendLine("    }")
            appendLine("}")
        }

        codeGenerator.createNewFile(
            Dependencies.ALL_FILES, generatedPackage, "Ioc", "kt"
        ).use { it.write(code.toByteArray()) }
    }

    // ============================================================
    // SPI module (all modules)
    // ============================================================

    private fun generateSpiModule(
        classInstances: List<BeanInfo>,
        objectInstances: List<BeanInfo>,
        classComponents: List<LsiClass>,
        allBeans: List<BeanInfo> = classInstances + objectInstances
    ) {
        if (classInstances.isEmpty() && objectInstances.isEmpty()) return

        val imports = (classInstances + objectInstances).map { it.name }.toSet()
        val interfaceMap = buildInterfaceMap(classComponents)
        val interfaceImports = interfaceMap.keys + interfaceMap.values.flatten()

        val code = buildString {
            appendLine("package $generatedPackage")
            appendLine()
            (imports + interfaceImports).toSet().forEach { appendLine("import $it") }
            appendLine("import site.addzero.ioc.registry.MutableBeanRegistry")
            appendLine("import site.addzero.ioc.spi.IocModuleProvider")
            appendLine("import site.addzero.ioc.spi.IocModuleRegistry")
            appendLine()
            appendLine("object ThisModuleProvider : IocModuleProvider {")
            appendLine("    override val moduleName: String = \"$generatedPackage\"")
            appendLine()
            appendLine("    override fun register(registry: MutableBeanRegistry) {")

            classInstances.forEach { bean ->
                appendLine("        registry.registerProvider(\"${deriveName(bean)}\", ${bean.name}::class) { ${bean.name}() }")
            }
            objectInstances.forEach { bean ->
                appendLine("        registry.register(${bean.name}::class, ${bean.name})")
            }
            if (interfaceMap.isNotEmpty()) {
                interfaceMap.forEach { (ifaceFqn, impls) ->
                    impls.forEach { implFqn ->
                        appendLine("        registry.registerImplementation(${ifaceFqn}::class, ${implFqn}::class)")
                    }
                }
            }

            // tags
            val taggedBeans = allBeans.filter { it.tags.isNotEmpty() }
            if (taggedBeans.isNotEmpty()) {
                taggedBeans.forEach { bean ->
                    val simpleName = bean.name.substringAfterLast(".")
                    val tagsLiteral = bean.tags.joinToString(", ") { "\"$it\"" }
                    appendLine("        registry.tagBean(${simpleName}::class, listOf($tagsLiteral))")
                }
            }

            appendLine("    }")
            appendLine("}")
            appendLine()
            appendLine("/** Call from library module entry point to register beans for cross-module discovery */")
            appendLine("fun registerThisModule() {")
            appendLine("    IocModuleRegistry.register(ThisModuleProvider)")
            appendLine("}")
        }

        codeGenerator.createNewFile(
            Dependencies.ALL_FILES, generatedPackage, "ThisModuleProvider", "kt"
        ).use { it.write(code.toByteArray()) }
    }

    // ============================================================
    // Extension / Composable / Suspend modules
    // ============================================================

    private fun generateExtensionModules(extensionFunctions: List<BeanInfo>) {
        data class ExtFuncInfo(
            val receiverFqn: String, val funcFqn: String,
            val funcSimpleName: String, val tags: List<String>
        )

        val parsed = extensionFunctions.map { bean ->
            val receiverFqn = bean.name.substringBefore("::")
            val funcFqn = bean.name.substringAfter("::")
            val funcSimpleName = funcFqn.substringAfterLast(".")
            ExtFuncInfo(receiverFqn, funcFqn, funcSimpleName, bean.tags)
        }

        parsed.groupBy { it.receiverFqn }.forEach { (receiverFqn, funcs) ->
            val receiverSimpleName = receiverFqn.substringAfterLast(".")
            val fileName = "Ioc${receiverSimpleName}Module"

            // tag groups for this receiver
            val tagGroups = mutableMapOf<String, MutableList<ExtFuncInfo>>()
            funcs.forEach { f -> f.tags.forEach { tag -> tagGroups.getOrPut(tag) { mutableListOf() }.add(f) } }

            val code = buildString {
                appendLine("package $generatedPackage")
                appendLine()
                appendLine("import $receiverFqn")
                funcs.forEach { appendLine("import ${it.funcFqn}") }
                appendLine()

                // flat map: all extensions keyed by simple name
                appendLine("/**")
                appendLine(" * All @Bean extension functions for $receiverSimpleName, keyed by function name.")
                appendLine(" */")
                appendLine("val ioc${receiverSimpleName}Extensions: Map<String, $receiverSimpleName.() -> Unit> = mapOf(")
                funcs.forEach { appendLine("    \"${it.funcSimpleName}\" to { ${it.funcSimpleName}() },") }
                appendLine(")")
                appendLine()

                // tag-grouped map
                if (tagGroups.isNotEmpty()) {
                    appendLine("/**")
                    appendLine(" * @Bean extension functions for $receiverSimpleName grouped by tag.")
                    appendLine(" */")
                    appendLine("val ioc${receiverSimpleName}ExtensionsByTag: Map<String, Map<String, $receiverSimpleName.() -> Unit>> = mapOf(")
                    tagGroups.forEach { (tag, tagFuncs) ->
                        appendLine("    \"$tag\" to mapOf(")
                        tagFuncs.forEach { appendLine("        \"${it.funcSimpleName}\" to { ${it.funcSimpleName}() },") }
                        appendLine("    ),")
                    }
                    appendLine(")")
                    appendLine()

                    appendLine("/** Get $receiverSimpleName extensions by tag */")
                    appendLine("fun ioc${receiverSimpleName}ExtensionsByTag(tag: String): Map<String, $receiverSimpleName.() -> Unit> =")
                    appendLine("    ioc${receiverSimpleName}ExtensionsByTag[tag] ?: emptyMap()")
                    appendLine()
                }

                // convenience: apply all
                appendLine("/** Apply all @Bean extensions to this $receiverSimpleName */")
                appendLine("fun $receiverSimpleName.iocModule() {")
                funcs.forEach { appendLine("    ${it.funcSimpleName}()") }
                appendLine("}")
                appendLine()

                // convenience: apply by tag
                if (tagGroups.isNotEmpty()) {
                    appendLine("/** Apply @Bean extensions matching the given tag */")
                    appendLine("fun $receiverSimpleName.iocModule(tag: String) {")
                    appendLine("    ioc${receiverSimpleName}ExtensionsByTag(tag).values.forEach { it() }")
                    appendLine("}")
                    appendLine()
                }

                // registry registration
                appendLine("fun register${receiverSimpleName}Extensions(registry: site.addzero.ioc.registry.MutableBeanRegistry) {")
                funcs.forEach { func ->
                    appendLine("    registry.registerExtension($receiverSimpleName::class, \"${func.funcSimpleName}\") { ${func.funcSimpleName}() }")
                }
                appendLine("}")
            }

            codeGenerator.createNewFile(
                Dependencies.ALL_FILES, generatedPackage, fileName, "kt"
            ).use { it.write(code.toByteArray()) }
        }
    }

    private fun generateComposableModule(composables: List<BeanInfo>) {
        val tagGroups = buildTagGroups(composables)

        val code = buildString {
            appendLine("package $generatedPackage")
            appendLine()
            appendLine("import androidx.compose.runtime.Composable")
            composables.forEach { appendLine("import ${it.name}") }
            appendLine()

            // flat map: all composables keyed by qualified name
            appendLine("/**")
            appendLine(" * All @Bean @Composable functions, keyed by qualified name.")
            appendLine(" * Use for nav3 route keys: `iocComposables.forEach { (key, content) -> ... }`")
            appendLine(" */")
            appendLine("val iocComposables: Map<String, @Composable () -> Unit> = mapOf(")
            composables.forEach { bean ->
                val simpleName = bean.name.substringAfterLast(".")
                appendLine("    \"${bean.name}\" to { $simpleName() },")
            }
            appendLine(")")
            appendLine()

            // tag-grouped map
            if (tagGroups.isNotEmpty()) {
                appendLine("/**")
                appendLine(" * @Bean @Composable functions grouped by tag.")
                appendLine(" * e.g. `iocComposablesByTag[\"screen\"]` returns all screen-tagged composables.")
                appendLine(" */")
                appendLine("val iocComposablesByTag: Map<String, Map<String, @Composable () -> Unit>> = mapOf(")
                tagGroups.forEach { (tag, beans) ->
                    appendLine("    \"$tag\" to mapOf(")
                    beans.forEach { bean ->
                        val simpleName = bean.name.substringAfterLast(".")
                        appendLine("        \"${bean.name}\" to { $simpleName() },")
                    }
                    appendLine("    ),")
                }
                appendLine(")")
                appendLine()

                appendLine("/**")
                appendLine(" * Get composables by tag. Returns empty map if tag not found.")
                appendLine(" */")
                appendLine("fun iocComposablesByTag(tag: String): Map<String, @Composable () -> Unit> =")
                appendLine("    iocComposablesByTag[tag] ?: emptyMap()")
                appendLine()
            }

            appendLine("@Composable")
            appendLine("fun IocComposableModule() {")
            appendLine("    iocComposables.values.forEach { it() }")
            appendLine("}")
        }

        codeGenerator.createNewFile(
            Dependencies.ALL_FILES, generatedPackage, "IocComposableModule", "kt"
        ).use { it.write(code.toByteArray()) }
    }

    private fun generateSuspendModule(suspends: List<BeanInfo>) {
        val tagGroups = buildTagGroups(suspends)

        val code = buildString {
            appendLine("package $generatedPackage")
            appendLine()
            suspends.forEach { appendLine("import ${it.name}") }
            appendLine()

            // flat map: all suspend functions keyed by qualified name
            appendLine("/**")
            appendLine(" * All @Bean suspend functions, keyed by qualified name.")
            appendLine(" */")
            appendLine("val iocSuspends: Map<String, suspend () -> Unit> = mapOf(")
            suspends.forEach { bean ->
                val simpleName = bean.name.substringAfterLast(".")
                appendLine("    \"${bean.name}\" to { $simpleName() },")
            }
            appendLine(")")
            appendLine()

            // tag-grouped map
            if (tagGroups.isNotEmpty()) {
                appendLine("/**")
                appendLine(" * @Bean suspend functions grouped by tag.")
                appendLine(" */")
                appendLine("val iocSuspendsByTag: Map<String, Map<String, suspend () -> Unit>> = mapOf(")
                tagGroups.forEach { (tag, beans) ->
                    appendLine("    \"$tag\" to mapOf(")
                    beans.forEach { bean ->
                        val simpleName = bean.name.substringAfterLast(".")
                        appendLine("        \"${bean.name}\" to { $simpleName() },")
                    }
                    appendLine("    ),")
                }
                appendLine(")")
                appendLine()

                appendLine("/**")
                appendLine(" * Get suspend functions by tag. Returns empty map if tag not found.")
                appendLine(" */")
                appendLine("fun iocSuspendsByTag(tag: String): Map<String, suspend () -> Unit> =")
                appendLine("    iocSuspendsByTag[tag] ?: emptyMap()")
                appendLine()
            }

            appendLine("/** Execute all @Bean suspend functions */")
            appendLine("suspend fun iocSuspendModule() {")
            appendLine("    iocSuspends.values.forEach { it() }")
            appendLine("}")

            if (tagGroups.isNotEmpty()) {
                appendLine()
                appendLine("/** Execute @Bean suspend functions matching the given tag */")
                appendLine("suspend fun iocSuspendModule(tag: String) {")
                appendLine("    iocSuspendsByTag(tag).values.forEach { it() }")
                appendLine("}")
            }
        }

        codeGenerator.createNewFile(
            Dependencies.ALL_FILES, generatedPackage, "IocSuspendModule", "kt"
        ).use { it.write(code.toByteArray()) }
    }

    // ============================================================
    // Helpers
    // ============================================================

    private fun generateBatchCode(label: String, beans: List<BeanInfo>, lambdaExpr: (BeanInfo) -> String): String {
        val tagGroups = buildTagGroups(beans)
        return buildString {
            // flat list
            appendLine("    private val collect$label = listOf(")
            appendLine("        ${beans.joinToString(",\n        ") { lambdaExpr(it) }}")
            appendLine("    )")
            appendLine("    fun start$label() { collect$label.forEach { it() } }")

            // tag-grouped map
            if (tagGroups.isNotEmpty()) {
                appendLine()
                appendLine("    private val collect${label}ByTag: Map<String, List<() -> Unit>> = mapOf(")
                tagGroups.forEach { (tag, tagBeans) ->
                    appendLine("        \"$tag\" to listOf(${tagBeans.joinToString(", ") { lambdaExpr(it) }}),")
                }
                appendLine("    )")
                appendLine("    fun start$label(tag: String) { collect${label}ByTag[tag]?.forEach { it() } }")
            }
        }.trimEnd()
    }

    private fun deriveName(bean: BeanInfo): String {
        return bean.beanName.ifEmpty {
            bean.name.substringAfterLast(".").replaceFirstChar { it.lowercase() }
        }
    }

    private fun buildInterfaceMap(classComponents: List<LsiClass>): Map<String, List<String>> {
        val map = mutableMapOf<String, MutableList<String>>()
        classComponents.forEach { component ->
            component.interfaces.forEach { iface ->
                val ifaceFqn = iface.qualifiedName ?: return@forEach
                val implFqn = component.qualifiedName ?: return@forEach
                map.getOrPut(ifaceFqn) { mutableListOf() }.add(implFqn)
            }
        }
        return map
    }

    private fun buildTagGroups(beans: List<BeanInfo>): Map<String, List<BeanInfo>> {
        val groups = mutableMapOf<String, MutableList<BeanInfo>>()
        beans.forEach { bean ->
            bean.tags.forEach { tag -> groups.getOrPut(tag) { mutableListOf() }.add(bean) }
        }
        return groups
    }
}
