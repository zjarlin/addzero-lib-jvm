package site.addzero.device.protocol.modbus.codegen

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import site.addzero.core.network.json.json
import site.addzero.device.protocol.modbus.codegen.model.GeneratedArtifact
import site.addzero.device.protocol.modbus.codegen.model.ModbusDocModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusFieldModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusMetadataDocPayload
import site.addzero.device.protocol.modbus.codegen.model.ModbusMetadataEnvelope
import site.addzero.device.protocol.modbus.codegen.model.ModbusMetadataFieldPayload
import site.addzero.device.protocol.modbus.codegen.model.ModbusMetadataOperationPayload
import site.addzero.device.protocol.modbus.codegen.model.ModbusMetadataParameterPayload
import site.addzero.device.protocol.modbus.codegen.model.ModbusMetadataPropertyPayload
import site.addzero.device.protocol.modbus.codegen.model.ModbusMetadataReturnTypePayload
import site.addzero.device.protocol.modbus.codegen.model.ModbusMetadataServicePayload
import site.addzero.device.protocol.modbus.codegen.model.ModbusMetadataWorkflowPayload
import site.addzero.device.protocol.modbus.codegen.model.ModbusOperationModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusParameterModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusPropertyModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusReturnKind
import site.addzero.device.protocol.modbus.codegen.model.ModbusReturnTypeModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusServiceModel
import site.addzero.device.protocol.modbus.codegen.model.ModbusTransportKind
import site.addzero.device.protocol.modbus.codegen.model.ModbusValueKind
import site.addzero.device.protocol.modbus.codegen.model.ModbusWorkflowKind
import site.addzero.device.protocol.modbus.codegen.model.ModbusWorkflowModel

/**
 * Modbus 契约默认值解析器。
 */
object ModbusContractDefaultsResolver {
    fun defaultServiceId(interfaceName: String): String =
        defaultOperationId(interfaceName.removeSuffix("Api"))

    fun defaultOperationId(methodName: String): String =
        methodName
            .replace(Regex("([a-z0-9])([A-Z])"), "$1-$2")
            .replace(Regex("[^A-Za-z0-9-]+"), "-")
            .replace(Regex("-+"), "-")
            .trim('-')
            .lowercase()

    fun resolveFunctionCodeName(
        explicitFunctionCodeName: String,
        parameters: List<ModbusParameterModel>,
        returnType: ModbusReturnTypeModel,
    ): String {
        if (explicitFunctionCodeName.isNotBlank() && explicitFunctionCodeName != "AUTO") {
            return explicitFunctionCodeName
        }
        if (parameters.isEmpty()) {
            return when (returnType.kind) {
                ModbusReturnKind.BOOLEAN,
                ModbusReturnKind.INT,
                ModbusReturnKind.STRING,
                ModbusReturnKind.DTO -> "READ_INPUT_REGISTERS"
                ModbusReturnKind.UNIT,
                ModbusReturnKind.COMMAND_RESULT -> error("无参数且返回 ${returnType.kind.name} 的操作无法自动推导 functionCode，请显式声明。")
            }
        }

        val totalWidth = parameters.maxOfOrNull { parameter -> parameter.registerOffset + parameter.registerWidth } ?: 0
        val allBooleans = parameters.isNotEmpty() && parameters.all { parameter -> parameter.valueKind == ModbusValueKind.BOOLEAN }
        if (parameters.size == 1) {
            val parameter = parameters.single()
            if (parameter.valueKind == ModbusValueKind.BOOLEAN) {
                return "WRITE_SINGLE_COIL"
            }
            if (totalWidth <= 1) {
                return "WRITE_SINGLE_REGISTER"
            }
        }
        if (allBooleans) {
            return "WRITE_MULTIPLE_COILS"
        }

        return "WRITE_MULTIPLE_REGISTERS"
    }

    fun resolveOperationIdsAndQuantities(operations: List<ModbusOperationModel>): List<ModbusOperationModel> =
        operations
            .map { operation ->
                if (operation.operationId.isNotBlank()) {
                    operation
                } else {
                    operation.copy(operationId = defaultOperationId(operation.methodName))
                }
            }.map { operation ->
                if (operation.quantity >= 0) {
                    operation
                } else {
                    operation.copy(quantity = inferQuantity(operation))
                }
            }

    private fun inferQuantity(operation: ModbusOperationModel): Int =
        when (operation.functionCodeName) {
            "WRITE_SINGLE_COIL",
            "WRITE_SINGLE_REGISTER" -> 1
            "WRITE_MULTIPLE_COILS" ->
                operation.parameters.maxOfOrNull { parameter -> parameter.registerOffset + 1 } ?: 0
            "WRITE_MULTIPLE_REGISTERS" ->
                operation.parameters.maxOfOrNull { parameter -> parameter.registerOffset + parameter.registerWidth } ?: 0
            "READ_COILS",
            "READ_DISCRETE_INPUTS",
            "READ_INPUT_REGISTERS",
            "READ_HOLDING_REGISTERS" -> inferredReturnWidth(operation.returnType)
            else -> error("暂不支持的功能码：${operation.functionCodeName}")
        }

