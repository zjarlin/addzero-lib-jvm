package site.addzero.ioc.apt

import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import javax.tools.StandardLocation
import java.io.OutputStreamWriter

// 定义常量
private const val BEAN_ANNOTATION_NAME = "site.addzero.ioc.annotation.Bean"
private const val COMPONENT_ANNOTATION_NAME = "site.addzero.ioc.annotation.Component"
private const val GENERATED_PACKAGE = "site.addzero.ioc.generated"
private const val CONTAINER_NAME = "IocContainer"
private const val REGISTRY_NAME = "AutoBeanRegistry"

// 存储函数信息的数据类
data class InitFunction(
    val className: String,
    val functionName: String?,
    val initType: String
)

// 存储 Component 信息的数据类
data class ComponentInfo(
    val className: String,
    val componentName: String,
    val interfaces: List<String>
)

class SimpleAptProcessor : AbstractProcessor() {

    private lateinit var elements: Elements
    private lateinit var types: Types
    private lateinit var filer: Filer
    private lateinit var messager: Messager

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        elements = processingEnv.elementUtils
        types = processingEnv.typeUtils
        filer = processingEnv.filer
        messager = processingEnv.messager
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        try {
            messager.printMessage(Diagnostic.Kind.NOTE, "SimpleAptProcessor 开始处理...")

            // 处理 @Bean 注解
            val beanElements = roundEnv.getElementsAnnotatedWith(elements.getTypeElement(BEAN_ANNOTATION_NAME))
            processBeanAnnotations(beanElements)

            // 处理 @Component 注解
            val componentElements = roundEnv.getElementsAnnotatedWith(elements.getTypeElement(COMPONENT_ANNOTATION_NAME))
            processComponentAnnotations(componentElements)

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            messager.printMessage(Diagnostic.Kind.ERROR, "Error in SimpleAptProcessor: ${e.message}")
            return false
        }
    }

    // 处理 @Bean 注解
    private fun processBeanAnnotations(beanElements: Set<Element>) {
        val functions = mutableListOf<InitFunction>()

        beanElements.forEach { element ->
            when (element.kind) {
                ElementKind.METHOD -> {
                    val executableElement = element as ExecutableElement
                    if (executableElement.parameters.isEmpty()) {
                        // 无参函数
                        extractFunctionInfo(executableElement, functions)
                    }
                }
                ElementKind.CLASS -> {
                    val classElement = element as TypeElement
                    extractClassInfo(classElement, functions)
                }
                ElementKind.INTERFACE -> {
                    val classElement = element as TypeElement
                    extractClassInfo(classElement, functions)
                }
                ElementKind.PACKAGE -> {}
                ElementKind.ANNOTATION_TYPE -> {}
                ElementKind.ENUM -> {}
                ElementKind.ENUM_CONSTANT -> {}
                ElementKind.FIELD -> {}
                ElementKind.PARAMETER -> {}
                ElementKind.LOCAL_VARIABLE -> {}
                ElementKind.EXCEPTION_PARAMETER -> {}
                ElementKind.INSTANCE_INIT -> {}
                ElementKind.STATIC_INIT -> {}
                ElementKind.TYPE_PARAMETER -> {}
                else -> {}
            }
        }

        if (functions.isNotEmpty()) {
            generateIocContainer(functions)
        }
    }

    // 处理 @Component 注解
    private fun processComponentAnnotations(componentElements: Set<Element>) {
        val components = mutableListOf<ComponentInfo>()

        componentElements.forEach { element ->
            when (element.kind) {
                ElementKind.CLASS, ElementKind.INTERFACE -> {
                    val classElement = element as TypeElement
                    extractComponentInfo(classElement, components)
                }
                else -> {}
            }
        }

        if (components.isNotEmpty()) {
            generateAutoBeanRegistry(components)
        }
    }

    // 提取函数信息
    private fun extractFunctionInfo(function: ExecutableElement, functions: MutableList<InitFunction>) {
        val className = function.enclosingElement.simpleName.toString()
        val functionName = function.simpleName.toString()

        val initType = when {
            function.modifiers.contains(javax.lang.model.element.Modifier.STATIC) -> "COMPANION_OBJECT"
            else -> "METHOD"
        }

        val initFunction = InitFunction(
            className = className,
            functionName = if (initType == "METHOD") functionName else null,
            initType = initType
        )

        functions.add(initFunction)
        messager.printMessage(Diagnostic.Kind.NOTE, "处理函数: $initFunction")
    }

    // 提取类信息
    private fun extractClassInfo(clazz: TypeElement, functions: MutableList<InitFunction>) {
        val className = clazz.simpleName.toString()

        val initFunction = InitFunction(
            className = className,
            functionName = null,
            initType = "CLASS_INSTANCE"
        )

        functions.add(initFunction)
        messager.printMessage(Diagnostic.Kind.NOTE, "处理类: $initFunction")
    }

    // 提取 Component 信息
    private fun extractComponentInfo(clazz: TypeElement, components: MutableList<ComponentInfo>) {
        val className = clazz.simpleName.toString()
        val packageName = elements.getPackageOf(clazz).qualifiedName.toString()

        val componentName = className.replaceFirstChar { it.lowercase() }

        // 获取所有实现的接口
        val interfaces = mutableListOf<String>()
        clazz.interfaces.forEach { typeMirror ->
            val typeElement = types.asElement(typeMirror) as? TypeElement
            if (typeElement?.kind == ElementKind.INTERFACE) {
                interfaces.add(typeElement.simpleName.toString())
            }
        }

        val componentInfo = ComponentInfo(
            className = className,
            componentName = componentName,
            interfaces = interfaces
        )

        components.add(componentInfo)
        messager.printMessage(Diagnostic.Kind.NOTE, "处理 Component: $componentInfo")
    }

    // 生成 IocContainer
    private fun generateIocContainer(functions: List<InitFunction>) {
        // 按类型分组
        val regularFunctions = functions.filter { it.initType == "METHOD" }
        val classInstances = functions.filter { it.initType == "CLASS_INSTANCE" }
        val companionObjects = functions.filter { it.initType == "COMPANION_OBJECT" }
        val allRegularFunctions = regularFunctions + companionObjects

        // 生成代码
        val code = buildString {
            appendLine("package $GENERATED_PACKAGE")
            appendLine()
            appendLine("/**")
            appendLine(" * 自动生成的 IOC 容器")
            appendLine(" * 包含所有 @Bean 注解的函数和类")
            appendLine(" */")
            appendLine("public object $CONTAINER_NAME {")

            // 生成函数列表
            if (allRegularFunctions.isNotEmpty()) {
                appendLine("    private val collectRegular = listOf(")
                allRegularFunctions.forEach { func ->
                    if (func.functionName != null) {
                        appendLine("        { ${func.className}.${func.functionName}() },")
                    } else {
                        appendLine("        { ${func.className}() },")
                    }
                }
                appendLine("    )")
                appendLine()
                appendLine("    fun iocRegularStart() {")
                appendLine("        collectRegular.forEach { it() }")
                appendLine("    }")
                appendLine()
            }

            if (classInstances.isNotEmpty()) {
                appendLine("    private val collectClassInstance = listOf(")
                classInstances.forEach { func ->
                    appendLine("        { ${func.className}() },")
                }
                appendLine("    )")
                appendLine()
                appendLine("    fun iocClassInstanceStart() {")
                appendLine("        collectClassInstance.forEach { it() }")
                appendLine("    }")
                appendLine()
            }

            // 生成启动方法
            if (functions.isNotEmpty()) {
                appendLine("    fun iocAllStart() {")
                if (allRegularFunctions.isNotEmpty()) appendLine("        iocRegularStart()")
                if (classInstances.isNotEmpty()) appendLine("        iocClassInstanceStart()")
                appendLine("    }")
            }

            appendLine("}")
        }

        // 写入文件
        writeGeneratedFile(CONTAINER_NAME, "kt", code)
        messager.printMessage(Diagnostic.Kind.NOTE, "生成 IocContainer 完成")
    }

    // 生成 AutoBeanRegistry
    private fun generateAutoBeanRegistry(components: List<ComponentInfo>) {
        // 生成代码
        val code = buildString {
            appendLine("package $GENERATED_PACKAGE")
            appendLine()
            appendLine("/**")
            appendLine(" * 自动生成的 Bean 注册表")
            appendLine(" * 包含所有 @Component 注解的类")
            appendLine(" */")
            appendLine("public object $REGISTRY_NAME {")
            appendLine()
            appendLine("    private val components = mapOf(")
            components.forEach { component ->
                appendLine("        \"${component.componentName}\" to \"${component.className}\"")
            }
            appendLine("    )")
            appendLine()
            appendLine("    /**")
            appendLine("     * 获取所有已注册的组件名称")
            appendLine("     */")
            appendLine("    public fun getComponentNames(): Set<String> {")
            appendLine("        return components.keys.toSet()")
            appendLine("    }")
            appendLine()
            appendLine("    /**")
            appendLine("     * 根据组件名称获取类名")
            appendLine("     */")
            appendLine("    public fun getComponentClassName(name: String): String? {")
            appendLine("        return components[name]")
            appendLine("    }")
            appendLine("}")
        }

        // 写入文件
        writeGeneratedFile(REGISTRY_NAME, "kt", code)
        messager.printMessage(Diagnostic.Kind.NOTE, "生成 AutoBeanRegistry 完成")
    }

    // 写入生成的文件
    private fun writeGeneratedFile(className: String, extension: String, content: String) {
        try {
            val file = filer.createResource(
                StandardLocation.SOURCE_OUTPUT,
                GENERATED_PACKAGE,
                "$className.$extension"
            )
            OutputStreamWriter(file.openOutputStream(), "UTF-8").use { writer ->
                writer.write(content)
                writer.flush()
            }
        } catch (e: Exception) {
            messager.printMessage(Diagnostic.Kind.ERROR, "写入生成的文件失败: ${e.message}")
        }
    }
}