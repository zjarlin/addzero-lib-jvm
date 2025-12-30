package site.addzero.genreified.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import site.addzero.kcp.annotations.GenerateReified
import java.io.BufferedWriter

class ReifiedSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ReifiedSymbolProcessor(environment.codeGenerator, environment.logger)
    }
}

private val SITE_ADDZERO_KCP_ANNOTATIONS_GENERATE_REIFIED = GenerateReified::class.qualifiedName?:""

class ReifiedSymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val generateReifiedAnnotation = SITE_ADDZERO_KCP_ANNOTATIONS_GENERATE_REIFIED

        val annotatedSymbols = resolver
            .getSymbolsWithAnnotation(generateReifiedAnnotation)
            .toList()

        // 处理类级别的注解
        annotatedSymbols.filterIsInstance<KSClassDeclaration>().forEach { classDecl ->
            if (!classDecl.validate()) return@forEach
            processClass(classDecl, hasClassAnnotation = true)
        }

        // 处理方法级别的注解
        val annotatedFunctions = annotatedSymbols.filterIsInstance<KSFunctionDeclaration>()
        annotatedFunctions.groupBy { it.parentDeclaration as? KSClassDeclaration }
            .forEach { (classDecl, functions) ->
                if (classDecl == null || !classDecl.validate()) return@forEach
                // 只处理方法级别注解的函数
                processClassWithSpecificFunctions(classDecl, functions)
            }

        return emptyList()
    }

    private fun processClass(classDecl: KSClassDeclaration, hasClassAnnotation: Boolean) {
        val packageName = classDecl.packageName.asString()
        val className = classDecl.simpleName.asString()
        val fileName = "${className}Reified"

        val functions = if (hasClassAnnotation) {
            // 类上有注解：处理所有符合条件的方法
            classDecl.getAllFunctions()
                .filter { it.hasReifiableParams() }
                .toList()
        } else {
            // 类上没有注解：只处理标注了注解的方法
            classDecl.getAllFunctions()
                .filter { it.hasGenerateReifiedAnnotation() && it.hasReifiableParams() }
                .toList()
        }

        if (functions.isEmpty()) return

        val file = codeGenerator.createNewFile(
            Dependencies(true, classDecl.containingFile!!),
            packageName,
            fileName
        )

        file.bufferedWriter().use { writer ->
            writer.write("@file:Suppress(\"NOTHING_TO_INLINE\")\n\n")
            writer.write("package $packageName\n\n")
            writer.write("import kotlin.reflect.KClass\n\n")

            functions.forEach { function ->
                generateReifiedFunction(writer, classDecl, function)
            }
        }
    }

    private fun processClassWithSpecificFunctions(
        classDecl: KSClassDeclaration,
        functions: List<KSFunctionDeclaration>
    ) {
        // 检查类是否已经有类级别的注解
        if (classDecl.annotations.any {
            it.annotationType.resolve().declaration.qualifiedName?.asString() ==
                    SITE_ADDZERO_KCP_ANNOTATIONS_GENERATE_REIFIED
        }) {
            // 类上已经有注解，跳过（已经在 processClass 中处理）
            return
        }

        val packageName = classDecl.packageName.asString()
        val className = classDecl.simpleName.asString()
        val fileName = "${className}Reified"

        val validFunctions = functions.filter { it.hasReifiableParams() }
        if (validFunctions.isEmpty()) return

        val file = codeGenerator.createNewFile(
            Dependencies(true, classDecl.containingFile!!),
            packageName,
            fileName
        )

        file.bufferedWriter().use { writer ->
            writer.write("@file:Suppress(\"NOTHING_TO_INLINE\")\n\n")
            writer.write("package $packageName\n\n")
            writer.write("import kotlin.reflect.KClass\n\n")

            validFunctions.forEach { function ->
                generateReifiedFunction(writer, classDecl, function)
            }
        }
    }

    private fun KSFunctionDeclaration.hasGenerateReifiedAnnotation(): Boolean {
        return annotations.any {
            it.annotationType.resolve().declaration.qualifiedName?.asString() ==
                    SITE_ADDZERO_KCP_ANNOTATIONS_GENERATE_REIFIED
        }
    }

    private fun KSFunctionDeclaration.hasReifiableParams(): Boolean {
        // 跳过有 vararg 参数的方法
        if (parameters.any { it.isVararg }) return false

        return parameters.any { param ->
            val typeName = param.type.resolve().declaration.qualifiedName?.asString()
            typeName == "kotlin.reflect.KClass" || typeName == "java.lang.Class"
        }
    }

    private fun generateReifiedFunction(
        writer: BufferedWriter,
        classDecl: KSClassDeclaration,
        function: KSFunctionDeclaration
    ) {
        val reifiableParams = function.parameters.mapIndexedNotNull { index, param ->
            val typeName = param.type.resolve().declaration.qualifiedName?.asString()
            when (typeName) {
                "kotlin.reflect.KClass" -> index to "KClass"
                "java.lang.Class" -> index to "Class"
                else -> null
            }
        }

        if (reifiableParams.isEmpty()) return

        // 获取自定义方法名（如果有）
        val customName = function.annotations
            .firstOrNull {
                it.annotationType.resolve().declaration.qualifiedName?.asString() ==
                        SITE_ADDZERO_KCP_ANNOTATIONS_GENERATE_REIFIED
            }
            ?.arguments
            ?.firstOrNull { it.name?.asString() == "value" }
            ?.value as? String

        val generatedFunctionName = customName?.takeIf { it.isNotBlank() }
            ?: function.simpleName.asString()

        // 提取类型参数及其约束
        val typeParams = reifiableParams.map { (paramIndex, _) ->
            val param = function.parameters[paramIndex]
            val typeArg = param.type.resolve().arguments.firstOrNull()?.type?.resolve()
            val typeParamDecl = typeArg?.declaration as? KSTypeParameter
            val typeParamName = typeParamDecl?.name?.asString() ?: "T"
            val bounds = typeParamDecl?.bounds?.toList() ?: emptyList()
            Triple(typeParamName, bounds, typeArg)
        }

        // 生成函数签名
        writer.write("inline fun <")
        writer.write(typeParams.joinToString(", ") { (name, bounds, _) ->
            val boundsStr = bounds.takeIf { it.isNotEmpty() }
                ?.joinToString(" & ") { it.resolve().toString() }
                ?.let { " : $it" } ?: ""
            "reified $name$boundsStr"
        })
        writer.write("> ")

        // 添加接收者
        val isObjectOrCompanion = classDecl.classKind == ClassKind.OBJECT ||
                                  classDecl.isCompanionObject
        if (!isObjectOrCompanion) {
            writer.write("${classDecl.simpleName.asString()}.")
        }

        writer.write("$generatedFunctionName(")

        // 生成参数列表（排除 KClass/Class 参数）
        val remainingParams = function.parameters.filterIndexed { index, _ ->
            reifiableParams.none { it.first == index }
        }

        writer.write(remainingParams.joinToString(", ") { param ->
            "${param.name?.asString()}: ${param.type.resolve()}"
        })

        // 生成返回类型
        val returnType = function.returnType?.resolve()?.toString() ?: "Unit"
        writer.write("): $returnType")

        // 生成函数体
        writer.write(" = ")

        // 对于 object，需要加上类名前缀
        if (isObjectOrCompanion) {
            writer.write("${classDecl.simpleName.asString()}.")
        }

        writer.write("${function.simpleName.asString()}(")

        val callArgs = function.parameters.mapIndexed { index, param ->
            val reifiableIndex = reifiableParams.indexOfFirst { it.first == index }
            if (reifiableIndex >= 0) {
                val (_, kind) = reifiableParams[reifiableIndex]
                val typeName = typeParams[reifiableIndex].first
                when (kind) {
                    "KClass" -> "$typeName::class"
                    "Class" -> "$typeName::class.java"
                    else -> param.name?.asString() ?: ""
                }
            } else {
                param.name?.asString() ?: ""
            }
        }

        writer.write(callArgs.joinToString(", "))
        writer.write(")\n\n")
    }
}
