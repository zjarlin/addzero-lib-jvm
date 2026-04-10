package site.addzero.processor

import site.addzero.controller2api.processor.context.Settings
import site.addzero.processor.type.TypeMappingManager
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.File

private const val REST_CONTROLLER_ANNOTATION = "org.springframework.web.bind.annotation.RestController"
private const val FILE_REQUEST_MAPPING_ANNOTATION = "site.addzero.springktor.runtime.RequestMapping"
private const val API_CLIENT_BRIDGE_PACKAGE_NAME_OPTION = "apiClientBridgePackageName"
private const val API_CLIENT_BRIDGE_OUTPUT_DIR_OPTION = "apiClientBridgeOutputDir"
private const val API_CLIENT_BRIDGE_FILE_NAME_OPTION = "apiClientBridgeFileName"

private val springMvcMappingAnnotations = listOf(
    "org.springframework.web.bind.annotation.GetMapping",
    "org.springframework.web.bind.annotation.PostMapping",
    "org.springframework.web.bind.annotation.PutMapping",
    "org.springframework.web.bind.annotation.DeleteMapping",
    "org.springframework.web.bind.annotation.PatchMapping",
    "org.springframework.web.bind.annotation.RequestMapping",
)

/**
 * KSP处理器：解析Controller符号生成Ktorfit接口
 *
 * 使用两阶段处理：
 * 1. process() 阶段：收集元数据
 * 2. finish() 阶段：生成代码
 */
class ControllerApiProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    // 存储收集到的控制器元数据
    private val collectedControllers = mutableListOf<ControllerMetadata>()
    private val collectedSourceKeys = mutableSetOf<String>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        Settings.fromOptions(options)
        logger.warn("解析Controller符号生成Ktorfit接口初始化配置: ${Settings.toOptions()}")

        val invalidSymbols = mutableListOf<KSAnnotated>()
        invalidSymbols += collectControllerSymbols(resolver)
        invalidSymbols += collectTopLevelFileSymbols(resolver)
        return invalidSymbols
    }

    override fun finish() {
        // 第二阶段：生成代码
        logger.info("开始生成代码，共收集到 ${collectedControllers.size} 个来源")
        collectedControllers.forEach { metadata ->
            generateKtorfitInterfaceFromMetadata(metadata)
        }
        generateApiAggregator(collectedControllers)
        collectedControllers.clear()
        collectedSourceKeys.clear()
    }

    /**
     * 收集类控制器元数据
     */
    private fun collectControllerSymbols(resolver: Resolver): List<KSAnnotated> {
        val controllerSymbols = resolver
            .getSymbolsWithAnnotation(REST_CONTROLLER_ANNOTATION)
            .filterIsInstance<KSClassDeclaration>()

        val invalidSymbols = mutableListOf<KSAnnotated>()

        controllerSymbols.forEach { controller ->
            if (controller.validate()) {
                collectControllerMetadata(controller)
            } else {
                logger.info("控制器 ${controller.simpleName.asString()} 暂时无法解析，推迟到下一轮处理")
                invalidSymbols.add(controller)
            }
        }

        return invalidSymbols
    }

    /**
     * 收集顶层 Spring MVC 路由元数据
     */
    private fun collectTopLevelFileSymbols(resolver: Resolver): List<KSAnnotated> {
        val groupedFiles = linkedMapOf<String, TopLevelFileRound>()
        val invalidSymbols = mutableListOf<KSAnnotated>()
        val visitedFunctions = mutableSetOf<String>()

        springMvcMappingAnnotations.forEach { annotationName ->
            resolver.getSymbolsWithAnnotation(annotationName)
                .filterIsInstance<KSFunctionDeclaration>()
                .forEach { function ->
                    if (function.parentDeclaration != null) {
                        return@forEach
                    }

                    val functionKey = function.signatureKey()
                    if (!visitedFunctions.add(functionKey)) {
                        return@forEach
                    }

                    val file = function.containingFile
                    if (file == null) {
                        logger.warn("跳过顶层函数 ${function.simpleName.asString()}，无法解析所属文件")
                        return@forEach
                    }

                    val fileKey = "${function.packageName.asString()}:${file.fileName}"
                    val fileRound = groupedFiles.getOrPut(fileKey) {
                        TopLevelFileRound(file = file)
                    }

                    if (!function.validate()) {
                        logger.info("顶层路由 ${function.simpleName.asString()} 暂时无法解析，推迟到下一轮处理")
                        fileRound.hasInvalidSymbols = true
                        invalidSymbols.add(function)
                        return@forEach
                    }

                    fileRound.functions.add(function)
                }
        }

        groupedFiles.values.forEach { fileRound ->
            if (fileRound.hasInvalidSymbols) {
                logger.info("顶层路由文件 ${fileRound.file.fileName} 含有未完成解析的方法，本轮跳过生成")
                return@forEach
            }
            collectTopLevelFileMetadata(fileRound.file, fileRound.functions)
        }

        return invalidSymbols
    }

    /**
     * 从元数据生成 Ktorfit 接口（第二阶段）
     */
    private fun generateKtorfitInterfaceFromMetadata(metadata: ControllerMetadata) {
        try {
            logger.info("生成 Ktorfit 接口: ${metadata.originalClassName}")

            // 将元数据转换为 ControllerInfo（兼容现有的生成逻辑）
            val controllerInfo = ControllerInfo(
                originalClassName = metadata.originalClassName,
                packageName = metadata.packageName,
                basePath = metadata.basePath,
                generatedApiClassName = metadata.generatedApiClassName,
                sourceDescription = metadata.sourceDescription,
                methods = metadata.methods.map { methodMetadata ->
                    MethodInfo(
                        name = methodMetadata.name,
                        httpMethod = methodMetadata.httpMethod,
                        path = methodMetadata.path,
                        returnType = methodMetadata.returnTypeString,
                        parameters = methodMetadata.parameters.map { paramMetadata ->
                            ParameterInfo(
                                name = paramMetadata.name,
                                type = paramMetadata.typeString,
                                isRequestBody = paramMetadata.isRequestBody,
                                isPathVariable = paramMetadata.isPathVariable,
                                isRequestParam = paramMetadata.isRequestParam,
                                isRequestPart = paramMetadata.isRequestPart
                            )
                        }
                    )
                }
            )

            // 使用现有的生成逻辑
            generateKtorfitInterface(controllerInfo)

        } catch (e: Exception) {
            logger.error("生成 Ktorfit 接口时发生错误 ${metadata.originalClassName}: ${e.message}")
        }
    }


    /**
     * 收集控制器元数据（第一阶段）
     */
    private fun collectControllerMetadata(controller: KSClassDeclaration) {
        try {
            logger.info("收集控制器元数据: ${controller.qualifiedName?.asString()}")

            val metadata = extractControllerMetadata(controller)
            registerCollectedMetadata(
                sourceKey = "controller:${controller.qualifiedName?.asString()}",
                metadata = metadata
            )

        } catch (e: Exception) {
            logger.error("收集控制器元数据时发生错误 ${controller.qualifiedName?.asString()}: ${e.message}")
        }
    }

    /**
     * 收集顶层文件元数据
     */
    private fun collectTopLevelFileMetadata(file: KSFile, functions: List<KSFunctionDeclaration>) {
        try {
            logger.info("收集顶层路由元数据: ${file.packageName.asString()}.${file.fileName}")

            val metadata = extractTopLevelFileMetadata(file, functions)
            registerCollectedMetadata(
                sourceKey = "file:${file.packageName.asString()}.${file.fileName}",
                metadata = metadata
            )
        } catch (e: Exception) {
            logger.error("收集顶层路由元数据时发生错误 ${file.packageName.asString()}.${file.fileName}: ${e.message}")
        }
    }

    /**
     * 注册收集结果，避免重复来源重复生成
     */
    private fun registerCollectedMetadata(sourceKey: String, metadata: ControllerMetadata) {
        if (metadata.methods.isEmpty()) {
            logger.info("来源 ${metadata.sourceDescription} 没有可生成的方法，跳过")
            return
        }

        if (!collectedSourceKeys.add(sourceKey)) {
            logger.info("来源 $sourceKey 已收集，跳过重复处理")
            return
        }

        collectedControllers.add(metadata)
    }

    /**
     * 提取控制器元数据（只收集字符串信息，避免类型解析问题）
     */
    private fun extractControllerMetadata(controller: KSClassDeclaration): ControllerMetadata {
        val className = controller.simpleName.asString()
        val packageName = controller.packageName.asString()

        // 提取RequestMapping基础路径
        val requestMapping = controller.annotations.find {
            it.shortName.asString() == "RequestMapping"
        }
        val basePath = if (requestMapping != null) {
            getPathFromAnnotation(requestMapping)
        } else {
            ""
        }

        // 提取所有带有Spring MVC注解的方法
        val allFunctions = controller.getAllFunctions().toList()
        logger.info("Controller ${className} has ${allFunctions.size} total functions")

        val springMvcFunctions = allFunctions.filter { it.isPublic() && it.hasSpringMvcAnnotation() && it.validate() }
        logger.info("Found ${springMvcFunctions.size} valid Spring MVC annotated functions")

        val methods = springMvcFunctions.mapNotNull { function ->
            try {
                logger.info("Processing method: ${function.simpleName.asString()}")
                extractMethodMetadata(function, basePath)
            } catch (e: Exception) {
                logger.warn("跳过方法 ${function.simpleName.asString()}: ${e.message}")
                null
            }
        }

        return ControllerMetadata(
            originalClassName = className,
            packageName = packageName,
            basePath = basePath,
            methods = methods,
            sourceDescription = "原始Controller: $packageName.$className"
        )
    }

    /**
     * 提取顶层文件元数据
     */
    private fun extractTopLevelFileMetadata(file: KSFile, functions: List<KSFunctionDeclaration>): ControllerMetadata {
        val fileName = file.fileName.removeSuffix(".kt")
        val packageName = file.packageName.asString()
        val basePath = extractFileBasePath(file)

        val methods = functions
            .filter { it.isPublic() && it.hasSpringMvcAnnotation() && it.validate() }
            .mapNotNull { function ->
                try {
                    logger.info("处理顶层方法: ${function.simpleName.asString()}")
                    extractMethodMetadata(function, basePath)
                } catch (e: Exception) {
                    logger.warn("跳过顶层方法 ${function.simpleName.asString()}: ${e.message}")
                    null
                }
            }

        return ControllerMetadata(
            originalClassName = fileName,
            packageName = packageName,
            basePath = basePath,
            methods = methods,
            generatedApiClassName = fileName.toTopLevelApiClassName(),
            sourceDescription = "原始文件: $packageName.${file.fileName}"
        )
    }

    /**
     * 提取方法元数据（只收集字符串信息，避免类型解析问题）
     */
    private fun extractMethodMetadata(function: KSFunctionDeclaration, basePath: String): MethodMetadata {
        val methodName = function.simpleName.asString()

        // 提取HTTP方法和路径
        val httpInfo = extractHttpMapping(function)
        val fullPath = if (basePath.isNotEmpty()) "$basePath${httpInfo.path}" else httpInfo.path

        // 提取参数（只收集字符串信息）
        val parameters = function.parameters.map { param ->
            try {
                val paramName = param.name?.asString() ?: ""

                // 使用类型映射管理器进行类型转换
                val typeString = TypeMappingManager.mapParameterType(param)

                ParameterMetadata(
                    name = paramName,
                    typeString = typeString,
                    isRequestBody = hasAnnotation(param, "RequestBody"),
                    isPathVariable = hasAnnotation(param, "PathVariable"),
                    isRequestParam = hasAnnotation(param, "RequestParam"),
                    isRequestPart = hasAnnotation(param, "RequestPart")
                )
            } catch (e: Exception) {
                logger.error("处理参数 ${param.name?.asString()} 时发生严重错误: ${e.message}")
                // 即使发生错误，也要保留参数，使用基本信息
                ParameterMetadata(
                    name = param.name?.asString() ?: "unknownParam",
                    typeString = "Any",
                    isRequestBody = hasAnnotation(param, "RequestBody"),
                    isPathVariable = hasAnnotation(param, "PathVariable"),
                    isRequestParam = hasAnnotation(param, "RequestParam"),
                    isRequestPart = hasAnnotation(param, "RequestPart")
                )
            }
        }

        // 使用类型映射管理器进行返回类型转换
        val returnTypeString = TypeMappingManager.mapReturnType(function)

        return MethodMetadata(
            name = methodName,
            httpMethod = httpInfo.method,
            path = fullPath,
            parameters = parameters,
            returnTypeString = returnTypeString
        )
    }

    private fun extractHttpMapping(function: KSFunctionDeclaration): HttpMappingInfo {
        val annotations = function.annotations.toList()

        // 检查各种HTTP映射注解
        for (annotation in annotations) {
            when (annotation.shortName.asString()) {
                "GetMapping" -> return HttpMappingInfo("GET", getPathFromAnnotation(annotation))
                "PostMapping" -> return HttpMappingInfo("POST", getPathFromAnnotation(annotation))
                "PutMapping" -> return HttpMappingInfo("PUT", getPathFromAnnotation(annotation))
                "DeleteMapping" -> return HttpMappingInfo("DELETE", getPathFromAnnotation(annotation))
                "PatchMapping" -> return HttpMappingInfo("PATCH", getPathFromAnnotation(annotation))
                "RequestMapping" -> {
                    val method = annotation.arguments.find { it.name?.asString() == "method" }
                        ?.value?.toString()?.substringAfter(".")?.removeSurrounding("\"") ?: "GET"
                    return HttpMappingInfo(method, getPathFromAnnotation(annotation))
                }
            }
        }

        return HttpMappingInfo("GET", "")
    }

    private fun getPathFromAnnotation(annotation: KSAnnotation): String {
        val pathValue = annotation.arguments.find {
            it.name?.asString() == "value" || it.name?.asString() == "path"
        }?.value?.toString() ?: ""

        // 清理路径值，移除多余的引号和方括号
        return pathValue
            .removeSurrounding("\"")
            .removeSurrounding("[", "]")
            .removeSurrounding("\"")
            .trim()
    }

    private fun hasAnnotation(param: KSValueParameter, annotationName: String): Boolean {
        return param.annotations.any { it.shortName.asString() == annotationName }
    }

    /**
     * 提取文件级 RequestMapping 基础路径
     */
    private fun extractFileBasePath(file: KSFile?): String {
        if (file == null) {
            return ""
        }

        val annotation = file.annotations.firstOrNull { fileAnnotation ->
            val qualifiedName = fileAnnotation.annotationType.resolve().declaration.qualifiedName?.asString()
            qualifiedName == FILE_REQUEST_MAPPING_ANNOTATION ||
                qualifiedName == "org.springframework.web.bind.annotation.RequestMapping" ||
                fileAnnotation.shortName.asString() == "RequestMapping"
        } ?: return ""

        return getPathFromAnnotation(annotation)
    }

    /**
     * 提取类型字符串（安全的字符串提取，遇到 Jimmer 实体自动转换为同构体）
     */
    private fun extractTypeString(type: KSType): String {
        try {
            val declaration = type.declaration
            val qualifiedName = declaration.qualifiedName?.asString()

            // 检查是否为 Jimmer 实体，如果是则转换为同构体类型
            if (isJimmerEntity(declaration)) {
                val entitySimpleName = declaration.simpleName.asString()
                return "site.addzero.generated.isomorphic.${entitySimpleName}Iso"
            }

            // 特殊类型映射
            val mappedType = mapSpecialTypes(qualifiedName ?: "")
            if (mappedType != null) {
                return mappedType
            }

            return when {
                // 处理泛型类型
                type.arguments.isNotEmpty() -> {
                    val baseType = qualifiedName ?: ""
                    val mappedBaseType = mapSpecialTypes(baseType) ?: baseType
                    val typeArgs = type.arguments.joinToString(", ") { arg ->
                        when (val argType = arg.type?.resolve()) {
                            null -> "*"
                            else -> extractTypeString(argType)
                        }
                    }
                    "$mappedBaseType<$typeArgs>"
                }
                // 处理基础类型
                else -> qualifiedName ?: declaration.simpleName.asString()
            }
        } catch (e: Exception) {
            logger.error("获取类型名称时发生错误: ${e.message}")
            // 回退到简单名称
            return type.declaration.simpleName.asString()
        }
    }


    /**
     * 映射特殊类型
     */
    private fun mapSpecialTypes(typeName: String): String? {
        return when (typeName) {
            // Spring MultipartFile -> Ktor MultiPartFormDataContent
            "org.springframework.web.multipart.MultipartFile",
            "MultipartFile" -> "io.ktor.client.request.forms.MultiPartFormDataContent"

            // Spring ResponseEntity -> 直接使用泛型参数类型
            "org.springframework.http.ResponseEntity",
            "ResponseEntity" -> null // 需要特殊处理，提取泛型参数

            // 其他Spring类型映射
            "org.springframework.data.domain.Page" -> "kotlin.collections.List" // Page -> List
            "org.springframework.data.domain.Pageable" -> null // Pageable通常不需要在客户端

            else -> null
        }
    }

    /**
     * 转换原始类型字符串为合适的 Kotlin 类型
     * 处理无法解析的类型，尝试转换为 Iso 类型或保持原样
     */
    private fun convertRawTypeString(rawTypeString: String): String {
        // 移除泛型参数，获取基础类型名
        val baseTypeName = rawTypeString.substringBefore('<').trim()

        // 检查是否包含非ASCII字符（如中文），如果是则使用 Any 类型
        if (baseTypeName.any { !it.isLetterOrDigit() && it != '.' && it != '_' && it != '$' }) {
            logger.warn("类型名称包含特殊字符，使用 Any 类型: $baseTypeName")
            return "kotlin.Any"
        }

        // 检查是否是已知的 DTO 类型，转换为 Iso 类型
        return when {
            // 如果是以 DTO 结尾的类型，转换为 Iso 类型
            baseTypeName.endsWith("DTO") -> {
                val entityName = baseTypeName.removeSuffix("DTO")
                "site.addzero.generated.isomorphic.${entityName}Iso"
            }

            // 如果是以 Response 结尾的类型，转换为 Iso 类型
            baseTypeName.endsWith("Response") -> {
                val entityName = baseTypeName.removeSuffix("Response")
                "site.addzero.generated.isomorphic.${entityName}Iso"
            }

            // 如果包含包名，提取简单类名并转换
            baseTypeName.contains('.') -> {
                val simpleName = baseTypeName.substringAfterLast('.')
                when {
                    simpleName.endsWith("DTO") -> {
                        val entityName = simpleName.removeSuffix("DTO")
                        "site.addzero.generated.isomorphic.${entityName}Iso"
                    }

                    simpleName.endsWith("Response") -> {
                        val entityName = simpleName.removeSuffix("Response")
                        "site.addzero.generated.isomorphic.${entityName}Iso"
                    }

                    else -> rawTypeString // 保持原样
                }
            }

            // 其他情况保持原样
            else -> rawTypeString
        }
    }

    /**
     * 处理返回类型，特别处理ResponseEntity
     */
    private fun processReturnType(type: KSType): String {
        val declaration = type.declaration
        val qualifiedName = declaration.qualifiedName?.asString()

        return when (qualifiedName) {
            "org.springframework.http.ResponseEntity" -> {
                // ResponseEntity<T> -> T
                if (type.arguments.isNotEmpty()) {
                    val firstArg = type.arguments.first()
                    firstArg.type?.resolve()?.let { argType ->
                        extractTypeString(argType)
                    } ?: "kotlin.Any"
                } else {
                    "kotlin.Any"
                }
            }

            "org.springframework.data.domain.Page" -> {
                // Page<T> -> List<T>
                if (type.arguments.isNotEmpty()) {
                    val firstArg = type.arguments.first()
                    firstArg.type?.resolve()?.let { argType ->
                        "kotlin.collections.List<${extractTypeString(argType)}>"
                    } ?: "kotlin.collections.List<kotlin.Any>"
                } else {
                    "kotlin.collections.List<kotlin.Any>"
                }
            }

            else -> extractTypeString(type)
        }
    }

    private fun generateKtorfitInterface(controllerInfo: ControllerInfo) {
        val apiClassName = controllerInfo.generatedApiClassName
            ?: controllerInfo.originalClassName.replace("Controller", "Api")
        val fileName = "${apiClassName}.kt"

        try {
            // 从SettingContext获取配置
            val outputDir = Settings.apiClientOutputDir


            logger.info("api客户端输出目录为: $outputDir")

            val packageName = Settings.apiClientPackageName

            // 创建输出目录
            val outputDirFile = File(outputDir)
            if (!outputDirFile.exists()) {
                outputDirFile.mkdirs()
                logger.info("Created output directory: $outputDir")
            }

            // 生成文件到源码目录
            val outputFile = File(outputDirFile, fileName)
            val code = generateKtorfitCode(controllerInfo, apiClassName, packageName)

            outputFile.writeText(code)

            logger.info("Generated Ktorfit interface: $packageName.$apiClassName")
            logger.info("Output file: ${outputFile.absolutePath}")

        } catch (e: Exception) {
            logger.error("Failed to generate Ktorfit interface for ${controllerInfo.originalClassName}: ${e.message}")
            // 回退到使用CodeGenerator
            fallbackToCodeGenerator(controllerInfo, apiClassName)
        }
    }

    /**
     * 回退方案：使用CodeGenerator生成到build目录
     */
    private fun fallbackToCodeGenerator(controllerInfo: ControllerInfo, apiClassName: String) {
        try {
            val file = codeGenerator.createNewFile(
                dependencies = Dependencies(false),
                packageName = Settings.apiClientPackageName,
                fileName = apiClassName
            )

            file.use { outputStream ->
                val code = generateKtorfitCode(controllerInfo, apiClassName, Settings.apiClientPackageName)
                outputStream.write(code.toByteArray())
            }

            logger.info("Fallback: Generated Ktorfit interface to build directory: ${Settings.apiClientPackageName}.$apiClassName")
        } catch (e: Exception) {
            logger.error("Fallback generation also failed: ${e.message}")
        }
    }

    /**
     * 生成聚合后的 API 对象。
     */
    private fun generateApiAggregator(metadataList: List<ControllerMetadata>) {
        val generatedApis = metadataList
            .map(::toGeneratedApiDescriptor)
            .distinctBy { it.apiClassName }
            .sortedBy { it.propertyName }
        val bridgeSpec = resolveApiClientBridgeSpec()
        if (bridgeSpec == null && !isApiAggregatorExplicitlyEnabled()) {
            logger.info("未显式配置 API 聚合器或源码级桥接，跳过聚合生成")
            return
        }
        val aggregatorObjectName = resolveApiAggregatorObjectName()

        if (bridgeSpec != null) {
            cleanupApiAggregatorFiles(
                outputDirFile = File(resolveApiAggregatorOutputDir()),
                aggregatorObjectName = aggregatorObjectName,
            )
            generateApiClientBridge(
                bridgeSpec = bridgeSpec,
                generatedApis = generatedApis,
            )
            return
        }

        val aggregatorStyle = parseApiAggregatorStyle(Settings.apiClientAggregatorStyle)
        val outputDirFile = File(resolveApiAggregatorOutputDir())
        val generatedFiles = buildApiAggregatorFiles(
            packageName = Settings.apiClientPackageName,
            aggregatorObjectName = aggregatorObjectName,
            aggregatorStyle = aggregatorStyle,
            generatedApis = generatedApis,
        )

        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs()
        }
        cleanupApiAggregatorFiles(
            outputDirFile = outputDirFile,
            aggregatorObjectName = aggregatorObjectName,
        )

        if (generatedApis.isEmpty()) {
            logger.info("没有生成任何 Ktorfit 接口，跳过 $aggregatorObjectName 生成")
            return
        }

        try {
            generatedFiles.forEach { generatedFile ->
                val outputFile = File(outputDirFile, generatedFile.fileName)
                outputFile.writeText(generatedFile.content)
                logger.info("Generated ${generatedFile.fileName}: ${outputFile.absolutePath}")
            }
        } catch (e: Exception) {
            logger.error("Failed to generate $aggregatorObjectName via file IO: ${e.message}")
            fallbackApiAggregatorToCodeGenerator(
                generatedFiles = generatedFiles,
            )
        }
    }

    /**
     * 清理聚合对象与 Koin 绑定文件，避免生成配置切换后遗留旧文件。
     */
    private fun cleanupApiAggregatorFiles(
        outputDirFile: File,
        aggregatorObjectName: String,
    ) {
        listOf(
            "$aggregatorObjectName.kt",
            "${aggregatorObjectName}Module.kt",
        ).forEach { fileName ->
            val targetFile = File(outputDirFile, fileName)
            if (targetFile.exists()) {
                targetFile.delete()
            }
        }
    }

    /**
     * 聚合入口默认沿用接口输出目录；需要分流时由外部显式指定。
     */
    private fun resolveApiAggregatorOutputDir(): String {
        return Settings.apiClientAggregatorOutputDir
            .takeIf { it.isNotBlank() }
            ?: Settings.apiClientOutputDir
    }

    private fun resolveApiAggregatorObjectName(): String {
        return Settings.apiClientAggregatorObjectName
            .takeIf { it.isNotBlank() }
            ?: "Apis"
    }

    private fun isApiAggregatorExplicitlyEnabled(): Boolean {
        return Settings.apiClientAggregatorObjectName.isNotBlank() ||
            Settings.apiClientAggregatorStyle.isNotBlank() ||
            Settings.apiClientAggregatorOutputDir.isNotBlank()
    }

    /**
     * 回退方案：将 API 聚合对象生成到 build 目录。
     */
    private fun fallbackApiAggregatorToCodeGenerator(
        generatedFiles: List<GeneratedSourceFile>,
    ) {
        try {
            generatedFiles.forEach { generatedFile ->
                codeGenerator.createNewFile(
                    dependencies = Dependencies(false),
                    packageName = Settings.apiClientPackageName,
                    fileName = generatedFile.fileName.removeSuffix(".kt"),
                ).use { outputStream ->
                    outputStream.write(generatedFile.content.toByteArray())
                }
                logger.info(
                    "Fallback: Generated ${generatedFile.fileName} to build directory: " +
                        "${Settings.apiClientPackageName}.${generatedFile.fileName.removeSuffix(".kt")}"
                )
            }
        } catch (e: Exception) {
            logger.error("Fallback ${resolveApiAggregatorObjectName()} generation also failed: ${e.message}")
        }
    }

    /**
     * 生成源码级 API client 桥接文件。
     */
    private fun generateApiClientBridge(
        bridgeSpec: ApiClientBridgeSpec,
        generatedApis: List<GeneratedApiDescriptor>,
    ) {
        val outputDirFile = File(bridgeSpec.outputDir)
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs()
        }

        val outputFile = File(outputDirFile, bridgeSpec.fileName.ensureKtSuffix())
        if (generatedApis.isEmpty()) {
            if (outputFile.exists()) {
                outputFile.delete()
            }
            logger.info("没有生成任何 Ktorfit 接口，删除源码级 API client 桥接: ${outputFile.absolutePath}")
            return
        }

        val code = renderApiClientBridgeCode(
            bridgePackageName = bridgeSpec.packageName,
            bridgeModuleClassName = bridgeSpec.fileName.ensureKtSuffix().removeKtSuffix(),
            generatedApiPackageName = Settings.apiClientPackageName,
            generatedApis = generatedApis,
        )
        outputFile.writeText(code)
        logger.info("Generated API client bridge: ${outputFile.absolutePath}")
    }

    /**
     * 按配置解析源码级 API client 桥接输出。
     */
    private fun resolveApiClientBridgeSpec(): ApiClientBridgeSpec? {
        val packageName = options[API_CLIENT_BRIDGE_PACKAGE_NAME_OPTION].orEmpty().trim()
        val outputDir = options[API_CLIENT_BRIDGE_OUTPUT_DIR_OPTION].orEmpty().trim()
        val fileName = options[API_CLIENT_BRIDGE_FILE_NAME_OPTION].orEmpty().trim()
        if (packageName.isBlank() || outputDir.isBlank() || fileName.isBlank()) {
            return null
        }
        return ApiClientBridgeSpec(
            packageName = packageName,
            outputDir = outputDir,
            fileName = fileName,
        )
    }

    private fun generateKtorfitCode(controllerInfo: ControllerInfo, apiClassName: String, packageName: String): String {
        // 收集所有需要导入的类型
        val imports = collectImports(controllerInfo)

        return generateCodeWithTemplate(controllerInfo, apiClassName, packageName, imports)
    }


    /**
     * 收集需要导入的类型（安全版本，过滤错误类型）
     */
    private fun collectImports(controllerInfo: ControllerInfo): Set<String> {
        val imports = mutableSetOf<String>()

        // 添加Ktorfit相关导入
        imports.add("de.jensklingenberg.ktorfit.http.*")

        // 检查是否需要Ktor相关导入
        var needsMultiPartImport = false

        // 收集方法中使用的类型
        controllerInfo.methods.forEach { method ->
            // 收集返回类型（安全处理）
            if (method.returnType != "Unit" && method.returnType.contains(".")) {
                val baseType = method.returnType.substringBeforeLast("<")
                if (isValidTypeForImport(baseType)) {
                    imports.add(baseType)
                }

                // 检查是否包含MultiPartFormDataContent
                if (method.returnType.contains("MultiPartFormDataContent")) {
                    needsMultiPartImport = true
                }
            }

            // 收集参数类型（安全处理）
            method.parameters.forEach { param ->
                if (param.type.contains(".")) {
                    val baseType = param.type.substringBeforeLast("<")
                    if (isValidTypeForImport(baseType)) {
                        imports.add(baseType)
                    }
                }

                // 检查是否包含MultiPartFormDataContent
                if (param.type.contains("MultiPartFormDataContent")) {
                    needsMultiPartImport = true
                }
            }
        }

        // 添加Ktor MultiPart相关导入
        if (needsMultiPartImport) {
            imports.add("io.ktor.client.request.forms.MultiPartFormDataContent")
            imports.add("io.ktor.client.request.forms.formData")
            imports.add("io.ktor.http.Headers")
            imports.add("io.ktor.http.HttpHeaders")
        }

        return imports
    }

    /**
     * 检查类型是否适合导入
     */
    private fun isValidTypeForImport(typeName: String): Boolean {
        return !typeName.startsWith("kotlin.") &&
                !typeName.contains("<ERROR") &&
                !typeName.any { !it.isLetterOrDigit() && it != '.' && it != '_' && it != '$' } &&
                typeName.isNotBlank()
    }

    /**
     * 使用模板字符串生成代码
     */
    private fun generateCodeWithTemplate(
        controllerInfo: ControllerInfo,
        apiClassName: String,
        packageName: String,
        imports: Set<String>
    ): String {
        val importStatements = imports.joinToString("\n") { "import $it" }
        val methodsCode = controllerInfo.methods.joinToString("\n\n") { method ->
            generateMethodWithTemplate(method)
        }

        return """
package $packageName

$importStatements

/**
 * ${controllerInfo.sourceDescription}
 * 基础路径: ${controllerInfo.basePath}
 */
interface $apiClassName {

$methodsCode

}
        """.trimIndent()
    }

    /**
     * 使用模板字符串生成单个方法
     */
    private fun generateMethodWithTemplate(method: MethodInfo): String {
        val methodDoc = generateMethodDocumentation(method)
        val httpAnnotation = generateHttpAnnotation(method)
        val headersAnnotation = generateHeadersAnnotation(method)
        val methodSignature = generateMethodSignature(method)

        return """
$methodDoc
$httpAnnotation
$headersAnnotation$methodSignature
        """.trimIndent()
    }

    /**
     * 生成Headers注解
     */
    private fun generateHeadersAnnotation(method: MethodInfo): String {
        return when {
            // 检查是否包含文件上传参数或@RequestPart参数
            method.parameters.any {
                it.type.contains("MultiPartFormDataContent") || it.isRequestPart
            } -> {
                "    @Headers(\"Content-Type: multipart/form-data\")\n"
            }
            // 仅在存在请求体时声明 JSON 内容类型，避免无 body 的 POST 被误标注
            method.parameters.any { it.isRequestBody } -> {
                "    @Headers(\"Content-Type: application/json\")\n"
            }

            else -> ""
        }
    }

    /**
     * 生成方法文档
     */
    private fun generateMethodDocumentation(method: MethodInfo): String {
        val paramDocs = if (method.parameters.isNotEmpty()) {
            val paramLines = method.parameters.joinToString("\n") { param ->
                val paramType = when {
                    param.isRequestBody -> "RequestBody"
                    param.isPathVariable -> "PathVariable"
                    param.isRequestParam -> "RequestParam"
                    param.isRequestPart -> "RequestPart"
                    else -> "Query"
                }
                "     *   - ${param.name}: ${param.type} ($paramType)"
            }
            "\n     * 参数:\n$paramLines"
        } else {
            ""
        }

        return """
    /**
     * ${method.name}
     * HTTP方法: ${method.httpMethod}
     * 路径: ${method.path}$paramDocs
     * 返回类型: ${method.returnType}
     */""".trimIndent()
    }

    /**
     * 生成HTTP注解
     */
    private fun generateHttpAnnotation(method: MethodInfo): String {
        return if (method.path.isNotEmpty()) {
            "    @${method.httpMethod}(\"${method.path}\")"
        } else {
            "    @${method.httpMethod}"
        }
    }

    /**
     * 生成方法签名
     */
    private fun generateMethodSignature(method: MethodInfo): String {
        val parameters = method.parameters.joinToString(",\n        ") { param ->
            val annotation = when {
                param.isRequestBody -> "@Body"
                param.isRequestPart -> "@Body"
                param.isPathVariable -> "@Path(\"${param.name}\")"
                param.isRequestParam -> "@Query(\"${param.name}\")"
                else -> "@Query(\"${param.name}\")"
            }
            "$annotation ${param.name}: ${param.type}"
        }

        val paramString = if (parameters.isNotEmpty()) {
            "\n        $parameters\n    "
        } else {
            ""
        }

        return "    suspend fun ${method.name}($paramString): ${method.returnType}"
    }


    // 数据类定义
    data class ControllerInfo(
        val originalClassName: String,
        val packageName: String,
        val basePath: String,
        val generatedApiClassName: String?,
        val sourceDescription: String,
        val methods: List<MethodInfo>
    )

    data class MethodInfo(
        val name: String,
        val httpMethod: String,
        val path: String,
        val parameters: List<ParameterInfo>,
        val returnType: String
    )

    data class ParameterInfo(
        val name: String,
        val type: String,
        val isRequestBody: Boolean,
        val isPathVariable: Boolean,
        val isRequestParam: Boolean,
        val isRequestPart: Boolean
    )

    data class HttpMappingInfo(
        val method: String,
        val path: String
    )
}

