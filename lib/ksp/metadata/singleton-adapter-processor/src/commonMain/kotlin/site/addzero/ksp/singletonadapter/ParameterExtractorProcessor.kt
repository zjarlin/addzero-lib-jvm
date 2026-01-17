package site.addzero.ksp.singletonadapter

import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import site.addzero.ksp.singletonadapter.anno.ExtractCommonParameters

class ParameterExtractorProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(ExtractCommonParameters::class.qualifiedName!!)

        val (valid, invalid) = symbols.partition { it.validate() }

        valid.filterIsInstance<KSClassDeclaration>().forEach { classDeclaration ->
            generateCommonParams(classDeclaration)
        }

        return invalid
    }

    private fun generateCommonParams(classDeclaration: KSClassDeclaration) {
        val annotation = classDeclaration.annotations
            .first { it.shortName.asString() == ExtractCommonParameters::class.simpleName }

        val originalClassName = classDeclaration.simpleName.asString()
        val commonParamsName = getCommonParamsName(annotation, originalClassName)
        val packageName = classDeclaration.packageName.asString()

        // 分析方法参数
        val methodParams = mutableMapOf<String, MutableList<KSFunctionDeclaration>>()
        classDeclaration.getAllFunctions()
            .filter { it.isPublic() && it.simpleName.asString() !in listOf("<init>", "equals", "hashCode", "toString") }
            .forEach { func ->
                func.parameters.forEach { param ->
                    val paramName = param.name!!.asString()
                    methodParams.getOrPut(paramName) { mutableListOf() }.add(func)
                }
            }

        // 找到重复>=2的参数
        val commonParams = methodParams.filter { it.value.size >= 2 }

        if (commonParams.isEmpty()) {
            logger.info("No common parameters found in ${classDeclaration.simpleName.asString()}")
            return
        }

        // 选择重复次数最多的参数组（如果有多个，选择最大的）
        val maxCount = commonParams.values.maxOf { it.size }
        val selectedParams = commonParams.filter { it.value.size == maxCount }

        // 生成新的类，构造函数包含共同参数和原有类实例
        val classBuilder = TypeSpec.classBuilder(commonParamsName)

        // 添加原有类的修饰符
        classDeclaration.modifiers.forEach { modifier ->
            modifier.toKModifier()?.let { classBuilder.addModifiers(it) }
        }

        val constructorBuilder = FunSpec.constructorBuilder()
        val properties = mutableListOf<PropertySpec>()

        // 添加共同参数作为属性
        selectedParams.forEach { (paramName, funcs) ->
            val paramType = funcs.first().parameters.first { it.name!!.asString() == paramName }.type.resolve().toTypeName()
            val property = PropertySpec.builder(paramName, paramType)
                .initializer(paramName)
                .build()
            properties.add(property)
            constructorBuilder.addParameter(paramName, paramType)
        }

        // 添加原有类实例作为属性
        val originalClassType = classDeclaration.toClassName()
        val delegateProperty = PropertySpec.builder("delegate", originalClassType)
            .initializer("delegate")
            .build()
        properties.add(delegateProperty)

        // 检查是否有无参数构造函数，如果有则提供默认值
        val hasNoArgConstructor = classDeclaration.primaryConstructor?.parameters?.isEmpty() == true ||
                                  classDeclaration.primaryConstructor == null ||
                                  classDeclaration.primaryConstructor?.parameters?.all { it.hasDefault } == true

        val delegateParam = ParameterSpec.builder("delegate", originalClassType)
        if (hasNoArgConstructor) {
            delegateParam.defaultValue("%T()", originalClassType)
        }
        constructorBuilder.addParameter(delegateParam.build())

        classBuilder.primaryConstructor(constructorBuilder.build())
        classBuilder.addProperties(properties)

        // 生成方法，去掉共同参数，调用delegate的方法
        classDeclaration.getAllFunctions()
            .filter { it.isPublic() && it.simpleName.asString() !in listOf("<init>", "equals", "hashCode", "toString") }
            .forEach { func ->
                val funcBuilder = FunSpec.builder(func.simpleName.asString())
                    .addModifiers(func.modifiers.mapNotNull { it.toKModifier() })
                    .returns(func.returnType!!.resolve().toTypeName())

                // 只添加非共同参数
                val remainingParams = func.parameters.filter { it.name!!.asString() !in selectedParams.keys }
                remainingParams.forEach { param ->
                    val pName = param.name!!.asString()
                    val pType = param.type.resolve().toTypeName()
                    funcBuilder.addParameter(pName, pType)
                }

                // 调用delegate方法，传递所有参数（共同参数用this.xxx，剩余参数用传入的）
                val callArgs = func.parameters.map { param ->
                    val paramName = param.name!!.asString()
                    if (paramName in selectedParams.keys) {
                        "this.$paramName"
                    } else {
                        paramName
                    }
                }

                funcBuilder.addStatement("return delegate.%N(${callArgs.joinToString(", ")})", func.simpleName.asString())

                classBuilder.addFunction(funcBuilder.build())
            }

        // 写文件
        FileSpec.builder(packageName, commonParamsName)
            .addType(classBuilder.build())
            .build()
            .writeTo(codeGenerator, Dependencies(true, classDeclaration.containingFile!!))
    }

    private fun getCommonParamsName(annotation: KSAnnotation, originalClassName: String): String {
        val nameArg = annotation.arguments.find { it.name?.asString() == "value" }?.value as? String
        return if (!nameArg.isNullOrEmpty()) nameArg else "${originalClassName}Delegate"
    }
}

class ParameterExtractorProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ParameterExtractorProcessor(environment.codeGenerator, environment.logger)
    }
}
