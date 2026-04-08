package site.addzero.device.protocol.modbus.ksp.core

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter

/**
 * KSP 符号采集器。
 */
class ModbusSymbolCollector(
    private val logger: KSPLogger,
) {
    fun collect(
        resolver: Resolver,
        transport: ModbusTransportKind,
        contractPackages: List<String>,
    ): List<CollectedModbusService> =
        resolver.collectDeclarationsFromPackages(contractPackages)
            .filterIsInstance<KSClassDeclaration>()
            .filter { declaration ->
                declaration.classKind == ClassKind.INTERFACE &&
                    declaration.hasProtocolOperations() &&
                    declaration.matchesTransport(transport)
            }
            .mapNotNull { declaration -> declaration.toCollectedService(transport) }
            .toList()

    @OptIn(KspExperimental::class)
    private fun Resolver.collectDeclarationsFromPackages(contractPackages: List<String>): Sequence<KSDeclaration> =
        contractPackages
            .asSequence()
            .flatMap { contractPackage -> getDeclarationsFromPackage(contractPackage) }
            .distinctBy { declaration -> declaration.qualifiedName?.asString().orEmpty() }

    private fun KSClassDeclaration.toCollectedService(transport: ModbusTransportKind): CollectedModbusService? {
        if (classKind != ClassKind.INTERFACE) {
            logger.error("只有接口可以作为 Modbus 生成源：${qualifiedName?.asString()}", this)
            return null
        }

        val interfaceName = simpleName.asString()
        val serviceId = ModbusContractDefaultsResolver.defaultServiceId(interfaceName)
        val interfaceDoc = ModbusKdocParser.parse(docString, fallbackSummary = "设备服务接口。")
        val summary = interfaceDoc.summary
        val basePath = "/api/modbus"
        val requestPrefix = interfaceName + transport.transportId.replaceFirstChar(Char::uppercase)
        val functions =
            declarations
                .filterIsInstance<KSFunctionDeclaration>()
                .filter { function -> function.isCandidateContractMethod() }
                .toList()
        val operations =
            functions
                .filter { function -> function.isLowLevelOperation() }
                .mapNotNull { function -> function.toOperationModel(requestPrefix) }
                .toList()
                .let(ModbusContractDefaultsResolver::resolveOperationIdsAndQuantities)
        val workflows =
            functions
                .filter { function -> function.isFirmwareWorkflow() }
                .mapNotNull { function -> function.toWorkflowModel(requestPrefix) }
                .toList()

        return CollectedModbusService(
            model =
                ModbusServiceModel(
                    interfacePackage = packageName.asString(),
                    interfaceSimpleName = interfaceName,
                    interfaceQualifiedName = qualifiedName?.asString().orEmpty(),
                    serviceId = serviceId,
                    summary = summary,
                    basePath = normalizeBasePath(basePath),
                    transport = transport,
                    doc = interfaceDoc,
                    operations = operations,
                    workflows = workflows,
                ),
            originatingFiles = listOfNotNull(containingFile),
        )
    }

    private fun KSFunctionDeclaration.toOperationModel(requestPrefix: String): ModbusOperationModel? {
        val operationAnnotation = requireAnnotation(ModbusAnnotationNames.operation)
        val methodName = simpleName.asString()
        val doc = ModbusKdocParser.parse(docString, fallbackSummary = "执行 $methodName 操作。")
        val parameters =
            parameters
                .mapNotNull { parameter -> parameter.toParameterModel(doc.parameterDocs[parameter.name?.asString().orEmpty()].orEmpty()) }
                .sortedBy(ModbusParameterModel::order)
                .withSequentialOffsets()
        val rawReturnType = toReturnTypeModel(operationAnnotation) ?: return null
        val functionCodeName =
            try {
                ModbusContractDefaultsResolver.resolveFunctionCodeName(
                    explicitFunctionCodeName = operationAnnotation?.enumArg("functionCode").orEmpty(),
                    parameters = parameters,
                    returnType = rawReturnType,
                )
            } catch (exception: IllegalArgumentException) {
                logger.error(
                    "操作 ${qualifiedName?.asString().orEmpty()} 无法自动推导 functionCode：${exception.message}",
                    this,
                )
                return null
            }
        val resolvedParameters = parameters.resolveAutoCodecNames(functionCodeName)
        val returnType = rawReturnType.resolveAutoCodecNames(functionCodeName)
        val requestClassName =
            requestPrefix + methodName.replaceFirstChar(Char::uppercase) + "Request"

        return ModbusOperationModel(
            methodName = methodName,
            operationId = "",
            functionCodeName = functionCodeName,
            address = operationAnnotation?.intArg("address") ?: -1,
            quantity = -1,
            requestClassName = requestClassName,
            requestQualifiedName = "${requestPrefix.substringBeforeLast("Request", requestPrefix)}.$requestClassName",
            parameters = resolvedParameters,
            returnType = returnType,
            doc = doc,
        )
    }

    private fun KSFunctionDeclaration.toReturnTypeModel(operationAnnotation: KSAnnotation?): ModbusReturnTypeModel? {
        val resolved = returnType?.resolve()
        val declaration = resolved?.declaration as? KSClassDeclaration
        val qualifiedName = declaration?.qualifiedName?.asString().orEmpty()
        val simpleName = declaration?.simpleName?.asString().orEmpty()
        val returnCodecName = operationAnnotation?.enumArg("returnCodec").orEmpty().ifBlank { "AUTO" }
        val returnLength = (operationAnnotation?.intArg("returnLength") ?: 1).coerceAtLeast(1)
        return when (qualifiedName) {
            "kotlin.Unit" ->
                ModbusReturnTypeModel(
                    qualifiedName = qualifiedName,
                    simpleName = simpleName,
                    kind = ModbusReturnKind.UNIT,
                )

            "kotlin.Boolean" ->
                ModbusReturnTypeModel(
                    qualifiedName = qualifiedName,
                    simpleName = simpleName,
                    kind = ModbusReturnKind.BOOLEAN,
                    valueKind = ModbusValueKind.BOOLEAN,
                    codecName = returnCodecName,
                    length = returnLength,
                    registerWidth = registerWidth(returnCodecName, returnLength),
                )

            "kotlin.Int" ->
                ModbusReturnTypeModel(
                    qualifiedName = qualifiedName,
                    simpleName = simpleName,
                    kind = ModbusReturnKind.INT,
                    valueKind = ModbusValueKind.INT,
                    codecName = returnCodecName,
                    length = returnLength,
                    registerWidth = registerWidth(returnCodecName, returnLength),
                )

            "kotlin.String" ->
                ModbusReturnTypeModel(
                    qualifiedName = qualifiedName,
                    simpleName = simpleName,
                    kind = ModbusReturnKind.STRING,
                    valueKind = ModbusValueKind.STRING,
                    codecName = returnCodecName,
                    length = returnLength,
                    registerWidth = registerWidth(returnCodecName, returnLength),
                )

            "site.addzero.device.protocol.modbus.model.ModbusCommandResult" ->
                ModbusReturnTypeModel(
                    qualifiedName = qualifiedName,
                    simpleName = simpleName,
                    kind = ModbusReturnKind.COMMAND_RESULT,
                )

            else -> {
                val classDeclaration = declaration ?: run {
                    logger.error("无法解析返回类型：${qualifiedName.ifBlank { "<unknown>" }}", this)
                    return null
                }
                val properties = classDeclaration.toPropertyModels()
                val typeDoc = ModbusKdocParser.parse(classDeclaration.docString, fallbackSummary = "")
                ModbusReturnTypeModel(
                    qualifiedName = qualifiedName,
                    simpleName = simpleName,
                    kind = ModbusReturnKind.DTO,
                    docSummary = typeDoc.summary,
                    properties = properties,
                )
            }
        }
    }

    private fun KSFunctionDeclaration.toWorkflowModel(requestPrefix: String): ModbusWorkflowModel? {
        val methodName = simpleName.asString()
        val doc = ModbusKdocParser.parse(docString, fallbackSummary = "执行 $methodName 工作流。")
        val bytesParameter = parameters.singleOrNull()
        if (bytesParameter == null) {
            logger.error("高层工作流 $methodName 必须且只能包含一个 bytes: ByteArray 参数。", this)
            return null
        }
        val parameterName = bytesParameter.name?.asString().orEmpty()
        val parameterType = bytesParameter.type.resolve().declaration.qualifiedName?.asString().orEmpty()
        if (parameterName != "bytes" || parameterType != "kotlin.ByteArray") {
            logger.error("高层工作流 $methodName 必须声明为 suspend fun $methodName(bytes: ByteArray): FlashResult。", this)
            return null
        }
        val returnType = toReturnTypeModel(operationAnnotation = null) ?: return null
        if (returnType.kind != ModbusReturnKind.DTO || returnType.simpleName != "FlashResult") {
            logger.error("高层工作流 $methodName 的返回类型必须是 FlashResult DTO。", this)
            return null
        }
        val requestClassName = requestPrefix + methodName.replaceFirstChar(Char::uppercase) + "Request"
        return ModbusWorkflowModel(
            kind = ModbusWorkflowKind.FLASH_FIRMWARE,
            methodName = methodName,
            workflowId = ModbusContractDefaultsResolver.defaultOperationId(methodName),
            requestClassName = requestClassName,
            requestQualifiedName = "${requestPrefix.substringBeforeLast("Request", requestPrefix)}.$requestClassName",
            bytesParameterName = parameterName,
            returnType = returnType,
            doc = doc,
            startMethodName = "firmwareStart",
            chunkMethodName = "firmwareChunk",
            commitMethodName = "firmwareCommit",
            resetMethodName = "resetDevice",
        )
    }

    private fun KSClassDeclaration.toPropertyModels(): List<ModbusPropertyModel> {
        val propertyByName =
            declarations
                .filterIsInstance<KSPropertyDeclaration>()
                .associateBy { property -> property.simpleName.asString() }

        val orderedPropertyNames =
            primaryConstructor?.parameters
                ?.mapNotNull { parameter -> parameter.name?.asString() }
                ?.filter(propertyByName::containsKey)
                ?: propertyByName.keys.sorted()

        return orderedPropertyNames
            .mapNotNull { propertyName ->
                val property = propertyByName[propertyName] ?: return@mapNotNull null
                property.toPropertyModel()
            }
            .withSequentialFieldOffsets()
    }

    private fun KSPropertyDeclaration.toPropertyModel(): ModbusPropertyModel? {
        val resolvedType = type.resolve()
        val qualifiedType = resolvedType.declaration.qualifiedName?.asString().orEmpty()
        val valueKind = resolveScalarType(qualifiedType)
        if (valueKind == null) {
            logger.error("暂不支持的 DTO 字段类型：$qualifiedType", this)
            return null
        }
        val fieldAnnotation = requireAnnotation(ModbusAnnotationNames.field)
        val fieldModel =
            fieldAnnotation?.let { annotation ->
                val codecName = annotation.enumArg("codec")
                ModbusFieldModel(
                    codecName = codecName,
                    registerOffset = annotation.intArg("registerOffset"),
                    bitOffset = annotation.intArg("bitOffset"),
                    length = annotation.intArg("length"),
                    registerWidth = registerWidth(codecName, annotation.intArg("length")),
                )
            }

        return ModbusPropertyModel(
            name = simpleName.asString(),
            qualifiedType = qualifiedType,
            valueKind = valueKind,
            field = fieldModel,
            doc = ModbusKdocParser.parse(docString, fallbackSummary = "${simpleName.asString()} 字段。").summary,
        )
    }

    private fun KSValueParameter.toParameterModel(docFallback: String): ModbusParameterModel? {
        val paramAnnotation = requireAnnotation(ModbusAnnotationNames.param) ?: run {
            logger.error("Modbus 操作参数必须显式声明 @ModbusParam：${name?.asString().orEmpty()}", this)
            return null
        }
        val qualifiedType = type.resolve().declaration.qualifiedName?.asString().orEmpty()
        val valueKind = resolveScalarType(qualifiedType)
        if (valueKind == null) {
            logger.error("暂不支持的参数类型：$qualifiedType", this)
            return null
        }
        val codecName = paramAnnotation.enumArg("codec")
        return ModbusParameterModel(
            name = name?.asString().orEmpty(),
            qualifiedType = qualifiedType,
            valueKind = valueKind,
            order = paramAnnotation.intArg("order"),
            codecName = codecName,
            registerOffset = paramAnnotation.intArg("registerOffset"),
            bitOffset = paramAnnotation.intArg("bitOffset"),
            registerWidth = registerWidth(codecName, paramAnnotation.intArg("length")),
            doc = docFallback.ifBlank { "${name?.asString().orEmpty()} 参数。" },
        )
    }

    private fun resolveScalarType(qualifiedType: String): ModbusValueKind? =
        when (qualifiedType) {
            "kotlin.Boolean" -> ModbusValueKind.BOOLEAN
            "kotlin.Int" -> ModbusValueKind.INT
            "kotlin.String" -> ModbusValueKind.STRING
            else -> null
        }

    private fun KSAnnotated.requireAnnotation(qualifiedName: String): KSAnnotation? =
        annotations.firstOrNull { annotation ->
            annotation.annotationType.resolve().declaration.qualifiedName?.asString() == qualifiedName
        }

    private fun KSAnnotated.hasAnnotation(qualifiedName: String): Boolean =
        requireAnnotation(qualifiedName) != null

    private fun KSClassDeclaration.hasProtocolOperations(): Boolean =
        declarations
            .filterIsInstance<KSFunctionDeclaration>()
            .any { function -> function.isLowLevelOperation() || function.isFirmwareWorkflow() }

    private fun KSFunctionDeclaration.isCandidateContractMethod(): Boolean =
        simpleName.asString() != "<init>"

    private fun KSFunctionDeclaration.isLowLevelOperation(): Boolean =
        requireAnnotation(ModbusAnnotationNames.operation) != null

    private fun KSFunctionDeclaration.isFirmwareWorkflow(): Boolean =
        simpleName.asString() == "flashFirmware" &&
            requireAnnotation(ModbusAnnotationNames.operation) == null

    private fun KSClassDeclaration.matchesTransport(transport: ModbusTransportKind): Boolean {
        val hasRtu = hasAnnotation(ModbusAnnotationNames.generateRtuServer)
        val hasTcp = hasAnnotation(ModbusAnnotationNames.generateTcpServer)
        if (!hasRtu && !hasTcp) {
            return true
        }
        return when (transport) {
            ModbusTransportKind.RTU -> hasRtu
            ModbusTransportKind.TCP -> hasTcp
        }
    }

    private fun KSAnnotation.stringArg(name: String): String =
        arguments.firstOrNull { argument -> argument.name?.asString() == name }?.value?.toString().orEmpty()

    private fun KSAnnotation.intArg(name: String): Int =
        (arguments.firstOrNull { argument -> argument.name?.asString() == name }?.value as? Int) ?: -1

    private fun KSAnnotation.enumArg(name: String): String =
        arguments.firstOrNull { argument -> argument.name?.asString() == name }
            ?.value
            ?.toString()
            ?.substringAfterLast('.')
            .orEmpty()

    private fun normalizeBasePath(basePath: String): String =
        "/" + basePath.trim().trim('/').ifBlank { "api/modbus" }
}