/**
 * KSP处理器提供者
 */
class ControllerApiProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ControllerApiProcessor(environment.codeGenerator, environment.logger, environment.options)
    }
}

/**
 * 扩展函数：检查函数是否为public
 */
private fun KSFunctionDeclaration.isPublic(): Boolean {
    return modifiers.contains(Modifier.PUBLIC) ||
            (!modifiers.contains(Modifier.PRIVATE) && !modifiers.contains(Modifier.PROTECTED))
}

/**
 * 扩展函数：检查函数是否有Spring MVC注解
 */
private fun KSFunctionDeclaration.hasSpringMvcAnnotation(): Boolean {
    val springMvcAnnotations = setOf(
        "GetMapping",
        "PostMapping",
        "PutMapping",
        "DeleteMapping",
        "PatchMapping",
        "RequestMapping"
    )

    return annotations.any { annotation ->
        springMvcAnnotations.contains(annotation.shortName.asString())
    }
}

// ===== 元数据类（第一阶段收集的数据）=====
data class ParameterMetadata(
    val name: String,
    val typeString: String,
    val isRequestBody: Boolean = false,
    val isPathVariable: Boolean = false,
    val isRequestParam: Boolean = false,
    val isRequestPart: Boolean = false
)

data class MethodMetadata(
    val name: String,
    val httpMethod: String,
    val path: String,
    val parameters: List<ParameterMetadata>,
    val returnTypeString: String
)