    private fun inferredReturnWidth(returnType: ModbusReturnTypeModel): Int =
        when (returnType.kind) {
            ModbusReturnKind.UNIT,
            ModbusReturnKind.COMMAND_RESULT -> 0
            ModbusReturnKind.BOOLEAN,
            ModbusReturnKind.INT,
            ModbusReturnKind.STRING -> returnType.registerWidth
            ModbusReturnKind.DTO ->
                returnType.properties.maxOfOrNull { property ->
                    property.field?.let { field -> field.registerOffset + field.registerWidth } ?: 0
                } ?: 0
        }
}

/**
 * KDoc 解析器。
 */
object ModbusKdocParser {
    fun parse(rawDoc: String?, fallbackSummary: String): ModbusDocModel {
        val doc = rawDoc?.trim().orEmpty()
        if (doc.isBlank()) {
            return ModbusDocModel(summary = fallbackSummary)
        }

        val normalizedLines =
            doc.lineSequence()
                .map { line ->
                    line.trim()
                        .removePrefix("*")
                        .trim()
                }.toList()

        val narrativeLines = mutableListOf<String>()
        val parameterDocs = linkedMapOf<String, StringBuilder>()
        var currentParam: String? = null

        normalizedLines.forEach { line ->
            when {
                line.isBlank() -> {
                    currentParam = null
                    if (narrativeLines.isNotEmpty() && narrativeLines.last().isNotEmpty()) {
                        narrativeLines += ""
                    }
                }

                line.startsWith("@param ") -> {
                    val match = PARAM_REGEX.find(line)
                    if (match != null) {
                        val name = match.groupValues[1]
                        val text = match.groupValues[2].trim()
                        parameterDocs.getOrPut(name) { StringBuilder() }.append(text)
                        currentParam = name
                    }
                }

                line.startsWith("@") -> {
                    currentParam = null
                }

                currentParam != null -> {
                    val builder = parameterDocs.getValue(currentParam)
                    if (builder.isNotEmpty()) {
                        builder.append(' ')
                    }
                    builder.append(line)
                }

                else -> {
                    narrativeLines += line
                }
            }
        }

        val compactNarrative =
            narrativeLines
                .fold(mutableListOf<String>()) { acc, line ->
                    if (line.isBlank()) {
                        if (acc.isNotEmpty() && acc.last().isNotEmpty()) {
                            acc += ""
                        }
                    } else {
                        acc += line
                    }
                    acc
                }.dropLastWhile(String::isEmpty)

        val summary = compactNarrative.firstOrNull(String::isNotBlank) ?: fallbackSummary
        val descriptionLines =
            compactNarrative
                .dropWhile(String::isBlank)
                .drop(1)
                .dropWhile(String::isBlank)

        return ModbusDocModel(
            summary = summary,
            descriptionLines = descriptionLines,
            parameterDocs = parameterDocs.mapValues { (_, value) -> value.toString().trim() }.filterValues(String::isNotBlank),
        )
    }

    private val PARAM_REGEX = Regex("""^@param\s+([A-Za-z0-9_]+)\s+(.*)$""")
}

fun List<ModbusParameterModel>.withSequentialOffsets(): List<ModbusParameterModel> {
    var cursor = 0
    return map { parameter ->
        val resolvedOffset = if (parameter.registerOffset >= 0) parameter.registerOffset else cursor
        cursor = maxOf(cursor, resolvedOffset + parameter.registerWidth)
        parameter.copy(
            registerOffset = resolvedOffset,
            bitOffset = if (parameter.bitOffset >= 0) parameter.bitOffset else 0,
        )
    }
}

fun List<ModbusPropertyModel>.withSequentialFieldOffsets(): List<ModbusPropertyModel> {
    var cursor = 0
    return map { property ->
        val field = property.field ?: return@map property
        val resolvedOffset = if (field.registerOffset >= 0) field.registerOffset else cursor
        cursor = maxOf(cursor, resolvedOffset + field.registerWidth)
        property.copy(
            field = field.copy(
                registerOffset = resolvedOffset,
                bitOffset = if (field.bitOffset >= 0) field.bitOffset else 0,
            ),
        )
    }
}

fun registerWidth(
    codecName: String,
    length: Int = 1,
): Int =
    when (codecName) {
        "BYTE_ARRAY" -> (length + 1) / 2
        "U32_BE" -> 2 * length
        else -> length
    }

fun List<ModbusParameterModel>.resolveAutoCodecNames(functionCodeName: String): List<ModbusParameterModel> =
    map { parameter ->
        if (parameter.codecName != "AUTO") {
            parameter
        } else {
            parameter.copy(codecName = inferCodecName(parameter.valueKind, functionCodeName))
        }
    }

fun ModbusReturnTypeModel.resolveAutoCodecNames(functionCodeName: String): ModbusReturnTypeModel =
    if (kind == ModbusReturnKind.DTO) {
        copy(
            properties =
                properties.map { property ->
                    val field = property.field
                    if (field == null || field.codecName != "AUTO") {
                        property
                    } else {
                        property.copy(
                            field = field.copy(codecName = inferCodecName(property.valueKind, functionCodeName)),
                        )
                    }
                },
        )
    } else if (codecName == "AUTO" && valueKind != null) {
        val resolvedValueKind = requireNotNull(valueKind)
        val resolvedCodecName = inferCodecName(resolvedValueKind, functionCodeName)
        copy(
            codecName = resolvedCodecName,
            registerWidth = registerWidth(resolvedCodecName, length),
        )
    } else {
        copy(registerWidth = registerWidth(codecName, length))
    }

