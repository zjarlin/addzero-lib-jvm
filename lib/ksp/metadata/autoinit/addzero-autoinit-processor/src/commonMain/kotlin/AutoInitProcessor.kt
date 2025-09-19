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
        val className: String,        // 类全名
        val functionName: String,     // 函数名
        val isSuspend: Boolean,       // 是否挂起函数
        val isComposable: Boolean,    // 是否Composable函数
        val isStatic: Boolean         // 是否静态函数（伴生对象中）
    )

    private val functions = mutableListOf<InitFunction>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        // 1. 扫描所有带@AutoInit注解的函数
        val autoInitFunctions = resolver.getSymbolsWithAnnotation(AutoInit::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()
            .filter { it.validate() }

        // 2. 提取函数信息
        autoInitFunctions.forEach { function ->
            try {
                extractFunctionInfo(function)
            } catch (e: Exception) {
                logger.error("处理函数 ${function.simpleName} 失败: ${e.message}")
            }
        }

        // 3. 动态生成代码（只包含实际存在的函数类型）
        generateAutoInitCode()

        return autoInitFunctions.filterNot { it.validate() }.toList()
    }

    // 提取函数信息（判断是否为挂起/Composable函数）
    private fun extractFunctionInfo(function: KSFunctionDeclaration) {
        // 只处理无参函数
        if (function.parameters.isNotEmpty()) {
            logger.warn("跳过带参数的函数 ${function.simpleName}（@AutoInit仅支持无参函数）")
            return
        }

        // 获取函数所在类
        val parentClass = function.parentDeclaration as? KSClassDeclaration
            ?: run {
                logger.warn("跳过顶层函数 ${function.simpleName}（@AutoInit仅支持类中的函数）")
                return
            }

        // 判断是否为Composable函数（通过注解全类名判断，避免直接依赖Compose）
        val isComposable = function.annotations.any { annotation ->
            val annotationType = annotation.annotationType.resolve().declaration.qualifiedName?.asString()
            annotationType == "androidx.compose.runtime.Composable"
        }

        // 判断是否为挂起函数
        val isSuspend = function.modifiers.any { it == Modifier.SUSPEND }

        // 判断是否为伴生对象中的函数
        val isStatic = function.parentDeclaration is KSClassDeclaration && (function.parentDeclaration as KSClassDeclaration).isCompanionObject

        functions.add(
            InitFunction(
                className = parentClass.qualifiedName!!.asString(),
                functionName = function.simpleName.asString(),
                isSuspend = isSuspend,
                isComposable = isComposable,
                isStatic = isStatic
            )
        )
    }

    // 动态生成代码（只生成存在的函数类型）
    private fun generateAutoInitCode() {
        // 按类型分组
        val regularFunctions = functions.filter { !it.isSuspend && !it.isComposable }
        val suspendFunctions = functions.filter { it.isSuspend && !it.isComposable }
        val composableFunctions = functions.filter { it.isComposable }

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
    }

    // 工具方法：将函数列表转换为代码块
    private fun List<InitFunction>.joinToCode(): CodeBlock {
        if (this.isEmpty()) {
            return CodeBlock.of("")
        }

        val codeBlocks = this.map { func ->
            if (func.isStatic) {
                // 对于伴生对象函数，直接使用类名调用
                val className = ClassName.bestGuess(func.className.substringBefore(".Companion"))
                CodeBlock.of("{ %T.${func.functionName}() }", className)
            } else {
                // 对于实例函数，需要创建实例再调用
                val classType = ClassName.bestGuess(func.className)
                CodeBlock.of("{ %T().${func.functionName}() }", classType)
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
        return AutoInitProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}
