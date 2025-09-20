package com.example.autoinit.ksp

import site.addzero.autoinit.annotation.AutoInit
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.OutputStream

// 存储函数信息的数据类
private data class InitFunction(
    val packageName: String?,     // 包名
    val className: String?,       // 类全名（顶层函数为null）
    val fileName: String,         // 文件名
    val functionName: String?,    // 函数名（object/class为null）
    val isSuspend: Boolean,       // 是否挂起函数
    val isComposable: Boolean,    // 是否Composable函数
    val isStatic: Boolean,        // 是否静态函数（伴生对象中）
    val initType: InitType        // 初始化类型
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
private enum class FunctionType(private val functionCallTemplate: String, private val isSuspend: Boolean, private val hasComposeAnnotation: Boolean) : CodeGenerationStrategy {
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
                // 为类实例调用
                functionCallTemplate.format("${function.className}()")
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
            REGULAR -> "regular"
            CLASS_INSTANCE -> "classInstance"
            OBJECT_INSTANCE -> "objectInstance"
            SUSPEND -> "suspend"
            COMPOSABLE -> "composable"
        }
        
        val suspendModifier = if (isSuspend) "suspend " else ""
        val composeAnnotation = if (hasComposeAnnotation) "@androidx.compose.runtime.Composable\n    " else ""
        // 更正方法名，保持统一的命名规则
        val methodName = when (this) {
            REGULAR -> "iocRegularStart"
            CLASS_INSTANCE -> "iocClassInstanceStart"
            OBJECT_INSTANCE -> "iocObjectInstanceStart"
            SUSPEND -> "iocSuspendStart"
            COMPOSABLE -> "iocComposeableStart"
        }

        return """
    ${composeAnnotation}${suspendModifier}fun ${methodName}() {
        ${functionName}Functions.forEach { it() }
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

class AutoInitProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    private val functions = mutableListOf<InitFunction>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.warn("AutoInitProcessor 开始处理...")
        System.out.println("AutoInitProcessor 开始处理...")

        // 1. 扫描所有带@AutoInit注解的符号
        val autoInitAnnotationName = "site.addzero.autoinit.annotation.AutoInit"
        logger.warn("查找注解: $autoInitAnnotationName")
        System.out.println("查找注解: $autoInitAnnotationName")

        val autoInitSymbols = resolver.getSymbolsWithAnnotation(autoInitAnnotationName)
        logger.warn("找到带@AutoInit注解的符号数量: ${autoInitSymbols.toList().size}")
        System.out.println("找到带@AutoInit注解的符号数量: ${autoInitSymbols.toList().size}")

        // 2. 分别处理函数、类和对象
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

        // 注意：不在process阶段生成代码，避免多轮处理时的覆盖问题
        logger.warn("AutoInitProcessor 处理完成")
        System.out.println("AutoInitProcessor 处理完成")
        return emptyList()
    }

    override fun finish() {
        // 在finish阶段生成代码，确保只生成一次
        logger.warn("总共收集到 ${functions.size} 个初始化项")
        System.out.println("总共收集到 ${functions.size} 个初始化项")
        generateAutoInitCode()
    }

    // 提取函数信息
    private fun extractFunctionInfo(function: KSFunctionDeclaration) {
        logger.warn("开始提取函数信息: ${function.simpleName.asString()}")
        System.out.println("开始提取函数信息: ${function.simpleName.asString()}")

        // 只处理无参函数或所有参数都有默认值的函数
        if (function.parameters.isNotEmpty() && function.parameters.any { !it.hasDefault }) {
            logger.warn("跳过带必需参数的函数 ${function.simpleName}（@AutoInit仅支持无参函数或所有参数都有默认值的函数）")
            System.out.println("跳过带必需参数的函数 ${function.simpleName}（@AutoInit仅支持无参函数或所有参数都有默认值的函数）")
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
            logger.warn("跳过类 ${clazz.simpleName}（@AutoInit仅支持有无参构造函数的类）")
            System.out.println("跳过类 ${clazz.simpleName}（@AutoInit仅支持有无参构造函数的类）")
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

        val initFunction = InitFunction(
            packageName = packageName,
            className = clazz.qualifiedName?.asString(),
            fileName = fileName,
            functionName = null,
            isSuspend = false,
            isComposable = isComposable,
            isStatic = false,
            initType = InitType.CLASS_INSTANCE
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
    private fun generateImports(regularFunctions: List<InitFunction>, suspendFunctions: List<InitFunction>, composableFunctions: List<InitFunction>): Set<String> {
        val imports = mutableSetOf<String>()

        // 添加 Compose 必需的导入
        imports.add("androidx.compose.runtime.Composable")

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
        logger.warn("开始生成代码，初始化项总数: ${functions.size}")
        System.out.println("开始生成代码，初始化项总数: ${functions.size}")

        // 按类型分组，包括新增的类实例和对象实例类型
        val regularFunctions = functions.filter { !it.isSuspend && !it.isComposable && it.initType == InitType.TOP_LEVEL_FUNCTION }
        val classInstances = functions.filter { !it.isSuspend && !it.isComposable && it.initType == InitType.CLASS_INSTANCE }
        val objectInstances = functions.filter { !it.isSuspend && !it.isComposable && it.initType == InitType.OBJECT_INSTANCE }
        val companionObjects = functions.filter { !it.isSuspend && !it.isComposable && it.initType == InitType.COMPANION_OBJECT }
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
        val functionListCode = FunctionType.values().joinToString("\n") { type ->
            val functions = functionGroups[type] ?: emptyList()
            if (functions.isNotEmpty()) {
                val functionName = when (type) {
                    FunctionType.REGULAR -> "regular"
                    FunctionType.CLASS_INSTANCE -> "classInstance"
                    FunctionType.OBJECT_INSTANCE -> "objectInstance"
                    FunctionType.SUSPEND -> "suspend"
                    FunctionType.COMPOSABLE -> "composable"
                }
                """
                    |    private val ${functionName}Functions = listOf(
                    |        ${functions.joinToString(",\n        ") { type.generateFunctionCall(it) }}
                    |    )
                    |
                    |${type.generateExecuteMethod(functions)}
                """.trimMargin()
            } else {
                ""
            }
        }.lines().filter { it.isNotBlank() }.joinToString("\n")

        // 生成代码
        val code = """
            |package com.example.autoinit.generated
            |
            |${imports.joinToString("\n") { "import $it" }}
            |
            |public object AutoInitContainer {
            |$functionListCode
            |}
        """.trimMargin()

        // 写入生成的代码
        val file = codeGenerator.createNewFile(Dependencies.ALL_FILES, "com.example.autoinit.generated", "AutoInitContainer", "kt")
        file.write(code.toByteArray())
        file.close()

        logger.warn("代码生成完成")
        System.out.println("代码生成完成")
    }

    // 扩展函数用于向OutputStream写入字节数组
    private fun OutputStream.write(bytes: ByteArray) {
        this.write(bytes)
        this.flush()
    }
}

// 处理器提供者
class AutoInitProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        System.out.println("创建 AutoInitProcessor 实例")
        return AutoInitProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}
