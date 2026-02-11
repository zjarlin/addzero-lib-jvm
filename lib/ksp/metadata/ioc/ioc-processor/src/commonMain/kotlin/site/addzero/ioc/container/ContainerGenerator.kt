package site.addzero.ioc.container

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import site.addzero.ioc.strategy.BeanInfo
import site.addzero.ioc.strategy.ClassInstanceStrategy
import site.addzero.ioc.strategy.InitType
import site.addzero.ioc.strategy.ObjectInstanceStrategy
import site.addzero.ioc.strategy.RegularFunctionStrategy

class ContainerGenerator(private val codeGenerator: CodeGenerator) {
    private val strategies = mapOf(
        "regular" to RegularFunctionStrategy(),
        "classInstance" to ClassInstanceStrategy(),
        "objectInstance" to ObjectInstanceStrategy()
    )

    fun generate(beans: List<BeanInfo>) {
        if (beans.isEmpty()) return

        val regularFunctions = beans.filter {
            it.initType == InitType.TOP_LEVEL_FUNCTION || it.initType == InitType.COMPANION_OBJECT
        }
        val classInstances = beans.filter { it.initType == InitType.CLASS_INSTANCE }
        val objectInstances = beans.filter { it.initType == InitType.OBJECT_INSTANCE }
        val extensionFunctions = beans.filter { it.initType == InitType.EXTENSION_FUNCTION }

        val imports = beans.filter { it.initType != InitType.EXTENSION_FUNCTION }
            .mapNotNull {
                when (it.initType) {
                    InitType.CLASS_INSTANCE, InitType.OBJECT_INSTANCE, InitType.COMPANION_OBJECT -> it.name
                    else -> null
                }
            }.toSet()

        generateIocContainer(imports, regularFunctions, classInstances, objectInstances)

        if (extensionFunctions.isNotEmpty()) {
            generateExtensionModules(extensionFunctions)
        }
    }

    private fun generateIocContainer(
        imports: Set<String>,
        regularFunctions: List<BeanInfo>,
        classInstances: List<BeanInfo>,
        objectInstances: List<BeanInfo>
    ) {
        if (regularFunctions.isEmpty() && classInstances.isEmpty() && objectInstances.isEmpty()) return

        val code = buildString {
            appendLine("package site.addzero.ioc.generated")
            appendLine()
            imports.forEach { appendLine("import $it") }
            appendLine("import site.addzero.ioc.registry.KmpBeanRegistry")
            appendLine("import site.addzero.ioc.registry.BeanRegistry")
            appendLine("import kotlin.reflect.KClass")
            appendLine()
            appendLine("public object IocContainer : BeanRegistry {")
            appendLine("    private val delegate = KmpBeanRegistry()")
            appendLine()

            appendLine("    init {")
            classInstances.forEach { bean ->
                val simpleName = bean.name.substringAfterLast(".")
                appendLine("        delegate.registerProvider(\"$simpleName\", ${bean.name}::class) { ${bean.name}() }")
            }
            objectInstances.forEach { bean ->
                val simpleName = bean.name.substringAfterLast(".")
                appendLine("        delegate.registerBean(${bean.name}::class, ${bean.name})")
                appendLine("        delegate.registerProvider(\"$simpleName\", ${bean.name}::class) { ${bean.name} }")
            }
            appendLine("    }")
            appendLine()

            // BeanRegistry delegation
            appendLine("    override fun <T : Any> getBean(clazz: KClass<T>): T? = delegate.getBean(clazz)")
            appendLine("    override fun getBean(name: String): Any? = delegate.getBean(name)")
            appendLine("    override fun <T : Any> registerBean(clazz: KClass<T>, instance: T) = delegate.registerBean(clazz, instance)")
            appendLine("    override fun <T : Any> registerProvider(clazz: KClass<T>, provider: () -> T) = delegate.registerProvider(clazz, provider)")
            appendLine("    override fun <T : Any> registerProvider(name: String, clazz: KClass<T>, provider: () -> T) = delegate.registerProvider(name, clazz, provider)")
            appendLine("    override fun <R : Any> registerExtension(receiverClass: KClass<R>, name: String, extension: R.() -> Any?) = delegate.registerExtension(receiverClass, name, extension)")
            appendLine("    override fun <R : Any> getExtensions(receiverClass: KClass<R>) = delegate.getExtensions(receiverClass)")
            appendLine("    override fun <R : Any> getExtension(receiverClass: KClass<R>, name: String) = delegate.getExtension(receiverClass, name)")
            appendLine("    override fun containsBean(clazz: KClass<*>): Boolean = delegate.containsBean(clazz)")
            appendLine("    override fun getBeanTypes(): Set<KClass<*>> = delegate.getBeanTypes()")
            appendLine("    override fun <T : Any> injectList(clazz: KClass<T>): List<T> = delegate.injectList(clazz)")
            appendLine("    override fun <T : Any> getBean(typeKey: site.addzero.ioc.registry.TypeKey): T? = delegate.getBean(typeKey)")
            appendLine("    override fun <T : Any> registerBean(typeKey: site.addzero.ioc.registry.TypeKey, instance: T) = delegate.registerBean(typeKey, instance)")
            appendLine("    override fun <T : Any> registerProvider(typeKey: site.addzero.ioc.registry.TypeKey, provider: () -> T) = delegate.registerProvider(typeKey, provider)")
            appendLine()

            // batch execution (order already applied)
            if (regularFunctions.isNotEmpty()) {
                appendLine(strategies["regular"]!!.generateCollectionCode(regularFunctions).prependIndent("    "))
                appendLine()
                appendLine(strategies["regular"]!!.generateExecuteMethod().prependIndent("    "))
                appendLine()
            }
            if (classInstances.isNotEmpty()) {
                appendLine(strategies["classInstance"]!!.generateCollectionCode(classInstances).prependIndent("    "))
                appendLine()
                appendLine(strategies["classInstance"]!!.generateExecuteMethod().prependIndent("    "))
                appendLine()
            }
            if (objectInstances.isNotEmpty()) {
                appendLine(strategies["objectInstance"]!!.generateCollectionCode(objectInstances).prependIndent("    "))
                appendLine()
                appendLine(strategies["objectInstance"]!!.generateExecuteMethod().prependIndent("    "))
                appendLine()
            }

            val methodNames = mutableListOf<String>()
            if (regularFunctions.isNotEmpty()) methodNames.add("iocRegularStart")
            if (classInstances.isNotEmpty()) methodNames.add("iocClassInstanceStart")
            if (objectInstances.isNotEmpty()) methodNames.add("iocObjectInstanceStart")

            appendLine("    fun iocAllStart() {")
            methodNames.forEach { appendLine("        $it()") }
            appendLine("    }")
            appendLine("}")
        }

        codeGenerator.createNewFile(
            Dependencies.ALL_FILES,
            "site.addzero.ioc.generated",
            "IocContainer",
            "kt"
        ).use { it.write(code.toByteArray()) }
    }