fun inferCodecName(
    valueKind: ModbusValueKind,
    functionCodeName: String,
): String =
    when (valueKind) {
        ModbusValueKind.BOOLEAN ->
            if (functionCodeName in setOf("READ_COILS", "READ_DISCRETE_INPUTS", "WRITE_SINGLE_COIL", "WRITE_MULTIPLE_COILS")) {
                "BOOL_COIL"
            } else {
                "BIT_FLAG"
            }

        ModbusValueKind.INT -> "U16"
        ModbusValueKind.BYTES -> "BYTE_ARRAY"
        ModbusValueKind.STRING -> "STRING_UTF8"
    }

/**
 * Modbus metadata JSON 编解码器。
 */
object ModbusMetadataJsonCodec {
    fun decodeServices(
        payload: String,
        transport: ModbusTransportKind? = null,
        contractPackages: List<String> = emptyList(),
    ): List<ModbusServiceModel> {
        val element = json.parseToJsonElement(payload)
        val rawServices =
            when (element) {
                is JsonArray -> element.map { item -> json.decodeFromJsonElement<ModbusMetadataServicePayload>(item) }
                is JsonObject ->
                    when (val servicesElement = element["services"]) {
                        null -> listOf(json.decodeFromJsonElement<ModbusMetadataServicePayload>(element))
                        is JsonArray -> servicesElement.map { item -> json.decodeFromJsonElement<ModbusMetadataServicePayload>(item) }
                        else -> error("Unsupported Modbus metadata payload: services must be a JSON array.")
                    }

                is JsonPrimitive -> error("Unsupported Modbus metadata payload: expected object or array, got primitive.")
            }
        return rawServices
            .map { service -> service.toModel() }
            .filter { service ->
                (transport == null || service.transport == transport) &&
                    (contractPackages.isEmpty() || contractPackages.any { contractPackage ->
                        service.interfacePackage == contractPackage || service.interfacePackage.startsWith("$contractPackage.")
                    })
            }
    }

    fun encodeServices(services: List<ModbusServiceModel>): String =
        json.encodeToString(
            ModbusMetadataEnvelope(
                services = services.map { service -> service.toPayload() },
            ),
        ) + "\n"

    private fun ModbusMetadataServicePayload.toModel(): ModbusServiceModel {
        val resolvedTransport = transport.toTransportKind()
        val resolvedQualifiedName = interfaceQualifiedName?.takeIf(String::isNotBlank) ?: "$interfacePackage.$interfaceSimpleName"
        val resolvedServiceId = serviceId.ifBlank { ModbusContractDefaultsResolver.defaultServiceId(interfaceSimpleName) }
        val resolvedDoc = doc.toModel(summary.ifBlank { "设备服务接口。" })
        val requestPrefix = interfaceSimpleName + resolvedTransport.transportId.replaceFirstChar(Char::uppercase)
        val normalizedOperations =
            ModbusContractDefaultsResolver.resolveOperationIdsAndQuantities(
                operations.map { operation ->
                    operation.toModel(
                        requestPrefix = requestPrefix,
                        fallbackPackage = interfacePackage,
                    )
                },
            )
        val normalizedWorkflows =
            workflows.map { workflow ->
                workflow.toModel(
                    requestPrefix = requestPrefix,
                    fallbackPackage = interfacePackage,
                )
            }
        return ModbusServiceModel(
            interfacePackage = interfacePackage,
            interfaceSimpleName = interfaceSimpleName,
            interfaceQualifiedName = resolvedQualifiedName,
            serviceId = resolvedServiceId,
            summary = summary.ifBlank { resolvedDoc.summary },
            basePath = basePath.ifBlank { "/api/modbus" },
            transport = resolvedTransport,
            doc = resolvedDoc,
            operations = normalizedOperations,
            workflows = normalizedWorkflows,
        )
    }

    private fun ModbusMetadataOperationPayload.toModel(
        requestPrefix: String,
        fallbackPackage: String,
    ): ModbusOperationModel {
        val rawParameters = parameters.map { parameter -> parameter.toModel() }.sortedBy(ModbusParameterModel::order).withSequentialOffsets()
        val rawReturnType = returnType.toModel()
        val resolvedFunctionCodeName =
            if (functionCodeName.isNotBlank()) {
                functionCodeName.uppercase()
            } else {
                ModbusContractDefaultsResolver.resolveFunctionCodeName(
                    explicitFunctionCodeName = "",
                    parameters = rawParameters,
                    returnType = rawReturnType,
                )
            }
        val resolvedParameters = rawParameters.resolveAutoCodecNames(resolvedFunctionCodeName)
        val resolvedReturnType = rawReturnType.resolveAutoCodecNames(resolvedFunctionCodeName)
        val resolvedRequestClassName =
            requestClassName.ifBlank {
                requestPrefix + methodName.replaceFirstChar(Char::uppercase) + "Request"
            }
        return ModbusOperationModel(
            methodName = methodName,
            operationId = operationId,
            functionCodeName = resolvedFunctionCodeName,
            address = address,
            quantity = quantity,
            requestClassName = resolvedRequestClassName,
            requestQualifiedName = requestQualifiedName.ifBlank { "$fallbackPackage.generated.$resolvedRequestClassName" },
            parameters = resolvedParameters,
            returnType = resolvedReturnType,
            doc = doc.toModel("执行 $methodName 操作。"),
        )
    }