data class ControllerMetadata(
    val originalClassName: String,
    val packageName: String,
    val basePath: String,
    val methods: List<MethodMetadata>,
    val generatedApiClassName: String? = null,
    val sourceDescription: String
)

private data class TopLevelFileRound(
    val file: KSFile,
    val functions: MutableList<KSFunctionDeclaration> = mutableListOf(),
    var hasInvalidSymbols: Boolean = false
)

internal data class GeneratedApiDescriptor(
    val apiClassName: String,
    val propertyName: String
)

internal data class GeneratedSourceFile(
    val fileName: String,
    val content: String,
)

internal data class ApiClientBridgeSpec(
    val packageName: String,
    val outputDir: String,
    val fileName: String,
)

internal enum class ApiAggregatorStyle {
    KOIN,
    SINGLETON,
}

internal fun toGeneratedApiDescriptor(metadata: ControllerMetadata): GeneratedApiDescriptor {
    val apiClassName = metadata.generatedApiClassName
        ?: metadata.originalClassName.replace("Controller", "Api")
    return GeneratedApiDescriptor(
        apiClassName = apiClassName,
        propertyName = apiClassName.replaceFirstChar { it.lowercase() },
    )
}

internal fun renderApiAggregatorCode(
    packageName: String,
    aggregatorObjectName: String,
    aggregatorStyle: ApiAggregatorStyle,
    generatedApis: List<GeneratedApiDescriptor>
): String {
    val generatedApiImports = generatedApis.joinToString("\n") { api ->
        "import $packageName.${api.apiClassName}"
    }
    val generatedFactoryImports = generatedApis.joinToString("\n") { api ->
        "import $packageName.create${api.apiClassName}"
    }
    val serviceMembers = when (aggregatorStyle) {
        ApiAggregatorStyle.KOIN -> {
            generatedApis.joinToString("\n\n") { api ->
                """
                |    /**
                |     * 创建 ${api.apiClassName} 服务实例
                |     */
                |    fun ${api.propertyName}(ktorfit: Ktorfit): ${api.apiClassName} {
                |        return ktorfit.create${api.apiClassName}()
                |    }
                """.trimMargin()
            }
        }
        ApiAggregatorStyle.SINGLETON -> {
            val serviceProperties = generatedApis.joinToString("\n\n") { api ->
                """
                |    /**
                |     * ${api.apiClassName} 服务实例
                |     */
                |    val ${api.propertyName}: ${api.apiClassName}
                |        get() = ktorfit().create${api.apiClassName}()
                """.trimMargin()
            }
            val wiringCode = """
            |    private var currentKtorfit: Ktorfit? = null
            |
            |    fun configure(ktorfit: Ktorfit) {
            |        currentKtorfit = ktorfit
            |    }
            |
            |    private fun ktorfit(): Ktorfit {
            |        return currentKtorfit
            |            ?: error("$aggregatorObjectName 尚未配置 Ktorfit，请先调用 $aggregatorObjectName.configure(ktorfit)")
            |    }
            """.trimMargin()
            listOf(wiringCode, serviceProperties)
                .filter(String::isNotBlank)
                .joinToString("\n\n")
        }
    }

    return """
        |package $packageName
        |
        |import de.jensklingenberg.ktorfit.Ktorfit
        |$generatedApiImports
        |$generatedFactoryImports
        |
        |/**
        | * 聚合后的 Ktorfit 服务提供者
        | *
        | * 仅聚合 controller2api 生成的接口，不扫描手写接口。
        | */
        |object $aggregatorObjectName {
        |$serviceMembers
        |}
    """.trimMargin()
}