    /**
     * 按 receiver 类型分组生成扩展函数聚合文件
     * order 已在传入前排好序，生成的调用顺序即为 order 顺序
     */
    private fun generateExtensionModules(extensionFunctions: List<BeanInfo>) {
        data class ExtFuncInfo(val receiverFqn: String, val funcFqn: String, val funcSimpleName: String)

        val parsed = extensionFunctions.map { bean ->
            val receiverFqn = bean.name.substringBefore("::")
            val funcFqn = bean.name.substringAfter("::")
            val funcSimpleName = funcFqn.substringAfterLast(".")
            ExtFuncInfo(receiverFqn, funcFqn, funcSimpleName)
        }

        val grouped = parsed.groupBy { it.receiverFqn }

        grouped.forEach { (receiverFqn, funcs) ->
            val receiverSimpleName = receiverFqn.substringAfterLast(".")
            val fileName = "Ioc${receiverSimpleName}Module"

            val code = buildString {
                appendLine("package site.addzero.ioc.generated")
                appendLine()
                appendLine("import $receiverFqn")
                funcs.forEach { appendLine("import ${it.funcFqn}") }
                appendLine()
                appendLine("/**")
                appendLine(" * $receiverSimpleName extension module (ordered by @Bean(order=...))")
                appendLine(" */")
                appendLine("fun $receiverSimpleName.iocModule() {")
                funcs.forEach { appendLine("    ${it.funcSimpleName}()") }
                appendLine("}")
                appendLine()
                appendLine("fun register${receiverSimpleName}Extensions(container: site.addzero.ioc.registry.BeanRegistry) {")
                funcs.forEach { func ->
                    appendLine("    container.registerExtension($receiverSimpleName::class, \"${func.funcSimpleName}\") { ${func.funcSimpleName}() }")
                }
                appendLine("}")
            }

            codeGenerator.createNewFile(
                Dependencies.ALL_FILES,
                "site.addzero.ioc.generated",
                fileName,
                "kt"
            ).use { it.write(code.toByteArray()) }
        }
    }
}