    private fun ModbusMetadataWorkflowPayload.toModel(
        requestPrefix: String,
        fallbackPackage: String,
    ): ModbusWorkflowModel {
        val resolvedRequestClassName =
            requestClassName.ifBlank {
                requestPrefix + methodName.replaceFirstChar(Char::uppercase) + "Request"
            }
        return ModbusWorkflowModel(
            kind = kind.toWorkflowKind(),
            methodName = methodName,
            workflowId = workflowId.ifBlank { ModbusContractDefaultsResolver.defaultOperationId(methodName) },
            requestClassName = resolvedRequestClassName,
            requestQualifiedName = requestQualifiedName.ifBlank { "$fallbackPackage.generated.$resolvedRequestClassName" },
            bytesParameterName = bytesParameterName.ifBlank { "bytes" },
            returnType = returnType.toModel(),
            doc = doc.toModel("执行 $methodName 工作流。"),
            startMethodName = startMethodName.ifBlank { "firmwareStart" },
            chunkMethodName = chunkMethodName.ifBlank { "firmwareChunk" },
            commitMethodName = commitMethodName.ifBlank { "firmwareCommit" },
            resetMethodName = resetMethodName?.ifBlank { null } ?: "resetDevice",
        )
    }

    private fun ModbusMetadataReturnTypePayload.toModel(): ModbusReturnTypeModel =
        ModbusReturnTypeModel(
            qualifiedName = qualifiedName,
            simpleName = simpleName,
            kind = kind.toReturnKind(),
            docSummary = docSummary,
            valueKind = valueKind?.toValueKindOrNull(),
            codecName = codecName,
            length = length,
            registerWidth = registerWidth ?: registerWidth(codecName, length),
            properties = properties.map { property -> property.toModel() }.withSequentialFieldOffsets(),
        )

    private fun ModbusMetadataPropertyPayload.toModel(): ModbusPropertyModel =
        ModbusPropertyModel(
            name = name,
            qualifiedType = qualifiedType,
            valueKind = valueKind.toValueKind(),
            field = field?.toModel(),
            doc = doc,
        )

    private fun ModbusMetadataFieldPayload.toModel(): ModbusFieldModel =
        ModbusFieldModel(
            codecName = codecName,
            registerOffset = registerOffset,
            bitOffset = bitOffset,
            length = length,
            registerWidth = registerWidth ?: registerWidth(codecName, length),
        )

    private fun ModbusMetadataParameterPayload.toModel(): ModbusParameterModel =
        ModbusParameterModel(
            name = name,
            qualifiedType = qualifiedType,
            valueKind = valueKind.toValueKind(),
            order = order,
            codecName = codecName,
            registerOffset = registerOffset,
            bitOffset = bitOffset,
            length = length,
            registerWidth = registerWidth ?: registerWidth(codecName, length),
            doc = doc,
        )

    private fun ModbusMetadataDocPayload.toModel(fallbackSummary: String): ModbusDocModel =
        ModbusDocModel(
            summary = summary.ifBlank { fallbackSummary },
            descriptionLines = descriptionLines,
            parameterDocs = parameterDocs,
        )

    private fun ModbusServiceModel.toPayload(): ModbusMetadataServicePayload =
        ModbusMetadataServicePayload(
            interfacePackage = interfacePackage,
            interfaceSimpleName = interfaceSimpleName,
            interfaceQualifiedName = interfaceQualifiedName,
            serviceId = serviceId,
            summary = summary,
            basePath = basePath,
            transport = transport.transportId,
            doc = doc.toPayload(),
            operations = operations.map { operation -> operation.toPayload() },
            workflows = workflows.map { workflow -> workflow.toPayload() },
        )

    private fun ModbusOperationModel.toPayload(): ModbusMetadataOperationPayload =
        ModbusMetadataOperationPayload(
            methodName = methodName,
            operationId = operationId,
            functionCodeName = functionCodeName,
            address = address,
            quantity = quantity,
            requestClassName = requestClassName,
            requestQualifiedName = requestQualifiedName,
            parameters = parameters.map { parameter -> parameter.toPayload() },
            returnType = returnType.toPayload(),
            doc = doc.toPayload(),
        )

    private fun ModbusWorkflowModel.toPayload(): ModbusMetadataWorkflowPayload =
        ModbusMetadataWorkflowPayload(
            kind = kind.name,
            methodName = methodName,
            workflowId = workflowId,
            requestClassName = requestClassName,
            requestQualifiedName = requestQualifiedName,
            bytesParameterName = bytesParameterName,
            returnType = returnType.toPayload(),
            doc = doc.toPayload(),
            startMethodName = startMethodName,
            chunkMethodName = chunkMethodName,
            commitMethodName = commitMethodName,
            resetMethodName = resetMethodName,
        )

    private fun ModbusReturnTypeModel.toPayload(): ModbusMetadataReturnTypePayload =
        ModbusMetadataReturnTypePayload(
            qualifiedName = qualifiedName,
            simpleName = simpleName,
            kind = kind.name,
            docSummary = docSummary,
            valueKind = valueKind?.name,
            codecName = codecName,
            length = length,
            registerWidth = registerWidth,
            properties = properties.map { property -> property.toPayload() },
        )