internal fun renderApiAggregatorModuleCode(
    packageName: String,
    aggregatorObjectName: String,
    generatedApis: List<GeneratedApiDescriptor>,
): String {
    val moduleClassName = "${aggregatorObjectName}Module"
    val providers = generatedApis.joinToString("\n\n") { api ->
        """
        |    @Single
        |    fun ${api.propertyName}(ktorfit: Ktorfit): ${api.apiClassName} {
        |        return $aggregatorObjectName.${api.propertyName}(ktorfit)
        |    }
        """.trimMargin()
    }

    return """
        |package $packageName
        |
        |import de.jensklingenberg.ktorfit.Ktorfit
        |import org.koin.core.annotation.Configuration
        |import org.koin.core.annotation.Module
        |import org.koin.core.annotation.Single
        |import $packageName.*
        |
        |/**
        | * 为 controller2api 生成的接口补充可自注册的 Koin 注入入口。
        | */
        |@Module
        |@Configuration
        |class $moduleClassName {
        |$providers
        |}
    """.trimMargin()
}

internal fun buildApiAggregatorFiles(
    packageName: String,
    aggregatorObjectName: String,
    aggregatorStyle: ApiAggregatorStyle,
    generatedApis: List<GeneratedApiDescriptor>,
): List<GeneratedSourceFile> {
    val files = mutableListOf(
        GeneratedSourceFile(
            fileName = "$aggregatorObjectName.kt",
            content = renderApiAggregatorCode(
                packageName = packageName,
                aggregatorObjectName = aggregatorObjectName,
                aggregatorStyle = aggregatorStyle,
                generatedApis = generatedApis,
            ),
        )
    )

    if (aggregatorStyle == ApiAggregatorStyle.KOIN) {
        files += GeneratedSourceFile(
            fileName = "${aggregatorObjectName}Module.kt",
            content = renderApiAggregatorModuleCode(
                packageName = packageName,
                aggregatorObjectName = aggregatorObjectName,
                generatedApis = generatedApis,
            ),
        )
    }

    return files
}

