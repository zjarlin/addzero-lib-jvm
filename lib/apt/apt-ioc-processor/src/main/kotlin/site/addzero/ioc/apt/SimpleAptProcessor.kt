package site.addzero.ioc.apt

import site.addzero.ioc.annotation.Bean
import site.addzero.ioc.annotation.Component
import java.io.OutputStreamWriter
import javax.annotation.processing.*
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic
import javax.tools.StandardLocation

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
            val beanElements = roundEnv.getElementsAnnotatedWith(elements.getTypeElement(Bean::class.qualifiedName))
            processBeanAnnotations(beanElements)

            // 处理 @Component 注解
            val componentElements = roundEnv.getElementsAnnotatedWith(elements.getTypeElement(Component::class.qualifiedName!!))
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

    // 生成 IocContainer (Java代码)
    private fun generateIocContainer(functions: List<InitFunction>) {
        // 按类型分组
        val regularFunctions = functions.filter { it.initType == "METHOD" }
        val classInstances = functions.filter { it.initType == "CLASS_INSTANCE" }
        val companionObjects = functions.filter { it.initType == "COMPANION_OBJECT" }
        val allRegularFunctions = regularFunctions + companionObjects

        // 生成代码
        val code = buildString {
            appendLine("package $GENERATED_PACKAGE;")
            appendLine()
            appendLine("/**")
            appendLine(" * 自动生成的 IOC 容器")
            appendLine(" * 包含所有 @Bean 注解的函数和类")
            appendLine(" */")
            appendLine("public final class $CONTAINER_NAME {")
            appendLine()
            appendLine("    private $CONTAINER_NAME() {}")
            appendLine()

            // 生成函数列表
            if (allRegularFunctions.isNotEmpty()) {
                appendLine("    private static final Runnable[] collectRegular = new Runnable[] {")
                allRegularFunctions.forEach { func ->
                    if (func.functionName != null) {
                        appendLine("        () -> ${func.className}.${func.functionName}(),")
                    } else {
                        appendLine("        () -> new ${func.className}(),")
                    }
                }
                appendLine("    };")
                appendLine()
                appendLine("    public static void iocRegularStart() {")
                appendLine("        for (Runnable runnable : collectRegular) {")
                appendLine("            runnable.run();")
                appendLine("        }")
                appendLine("    }")
                appendLine()
            }

            if (classInstances.isNotEmpty()) {
                appendLine("    private static final Runnable[] collectClassInstance = new Runnable[] {")
                classInstances.forEach { func ->
                    appendLine("        () -> new ${func.className}(),")
                }
                appendLine("    };")
                appendLine()
                appendLine("    public static void iocClassInstanceStart() {")
                appendLine("        for (Runnable runnable : collectClassInstance) {")
                appendLine("            runnable.run();")
                appendLine("        }")
                appendLine("    }")
                appendLine()
            }

            // 生成启动方法
            if (functions.isNotEmpty()) {
                appendLine("    public static void iocAllStart() {")
                if (allRegularFunctions.isNotEmpty()) appendLine("        iocRegularStart();")
                if (classInstances.isNotEmpty()) appendLine("        iocClassInstanceStart();")
                appendLine("    }")
            }

            appendLine("}")
        }

        // 写入文件
        writeGeneratedFile(CONTAINER_NAME, "java", code)
        messager.printMessage(Diagnostic.Kind.NOTE, "生成 IocContainer 完成")
    }

    // 生成 AutoBeanRegistry (Java代码)
    private fun generateAutoBeanRegistry(components: List<ComponentInfo>) {
        // 生成代码
        val code = buildString {
            appendLine("package $GENERATED_PACKAGE;")
            appendLine()
            appendLine("/**")
            appendLine(" * 自动生成的 Bean 注册表")
            appendLine(" * 包含所有 @Component 注解的类")
            appendLine(" */")
            appendLine("public final class $REGISTRY_NAME {")
            appendLine()
            appendLine("    private $REGISTRY_NAME() {}")
            appendLine()
            appendLine("    private static final String[] COMPONENT_NAMES = new String[] {")
            components.forEach { component ->
                appendLine("        \"${component.componentName}\",")
            }
            appendLine("    };")
            appendLine()
            appendLine("    private static final String[] COMPONENT_CLASSES = new String[] {")
            components.forEach { component ->
                appendLine("        \"${component.className}\",")
            }
            appendLine("    };")
            appendLine()
            appendLine("    private static final int COMPONENT_COUNT = COMPONENT_NAMES.length;")
            appendLine()
            appendLine("    /**")
            appendLine("     * 获取所有已注册的组件名称")
            appendLine("     */")
            appendLine("    public static String[] getComponentNames() {")
            appendLine("        return COMPONENT_NAMES.clone();")
            appendLine("    }")
            appendLine()
            appendLine("    /**")
            appendLine("     * 获取组件数量")
            appendLine("     */")
            appendLine("    public static int getComponentCount() {")
            appendLine("        return COMPONENT_COUNT;")
            appendLine("    }")
            appendLine()
            appendLine("    /**")
            appendLine("     * 根据索引获取组件名称")
            appendLine("     */")
            appendLine("    public static String getComponentName(int index) {")
            appendLine("        if (index < 0 || index >= COMPONENT_COUNT) {")
            appendLine("            throw new IndexOutOfBoundsException(\"Index: \" + index + \", Size: \" + COMPONENT_COUNT);")
            appendLine("        }")
            appendLine("        return COMPONENT_NAMES[index];")
            appendLine("    }")
            appendLine()
            appendLine("    /**")
            appendLine("     * 根据索引获取类名")
            appendLine("     */")
            appendLine("    public static String getComponentClassName(int index) {")
            appendLine("        if (index < 0 || index >= COMPONENT_COUNT) {")
            appendLine("            throw new IndexOutOfBoundsException(\"Index: \" + index + \", Size: \" + COMPONENT_COUNT);")
            appendLine("        }")
            appendLine("        return COMPONENT_CLASSES[index];")
            appendLine("    }")
            appendLine()
            appendLine("    /**")
            appendLine("     * 根据组件名称获取类名")
            appendLine("     */")
            appendLine("    public static String getComponentClassName(String name) {")
            appendLine("        for (int i = 0; i < COMPONENT_COUNT; i++) {")
            appendLine("            if (COMPONENT_NAMES[i].equals(name)) {")
            appendLine("                return COMPONENT_CLASSES[i];")
            appendLine("            }")
            appendLine("        }")
            appendLine("        return null;")
            appendLine("    }")
            appendLine()
            appendLine("    /**")
            appendLine("     * 检查是否包含指定名称的组件")
            appendLine("     */")
            appendLine("    public static boolean containsComponent(String name) {")
            appendLine("        for (String componentName : COMPONENT_NAMES) {")
            appendLine("            if (componentName.equals(name)) {")
            appendLine("                return true;")
            appendLine("            }")
            appendLine("        }")
            appendLine("        return false;")
            appendLine("    }")
            appendLine("}")
        }

        // 写入文件
        writeGeneratedFile(REGISTRY_NAME, "java", code)
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