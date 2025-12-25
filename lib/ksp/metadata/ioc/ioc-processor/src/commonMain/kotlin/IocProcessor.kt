package com.example.autoinit.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import site.addzero.ioc.annotation.Bean
import site.addzero.ioc.annotation.Component
import java.io.OutputStream

// 定义常量
private const val BEAN_ANNOTATION_NAME = "site.addzero.ioc.annotation.Bean"
private const val COMPONENT_ANNOTATION_NAME = "site.addzero.ioc.annotation.Component"
private const val GENERATED_PACKAGE = "site.addzero.ioc.generated"
private const val CONTAINER_NAME = "IocContainer"
private const val REGISTRY_NAME = "AutoBeanRegistry"

// 存储函数信息的数据类
private data class InitFunction(
    val packageName: String?,     // 包名
    val className: String?,       // 类全名（顶层函数为null）
    val fileName: String,         // 文件名
    val functionName: String?,    // 函数名（object/class为null）
    val isSuspend: Boolean,       // 是否挂起函数
    val isComposable: Boolean,    // 是否Composable函数
    val isStatic: Boolean,        // 是否静态函数（伴生对象中）
    val initType: InitType,       // 初始化类型
    val hasParentheses: Boolean = false  // 类声明是否已包含括号
)

// 存储 Component 信息的数据类
private data class ComponentInfo(
    val className: String,        // 类全名
    val packageName: String,      // 包名
    val componentName: String,    // 组件名称
    val isSingleton: Boolean = true,  // 是否单例（默认为单例）
    val interfaces: List<String> = emptyList()  // 实现的接口列表
)

// 初始化类型枚举
private enum class InitType {
    TOP_LEVEL_FUNCTION,  // 顶层函数
    CLASS_INSTANCE,      // 类实例（无参构造函数）
    OBJECT_INSTANCE,     // 对象实例
    COMPANION_OBJECT     // 伴生对象
}

// 策略接口
private interface CodeGenerationStrategy {
    fun generateFunctionCall(function: InitFunction): String
    fun generateExecuteMethod(functions: List<InitFunction>): String
}

// 定义函数类型的枚举，实现策略接口
private enum class FunctionType(
    private val functionCallTemplate: String,
    private val isSuspend: Boolean,
    private val hasComposeAnnotation: Boolean
) : CodeGenerationStrategy {
    REGULAR("{ %s() }", false, false),
    CLASS_INSTANCE("{ %s() }", false, false),
    OBJECT_INSTANCE("{ %s }", false, false),
    SUSPEND("suspend { %s() }", true, false),
    COMPOSABLE("@androidx.compose.runtime.Composable { %s() }", false, true);

    override fun generateFunctionCall(function: InitFunction): String {
        return when (function.initType) {
            InitType.TOP_LEVEL_FUNCTION -> {
                // 使用函数的包名直接调用函数，不使用文件类名
                val packageName = function.packageName
                val functionName = if (function.isComposable && function.functionName != null) {
                    function.functionName.capitalizeFirstChar()
                } else {
                    function.functionName
                }

                val fullFunctionName = if (packageName != null && functionName != null) {
                    "$packageName.$functionName"
                } else {
                    functionName ?: ""
                }

                functionCallTemplate.format(fullFunctionName)
            }

            InitType.CLASS_INSTANCE -> {
                // 为类实例调用，类实例化时需要括号，但只添加一次
                val className = function.className!!
                functionCallTemplate.format(className)
            }

            InitType.OBJECT_INSTANCE -> {
                // 为对象实例调用，不需要括号
                functionCallTemplate.format(function.className!!)
            }

            InitType.COMPANION_OBJECT -> {
                // 为伴生对象调用
                val className = function.className?.substringBefore(".Companion")
                functionCallTemplate.format(className!!)
            }
        }
    }

    override fun generateExecuteMethod(functions: List<InitFunction>): String {
        val functionName = when (this) {
            REGULAR -> "collectRegular"
            CLASS_INSTANCE -> "collectClassInstance"
            OBJECT_INSTANCE -> "collectObjectInstance"
            SUSPEND -> "collectSuspend"
            COMPOSABLE -> "collectComposable"
        }

        val suspendModifier = if (isSuspend) "suspend " else ""
        val composeAnnotation = if (hasComposeAnnotation) "@androidx.compose.runtime.Composable\n    " else ""
        // 更正方法名，保持统一的命名规则
        val methodName = when (this) {
            REGULAR -> "iocRegularStart"
            CLASS_INSTANCE -> "iocClassInstanceStart"
            OBJECT_INSTANCE -> "iocObjectInstanceStart"
            SUSPEND -> "iocSuspendStart"
            COMPOSABLE -> "IocComposeableStart"  // 首字母大写以符合Composable函数命名规范
        }

        return """
    ${composeAnnotation}${suspendModifier}fun ${methodName}() {
        ${functionName}.forEach { it() }
    }
        """.trimIndent()
    }

    // 扩展函数，将字符串首字母大写
    private fun String.capitalizeFirstChar(): String {
        return if (this.isNotEmpty()) {
            this[0].uppercaseChar() + this.substring(1)
        } else {
            this
        }
    }
}

class IocProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    private val functions = mutableListOf<InitFunction>()
    private val components = mutableListOf<ComponentInfo>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.warn("IocProcessor 开始处理...")
        System.out.println("IocProcessor 开始处理...")

        // 1. 扫描所有带@Bean注解的符号
        logger.warn("查找注解: $BEAN_ANNOTATION_NAME")
        System.out.println("查找注解: $BEAN_ANNOTATION_NAME")
        val autoInitSymbols = resolver.getSymbolsWithAnnotation(
            Bean::class
                .qualifiedName!!
        )
        logger.warn("找到带@Bean注解的符号数量: ${autoInitSymbols.toList().size}")
        System.out.println("找到带@Bean注解的符号数量: ${autoInitSymbols.toList().size}")

        // 2. 扫描所有带@Component注解的类
        logger.warn("查找注解: $COMPONENT_ANNOTATION_NAME")
        System.out.println("查找注解: $COMPONENT_ANNOTATION_NAME")
        val componentSymbols = resolver.getSymbolsWithAnnotation(
            Component::class
                .qualifiedName!!
        )
        logger.warn("找到带@Component注解的符号数量: ${componentSymbols.toList().size}")
        System.out.println("找到带@Component注解的符号数量: ${componentSymbols.toList().size}")

        // 3. 分别处理函数、类和对象
        val functionDeclarations = autoInitSymbols.filterIsInstance<KSFunctionDeclaration>()
        val classDeclarations = autoInitSymbols.filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.CLASS }
        val objectDeclarations = autoInitSymbols.filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.OBJECT }

        logger.warn("其中函数声明数量: ${functionDeclarations.toList().size}")
        logger.warn("其中类声明数量: ${classDeclarations.toList().size}")
        logger.warn("其中对象声明数量: ${objectDeclarations.toList().size}")
        System.out.println("其中函数声明数量: ${functionDeclarations.toList().size}")
        System.out.println("其中类声明数量: ${classDeclarations.toList().size}")
        System.out.println("其中对象声明数量: ${objectDeclarations.toList().size}")

        // 4. 处理 @Component 注解的类
        val componentClassDeclarations = componentSymbols.filterIsInstance<KSClassDeclaration>()
            .filter { it.classKind == ClassKind.CLASS }

        logger.warn("处理 @Component 类数量: ${componentClassDeclarations.toList().size}")
        System.out.println("处理 @Component 类数量: ${componentClassDeclarations.toList().size}")

        // 3. 提取函数信息
        functionDeclarations.forEach { function ->
            try {
                logger.warn("处理函数: ${function.simpleName.asString()}")
                System.out.println("处理函数: ${function.simpleName.asString()}")
                extractFunctionInfo(function)
                logger.warn("成功找到函数: ${function.simpleName.asString()}")
                System.out.println("成功找到函数: ${function.simpleName.asString()}")
            } catch (e: Exception) {
                logger.error("处理函数 ${function.simpleName} 失败: ${e.message}")
                e.printStackTrace()
            }
        }

        // 4. 提取类信息
        classDeclarations.forEach { clazz ->
            try {
                logger.warn("处理类: ${clazz.simpleName.asString()}")
                System.out.println("处理类: ${clazz.simpleName.asString()}")
                extractClassInfo(clazz)
                logger.warn("成功找到类: ${clazz.simpleName.asString()}")
                System.out.println("成功找到类: ${clazz.simpleName.asString()}")
            } catch (e: Exception) {
                logger.error("处理类 ${clazz.simpleName} 失败: ${e.message}")
                e.printStackTrace()
            }
        }

        // 5. 提取对象信息
        objectDeclarations.forEach { obj ->
            try {
                logger.warn("处理对象: ${obj.simpleName.asString()}")
                System.out.println("处理对象: ${obj.simpleName.asString()}")
                extractObjectInfo(obj)
                logger.warn("成功找到对象: ${obj.simpleName.asString()}")
                System.out.println("成功找到对象: ${obj.simpleName.asString()}")
            } catch (e: Exception) {
                logger.error("处理对象 ${obj.simpleName} 失败: ${e.message}")
                e.printStackTrace()
            }
        }

        // 6. 处理 Component 类
        componentClassDeclarations.forEach { clazz ->
            try {
                logger.warn("处理 Component 类: ${clazz.simpleName.asString()}")
                System.out.println("处理 Component 类: ${clazz.simpleName.asString()}")
                extractComponentInfo(clazz)
                logger.warn("成功处理 Component 类: ${clazz.simpleName.asString()}")
                System.out.println("成功处理 Component 类: ${clazz.simpleName.asString()}")
            } catch (e: Exception) {
                logger.error("处理 Component 类 ${clazz.simpleName} 失败: ${e.message}")
                e.printStackTrace()
            }
        }

        // 注意：不在process阶段生成代码，避免多轮处理时的覆盖问题
        logger.warn("IocProcessor 处理完成")
        System.out.println("IocProcessor 处理完成")
        return emptyList()
    }

    override fun finish() {
        // 在finish阶段生成代码，确保只生成一次
        logger.warn("总共收集到 ${functions.size} 个初始化项")
        logger.warn("总共收集到 ${components.size} 个组件")
        System.out.println("总共收集到 ${functions.size} 个初始化项")
        System.out.println("总共收集到 ${components.size} 个组件")
        generateAutoInitCode()
        generateBeanRegistry()
    }

    // 提取函数信息
    private fun extractFunctionInfo(function: KSFunctionDeclaration) {
        logger.warn("开始提取函数信息: ${function.simpleName.asString()}")
        System.out.println("开始提取函数信息: ${function.simpleName.asString()}")

        // 只处理无参函数或所有参数都有默认值的函数
        if (function.parameters.isNotEmpty() && function.parameters.any { !it.hasDefault }) {
            logger.warn("跳过带必需参数的函数 ${function.simpleName}（@Bean仅支持无参函数或所有参数都有默认值的函数）")
            System.out.println("跳过带必需参数的函数 ${function.simpleName}（@Bean仅支持无参函数或所有参数都有默认值的函数）")
            return
        }

        // 获取函数所在类（可为空，表示顶层函数）
        val parentClass = function.parentDeclaration as? KSClassDeclaration

        // 获取函数的包名（而不是文件所在的包名）
        val packageName = function.packageName.asString().takeIf { it.isNotEmpty() }

        // 获取文件名
        val fileName = function.containingFile?.fileName?.removeSuffix(".kt") ?: "UnknownFile"

        if (parentClass == null) {
            logger.warn("处理顶层函数: ${function.simpleName.asString()}")
            System.out.println("处理顶层函数: ${function.simpleName.asString()}")
        } else {
            logger.warn("处理类中函数: ${function.simpleName.asString()} in ${parentClass.qualifiedName?.asString()}")
            System.out.println("处理类中函数: ${function.simpleName.asString()} in ${parentClass.qualifiedName?.asString()}")
        }

        // 判断是否为Composable函数（通过注解全类名判断，避免直接依赖Compose）
        val isComposable = function.annotations.any { annotation ->
            val annotationType = annotation.annotationType.resolve().declaration.qualifiedName?.asString()
            logger.warn("函数 ${function.simpleName} 的注解: $annotationType")
            System.out.println("函数 ${function.simpleName} 的注解: $annotationType")
            annotationType == "androidx.compose.runtime.Composable"
        }

        // 判断是否为挂起函数
        val isSuspend = function.modifiers.any { it == Modifier.SUSPEND }
        logger.warn("函数 ${function.simpleName} 是否为挂起函数: $isSuspend")
        System.out.println("函数 ${function.simpleName} 是否为挂起函数: $isSuspend")

        // 判断是否为伴生对象中的函数
        val isStatic = parentClass != null && parentClass.isCompanionObject
        logger.warn("函数 ${function.simpleName} 是否为静态函数: $isStatic")
        System.out.println("函数 ${function.simpleName} 是否为静态函数: $isStatic")

        val initType = if (parentClass == null) {
            InitType.TOP_LEVEL_FUNCTION
        } else if (isStatic) {
            InitType.COMPANION_OBJECT
        } else {
            InitType.CLASS_INSTANCE
        }

        val initFunction = InitFunction(
            packageName = packageName,
            className = parentClass?.qualifiedName?.asString(),
            fileName = fileName,
            functionName = function.simpleName.asString(),
            isSuspend = isSuspend,
            isComposable = isComposable,
            isStatic = isStatic,
            initType = initType
        )

        functions.add(initFunction)
        logger.warn("添加函数信息: $initFunction")
        System.out.println("添加函数信息: $initFunction")
    }

    // 提取类信息（仅处理无参构造函数的类）
    private fun extractClassInfo(clazz: KSClassDeclaration) {
        logger.warn("开始提取类信息: ${clazz.simpleName.asString()}")
        System.out.println("开始提取类信息: ${clazz.simpleName.asString()}")

        // 检查是否有无参构造函数
        val hasNoArgConstructor = clazz.getAllFunctions()
            .filter { it.simpleName.asString() == "<init>" }
            .any { constructor ->
                constructor.parameters.isEmpty() || constructor.parameters.all { it.hasDefault }
            }

        if (!hasNoArgConstructor) {
            logger.warn("跳过类 ${clazz.simpleName}（@Bean仅支持有无参构造函数的类）")
            System.out.println("跳过类 ${clazz.simpleName}（@Bean仅支持有无参构造函数的类）")
            return
        }

        // 获取类的包名
        val packageName = clazz.packageName.asString().takeIf { it.isNotEmpty() }

        // 获取文件名
        val fileName = clazz.containingFile?.fileName?.removeSuffix(".kt") ?: "UnknownFile"

        // 判断是否为Composable类（检查是否有Composable注解）
        val isComposable = clazz.annotations.any { annotation ->
            val annotationType = annotation.annotationType.resolve().declaration.qualifiedName?.asString()
            annotationType == "androidx.compose.runtime.Composable"
        }

        // 对于类实例化，无论类声明中是否包含括号，我们都需要在实例化时添加括号
        // 所以这里hasParentheses始终为false
        val hasParentheses = false

        val initFunction = InitFunction(
            packageName = packageName,
            className = clazz.qualifiedName?.asString(),
            fileName = fileName,
            functionName = null,
            isSuspend = false,
            isComposable = isComposable,
            isStatic = false,
            initType = InitType.CLASS_INSTANCE,
            hasParentheses = hasParentheses
        )

        functions.add(initFunction)
        logger.warn("添加类信息: $initFunction")
        System.out.println("添加类信息: $initFunction")
    }

    // 提取对象信息
    private fun extractObjectInfo(obj: KSClassDeclaration) {
        logger.warn("开始提取对象信息: ${obj.simpleName.asString()}")
        System.out.println("开始提取对象信息: ${obj.simpleName.asString()}")

        // 获取对象的包名
        val packageName = obj.packageName.asString().takeIf { it.isNotEmpty() }

        // 获取文件名
        val fileName = obj.containingFile?.fileName?.removeSuffix(".kt") ?: "UnknownFile"

        // 判断是否为Composable对象（检查是否有Composable注解）
        val isComposable = obj.annotations.any { annotation ->
            val annotationType = annotation.annotationType.resolve().declaration.qualifiedName?.asString()
            annotationType == "androidx.compose.runtime.Composable"
        }

        val initFunction = InitFunction(
            packageName = packageName,
            className = obj.qualifiedName?.asString(),
            fileName = fileName,
            functionName = null,
            isSuspend = false,
            isComposable = isComposable,
            isStatic = false,
            initType = InitType.OBJECT_INSTANCE
        )

        functions.add(initFunction)
        logger.warn("添加对象信息: $initFunction")
        System.out.println("添加对象信息: $initFunction")
    }

    // 生成导入语句 - 只导入类和对象，不导入顶层函数所在文件的类（File-Class）
    private fun generateImports(
        regularFunctions: List<InitFunction>,
        suspendFunctions: List<InitFunction>,
        composableFunctions: List<InitFunction>
    ): Set<String> {
        val imports = mutableSetOf<String>()

        // 添加 Compose 必需的导入

        (regularFunctions + suspendFunctions + composableFunctions).forEach { func ->
            when (func.initType) {
                InitType.TOP_LEVEL_FUNCTION -> {
                    // 顶层函数：不进行任何导入，调用时使用包名+函数名全限定调用
                }

                InitType.CLASS_INSTANCE, InitType.OBJECT_INSTANCE, InitType.COMPANION_OBJECT -> {
                    // 仅对类、对象、伴生对象导入其全类名
                    func.className?.let { className ->
                        imports.add(className)
                    }
                }
            }
        }
        return imports
    }

    // 动态生成代码（只生成存在的函数类型）
    private fun generateAutoInitCode() {
        if (functions.isEmpty()) {
            logger.warn("没有找到 @Bean 注解的元素，跳过 IocContainer 生成")
            System.out.println("没有找到 @Bean 注解的元素，跳过 IocContainer 生成")
            return
        }

        logger.warn("开始生成代码，初始化项总数: ${functions.size}")
        System.out.println("开始生成代码，初始化项总数: ${functions.size}")

        // 按类型分组，包括新增的类实例和对象实例类型
        val regularFunctions =
            functions.filter { !it.isSuspend && !it.isComposable && it.initType == InitType.TOP_LEVEL_FUNCTION }
        val classInstances =
            functions.filter { !it.isSuspend && !it.isComposable && it.initType == InitType.CLASS_INSTANCE }
        val objectInstances =
            functions.filter { !it.isSuspend && !it.isComposable && it.initType == InitType.OBJECT_INSTANCE }
        val companionObjects =
            functions.filter { !it.isSuspend && !it.isComposable && it.initType == InitType.COMPANION_OBJECT }
        // 合并伴生对象到常规函数列表中，保持原有逻辑
        val allRegularFunctions = regularFunctions + companionObjects
        val suspendFunctions = functions.filter { it.isSuspend && !it.isComposable }
        val composableFunctions = functions.filter { it.isComposable }

        logger.warn("普通初始化项: ${allRegularFunctions.size}个")
        logger.warn("类实例初始化项: ${classInstances.size}个")
        logger.warn("对象实例初始化项: ${objectInstances.size}个")
        logger.warn("挂起初始化项: ${suspendFunctions.size}个")
        logger.warn("Composable初始化项: ${composableFunctions.size}个")
        System.out.println("普通初始化项: ${allRegularFunctions.size}个")
        System.out.println("类实例初始化项: ${classInstances.size}个")
        System.out.println("对象实例初始化项: ${objectInstances.size}个")
        System.out.println("挂起初始化项: ${suspendFunctions.size}个")
        System.out.println("Composable初始化项: ${composableFunctions.size}个")

        // 按类型分组
        val functionGroups = mapOf(
            FunctionType.REGULAR to allRegularFunctions,
            FunctionType.CLASS_INSTANCE to classInstances,
            FunctionType.OBJECT_INSTANCE to objectInstances,
            FunctionType.SUSPEND to suspendFunctions,
            FunctionType.COMPOSABLE to composableFunctions
        )

        // 生成导入语句
        val imports = generateImports(allRegularFunctions, suspendFunctions, composableFunctions)

        // 生成函数列表代码
        val functionListCode = buildString {
            val methodNames = mutableListOf<String>()

            FunctionType.values().forEach { type ->
                val functions = functionGroups[type] ?: emptyList()
                if (functions.isNotEmpty()) {
                    val functionName = when (type) {
                        FunctionType.REGULAR -> "collectRegular"
                        FunctionType.CLASS_INSTANCE -> "collectClassInstance"
                        FunctionType.OBJECT_INSTANCE -> "collectObjectInstance"
                        FunctionType.SUSPEND -> "collectSuspend"
                        FunctionType.COMPOSABLE -> "collectComposable"
                    }
                    methodNames.add(
                        when (type) {
                            FunctionType.REGULAR -> "iocRegularStart"
                            FunctionType.CLASS_INSTANCE -> "iocClassInstanceStart"
                            FunctionType.OBJECT_INSTANCE -> "iocObjectInstanceStart"
                            FunctionType.SUSPEND -> "iocSuspendStart"
                            FunctionType.COMPOSABLE -> "IocComposeableStart"
                        }
                    )

                    append(
                        """
                        |    val ${functionName} = listOf(
                        |        ${functions.joinToString(",\n        ") { type.generateFunctionCall(it) }}
                        |    )
                        |
                        |${type.generateExecuteMethod(functions)}
                    """.trimMargin()
                    )
                    append("\n")
                }
            }

            // 添加iocAllStart方法，根据条件动态添加注解和关键字
            val hasSuspend = suspendFunctions.isNotEmpty()
            val hasComposable = composableFunctions.isNotEmpty()

            val allStartAnnotation = when {
                hasSuspend -> ""
                hasComposable -> "@androidx.compose.runtime.Composable\n    "
                else -> ""
            }

            val allStartModifier = when {
                hasSuspend -> "suspend "
                else -> ""
            }

//            val allStartMethodName = when {
//                hasComposable && !hasSuspend -> "IocAllStart"  // Composable函数需要首字母大写
//                else -> "iocAllStart"
//            }
            // 保持方法名始终为小写的一致性
            val allStartMethodName = "iocAllStart"


            val allStartMethods = when {
                hasSuspend -> methodNames.filter { it != "IocComposeableStart" }
                hasComposable && !hasSuspend -> methodNames
                else -> methodNames
            }

            append(
                """
                |    ${allStartAnnotation}${allStartModifier}fun ${allStartMethodName}() {
                |        ${allStartMethods.joinToString("\n        ") { "$it()" }}
                |    }
            """.trimMargin()
            )
        }

        // 生成代码
        val code = """
            |package $GENERATED_PACKAGE
            |
            |${imports.joinToString("\n") { "import $it" }}
            |
            |public object $CONTAINER_NAME {
            |$functionListCode
            |}
        """.trimMargin()

        // 写入生成的代码
        val file = codeGenerator.createNewFile(Dependencies.ALL_FILES, GENERATED_PACKAGE, CONTAINER_NAME, "kt")
        file.write(code.toByteArray())
        file.close()

        logger.warn("代码生成完成")
        System.out.println("代码生成完成")
    }

      // 提取 Component 信息
    private fun extractComponentInfo(clazz: KSClassDeclaration) {
        logger.warn("开始提取 Component 信息: ${clazz.simpleName.asString()}")
        System.out.println("开始提取 Component 信息: ${clazz.simpleName.asString()}")

        // 获取类名
        val className = clazz.qualifiedName?.asString()
        if (className == null) {
            logger.error("无法获取类名: ${clazz.simpleName.asString()}")
            return
        }

        // 获取包名
        val packageName = clazz.packageName.asString().takeIf { it.isNotEmpty() } ?: ""

        // 获取 @Component 注解的属性
        val componentAnnotation = clazz.annotations.find { annotation ->
            annotation.annotationType.resolve().declaration.qualifiedName?.asString() == COMPONENT_ANNOTATION_NAME
        }

        val componentName = componentAnnotation?.arguments?.find { it.name?.asString() == "value" }?.value?.toString()
            ?.takeIf { it.isNotEmpty() }
            ?: clazz.simpleName.asString().replaceFirstChar { it.lowercase() }

        // 获取所有实现的接口，包括从抽象类继承的接口
        val interfaces = mutableListOf<String>()

        // 收集当前类直接实现的接口
        clazz.superTypes.forEach { superType ->
            val resolvedType = superType.resolve()
            val declaration = resolvedType.declaration

            // 只获取接口，不包括父类
            if (declaration is KSClassDeclaration && declaration.classKind == ClassKind.INTERFACE) {
                declaration.qualifiedName?.asString()?.let { interfaces.add(it) }
            }
        }

        // 递归收集父类（包括抽象类）实现的接口
        fun collectSuperclassInterfaces(superClass: KSClassDeclaration) {
            // 获取父类的接口
            superClass.superTypes.forEach { superType ->
                val resolvedType = superType.resolve()
                val declaration = resolvedType.declaration

                if (declaration is KSClassDeclaration) {
                    when {
                        declaration.classKind == ClassKind.INTERFACE -> {
                            declaration.qualifiedName?.asString()?.let {
                                if (!interfaces.contains(it)) {
                                    interfaces.add(it)
                                }
                            }
                        }
                        declaration.classKind == ClassKind.CLASS &&
                        declaration.modifiers.contains(Modifier.ABSTRACT) -> {
                            // 递归处理抽象类
                            collectSuperclassInterfaces(declaration)
                        }
                    }
                }
            }
        }

        // 查找并处理父类
        clazz.superTypes.forEach { superType ->
            val resolvedType = superType.resolve()
            val declaration = resolvedType.declaration

            if (declaration is KSClassDeclaration &&
                declaration.classKind == ClassKind.CLASS &&
                !declaration.qualifiedName?.asString().equals("kotlin.Any")) {
                collectSuperclassInterfaces(declaration)
            }
        }

        val componentInfo = ComponentInfo(
            className = className,
            packageName = packageName,
            componentName = componentName,
            isSingleton = true, // 默认为单例
            interfaces = interfaces
        )

        components.add(componentInfo)
        logger.warn("添加 Component 信息: $componentInfo")
        System.out.println("添加 Component 信息: $componentInfo")
    }

    // 生成 BeanRegistry 代码
    private fun generateBeanRegistry() {
        if (components.isEmpty()) {
            logger.warn("没有找到 @Component 注解的类，跳过 BeanRegistry 生成")
            System.out.println("没有找到 @Component 注解的类，跳过 BeanRegistry 生成")
            return
        }

        logger.warn("开始生成 BeanRegistry")
        System.out.println("开始生成 BeanRegistry")

        // 按包分组
        val componentsByPackage = components.groupBy { it.packageName }

        // 生成导入语句
        val imports = (components.map { it.className } + components.flatMap { it.interfaces }).toSet()

        // 生成注册代码
        val registrationCode = buildString {
            // 注册组件提供者
            components.forEach { component ->
                appendLine("        delegate.registerProvider(${component.className}::class) { ${component.className}() }")
            }

            appendLine()

            // 注册接口实现关系 - 直接使用 public 方法
            val interfaceMap = mutableMapOf<String, MutableList<String>>()
            components.forEach { component ->
                component.interfaces.forEach { interfaceName ->
                    val implementations = interfaceMap.getOrPut(interfaceName) { mutableListOf() }
                    implementations.add(component.className)
                }
            }

            if (interfaceMap.isNotEmpty()) {
                appendLine()
                appendLine("        // 注册接口实现关系")

                interfaceMap.forEach { (interfaceName, implementations) ->
                    implementations.forEach { implClass ->
                        appendLine("        delegate.registerImplementation(${interfaceName}::class, ${implClass}::class)")
                    }
                }
            }
        }

        // 生成代码
        val code = """
            package $GENERATED_PACKAGE

            ${imports.joinToString("\n") { "import $it" }}
            import site.addzero.ioc.registry.KmpBeanRegistry
            import site.addzero.ioc.registry.BeanRegistry
            import kotlin.reflect.KClass

            /**
             * 自动生成的 Bean 注册表
             * 包含所有标记了 @Component 注解的类
             * KMP 兼容实现
             */
            public object $REGISTRY_NAME : BeanRegistry {
                // 使用 KMP 兼容的注册表实现
                private val delegate = KmpBeanRegistry()

                // 实现 BeanRegistry 接口的所有方法
                override fun <T : Any> getBean(clazz: KClass<T>): T? = delegate.getBean(clazz)

                override fun <T : Any> getRequiredBean(clazz: KClass<T>): T = delegate.getRequiredBean(clazz)

                override fun <T : Any> registerBean(clazz: KClass<T>, instance: T) = delegate.registerBean(clazz, instance)

                override fun <T : Any> registerProvider(clazz: KClass<T>, provider: () -> T) = delegate.registerProvider(clazz, provider)

                override fun containsBean(clazz: KClass<*>): Boolean = delegate.containsBean(clazz)

                override fun getBeanTypes(): Set<KClass<*>> = delegate.getBeanTypes()

                override fun <T : Any> injectList(clazz: KClass<T>): List<T> = delegate.injectList(clazz)

                init {
                    // 注册所有 Component 类
$registrationCode
                }

                /**
                 * 获取所有已注册的组件名称
                 */
                fun getComponentNames(): Set<String> {
                    return setOf(${components.map { "\"${it.componentName}\"" }.joinToString(", ")})
                }

                /**
                 * 根据组件名称获取对应的类型
                 */
                fun getComponentType(name: String): KClass<*>? {
                    return when (name) {
${components.map { "                        \"${it.componentName}\" -> ${it.className}::class" }.joinToString("\n")}
                        else -> null
                    }
                }
            }
        """.trimIndent()

        // 写入生成的代码
        val file = codeGenerator.createNewFile(Dependencies.ALL_FILES, GENERATED_PACKAGE, REGISTRY_NAME, "kt")
        file.write(code.toByteArray())
        file.close()

        logger.warn("BeanRegistry 代码生成完成")
        System.out.println("BeanRegistry 代码生成完成")
    }

    // 扩展函数用于向OutputStream写入字节数组
    private fun OutputStream.write(bytes: ByteArray) {
        this.write(bytes)
        this.flush()
    }
}

// 处理器提供者
class IocProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        System.out.println("创建 IocProcessor 实例")
        return IocProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}