internal fun renderApiClientBridgeCode(
    bridgePackageName: String,
    bridgeModuleClassName: String,
    generatedApiPackageName: String,
    generatedApis: List<GeneratedApiDescriptor>,
): String {
    val generatedApiImports = generatedApis.joinToString("\n") { api ->
        "import $generatedApiPackageName.${api.apiClassName}"
    }
    val generatedFactoryImports = generatedApis.joinToString("\n") { api ->
        "import $generatedApiPackageName.create${api.apiClassName}"
    }
    val providers = generatedApis.joinToString("\n\n") { api ->
        """
        |    @Single
        |    public fun ${api.propertyName}(ktorfit: Ktorfit): ${api.apiClassName} {
        |        return ktorfit.create${api.apiClassName}()
        |    }
        """.trimMargin()
    }

    return """
        |package $bridgePackageName
        |
        |import de.jensklingenberg.ktorfit.Ktorfit
        |import org.koin.core.annotation.Configuration
        |import org.koin.core.annotation.Module
        |import org.koin.core.annotation.Single
        |$generatedApiImports
        |$generatedFactoryImports
        |
        |/**
        | * controller2api 生成的 Ktorfit API 客户端桥接入口。
        | */
        |@Module
        |@Configuration
        |public class $bridgeModuleClassName {
        |$providers
        |}
    """.trimMargin()
}

