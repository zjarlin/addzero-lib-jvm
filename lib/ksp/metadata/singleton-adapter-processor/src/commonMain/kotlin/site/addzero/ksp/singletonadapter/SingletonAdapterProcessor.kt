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
import site.addzero.ksp.singletonadapter.anno.SingletonAdapter

class SingletonAdapterProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(SingletonAdapter::class.qualifiedName!!)
        
        val (valid, invalid) = symbols.partition { it.validate() }
        
        valid.filterIsInstance<KSClassDeclaration>().forEach { classDeclaration ->
            generateSingleton(classDeclaration)
        }
        
        return invalid
    }

    private fun generateInlineAdapter(classDeclaration: KSClassDeclaration, annotation: KSAnnotation) {
        val singletonName = getSingletonName(classDeclaration, annotation)
        val packageName = classDeclaration.packageName.asString()
        val className = classDeclaration.toClassName()

        // 解析注入配置
        val injectConfig = parseInjectConfig(annotation)

        // 获取构造函数参数
        val constructorParams = classDeclaration.primaryConstructor?.parameters ?: emptyList()

        // 生成新的object（单例）
        val classBuilder = TypeSpec.objectBuilder(singletonName)

        // 添加构造函数参数作为object属性（用于默认值）
        val properties = mutableListOf<PropertySpec>()
        constructorParams.forEach { param ->
            val paramName = param.name!!.asString()
            val paramType = param.type.resolve().toTypeName()

            val propBuilder = PropertySpec.builder(paramName, paramType)
                .mutable(true)

            // 处理默认值初始化
            val config = injectConfig[paramName]
            if (config != null) {
                when {
                    config.startsWith("env:") -> {
                        val envKey = config.substringAfter("env:")
                        propBuilder.initializer("System.getenv(%S) ?: \"\"", envKey)
                    }
                    config.startsWith("const:") -> {
                        val value = config.substringAfter("const:")
                        propBuilder.initializer("%S", value)
                    }
                    else -> propBuilder.initializer("TODO(\"Unsupported inject type\")")
                }
            } else if (param.hasDefault) {
                // 暂时无法获取默认值，假设它有默认值
                if (paramType == String::class.asTypeName()) {
                    propBuilder.initializer("\"\"")
                } else {
                    propBuilder.initializer("TODO(\"Missing injection or default value for $paramName\")")
                }
            } else {
                // 必填参数且无配置
                if (paramType == String::class.asTypeName()) {
                    propBuilder.initializer("\"\"")
                } else {
                    propBuilder.initializer("TODO(\"Missing injection for $paramName\")")
                }
            }
            properties.add(propBuilder.build())
        }

        classBuilder.addProperties(properties)

        // 添加lazy client缓存（当参数与默认值相同时使用）
        val lazyClient = PropertySpec.builder("_cachedClient", className.copy(nullable = true))
            .addModifiers(KModifier.PRIVATE)
            .mutable(true)
            .initializer("null")
            .build()
        classBuilder.addProperty(lazyClient)

        // 生成代理方法，将构造函数参数作为方法参数前缀
        classDeclaration.getAllFunctions()
            .filter { it.isPublic() && it.simpleName.asString() !in listOf("<init>", "equals", "hashCode", "toString") }
            .forEach { func ->
                val funcBuilder = FunSpec.builder(func.simpleName.asString())
                    .addModifiers(func.modifiers.mapNotNull { it.toKModifier() })
                    .returns(func.returnType!!.resolve().toTypeName())

                // 先添加构造函数参数（不带默认值，改为在方法体中处理）
                constructorParams.forEach { param ->
                    val paramName = param.name!!.asString()
                    val paramType = param.type.resolve().toTypeName()
                    funcBuilder.addParameter(paramName, paramType)
                }

                // 再添加原有方法参数
                func.parameters.forEach { param ->
                    val pName = param.name!!.asString()
                    val pType = param.type.resolve().toTypeName()
                    funcBuilder.addParameter(pName, pType)
                }

                // 生成方法体：直接创建实例（object模式下不缓存，因为参数可能每次都不同）
                val constructorArgs = constructorParams.joinToString(", ") { it.name!!.asString() }
                val methodArgs = func.parameters.joinToString(", ") { it.name!!.asString() }

                funcBuilder.addCode(
                    """
                    val instance = %T($constructorArgs)
                    return instance.%N($methodArgs)
                    """.trimIndent(),
                    className, func.simpleName.asString()
                )

                classBuilder.addFunction(funcBuilder.build())
            }

        // 写文件
        FileSpec.builder(packageName, singletonName)
            .addType(classBuilder.build())
            .build()
            .writeTo(codeGenerator, Dependencies(true, classDeclaration.containingFile!!))
    }

    private fun generateSingleton(classDeclaration: KSClassDeclaration) {
        val annotation = classDeclaration.annotations
            .first { it.shortName.asString() == SingletonAdapter::class.simpleName }

        val inlineToParameters = annotation.arguments.find { it.name?.asString() == "inlineToParameters" }?.value as? Boolean ?: false

        if (inlineToParameters) {
            generateInlineAdapter(classDeclaration, annotation)
            return
        }

        val singletonName = getSingletonName(classDeclaration, annotation)
        val packageName = classDeclaration.packageName.asString()
        val className = classDeclaration.toClassName()

        // 1. 解析注入配置
        val injectConfig = parseInjectConfig(annotation)

        // 2. 构建 config 函数参数和属性
        val configProps = mutableListOf<PropertySpec>()
        val configFuncParams = mutableListOf<ParameterSpec>()
        val constructorArgs = mutableListOf<CodeBlock>()
        val defaultConfigAssigns = mutableListOf<CodeBlock>()

        classDeclaration.primaryConstructor?.parameters?.forEach { param ->
            val paramName = param.name!!.asString()
            val paramType = param.type.resolve().toTypeName()
            
            // 查找对应的注入配置
            val config = injectConfig[paramName]
            
            // 生成属性
            val propBuilder = PropertySpec.builder(paramName, paramType)
                .mutable(true)
            
            // 处理默认值初始化
            if (config != null) {
                when {
                    config.startsWith("env:") -> {
                        val envKey = config.substringAfter("env:")
                        propBuilder.initializer("System.getenv(%S) ?: \"\"", envKey)
                    }
                    config.startsWith("const:") -> {
                        val value = config.substringAfter("const:")
                        propBuilder.initializer("%S", value)
                    }
                    else -> propBuilder.initializer("TODO(\"Unsupported inject type\")")
                }
            } else if (param.hasDefault) {
                 // 暂时无法获取默认值，假设它有默认值
                 // 对于 KSP 来说，获取默认值比较困难，这里简化处理：
                 // 如果没有配置注入，生成空字符串或者报错？
                 // 简单起见，我们对 String 类型默认为 ""
                 if (paramType == String::class.asTypeName()) {
                     propBuilder.initializer("\"\"")
                 } else {
                     propBuilder.initializer("TODO(\"Missing injection or default value for $paramName\")")
                 }
            } else {
                 // 必填参数且无配置
                 if (paramType == String::class.asTypeName()) {
                     propBuilder.initializer("\"\"")
                 } else {
                     propBuilder.initializer("TODO(\"Missing injection for $paramName\")")
                 }
            }
            configProps.add(propBuilder.build())
            
            // config 函数参数
            configFuncParams.add(
                ParameterSpec.builder(paramName, paramType)
                    .defaultValue("this.%N", paramName) // 默认值指向当前属性值
                    .build()
            )
            
            // 构造函数传参
            constructorArgs.add(CodeBlock.of("%N", paramName))
            
            // config 函数体内赋值
            defaultConfigAssigns.add(CodeBlock.of("this.%N = %N", paramName, paramName))
        }

        // 3.构建单例对象
        val objectBuilder = TypeSpec.objectBuilder(singletonName)
            .addProperties(configProps)

        // 4. 添加 _cachedClient 和 client 属性
        val cachedClientProp = PropertySpec.builder("_cachedClient", className.copy(nullable = true))
            .addModifiers(KModifier.PRIVATE)
            .mutable(true)
            .initializer("null")
            .build()
        objectBuilder.addProperty(cachedClientProp)

        // 生成 client getter 的条件判断代码
        val conditionBlock = CodeBlock.builder()
        conditionBlock.add("currentClient == null")
        classDeclaration.primaryConstructor?.parameters?.forEach { param ->
             // 假设 client 有对应的 getter 方法（如 getApiKey()）
             // TODO: 这里需要确保 Client 类暴露了 getter，或者我们在 wrapper 里记录状态？
             // 为了简单，我们只比较 _cachedClient 是否为空，或者每次 config 改动置空 _cachedClient
        }
        
        val clientProp = PropertySpec.builder("client", className)
            .getter(
                FunSpec.getterBuilder()
                    .addCode(
                        """
                        val current = _cachedClient
                        if (current == null) {
                            _cachedClient = %T(${constructorArgs.joinToString(", ")})
                        }
                        return _cachedClient!!
                        """.trimIndent(),
                        className
                    )
                    .build()
            )
            .build()
        objectBuilder.addProperty(clientProp)

        // 5. 添加 config 方法
        val configFunc = FunSpec.builder("config")
            .addParameters(configFuncParams)
            .addCode(
                CodeBlock.builder()
                    .apply { defaultConfigAssigns.forEach { add(it).add("\n") } }
                    .add("_cachedClient = null") // 重置缓存
                    .build()
            )
            .build()
        objectBuilder.addFunction(configFunc)

        // 6. 代理公共方法
        classDeclaration.getAllFunctions()
            .filter { it.isPublic() && it.simpleName.asString() !in listOf("<init>", "equals", "hashCode", "toString") }
            .forEach { func ->
                val funcBuilder = FunSpec.builder(func.simpleName.asString())
                    .addModifiers(func.modifiers.mapNotNull { it.toKModifier() })
                    .returns(func.returnType!!.resolve().toTypeName())
                
                val args = mutableListOf<String>()
                func.parameters.forEach { param ->
                    val pName = param.name!!.asString()
                    val pType = param.type.resolve().toTypeName()
                    funcBuilder.addParameter(pName, pType)
                    args.add(pName)
                }

                funcBuilder.addStatement("return client.%N(${args.joinToString(", ")})", func.simpleName.asString())
                objectBuilder.addFunction(funcBuilder.build())
            }

        // 写文件
        FileSpec.builder(packageName, singletonName)
            .addType(objectBuilder.build())
            .build()
            .writeTo(codeGenerator, Dependencies(true, classDeclaration.containingFile!!))
    }

    private fun getSingletonName(classDeclaration: KSClassDeclaration, annotation: KSAnnotation): String {
        val nameArg = annotation.arguments.find { it.name?.asString() == "singletonName" }?.value as? String
        if (!nameArg.isNullOrEmpty()) return nameArg
        
        val className = classDeclaration.simpleName.asString()
        return if (className.endsWith("Client")) {
            className.removeSuffix("Client")
        } else {
            "${className}Adapter"
        }
    }
    
    private fun parseInjectConfig(annotation: KSAnnotation): Map<String, String> {
        val injectArg = annotation.arguments.find { it.name?.asString() == "inject" }?.value as? List<*>
        return injectArg?.filterIsInstance<String>()?.associate { 
            val (key, value) = it.split("=", limit = 2)
            key.trim() to value.trim()
        } ?: emptyMap()
    }
}

class SingletonAdapterProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return SingletonAdapterProcessor(environment.codeGenerator, environment.logger)
    }
}
