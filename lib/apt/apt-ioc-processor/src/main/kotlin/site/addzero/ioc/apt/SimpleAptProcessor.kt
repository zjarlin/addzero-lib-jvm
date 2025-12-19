package site.addzero.ioc.apt

import site.addzero.ioc.annotation.Bean
import site.addzero.ioc.annotation.Component
import java.io.OutputStreamWriter
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
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
    val initType: String,
    val packageName: String = "test"  // 默认包名
)

// 存储 Component 信息的数据类
data class ComponentInfo(
    val className: String,
    val componentName: String,
    val interfaces: List<String>,
    val packageName: String = "test"  // 默认包名
)

@SupportedAnnotationTypes("site.addzero.ioc.annotation.Bean", "site.addzero.ioc.annotation.Component")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
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
        val packageName = elements.getPackageOf(function.enclosingElement).qualifiedName.toString()

        val initType = when {
            function.modifiers.contains(javax.lang.model.element.Modifier.STATIC) -> "COMPANION_OBJECT"
            else -> "METHOD"
        }

        val initFunction = InitFunction(
            className = className,
            functionName = if (initType == "METHOD") functionName else null,
            initType = initType,
            packageName = packageName
        )

        functions.add(initFunction)
        messager.printMessage(Diagnostic.Kind.NOTE, "处理函数: $initFunction")
    }

    // 提取类信息
    private fun extractClassInfo(clazz: TypeElement, functions: MutableList<InitFunction>) {
        val className = clazz.simpleName.toString()
        val packageName = elements.getPackageOf(clazz).qualifiedName.toString()

        val initFunction = InitFunction(
            className = className,
            functionName = null,
            initType = "CLASS_INSTANCE",
            packageName = packageName
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
            interfaces = interfaces,
            packageName = packageName
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

        // 收集所有需要导入的类和它们的包
        val classImports = functions.groupBy({ it.packageName }, { it.className })

        // 生成代码
        val code = buildString {
            appendLine("package $GENERATED_PACKAGE;")
            appendLine()

            // 生成导入语句
            classImports.forEach { (pkg, classNames) ->
                classNames.distinct().forEach { className ->
                    appendLine("import $pkg.$className;")
                }
            }

            appendLine()
            appendLine("/**")
            appendLine(" * 自动生成的 IOC 容器")
            appendLine(" * 包含所有 @Bean 注解的函数和类")
            appendLine(" */")
            appendLine("public final class $CONTAINER_NAME {")
            appendLine()
            appendLine("    private $CONTAINER_NAME() {}")

            // 生成函数列表
            if (allRegularFunctions.isNotEmpty()) {
                appendLine()
                appendLine("    private static final Runnable[] collectRegular = new Runnable[] {")
                allRegularFunctions.forEach { func ->
                    if (func.functionName != null) {
                        if (func.initType == "COMPANION_OBJECT") {
                            // 静态方法
                            appendLine("        () -> ${func.className}.${func.functionName}(),")
                        } else {
                            // 实例方法，需要先创建实例
                            appendLine("        () -> new ${func.className}().${func.functionName}(),")
                        }
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
            }

            if (classInstances.isNotEmpty()) {
                appendLine()
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
            }

            // 生成启动方法
            if (functions.isNotEmpty()) {
                appendLine()
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

    // 生成 AutoBeanRegistry (Java代码) - 新版本
    private fun generateAutoBeanRegistry(components: List<ComponentInfo>) {
        // 生成代码
        val code = buildString {
            appendLine("package $GENERATED_PACKAGE;")
            appendLine()

            // 生成导入语句
            components.forEach { component ->
                appendLine("import ${component.packageName}.${component.className};")
            }
            appendLine()
            appendLine("import java.util.HashMap;")
            appendLine("import java.util.HashSet;")
            appendLine("import java.util.Map;")
            appendLine("import java.util.Set;")
            appendLine("import java.util.List;")
            appendLine("import java.util.ArrayList;")
            appendLine("import java.util.function.Supplier;")
            appendLine()
            appendLine("/**")
            appendLine(" * 自动生成的 Bean 注册表")
            appendLine(" * 包含所有 @Component 注解的类")
            appendLine(" */")
            appendLine("public final class $REGISTRY_NAME {")
            appendLine()
            appendLine("    private $REGISTRY_NAME() {}")
            appendLine()

            // Bean存储和Provider存储
            appendLine("    private static final Map<Class<?>, Object> beanInstances = new HashMap<>();")
            appendLine("    private static final Map<Class<?>, Supplier<?>> beanProviders = new HashMap<>();")
            appendLine("    private static final Map<Class<?>, Set<Class<?>>> interfaceImplementations = new HashMap<>();")
            appendLine()

            // init块 - 注册所有Component
            appendLine("    static {")
            components.forEach { component ->
                appendLine("        // 注册 ${component.className}")
                appendLine("        registerProvider(${component.className}.class, ${component.className}::new);")

                // 注册接口实现关系
                if (component.interfaces.isNotEmpty()) {
                    component.interfaces.forEach { interfaceName ->
                        // 使用Component的包名作为接口包名
                        appendLine("        registerImplementation(${interfaceName}.class, ${component.className}.class);")
                    }
                }
            }
            appendLine("    }")
            appendLine()

            // 实现getBean方法
            appendLine("    @SuppressWarnings(\"unchecked\")")
            appendLine("    public static <T> T getBean(Class<T> clazz) {")
            appendLine("        T instance = (T) beanInstances.get(clazz);")
            appendLine("        if (instance == null) {")
            appendLine("            Supplier<?> supplier = beanProviders.get(clazz);")
            appendLine("            if (supplier != null) {")
            appendLine("                instance = (T) supplier.get();")
            appendLine("                beanInstances.put(clazz, instance);")
            appendLine("            }")
            appendLine("        }")
            appendLine("        return instance;")
            appendLine("    }")
            appendLine()

            // 实现getRequiredBean方法
            appendLine("    @SuppressWarnings(\"unchecked\")")
            appendLine("    public static <T> T getRequiredBean(Class<T> clazz) {")
            appendLine("        T instance = getBean(clazz);")
            appendLine("        if (instance == null) {")
            appendLine("            throw new IllegalArgumentException(\"Required bean not found: \" + clazz.getName());")
            appendLine("        }")
            appendLine("        return instance;")
            appendLine("    }")
            appendLine()

            // 实现registerBean方法
            appendLine("    public static <T> void registerBean(Class<T> clazz, T instance) {")
            appendLine("        beanInstances.put(clazz, instance);")
            appendLine("    }")
            appendLine()

            // 实现registerProvider方法
            appendLine("    public static <T> void registerProvider(Class<T> clazz, Supplier<T> provider) {")
            appendLine("        beanProviders.put(clazz, provider);")
            appendLine("    }")
            appendLine()

            // 实现registerImplementation方法（用于接口实现映射）
            appendLine("    @SuppressWarnings(\"unchecked\")")
            appendLine("    public static <T> void registerImplementation(Class<T> interfaceClass, Class<? extends T> implementationClass) {")
            appendLine("        interfaceImplementations.computeIfAbsent(interfaceClass, k -> new HashSet<>()).add(implementationClass);")
            appendLine("        // 同时注册实现类的provider")
            appendLine("        registerProvider((Class<T>) implementationClass, () -> {")
            appendLine("            try {")
            appendLine("                return (T) implementationClass.getDeclaredConstructor().newInstance();")
            appendLine("            } catch (Exception e) {")
            appendLine("                throw new RuntimeException(\"Failed to create instance\", e);")
            appendLine("            }")
            appendLine("        });")
            appendLine("    }")
            appendLine()

            // 实现containsBean方法
            appendLine("    public static boolean containsBean(Class<?> clazz) {")
            appendLine("        return beanProviders.containsKey(clazz) || beanInstances.containsKey(clazz);")
            appendLine("    }")
            appendLine()

            // 实现getBeanTypes方法
            appendLine("    public static Set<Class<?>> getBeanTypes() {")
            appendLine("        Set<Class<?>> types = new HashSet<>(beanProviders.keySet());")
            appendLine("        types.addAll(beanInstances.keySet());")
            appendLine("        return types;")
            appendLine("    }")
            appendLine()

            // 实现injectList方法 - 返回所有指定类型的bean
            appendLine("    @SuppressWarnings(\"unchecked\")")
            appendLine("    public static <T> List<T> injectList(Class<T> clazz) {")
            appendLine("        List<T> instances = new ArrayList<>();")
            appendLine("        ")
            appendLine("        // 添加直接注册的bean")
            appendLine("        T instance = getBean(clazz);")
            appendLine("        if (instance != null) {")
            appendLine("            instances.add(instance);")
            appendLine("        }")
            appendLine("        ")
            appendLine("        // 添加接口的所有实现")
            appendLine("        Set<Class<?>> implementations = interfaceImplementations.get(clazz);")
            appendLine("        if (implementations != null) {")
            appendLine("            for (Class<?> implClass : implementations) {")
            appendLine("                T implInstance = getBean((Class<T>) implClass);")
            appendLine("                if (implInstance != null) {")
            appendLine("                    instances.add(implInstance);")
            appendLine("                }")
            appendLine("            }")
            appendLine("        }")
            appendLine("        ")
            appendLine("        return instances;")
            appendLine("    }")
            appendLine()

            // 便利方法：获取所有组件名称
            appendLine("    /**")
            appendLine("     * 获取所有已注册的组件名称")
            appendLine("     */")
            appendLine("    public static Set<String> getComponentNames() {")
            appendLine("        Set<String> names = new HashSet<>();")
            components.forEach { component ->
                appendLine("        names.add(\"${component.componentName}\");")
            }
            appendLine("        return names;")
            appendLine("    }")
            appendLine()

            // 便利方法：根据名称获取类型
            appendLine("    /**")
            appendLine("     * 根据组件名称获取对应的类型")
            appendLine("     */")
            appendLine("    public static Class<?> getComponentType(String name) {")
            appendLine("        try {")
            appendLine("            switch (name) {")
            components.forEach { component ->
                appendLine("                case \"${component.componentName}\": return Class.forName(\"${component.packageName}.${component.className}\");")
            }
            appendLine("                default: return null;")
            appendLine("            }")
            appendLine("        } catch (ClassNotFoundException e) {")
            appendLine("            return null;")
            appendLine("        }")
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