package com.example.autoinit.ksp

import site.addzero.autoinit.annotation.AutoInit
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.writeTo

private val LIST = ClassName("kotlin.collections", "List")
private val UNIT = ClassName("kotlin", "Unit")

class AutoInitProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    // 存储函数信息的数据类
    private data class InitFunction(
        val className: String?,       // 类全名（顶层函数为null）
        val fileName: String,         // 文件名
        val functionName: String,     // 函数名
        val isSuspend: Boolean,       // 是否挂起函数
        val isComposable: Boolean,    // 是否Composable函数
        val isStatic: Boolean         // 是否静态函数（伴生对象中）
    )

    private val functions = mutableListOf<InitFunction>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.warn("AutoInitProcessor 开始处理...")
        System.out.println("AutoInitProcessor 开始处理...")
        
        // 1. 扫描所有带@AutoInit注解的函数
        val autoInitAnnotationName = "site.addzero.autoinit.annotation.AutoInit"
        logger.warn("查找注解: $autoInitAnnotationName")
        System.out.println("查找注解: $autoInitAnnotationName")
        
        val autoInitSymbols = resolver.getSymbolsWithAnnotation(autoInitAnnotationName)
        logger.warn("找到带@AutoInit注解的符号数量: ${autoInitSymbols.toList().size}")
        System.out.println("找到带@AutoInit注解的符号数量: ${autoInitSymbols.toList().size}")
        
        val autoInitFunctions = autoInitSymbols
            .filterIsInstance<KSFunctionDeclaration>()
        logger.warn("其中函数声明数量: ${autoInitFunctions.toList().size}")
        System.out.println("其中函数声明数量: ${autoInitFunctions.toList().size}")

        // 2. 提取函数信息
        autoInitFunctions.forEach { function ->
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

        // 注意：不在process阶段生成代码，避免多轮处理时的覆盖问题
        logger.warn("AutoInitProcessor 处理完成")
        System.out.println("AutoInitProcessor 处理完成")
        return emptyList()
    }

    override fun finish() {
        // 在finish阶段生成代码，确保只生成一次
        logger.warn("总共收集到 ${functions.size} 个函数")
        System.out.println("总共收集到 ${functions.size} 个函数")
        generateAutoInitCode()
    }

    // 提取函数信息（判断是否为挂起/Composable函数）
    private fun extractFunctionInfo(function: KSFunctionDeclaration) {
        logger.warn("开始提取函数信息: ${function.simpleName.asString()}")
        System.out.println("开始提取函数信息: ${function.simpleName.asString()}")
        
        // 只处理无参函数
        if (function.parameters.isNotEmpty()) {
            logger.warn("跳过带参数的函数 ${function.simpleName}（@AutoInit仅支持无参函数）")
            System.out.println("跳过带参数的函数 ${function.simpleName}（@AutoInit仅支持无参函数）")
            return
        }

        // 获取函数所在类（可为空，表示顶层函数）
        val parentClass = function.parentDeclaration as? KSClassDeclaration
        
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

        val initFunction = InitFunction(
            className = parentClass?.qualifiedName?.asString(),
            fileName = fileName,
            functionName = function.simpleName.asString(),
            isSuspend = isSuspend,
            isComposable = isComposable,
            isStatic = isStatic
        )
        
        functions.add(initFunction)
        logger.warn("添加函数信息: $initFunction")
        System.out.println("添加函数信息: $initFunction")
    }

    // 动态生成代码（只生成存在的函数类型）
    private fun generateAutoInitCode() {
        logger.warn("开始生成代码，函数总数: ${functions.size}")
        System.out.println("开始生成代码，函数总数: ${functions.size}")
        
        // 按类型分组
        val regularFunctions = functions.filter { !it.isSuspend && !it.isComposable }
        val suspendFunctions = functions.filter { it.isSuspend && !it.isComposable }
        val composableFunctions = functions.filter { it.isComposable }

        logger.warn("普通函数: ${regularFunctions.size}个")
        logger.warn("挂起函数: ${suspendFunctions.size}个")
        logger.warn("Composable函数: ${composableFunctions.size}个")
        System.out.println("普通函数: ${regularFunctions.size}个")
        System.out.println("挂起函数: ${suspendFunctions.size}个")
        System.out.println("Composable函数: ${composableFunctions.size}个")

        // 生成AutoInitContainer类（包含所有检测到的函数列表）
        val fileSpec = FileSpec.builder("com.example.autoinit.generated", "AutoInitContainer")
            .addType(
                TypeSpec.objectBuilder("AutoInitContainer")
                    // 1. 普通函数列表（仅当存在时生成）
                    .apply {
                        if (regularFunctions.isNotEmpty()) {
                            addProperty(
                                PropertySpec.builder("regularFunctions", LIST.parameterizedBy(ClassName("kotlin", "Function0").parameterizedBy(UNIT)))
                                    .addModifiers(KModifier.PRIVATE)
                                    .initializer(CodeBlock.of("listOf(%L)", regularFunctions.joinToCode()))
                                    .build()
                            )
                        }
                    }
                    // 2. 挂起函数列表（仅当存在时生成）
                    .apply {
                        if (suspendFunctions.isNotEmpty()) {
                            addProperty(
                                PropertySpec.builder("suspendFunctions", LIST.parameterizedBy(ClassName("kotlin", "Function0").parameterizedBy(UNIT)))
                                    .addModifiers(KModifier.PRIVATE)
                                    .initializer(CodeBlock.of("listOf(%L)", suspendFunctions.joinToCode()))
                                    .build()
                            )
                        }
                    }
                    // 3. Composable函数列表（仅当存在时生成）
                    .apply {
                        if (composableFunctions.isNotEmpty()) {
                            // 生成Compose相关导入
                            addAnnotation(
                                AnnotationSpec.builder(ClassName("androidx.compose.runtime", "Composable"))
                                    .build()
                            )
                            addProperty(
                                PropertySpec.builder("composableFunctions", LIST.parameterizedBy(ClassName("kotlin", "Function0").parameterizedBy(UNIT)))
                                    .addModifiers(KModifier.PRIVATE)
                                    .initializer(CodeBlock.of("listOf(%L)", composableFunctions.joinToCode()))
                                    .build()
                            )
                        }
                    }
                    // 4. 执行所有普通函数的方法
                    .apply {
                        if (regularFunctions.isNotEmpty()) {
                            addFunction(
                                FunSpec.builder("executeRegular")
                                    .addStatement("regularFunctions.forEach { it() }")
                                    .build()
                            )
                        }
                    }
                    // 5. 执行所有挂起函数的方法
                    .apply {
                        if (suspendFunctions.isNotEmpty()) {
                            addFunction(
                                FunSpec.builder("executeSuspend")
                                    .addModifiers(KModifier.SUSPEND)
                                    .addStatement("suspendFunctions.forEach { it() }")
                                    .build()
                            )
                        }
                    }
                    // 6. 执行所有Composable函数的方法（仅当存在时生成）
                    .apply {
                        if (composableFunctions.isNotEmpty()) {
                            addFunction(
                                FunSpec.builder("executeComposable")
                                    .addAnnotation(ClassName("androidx.compose.runtime", "Composable"))
                                    .addStatement("composableFunctions.forEach { it() }")
                                    .build()
                            )
                        }
                    }
                    .build()
            )
            .build()

        // 写入生成的代码
        fileSpec.writeTo(codeGenerator, Dependencies.ALL_FILES)
        logger.warn("代码生成完成")
        System.out.println("代码生成完成")
    }

    // 工具方法：将函数列表转换为代码块
    private fun List<InitFunction>.joinToCode(): CodeBlock {
        if (this.isEmpty()) {
            return CodeBlock.of("")
        }

        val codeBlocks = this.map { func ->
            if (func.isStatic) {
                // 对于伴生对象函数，直接使用类名调用
                val className = ClassName.bestGuess(func.className?.substringBefore(".Companion") ?: "Unknown")
                CodeBlock.of("{ %T.${func.functionName}() }", className)
            } else if (func.className != null) {
                // 对于类中函数，需要创建实例再调用
                val classType = ClassName.bestGuess(func.className)
                CodeBlock.of("{ %T().${func.functionName}() }", classType)
            } else {
                // 对于顶层函数，直接调用
                val fileClass = ClassName("", func.fileName + "Kt")
                CodeBlock.of("{ %T.${func.functionName}() }", fileClass)
            }
        }

        return codeBlocks.reduce { acc, codeBlock ->
            CodeBlock.of("%L, %L", acc, codeBlock)
        }
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