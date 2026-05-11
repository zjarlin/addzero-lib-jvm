package site.addzero.device.protocol.modbus.ksp.core

import com.google.devtools.ksp.processing.KSPLogger
import java.sql.DriverManager
import java.util.Properties
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import site.addzero.core.network.json.json

/**
 * 默认 JDBC 数据库元数据提供者。
 *
 * 数据库查询结果需要返回 JSON 文本：
 * - 单个 service 对象
 * - service 数组
 * - 或 `{ "services": [...] }`
 */
class ModbusDatabaseMetadataProvider : ModbusMetadataProvider {
    override val providerId: String = "database"

    override fun isEnabled(context: ModbusMetadataCollectionContext): Boolean =
        context.environment.resolveDatabaseMetadataOptions() != null

    override fun collect(context: ModbusMetadataCollectionContext): List<CollectedModbusService> {
        val options = context.environment.resolveDatabaseMetadataOptions() ?: return emptyList()
        return collectFromDatabase(
            options = options,
            transport = context.transport,
            contractPackages = context.contractPackages,
            logger = context.environment.logger,
        )
    }

    internal fun collectFromDatabase(
        options: ModbusDatabaseMetadataOptions,
        transport: ModbusTransportKind,
        contractPackages: List<String>,
        logger: KSPLogger,
    ): List<CollectedModbusService> {
        options.driverClass?.takeIf(String::isNotBlank)?.let { driverClass ->
            runCatching { Class.forName(driverClass) }
                .getOrElse { exception ->
                    error("Unable to load Modbus metadata JDBC driver $driverClass: ${exception.message}")
                }
        }
        val connectionProperties =
            Properties().apply {
                options.username?.takeIf(String::isNotBlank)?.let { put("user", it) }
                options.password?.takeIf(String::isNotBlank)?.let { put("password", it) }
            }
        val payloads = mutableListOf<String>()
        DriverManager.getConnection(options.jdbcUrl, connectionProperties).use { connection ->
            val resolvedQuery =
                options.query
                    .replace("${'$'}{transport}", transport.transportId)
                    .replace("${'$'}{transportName}", transport.name)
            connection.prepareStatement(resolvedQuery).use { statement ->
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        val payload =
                            if (options.jsonColumn == null) {
                                resultSet.getString(1)
                            } else {
                                resultSet.getString(options.jsonColumn)
                            }
                        payload
                            ?.trim()
                            ?.takeIf(String::isNotBlank)
                            ?.let(payloads::add)
                    }
                }
            }
        }
        logger.logging("Loaded ${payloads.size} Modbus metadata payload row(s) from database.")
        return payloads
            .flatMap(::decodeServices)
            .map { payload -> payload.toModel() }
            .filter { service ->
                service.transport == transport &&
                    service.interfacePackage.matchesContractPackages(contractPackages)
            }
            .map { service -> CollectedModbusService(model = service) }
    }

    internal fun decodeServices(payload: String): List<DatabaseServicePayload> {
        val element = json.parseToJsonElement(payload)
        return when (element) {
            is JsonArray -> element.map { item -> json.decodeFromJsonElement<DatabaseServicePayload>(item) }
            is JsonObject ->
                when (val servicesElement = element["services"]) {
                    null -> listOf(json.decodeFromJsonElement<DatabaseServicePayload>(element))
                    is JsonArray -> servicesElement.map { item -> json.decodeFromJsonElement<DatabaseServicePayload>(item) }
                    else -> error("Unsupported Modbus metadata payload: services must be a JSON array.")
                }

            is JsonPrimitive -> error("Unsupported Modbus metadata payload: expected object or array, got primitive.")
        }
    }

    private fun DatabaseServicePayload.toModel(): ModbusServiceModel {
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

    private fun DatabaseOperationPayload.toModel(
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

    private fun DatabaseWorkflowPayload.toModel(
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

    private fun DatabaseReturnTypePayload.toModel(): ModbusReturnTypeModel =
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

    private fun DatabasePropertyPayload.toModel(): ModbusPropertyModel =
        ModbusPropertyModel(
            name = name,
            qualifiedType = qualifiedType,
            valueKind = valueKind.toValueKind(),
            field = field?.toModel(),
            doc = doc,
        )

    private fun DatabaseFieldPayload.toModel(): ModbusFieldModel =
        ModbusFieldModel(
            codecName = codecName,
            registerOffset = registerOffset,
            bitOffset = bitOffset,
            length = length,
            registerWidth = registerWidth ?: registerWidth(codecName, length),
        )

    private fun DatabaseParameterPayload.toModel(): ModbusParameterModel =
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

    private fun DatabaseDocPayload.toModel(fallbackSummary: String): ModbusDocModel =
        ModbusDocModel(
            summary = summary.ifBlank { fallbackSummary },
            descriptionLines = descriptionLines,
            parameterDocs = parameterDocs,
        )

    private fun String.toTransportKind(): ModbusTransportKind =
        ModbusTransportKind.entries.firstOrNull { entry ->
            equals(entry.name, ignoreCase = true) || equals(entry.transportId, ignoreCase = true)
        } ?: error("Unknown Modbus transport kind: $this")

    private fun String.toReturnKind(): ModbusReturnKind =
        enumValueOfNormalized<ModbusReturnKind>(this)

    private fun String.toWorkflowKind(): ModbusWorkflowKind =
        enumValueOfNormalized<ModbusWorkflowKind>(this)

    private fun String.toValueKind(): ModbusValueKind =
        enumValueOfNormalized<ModbusValueKind>(this)

    private fun String.toValueKindOrNull(): ModbusValueKind? =
        takeIf(String::isNotBlank)?.let(::enumValueOfNormalized)

    private inline fun <reified T : Enum<T>> enumValueOfNormalized(value: String): T =
        enumValues<T>().firstOrNull { entry -> entry.name.equals(value, ignoreCase = true) }
            ?: error("Unknown ${T::class.simpleName} value: $value")

    private fun String.matchesContractPackages(contractPackages: List<String>): Boolean =
        contractPackages.isEmpty() ||
            contractPackages.any { contractPackage ->
                this == contractPackage || this.startsWith("$contractPackage.")
            }
}

@Serializable
internal data class DatabaseServicePayload(
    val interfacePackage: String,
    val interfaceSimpleName: String,
    val interfaceQualifiedName: String? = null,
    val serviceId: String = "",
    val summary: String = "",
    val basePath: String = "/api/modbus",
    val transport: String,
    val doc: DatabaseDocPayload = DatabaseDocPayload(),
    val operations: List<DatabaseOperationPayload> = emptyList(),
    val workflows: List<DatabaseWorkflowPayload> = emptyList(),
)

@Serializable
internal data class DatabaseOperationPayload(
    val methodName: String,
    val operationId: String = "",
    val functionCodeName: String = "",
    val address: Int,
    val quantity: Int = -1,
    val requestClassName: String = "",
    val requestQualifiedName: String = "",
    val parameters: List<DatabaseParameterPayload> = emptyList(),
    val returnType: DatabaseReturnTypePayload,
    val doc: DatabaseDocPayload = DatabaseDocPayload(),
)

@Serializable
internal data class DatabaseWorkflowPayload(
    val kind: String = "FLASH_FIRMWARE",
    val methodName: String,
    val workflowId: String = "",
    val requestClassName: String = "",
    val requestQualifiedName: String = "",
    val bytesParameterName: String = "bytes",
    val returnType: DatabaseReturnTypePayload,
    val doc: DatabaseDocPayload = DatabaseDocPayload(),
    val startMethodName: String = "firmwareStart",
    val chunkMethodName: String = "firmwareChunk",
    val commitMethodName: String = "firmwareCommit",
    val resetMethodName: String? = "resetDevice",
)

@Serializable
internal data class DatabaseReturnTypePayload(
    val qualifiedName: String,
    val simpleName: String,
    val kind: String,
    val docSummary: String = "",
    val valueKind: String? = null,
    val codecName: String = "AUTO",
    val length: Int = 1,
    val registerWidth: Int? = null,
    val properties: List<DatabasePropertyPayload> = emptyList(),
)

@Serializable
internal data class DatabasePropertyPayload(
    val name: String,
    val qualifiedType: String,
    val valueKind: String,
    val field: DatabaseFieldPayload? = null,
    val doc: String = "",
)

@Serializable
internal data class DatabaseFieldPayload(
    val codecName: String = "AUTO",
    val registerOffset: Int = -1,
    val bitOffset: Int = -1,
    val length: Int = 1,
    val registerWidth: Int? = null,
)

@Serializable
internal data class DatabaseParameterPayload(
    val name: String,
    val qualifiedType: String,
    val valueKind: String,
    val order: Int = 0,
    val codecName: String = "AUTO",
    val registerOffset: Int = -1,
    val bitOffset: Int = -1,
    val length: Int = 1,
    val registerWidth: Int? = null,
    val doc: String = "",
)

@Serializable
internal data class DatabaseDocPayload(
    val summary: String = "",
    val descriptionLines: List<String> = emptyList(),
    val parameterDocs: Map<String, String> = emptyMap(),
)