    private fun ModbusPropertyModel.toPayload(): ModbusMetadataPropertyPayload =
        ModbusMetadataPropertyPayload(
            name = name,
            qualifiedType = qualifiedType,
            valueKind = valueKind.name,
            field = field?.toPayload(),
            doc = doc,
        )

    private fun ModbusFieldModel.toPayload(): ModbusMetadataFieldPayload =
        ModbusMetadataFieldPayload(
            codecName = codecName,
            registerOffset = registerOffset,
            bitOffset = bitOffset,
            length = length,
            registerWidth = registerWidth,
        )

    private fun ModbusParameterModel.toPayload(): ModbusMetadataParameterPayload =
        ModbusMetadataParameterPayload(
            name = name,
            qualifiedType = qualifiedType,
            valueKind = valueKind.name,
            order = order,
            codecName = codecName,
            registerOffset = registerOffset,
            bitOffset = bitOffset,
            length = length,
            registerWidth = registerWidth,
            doc = doc,
        )

    private fun ModbusDocModel.toPayload(): ModbusMetadataDocPayload =
        ModbusMetadataDocPayload(
            summary = summary,
            descriptionLines = descriptionLines,
            parameterDocs = parameterDocs,
        )

    private fun String.toTransportKind(): ModbusTransportKind =
        ModbusTransportKind.entries.firstOrNull { entry ->
            equals(entry.name, ignoreCase = true) || equals(entry.transportId, ignoreCase = true)
        } ?: error("Unknown Modbus transport kind: $this")

    private fun String.toReturnKind(): ModbusReturnKind =
        enumValueOfNormalized(this)

    private fun String.toWorkflowKind(): ModbusWorkflowKind =
        enumValueOfNormalized(this)

    private fun String.toValueKind(): ModbusValueKind =
        enumValueOfNormalized(this)

    private fun String.toValueKindOrNull(): ModbusValueKind? =
        takeIf(String::isNotBlank)?.let(::enumValueOfNormalized)

    private inline fun <reified T : Enum<T>> enumValueOfNormalized(value: String): T =
        enumValues<T>().firstOrNull { entry -> entry.name.equals(value, ignoreCase = true) }
            ?: error("Unknown ${T::class.simpleName} value: $value")
}

/**
 * Modbus IR 校验器。
 */
object ModbusModelValidator {
    fun validate(services: List<ModbusServiceModel>): List<String> {
        val errors = mutableListOf<String>()
        val routeIndex = mutableMapOf<String, String>()

        services.forEach { service ->
            if (service.serviceId.isBlank()) {
                errors += "服务 ${service.interfaceQualifiedName} 的 serviceId 不能为空。"
            }
            val operationIds = mutableSetOf<String>()
            service.operations.forEach { operation ->
                if (!operationIds.add(operation.operationId)) {
                    errors += "服务 ${service.interfaceQualifiedName} 存在重复的 operationId：${operation.operationId}。"
                }
                val route = "${service.basePath}/${service.transport.transportId}/${service.serviceId}/${operation.operationId}"
                val previous = routeIndex.putIfAbsent(route, service.interfaceQualifiedName)
                if (previous != null && previous != service.interfaceQualifiedName) {
                    errors += "路由冲突：$route 同时来自 $previous 和 ${service.interfaceQualifiedName}。"
                }
                validateParameters(service, operation, errors)
                validateReturnType(service, operation, errors)
            }
        }

        validateAddressConflicts(services, errors)

        return errors
    }

    private fun validateAddressConflicts(
        services: List<ModbusServiceModel>,
        errors: MutableList<String>,
    ) {
        site.addzero.device.protocol.modbus.codegen.model.ModbusAddressSpace.entries.forEach { addressSpace ->
            val operations =
                services.flatMap { service ->
                    service.operations.map { operation -> service to operation }
                }.filter { (_, operation) -> operation.addressSpace == addressSpace }
                    .sortedBy { (_, operation) -> operation.address }

            operations.zipWithNext().forEach { (left, right) ->
                val (leftService, leftOperation) = left
                val (rightService, rightOperation) = right
                val leftEndExclusive = leftOperation.address + leftOperation.registerSpan
                if (leftEndExclusive > rightOperation.address) {
                    errors +=
                        "地址冲突：${leftService.interfaceQualifiedName}.${leftOperation.methodName} " +
                            "与 ${rightService.interfaceQualifiedName}.${rightOperation.methodName} " +
                            "在 $addressSpace 空间发生重叠，请为其中一个操作显式指定 address。"
                }
            }
        }
    }

    private fun validateParameters(
        service: ModbusServiceModel,
        operation: ModbusOperationModel,
        errors: MutableList<String>,
    ) {
        val orders = mutableSetOf<Int>()
        val occupied = mutableSetOf<String>()
        operation.parameters.forEach { parameter ->
            if (!orders.add(parameter.order)) {
                errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 存在重复的参数顺序：${parameter.order}。"
            }
            validateCodecShape(service, operation, parameter.name, parameter.codecName, parameter.valueKind, parameter.bitOffset, errors)
            validateCodecUsageInFunction(service, operation, parameter.name, parameter.codecName, errors)
            parameter.locationKeys(operation).forEach { key ->
                if (!occupied.add(key)) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的参数映射冲突：${parameter.name} -> $key。"
                }
            }
        }

