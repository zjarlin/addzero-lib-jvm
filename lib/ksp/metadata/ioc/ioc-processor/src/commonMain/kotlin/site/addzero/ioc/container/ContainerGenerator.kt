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
        val startableRegularFunctions = regularFunctions.filter { it.enabled }
        val startableClassInstances = classInstances.filter { it.enabled }
        val startableObjectInstances = objectInstances.filter { it.enabled }
        val startableExtensionFunctions = extensionFunctions.filter { it.enabled }
        val startableComposableFunctions = composableFunctions.filter { it.enabled }
        val startableSuspendFunctions = suspendFunctions.filter { it.enabled }

        generateSpiModule(beans, startableClassInstances, startableObjectInstances, classComponents)

        if (isApp) {
            generateIoc(
                allBeans = beans,
                regularFunctions = startableRegularFunctions,
                classInstances = startableClassInstances,
                objectInstances = startableObjectInstances,
                classComponents = classComponents
            )
        }

        if (startableExtensionFunctions.isNotEmpty()) generateExtensionModules(startableExtensionFunctions)
        if (startableComposableFunctions.isNotEmpty()) generateComposableModule(startableComposableFunctions)
        if (startableSuspendFunctions.isNotEmpty()) generateSuspendModule(startableSuspendFunctions)
    }

    // ============================================================
    // Ioc object (app module only)
    // ============================================================

    private fun generateIoc(
        allBeans: List<BeanInfo>,
        regularFunctions: List<BeanInfo>,
        classInstances: List<BeanInfo>,
        objectInstances: List<BeanInfo>,
        classComponents: List<LsiClass>
    ) {
        val beanImports = (classInstances + objectInstances)
            .filter { it.enabled }
            .map { it.name }
            .toSet()
        val interfaceMap = buildInterfaceMap(classComponents)
        val interfaceImports = interfaceMap.keys + interfaceMap.values.flatten()
        val code = buildString {
            appendLine("package $generatedPackage")
            appendLine()
            (beanImports + interfaceImports).toSet().forEach { appendLine("import $it") }
            appendLine("import site.addzero.ioc.registry.BeanDefinition")
            appendLine("import site.addzero.ioc.registry.BeanDefinitions")
            appendLine("import site.addzero.ioc.registry.BeanRegistry")
            appendLine("import site.addzero.ioc.registry.KmpBeanRegistry")
            appendLine("import site.addzero.ioc.spi.IocModuleRegistry")
            appendLine()
            appendLine("/**")
            appendLine(" * Generated IoC container.")
            appendLine(" *")
            appendLine(" * Usage:")
            appendLine(" *   val service = Ioc.getBean<MyService>()")
            appendLine(" *   val required = Ioc.require<MyService>()")
            appendLine(" *   val all = Ioc.injectList<MyInterface>()")
            appendLine(" */")
            appendLine("object Ioc {")
            appendLine("    private val _registry = KmpBeanRegistry()")
            appendLine("    private var _moduleBeanDefinitions: List<BeanDefinition> = emptyList()")
            appendLine("    val registry: BeanRegistry get() = _registry")
            appendLine("    private var _initialized = false")
            appendLine()
            appendLine("    private val localBeanDefinitions = listOf(")
            appendBeanDefinitions(allBeans, "        ")
            appendLine("    )")
            appendLine()
            appendLine("    val beanDefinitions: List<BeanDefinition>")
            appendLine("        get() = BeanDefinitions.unique(localBeanDefinitions + _moduleBeanDefinitions)")
            appendLine()
            appendLine("    val activeBeanDefinitions: List<BeanDefinition>")
            appendLine("        get() = BeanDefinitions.enabled(beanDefinitions)")
            appendLine()
            appendLine("    val beanDefinitionsByTag: Map<String, List<BeanDefinition>>")
            appendLine("        get() = BeanDefinitions.groupByTag(activeBeanDefinitions)")
            appendLine()
            appendLine("    fun findBeanDefinition(name: String): BeanDefinition? = BeanDefinitions.find(beanDefinitions, name)")
            appendLine()
            appendLine("    fun findBeanDefinitions(tag: String, includeDependsOn: Boolean = true): List<BeanDefinition> =")
            appendLine("        if (includeDependsOn) BeanDefinitions.resolve(beanDefinitions, tag) else beanDefinitionsByTag[tag] ?: emptyList()")
            appendLine()
            appendLine("    init {")

            classInstances.forEach { bean ->
                appendLine("        _registry.registerDefinition(${bean.name}::class, ${beanDefinitionLiteral(bean)})")
                appendLine("        _registry.registerProvider(\"${deriveName(bean)}\", ${bean.name}::class) { ${bean.name}() }")
            }
            objectInstances.forEach { bean ->
                appendLine("        _registry.registerDefinition(${bean.name}::class, ${beanDefinitionLiteral(bean)})")
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

            appendLine("    }")
            appendLine()
            appendLine("    /**")
            appendLine("     * Call after all library modules have called registerThisModule().")
            appendLine("     * Applies cross-module SPI providers to the registry.")
            appendLine("     */")
            appendLine("    fun initialize() {")
            appendLine("        if (_initialized) return")
            appendLine("        _initialized = true")
            appendLine("        val providers = IocModuleRegistry.getProviders()")
            appendLine("        providers.forEach { it.register(_registry) }")
            appendLine("        _moduleBeanDefinitions = providers.flatMap { it.definitions() }")
            appendLine("    }")
            appendLine()
            appendLine("    inline fun <reified T : Any> getBean(): T? { initialize(); return registry.getBean(T::class) }")
            appendLine("    inline fun <reified T : Any> getBean(vararg generics: kotlin.reflect.KClass<*>): T? { initialize(); return registry.getBean(site.addzero.ioc.registry.TypeKey.of(T::class, *generics)) }")
            appendLine("    inline fun <reified T : Any> require(): T = getBean<T>() ?: throw IllegalArgumentException(\"No bean: \${T::class.simpleName}\")")
            appendLine("    inline fun <reified T : Any> require(vararg generics: kotlin.reflect.KClass<*>): T = getBean<T>(*generics) ?: throw IllegalArgumentException(\"No bean: \${T::class.simpleName}\")")
            appendLine("    inline fun <reified T : Any> injectList(): List<T> { initialize(); return registry.injectList(T::class) }")
            appendLine("    inline fun <reified T : Any> injectList(tag: String): List<T> { initialize(); return registry.injectList(T::class, tag) }")
            appendLine("    inline fun <reified T : Any> getBean(name: String): T? { initialize(); return registry.getBean(name) as? T }")
            appendLine()
            appendLine("    private val startableBeanDefinitions = listOf(")
            appendBeanDefinitions(regularFunctions + classInstances + objectInstances, "        ")
            appendLine("    )")
            appendLine("    private val startableActions: Map<String, () -> Unit> = linkedMapOf(")
            regularFunctions.forEach { bean ->
                appendLine("        \"${identityOf(bean)}\" to { ${bean.name}() },")
            }
            classInstances.forEach { bean ->
                appendLine("        \"${identityOf(bean)}\" to { ${bean.name}() },")
            }
            objectInstances.forEach { bean ->
                appendLine("        \"${identityOf(bean)}\" to { ${bean.name} },")
            }
            appendLine("    )")

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

            appendLine()
            appendLine("    fun startAll() {")
            appendLine("        initialize()")
            appendLine("        BeanDefinitions.resolve(startableBeanDefinitions).forEach { definition ->")
            appendLine("            startableActions[definition.identity]?.invoke()")
            appendLine("        }")
            appendLine("    }")
            appendLine()
            appendLine("    /** Start only beans matching the given tag */")
            appendLine("    fun startAll(tag: String) {")
            appendLine("        initialize()")
            appendLine("        BeanDefinitions.resolve(startableBeanDefinitions, tags = setOf(tag)).forEach { definition ->")
            appendLine("            startableActions[definition.identity]?.invoke()")
            appendLine("        }")
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
        allBeans: List<BeanInfo>,
        classInstances: List<BeanInfo>,
        objectInstances: List<BeanInfo>,
        classComponents: List<LsiClass>
    ) {
        if (allBeans.isEmpty()) return

        val imports = (classInstances + objectInstances)
            .filter { it.enabled }
            .map { it.name }
            .toSet()
        val interfaceMap = buildInterfaceMap(classComponents)
        val interfaceImports = interfaceMap.keys + interfaceMap.values.flatten()
        val code = buildString {
            appendLine("package $generatedPackage")
            appendLine()
            (imports + interfaceImports).toSet().forEach { appendLine("import $it") }
            appendLine("import site.addzero.ioc.registry.BeanDefinition")
            appendLine("import site.addzero.ioc.registry.MutableBeanRegistry")
            appendLine("import site.addzero.ioc.spi.IocModuleProvider")
            appendLine("import site.addzero.ioc.spi.IocModuleRegistry")
            appendLine()
            appendLine("private val thisModuleBeanDefinitions = listOf(")
            appendBeanDefinitions(allBeans, "    ")
            appendLine(")")
            appendLine()
            appendLine("object ThisModuleProvider : IocModuleProvider {")
            appendLine("    override val moduleName: String = \"$generatedPackage\"")
            appendLine()
            appendLine("    override fun definitions(): List<BeanDefinition> = thisModuleBeanDefinitions")
            appendLine()
            appendLine("    override fun register(registry: MutableBeanRegistry) {")

            classInstances.forEach { bean ->
                appendLine("        registry.registerDefinition(${bean.name}::class, ${beanDefinitionLiteral(bean)})")
                appendLine("        registry.registerProvider(\"${deriveName(bean)}\", ${bean.name}::class) { ${bean.name}() }")
            }
            objectInstances.forEach { bean ->
                appendLine("        registry.registerDefinition(${bean.name}::class, ${beanDefinitionLiteral(bean)})")
                appendLine("        registry.register(${bean.name}::class, ${bean.name})")
                appendLine("        registry.registerProvider(\"${deriveName(bean)}\", ${bean.name}::class) { ${bean.name} }")
            }
            if (interfaceMap.isNotEmpty()) {
                interfaceMap.forEach { (ifaceFqn, impls) ->
                    impls.forEach { implFqn ->
                        appendLine("        registry.registerImplementation(${ifaceFqn}::class, ${implFqn}::class)")
                    }
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
            val receiverFqn: String,
            val funcFqn: String,
            val funcSimpleName: String,
            val beanInfo: BeanInfo
        )

        val parsed = extensionFunctions.map { bean ->
            val receiverFqn = bean.name.substringBefore("::")
            val funcFqn = bean.name.substringAfter("::")
            val funcSimpleName = funcFqn.substringAfterLast(".")
            ExtFuncInfo(receiverFqn, funcFqn, funcSimpleName, bean)
        }

        parsed.groupBy { it.receiverFqn }.forEach { (receiverFqn, funcs) ->
            val receiverSimpleName = receiverFqn.substringAfterLast(".")
            val fileName = "Ioc${receiverSimpleName}Module"

            val code = buildString {
                appendLine("package $generatedPackage")
                appendLine()
                appendLine("import $receiverFqn")
                appendLine("import site.addzero.ioc.registry.BeanDefinition")
                appendLine("import site.addzero.ioc.registry.BeanDefinitions")
                funcs.forEach { appendLine("import ${it.funcFqn}") }
                appendLine()
                appendLine("private val ioc${receiverSimpleName}ExtensionDefinitions = listOf(")
                appendBeanDefinitions(funcs.map { it.beanInfo }, "    ")
                appendLine(")")
                appendLine()
                appendLine("private val ioc${receiverSimpleName}ExtensionActions: Map<String, $receiverSimpleName.() -> Unit> = linkedMapOf(")
                funcs.forEach { func ->
                    appendLine("    \"${identityOf(func.beanInfo)}\" to { ${func.funcSimpleName}() },")
                }
                appendLine(")")
                appendLine()
                appendLine("/**")
                appendLine(" * All @Bean extension functions for $receiverSimpleName, keyed by bean identity.")
                appendLine(" */")
                appendLine("val ioc${receiverSimpleName}Extensions: Map<String, $receiverSimpleName.() -> Unit>")
                appendLine("    get() = ioc${receiverSimpleName}ExtensionDefinitions")
                appendLine("        .associate { it.identity to ioc${receiverSimpleName}ExtensionActions.getValue(it.identity) }")
                appendLine()
                appendLine("/**")
                appendLine(" * @Bean extension functions for $receiverSimpleName grouped by tag.")
                appendLine(" */")
                appendLine("val ioc${receiverSimpleName}ExtensionsByTag: Map<String, Map<String, $receiverSimpleName.() -> Unit>>")
                appendLine("    get() = BeanDefinitions.groupByTag(BeanDefinitions.enabled(ioc${receiverSimpleName}ExtensionDefinitions))")
                appendLine("        .mapValues { (_, beans) -> beans.associate { it.identity to ioc${receiverSimpleName}ExtensionActions.getValue(it.identity) } }")
                appendLine()
                appendLine("fun ioc${receiverSimpleName}ExtensionsByTag(tag: String): Map<String, $receiverSimpleName.() -> Unit> =")
                appendLine("    ioc${receiverSimpleName}ExtensionsByTag[tag] ?: emptyMap()")
                appendLine()
                appendLine("/** Apply all @Bean extensions to this $receiverSimpleName */")
                appendLine("fun $receiverSimpleName.iocModule() {")
                appendLine("    BeanDefinitions.resolve(ioc${receiverSimpleName}ExtensionDefinitions).forEach { definition ->")
                appendLine("        ioc${receiverSimpleName}ExtensionActions[definition.identity]?.invoke(this)")
                appendLine("    }")
                appendLine("}")
                appendLine()
                appendLine("/** Apply @Bean extensions matching the given tag */")
                appendLine("fun $receiverSimpleName.iocModule(tag: String) {")
                appendLine("    BeanDefinitions.resolve(ioc${receiverSimpleName}ExtensionDefinitions, tags = setOf(tag)).forEach { definition ->")
                appendLine("        ioc${receiverSimpleName}ExtensionActions[definition.identity]?.invoke(this)")
                appendLine("    }")
                appendLine("}")
                appendLine()
                appendLine("fun register${receiverSimpleName}Extensions(registry: site.addzero.ioc.registry.MutableBeanRegistry) {")
                funcs.forEach { func ->
                    appendLine("    registry.registerExtension($receiverSimpleName::class, \"${func.beanInfo.resolvedBeanName}\") { ${func.funcSimpleName}() }")
                }
                appendLine("}")
            }

            codeGenerator.createNewFile(
                Dependencies.ALL_FILES, generatedPackage, fileName, "kt"
            ).use { it.write(code.toByteArray()) }
        }
    }

    private fun generateComposableModule(composables: List<BeanInfo>) {
        val code = buildString {
            appendLine("package $generatedPackage")
            appendLine()
            appendLine("import androidx.compose.runtime.Composable")
            appendLine("import site.addzero.ioc.registry.BeanDefinition")
            appendLine("import site.addzero.ioc.registry.BeanDefinitions")
            composables.forEach { appendLine("import ${it.name}") }
            appendLine()
            appendLine("private val iocComposableDefinitions = listOf(")
            appendBeanDefinitions(composables, "    ")
            appendLine(")")
            appendLine()
            appendLine("private val iocComposableActions: Map<String, @Composable () -> Unit> = linkedMapOf(")
            composables.forEach { bean ->
                appendLine("    \"${identityOf(bean)}\" to { ${bean.simpleName}() },")
            }
            appendLine(")")
            appendLine()
            appendLine("/**")
            appendLine(" * All @Bean @Composable functions, keyed by bean identity.")
            appendLine(" */")
            appendLine("val iocComposables: Map<String, @Composable () -> Unit>")
            appendLine("    get() = iocComposableDefinitions.associate { it.identity to iocComposableActions.getValue(it.identity) }")
            appendLine()
            appendLine("/**")
            appendLine(" * @Bean @Composable functions grouped by tag.")
            appendLine(" */")
            appendLine("val iocComposablesByTag: Map<String, Map<String, @Composable () -> Unit>>")
            appendLine("    get() = BeanDefinitions.groupByTag(BeanDefinitions.enabled(iocComposableDefinitions))")
            appendLine("        .mapValues { (_, beans) -> beans.associate { it.identity to iocComposableActions.getValue(it.identity) } }")
            appendLine()
            appendLine("fun iocComposablesByTag(tag: String): Map<String, @Composable () -> Unit> =")
            appendLine("    iocComposablesByTag[tag] ?: emptyMap()")
            appendLine()
            appendLine("@Composable")
            appendLine("fun IocComposableModule() {")
            appendLine("    BeanDefinitions.resolve(iocComposableDefinitions).forEach { definition ->")
            appendLine("        iocComposableActions[definition.identity]?.invoke()")
            appendLine("    }")
            appendLine("}")
            appendLine()
            appendLine("@Composable")
            appendLine("fun IocComposableModule(tag: String) {")
            appendLine("    BeanDefinitions.resolve(iocComposableDefinitions, tags = setOf(tag)).forEach { definition ->")
            appendLine("        iocComposableActions[definition.identity]?.invoke()")
            appendLine("    }")
            appendLine("}")
        }

        codeGenerator.createNewFile(
            Dependencies.ALL_FILES, generatedPackage, "IocComposableModule", "kt"
        ).use { it.write(code.toByteArray()) }
    }

    private fun generateSuspendModule(suspends: List<BeanInfo>) {
        val code = buildString {
            appendLine("package $generatedPackage")
            appendLine()
            appendLine("import site.addzero.ioc.registry.BeanDefinition")
            appendLine("import site.addzero.ioc.registry.BeanDefinitions")
            suspends.forEach { appendLine("import ${it.name}") }
            appendLine()
            appendLine("private val iocSuspendDefinitions = listOf(")
            appendBeanDefinitions(suspends, "    ")
            appendLine(")")
            appendLine()
            appendLine("private val iocSuspendActions: Map<String, suspend () -> Unit> = linkedMapOf(")
            suspends.forEach { bean ->
                appendLine("    \"${identityOf(bean)}\" to { ${bean.simpleName}() },")
            }
            appendLine(")")
            appendLine()
            appendLine("/**")
            appendLine(" * All @Bean suspend functions, keyed by bean identity.")
            appendLine(" */")
            appendLine("val iocSuspends: Map<String, suspend () -> Unit>")
            appendLine("    get() = iocSuspendDefinitions.associate { it.identity to iocSuspendActions.getValue(it.identity) }")
            appendLine()
            appendLine("/**")
            appendLine(" * @Bean suspend functions grouped by tag.")
            appendLine(" */")
            appendLine("val iocSuspendsByTag: Map<String, Map<String, suspend () -> Unit>>")
            appendLine("    get() = BeanDefinitions.groupByTag(BeanDefinitions.enabled(iocSuspendDefinitions))")
            appendLine("        .mapValues { (_, beans) -> beans.associate { it.identity to iocSuspendActions.getValue(it.identity) } }")
            appendLine()
            appendLine("fun iocSuspendsByTag(tag: String): Map<String, suspend () -> Unit> =")
            appendLine("    iocSuspendsByTag[tag] ?: emptyMap()")
            appendLine()
            appendLine("/** Execute all @Bean suspend functions */")
            appendLine("suspend fun iocSuspendModule() {")
            appendLine("    BeanDefinitions.resolve(iocSuspendDefinitions).forEach { definition ->")
            appendLine("        iocSuspendActions[definition.identity]?.invoke()")
            appendLine("    }")
            appendLine("}")
            appendLine()
            appendLine("/** Execute @Bean suspend functions matching the given tag */")
            appendLine("suspend fun iocSuspendModule(tag: String) {")
            appendLine("    BeanDefinitions.resolve(iocSuspendDefinitions, tags = setOf(tag)).forEach { definition ->")
            appendLine("        iocSuspendActions[definition.identity]?.invoke()")
            appendLine("    }")
            appendLine("}")
        }

        codeGenerator.createNewFile(
            Dependencies.ALL_FILES, generatedPackage, "IocSuspendModule", "kt"
        ).use { it.write(code.toByteArray()) }
    }

    // ============================================================
    // Helpers
    // ============================================================

    private fun generateBatchCode(
        label: String,
        beans: List<BeanInfo>,
        lambdaExpr: (BeanInfo) -> String
    ): String {
        return buildString {
            appendLine("    private val ${label.replaceFirstChar { it.lowercase() }}BeanDefinitions = listOf(")
            appendBeanDefinitions(beans, "        ")
            appendLine("    )")
            appendLine("    private val ${label.replaceFirstChar { it.lowercase() }}Actions: Map<String, () -> Unit> = linkedMapOf(")
            beans.forEach { bean ->
                appendLine("        \"${identityOf(bean)}\" to ${lambdaExpr(bean)},")
            }
            appendLine("    )")
            appendLine("    fun start$label() {")
            appendLine("        BeanDefinitions.resolve(${label.replaceFirstChar { it.lowercase() }}BeanDefinitions).forEach { definition ->")
            appendLine("            ${label.replaceFirstChar { it.lowercase() }}Actions[definition.identity]?.invoke()")
            appendLine("        }")
            appendLine("    }")
            appendLine("    fun start$label(tag: String) {")
            appendLine("        BeanDefinitions.resolve(${label.replaceFirstChar { it.lowercase() }}BeanDefinitions, tags = setOf(tag)).forEach { definition ->")
            appendLine("            ${label.replaceFirstChar { it.lowercase() }}Actions[definition.identity]?.invoke()")
            appendLine("        }")
            appendLine("    }")
        }.trimEnd()
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

    private fun deriveName(bean: BeanInfo): String = bean.resolvedBeanName

    private fun identityOf(bean: BeanInfo): String = bean.qualifiedName.ifBlank { deriveName(bean) }

    private fun beanDefinitionLiteral(bean: BeanInfo): String {
        return buildString {
            append("BeanDefinition(")
            append("simpleName = ${stringLiteral(bean.simpleName)}, ")
            append("qualifiedName = ${stringLiteral(bean.qualifiedName)}, ")
            append("beanName = ${stringLiteral(deriveName(bean))}, ")
            append("enabled = ${bean.enabled}, ")
            append("tags = ${stringListLiteral(bean.tags)}, ")
            append("order = ${bean.order}, ")
            append("dependsOn = ${stringListLiteral(bean.dependsOn)}")
            append(")")
        }
    }

    private fun StringBuilder.appendBeanDefinitions(beans: List<BeanInfo>, indent: String) {
        if (beans.isEmpty()) {
            appendLine("${indent}// no bean definitions")
            return
        }
        beans.forEachIndexed { index, bean ->
            val suffix = if (index == beans.lastIndex) "" else ","
            appendLine("$indent${beanDefinitionLiteral(bean)}$suffix")
        }
    }

    private fun stringListLiteral(values: List<String>): String {
        return if (values.isEmpty()) {
            "emptyList()"
        } else {
            values.distinct().joinToString(prefix = "listOf(", postfix = ")") { stringLiteral(it) }
        }
    }

    private fun stringLiteral(value: String): String {
        return buildString {
            append('"')
            value.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(char)
                }
            }
            append('"')
        }
    }
}