internal fun parseApiAggregatorStyle(raw: String?): ApiAggregatorStyle {
    return when (raw?.trim()?.lowercase()) {
        null, "", "koin" -> ApiAggregatorStyle.KOIN
        "singleton" -> ApiAggregatorStyle.SINGLETON
        else -> ApiAggregatorStyle.KOIN
    }
}

private fun String.ensureKtSuffix(): String {
    return if (endsWith(".kt")) {
        this
    } else {
        "$this.kt"
    }
}

private fun String.removeKtSuffix(): String {
    return removeSuffix(".kt")
}

private fun KSFunctionDeclaration.signatureKey(): String {
    val parentName = parentDeclaration?.qualifiedName?.asString()
        ?: packageName.asString()
    val parameterSignature = parameters.joinToString(",") { parameter ->
        parameter.type.toString()
    }
    return "$parentName#${simpleName.asString()}($parameterSignature)"
}

private fun String.toTopLevelApiClassName(): String {
    return when {
        endsWith("Api") -> this
        endsWith("Controller") -> removeSuffix("Controller") + "Api"
        else -> "${this}Api"
    }
}

private fun isJimmerEntity(declaration: KSDeclaration): Boolean {
    return try {
        declaration.annotations.any { annotation ->
            annotation.shortName.asString() == "Entity" &&
                annotation.annotationType.resolve().declaration.qualifiedName?.asString()?.contains("jimmer") == true
        }
    } catch (e: Exception) {
        false
    }
}