        val totalWidth = operation.parameters.maxOfOrNull { parameter -> parameter.registerOffset + parameter.registerWidth } ?: 0
        val coilWidth = operation.parameters.maxOfOrNull { parameter -> parameter.registerOffset + 1 } ?: 0
        when (operation.functionCodeName) {
            "READ_COILS",
            "READ_DISCRETE_INPUTS",
            "READ_INPUT_REGISTERS",
            "READ_HOLDING_REGISTERS" -> {
                if (operation.parameters.isNotEmpty()) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 是读功能码，不应声明请求参数。"
                }
            }

            "WRITE_SINGLE_COIL" -> {
                if (operation.parameters.size != 1 || operation.parameters.singleOrNull()?.valueKind != ModbusValueKind.BOOLEAN) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 使用 WRITE_SINGLE_COIL 时只能有一个 Boolean 参数。"
                }
                if (operation.parameters.singleOrNull()?.codecName != "BOOL_COIL") {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 使用 WRITE_SINGLE_COIL 时必须使用 BOOL_COIL。"
                }
                if (operation.quantity != 1) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 使用 WRITE_SINGLE_COIL 时 quantity 必须等于 1。"
                }
            }

            "WRITE_MULTIPLE_COILS" -> {
                if (operation.parameters.isEmpty()) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 使用 WRITE_MULTIPLE_COILS 时至少需要一个 Boolean 参数。"
                }
                if (operation.parameters.any { parameter -> parameter.codecName != "BOOL_COIL" || parameter.valueKind != ModbusValueKind.BOOLEAN }) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 使用 WRITE_MULTIPLE_COILS 时所有参数都必须是 BOOL_COIL Boolean。"
                }
                if (operation.quantity in 0 until coilWidth) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 quantity=${operation.quantity} 小于线圈编码宽度 $coilWidth。"
                }
            }

            "WRITE_SINGLE_REGISTER" -> {
                if (totalWidth > 1) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 使用 WRITE_SINGLE_REGISTER 时总寄存器宽度不能超过 1。"
                }
            }

            "WRITE_MULTIPLE_REGISTERS" -> {
                if (operation.quantity in 0 until totalWidth) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 quantity=${operation.quantity} 小于寄存器编码宽度 $totalWidth。"
                }
            }
        }
    }

    private fun validateReturnType(
        service: ModbusServiceModel,
        operation: ModbusOperationModel,
        errors: MutableList<String>,
    ) {
        val returnType = operation.returnType
        when (operation.functionCodeName) {
            "READ_COILS",
            "READ_DISCRETE_INPUTS" -> {
                validateReadReturnType(service, operation, returnType, setOf(ModbusValueKind.BOOLEAN), errors)
            }

            "READ_INPUT_REGISTERS",
            "READ_HOLDING_REGISTERS" -> {
                validateReadReturnType(service, operation, returnType, setOf(ModbusValueKind.BOOLEAN, ModbusValueKind.INT, ModbusValueKind.STRING, ModbusValueKind.BYTES), errors)
            }

            else -> {
                if (returnType.kind != ModbusReturnKind.COMMAND_RESULT) {
                    errors += "写操作 ${service.interfaceQualifiedName}.${operation.methodName} 必须返回 ModbusCommandResult。"
                }
            }
        }
    }

    private fun validateReadReturnType(
        service: ModbusServiceModel,
        operation: ModbusOperationModel,
        returnType: ModbusReturnTypeModel,
        allowedScalarKinds: Set<ModbusValueKind>,
        errors: MutableList<String>,
    ) {
        when (returnType.kind) {
            ModbusReturnKind.BOOLEAN,
            ModbusReturnKind.INT,
            ModbusReturnKind.STRING -> {
                val valueKind = requireNotNull(returnType.valueKind)
                if (valueKind !in allowedScalarKinds) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的返回值类型 $valueKind 与 ${operation.functionCodeName} 不兼容。"
                }
                validateCodecShape(service, operation, "return", returnType.codecName, valueKind, bitOffset = 0, errors = errors)
            }

            ModbusReturnKind.DTO -> {
                val occupied = mutableSetOf<String>()
                returnType.properties.forEach { property ->
                    val field = property.field
                    if (field == null) {
                        errors += "DTO ${returnType.qualifiedName}.${property.name} 缺少字段映射。"
                    } else {
                        validateCodecShape(service, operation, property.name, field.codecName, property.valueKind, field.bitOffset, errors)
                        validateCodecUsageInFunction(service, operation, property.name, field.codecName, errors)
                        property.locationKeys().forEach { key ->
                            if (!occupied.add(key)) {
                                errors += "DTO ${returnType.qualifiedName} 的字段映射冲突：${property.name} -> $key。"
                            }
                        }
                    }
                }
            }

            else -> {
                errors += "读操作 ${service.interfaceQualifiedName}.${operation.methodName} 必须返回标量或 DTO。"
            }
        }
    }

    private fun validateCodecShape(
        service: ModbusServiceModel,
        operation: ModbusOperationModel,
        subjectName: String,
        codecName: String,
        valueKind: ModbusValueKind,
        bitOffset: Int,
        errors: MutableList<String>,
    ) {
        when (codecName) {
            "BOOL_COIL" -> {
                if (valueKind != ModbusValueKind.BOOLEAN) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 $subjectName 使用 BOOL_COIL 时必须是 Boolean。"
                }
            }

            "BIT_FLAG" -> {
                if (valueKind != ModbusValueKind.BOOLEAN) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 $subjectName 使用 BIT_FLAG 时必须是 Boolean。"
                }
                if (bitOffset !in 0..15) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 $subjectName 使用 BIT_FLAG 时 bitOffset 必须在 0..15。"
                }
            }

            "U16",
            "U32_BE",
            "U8" -> {
                if (valueKind != ModbusValueKind.INT) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 $subjectName 使用 $codecName 时必须是 Int。"
                }
            }

            "STRING_ASCII",
            "STRING_UTF8" -> {
                if (valueKind != ModbusValueKind.STRING) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 $subjectName 使用 $codecName 时必须是 String。"
                }
            }

            "BYTE_ARRAY" -> {
                if (valueKind != ModbusValueKind.BYTES) {
                    errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 $subjectName 使用 BYTE_ARRAY 时必须是 ByteArray。"
                }
            }
        }
    }

    private fun validateCodecUsageInFunction(
        service: ModbusServiceModel,
        operation: ModbusOperationModel,
        subjectName: String,
        codecName: String,
        errors: MutableList<String>,
    ) {
        if (operation.usesCoilBits && codecName !in setOf("BOOL_COIL", "BIT_FLAG")) {
            errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 $subjectName 在线圈空间只能使用 BOOL_COIL 或 BIT_FLAG。"
        }
        if (!operation.usesCoilBits && codecName == "BOOL_COIL") {
            errors += "操作 ${service.interfaceQualifiedName}.${operation.methodName} 的 $subjectName 在寄存器空间不能使用 BOOL_COIL。"
        }
    }

    private fun ModbusParameterModel.locationKeys(operation: ModbusOperationModel): List<String> =
        if (operation.usesCoilBits) {
            listOf("coil:${registerOffset}")
        } else if (codecName == "BIT_FLAG") {
            listOf("register:${registerOffset}:bit:$bitOffset")
        } else {
            (0 until registerWidth).map { offset -> "register:${registerOffset + offset}" }
        }

    private fun ModbusPropertyModel.locationKeys(): List<String> {
        val field = field ?: return emptyList()
        return if (field.codecName == "BOOL_COIL") {
            listOf("coil:${field.registerOffset}")
        } else if (field.codecName == "BIT_FLAG") {
            listOf("register:${field.registerOffset}:bit:${field.bitOffset}")
        } else {
            (0 until field.registerWidth).map { offset -> "register:${field.registerOffset + offset}" }
        }
    }
}

data class ModbusApiModelBridge(
    val qualifiedName: String,
    val summary: String? = null,
    val propertyDocs: Map<String, String> = emptyMap(),
)

data class ModbusDtoCustomization(
    val summary: String? = null,
    val propertyDocs: Map<String, String> = emptyMap(),
    val apiModelBridge: ModbusApiModelBridge? = null,
)

data class ModbusKotlinContractGenerationRequest(
    val services: List<ModbusServiceModel>,
    val fileHeader: String = "Generated by modbus-codegen. DO NOT EDIT.",
    val dtoCustomizations: Map<String, ModbusDtoCustomization> = emptyMap(),
)

/**
 * 无注解 Kotlin 契约生成器。
 *
 * 这里输出纯类型签名，把协议语义留在 metadata 中。
 */
object ModbusKotlinContractGenerator {
    fun render(request: ModbusKotlinContractGenerationRequest): List<GeneratedArtifact> {
        val artifacts = mutableListOf<GeneratedArtifact>()
        val renderedDtoNames = linkedSetOf<String>()

        request.services.forEach { service ->
            artifacts += renderService(request.fileHeader, service)
            service.operations
                .map(ModbusOperationModel::returnType)
                .filter { returnType -> returnType.kind == ModbusReturnKind.DTO }
                .forEach { returnType ->
                    if (renderedDtoNames.add(returnType.qualifiedName)) {
                        artifacts += renderDto(
                            fileHeader = request.fileHeader,
                            returnType = returnType,
                            customization = request.dtoCustomizations[returnType.qualifiedName],
                        )
                    }
                }
        }

        return artifacts
    }

    private fun renderService(
        fileHeader: String,
        service: ModbusServiceModel,
    ): GeneratedArtifact {
        val usesCommandResult =
            service.operations.any { operation -> operation.returnType.kind == ModbusReturnKind.COMMAND_RESULT } ||
                service.workflows.any { workflow -> workflow.returnType.kind == ModbusReturnKind.COMMAND_RESULT }
        val methods =
            (service.operations.map { operation -> renderOperationSignature(operation) } +
                service.workflows.map { workflow -> renderWorkflowSignature(workflow) })
                .joinToString("\n\n")

        val imports =
            buildList {
                if (usesCommandResult) {
                    add("import site.addzero.device.protocol.modbus.model.ModbusCommandResult")
                }
            }.joinToString("\n")

        val fileContent =
            buildString {
                appendLine("// $fileHeader")
                appendLine("package ${service.interfacePackage}")
                if (imports.isNotBlank()) {
                    appendLine()
                    appendLine(imports)
                }
                appendLine()
                appendLine("interface ${service.interfaceSimpleName} {")
                if (methods.isNotBlank()) {
                    appendLine(methods.prependIndent("    "))
                }
                appendLine("}")
            }

        return GeneratedArtifact(
            packageName = service.interfacePackage,
            fileName = service.interfaceSimpleName,
            extensionName = "kt",
            content = fileContent,
        )
    }

    private fun renderOperationSignature(operation: ModbusOperationModel): String {
        val docBlock = renderKDoc(operation.doc.summary.ifBlank { operation.methodName })
        val params =
            operation.parameters.joinToString(",\n") { parameter ->
                "${parameter.name}: ${parameter.kotlinTypeSimpleName()}"
            }
        val parameterBlock =
            if (params.isBlank()) {
                "()"
            } else {
                "(\n${params.prependIndent("    ")}\n)"
            }
        return buildString {
            appendLine(docBlock)
            append("suspend fun ${operation.methodName}$parameterBlock: ${operation.returnType.renderKotlinType()}")
        }
    }

    private fun renderWorkflowSignature(workflow: ModbusWorkflowModel): String {
        val docBlock = renderKDoc(workflow.doc.summary.ifBlank { workflow.methodName })
        return buildString {
            appendLine(docBlock)
            append("suspend fun ${workflow.methodName}(${workflow.bytesParameterName}: ByteArray): ${workflow.returnType.renderKotlinType()}")
        }
    }

    private fun renderDto(
        fileHeader: String,
        returnType: ModbusReturnTypeModel,
        customization: ModbusDtoCustomization?,
    ): GeneratedArtifact {
        val packageName = returnType.qualifiedName.substringBeforeLast('.', missingDelimiterValue = "")
        val dtoSummary = customization?.summary ?: returnType.docSummary.ifBlank { "${returnType.simpleName} 寄存器快照。" }
        val properties =
            returnType.properties.joinToString(",\n") { property ->
                val propertyDoc = customization?.propertyDocs?.get(property.name)?.cleanDocSummary() ?: property.doc.cleanDocSummary()
                buildString {
                    propertyDoc?.let { doc ->
                        appendLine(renderKDoc(doc))
                    }
                    append("val ${property.name}: ${property.kotlinTypeSimpleName()}")
                }
            }
        val bridgeBlock = customization?.apiModelBridge?.let { bridge ->
            renderApiModelBridge(returnType, bridge)
        }.orEmpty()
        val fileContent =
            buildString {
                appendLine("// $fileHeader")
                appendLine("package $packageName")
                appendLine()
                appendLine(renderKDoc(dtoSummary))
                appendLine("data class ${returnType.simpleName}(")
                if (properties.isNotBlank()) {
                    appendLine(properties.prependIndent("    "))
                }
                append(")")
                if (bridgeBlock.isNotBlank()) {
                    appendLine(" {")
                    appendLine(bridgeBlock.prependIndent("    "))
                    appendLine("}")
                } else {
                    appendLine()
                }
            }

        return GeneratedArtifact(
            packageName = packageName,
            fileName = returnType.simpleName,
            extensionName = "kt",
            content = fileContent,
        )
    }

    private fun renderApiModelBridge(
        returnType: ModbusReturnTypeModel,
        bridge: ModbusApiModelBridge,
    ): String {
        val assignments =
            returnType.properties.joinToString(",\n") { property ->
                val propertyDoc = bridge.propertyDocs[property.name].cleanDocSummary()
                buildString {
                    propertyDoc?.let { doc ->
                        appendLine("// $doc")
                    }
                    append("${property.name} = ${property.name}")
                }
            }
        val summary = bridge.summary.cleanDocSummary() ?: "转换为 ${bridge.qualifiedName.substringAfterLast('.')}。"
        return buildString {
            appendLine(renderKDoc(summary))
            appendLine("fun toApiModel(): ${bridge.qualifiedName} {")
            appendLine("    return ${bridge.qualifiedName.substringAfterLast('.')}(")
            appendLine(assignments.prependIndent("        "))
            appendLine("    )")
            append("}")
        }
    }

    private fun renderKDoc(summary: String): String =
        buildString {
            appendLine("/**")
            appendLine(" * $summary")
            append(" */")
        }

    private fun ModbusReturnTypeModel.renderKotlinType(): String =
        when (kind) {
            ModbusReturnKind.UNIT -> "Unit"
            ModbusReturnKind.BOOLEAN -> "Boolean"
            ModbusReturnKind.INT -> "Int"
            ModbusReturnKind.STRING -> "String"
            ModbusReturnKind.DTO -> simpleName
            ModbusReturnKind.COMMAND_RESULT -> "ModbusCommandResult"
        }

    private fun ModbusParameterModel.kotlinTypeSimpleName(): String =
        when (valueKind) {
            ModbusValueKind.BOOLEAN -> "Boolean"
            ModbusValueKind.INT -> "Int"
            ModbusValueKind.BYTES -> "ByteArray"
            ModbusValueKind.STRING -> "String"
        }

    private fun ModbusPropertyModel.kotlinTypeSimpleName(): String =
        when (valueKind) {
            ModbusValueKind.BOOLEAN -> "Boolean"
            ModbusValueKind.INT -> "Int"
            ModbusValueKind.BYTES -> "ByteArray"
            ModbusValueKind.STRING -> "String"
        }

    private fun String?.cleanDocSummary(): String? =
        this?.trim()?.takeIf(String::isNotBlank)
}
