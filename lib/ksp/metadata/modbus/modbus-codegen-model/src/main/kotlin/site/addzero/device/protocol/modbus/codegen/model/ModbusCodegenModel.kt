package site.addzero.device.protocol.modbus.codegen.model

import kotlinx.serialization.Serializable

/**
 * 传输类型。
 */
enum class ModbusTransportKind(
    val transportId: String,
    val displayName: String,
    val generatedPackage: String,
    val generatedFileName: String,
) {
    RTU(
        transportId = "rtu",
        displayName = "Modbus RTU",
        generatedPackage = "site.addzero.esp32_host_computer.generated.modbus.rtu",
        generatedFileName = "GeneratedModbusRtu",
    ),
    TCP(
        transportId = "tcp",
        displayName = "Modbus TCP",
        generatedPackage = "site.addzero.esp32_host_computer.generated.modbus.tcp",
        generatedFileName = "GeneratedModbusTcp",
    ),
    MQTT(
        transportId = "mqtt",
        displayName = "Modbus MQTT",
        generatedPackage = "site.addzero.esp32_host_computer.generated.modbus.mqtt",
        generatedFileName = "GeneratedModbusMqtt",
    ),
    ;

    companion object {
        fun parseConfigured(rawValue: String?): Set<ModbusTransportKind> {
            val tokens =
                rawValue
                    ?.split(',', ';', '\n')
                    ?.map(String::trim)
                    ?.filter(String::isNotBlank)
                    .orEmpty()
            if (tokens.isEmpty()) {
                return emptySet()
            }

            val resolved = linkedSetOf<ModbusTransportKind>()
            tokens.forEach { token ->
                val normalized = token.lowercase()
                val matched =
                    entries.firstOrNull { kind ->
                        normalized == kind.transportId || normalized == kind.name.lowercase()
                    }
                        ?: error(
                            "未知的 Modbus transport 选项：$rawValue；当前可选值为 ${
                                entries.joinToString(",") { it.transportId }
                            }，支持逗号分隔多选。",
                        )
                resolved += matched
            }
            return resolved
        }

        fun resolveConfiguredOrDefault(
            rawValue: String?,
            defaultTransport: ModbusTransportKind,
        ): Set<ModbusTransportKind> = parseConfigured(rawValue).ifEmpty { linkedSetOf(defaultTransport) }
    }
}

/**
 * 文档注释元数据。
 */
data class ModbusDocModel(
    val summary: String,
    val descriptionLines: List<String> = emptyList(),
    val parameterDocs: Map<String, String> = emptyMap(),
) {
    val fullText: String =
        buildString {
            if (summary.isNotBlank()) {
                append(summary)
            }
            if (descriptionLines.isNotEmpty()) {
                if (isNotEmpty()) {
                    append('\n')
                }
                append(descriptionLines.joinToString("\n"))
            }
        }
}

/**
 * 标量值类型。
 */
enum class ModbusValueKind {
    BOOLEAN,
    INT,
    BYTES,
    STRING,
}

/**
 * 返回类型分类。
 */
enum class ModbusReturnKind {
    UNIT,
    BOOLEAN,
    INT,
    STRING,
    DTO,
    COMMAND_RESULT,
}

enum class ModbusAddressSpace {
    COIL_READ,
    DISCRETE_INPUT,
    INPUT_REGISTER,
    HOLDING_REGISTER_READ,
    COIL_WRITE,
    HOLDING_REGISTER_WRITE,
}

/**
 * Modbus 字段映射。
 */
data class ModbusFieldModel(
    val codecName: String,
    val registerOffset: Int,
    val bitOffset: Int,
    val length: Int,
    val registerWidth: Int,
)

/**
 * DTO 属性定义。
 */
data class ModbusPropertyModel(
    val name: String,
    val qualifiedType: String,
    val valueKind: ModbusValueKind,
    val field: ModbusFieldModel?,
    val doc: String,
)

/**
 * 方法参数定义。
 */
data class ModbusParameterModel(
    val name: String,
    val qualifiedType: String,
    val valueKind: ModbusValueKind,
    val order: Int,
    val codecName: String,
    val registerOffset: Int,
    val bitOffset: Int,
    val registerWidth: Int,
    val length: Int = 1,
    val doc: String,
)

/**
 * 返回类型定义。
 */
data class ModbusReturnTypeModel(
    val qualifiedName: String,
    val simpleName: String,
    val kind: ModbusReturnKind,
    val docSummary: String = "",
    val valueKind: ModbusValueKind? = null,
    val codecName: String = "AUTO",
    val length: Int = 1,
    val registerWidth: Int = 1,
    val properties: List<ModbusPropertyModel> = emptyList(),
)

/**
 * 单个操作定义。
 */
data class ModbusOperationModel(
    val methodName: String,
    val operationId: String,
    val functionCodeName: String,
    val address: Int,
    val quantity: Int,
    val requestClassName: String,
    val requestQualifiedName: String,
    val parameters: List<ModbusParameterModel>,
    val returnType: ModbusReturnTypeModel,
    val doc: ModbusDocModel,
) {
    val isReadOperation: Boolean = functionCodeName.startsWith("READ_")

    val usesCoilBits: Boolean =
        functionCodeName in
            setOf(
                "READ_COILS",
                "READ_DISCRETE_INPUTS",
                "WRITE_SINGLE_COIL",
                "WRITE_MULTIPLE_COILS",
            )

    val addressSpace: ModbusAddressSpace =
        when (functionCodeName) {
            "READ_COILS" -> ModbusAddressSpace.COIL_READ
            "READ_DISCRETE_INPUTS" -> ModbusAddressSpace.DISCRETE_INPUT
            "READ_INPUT_REGISTERS" -> ModbusAddressSpace.INPUT_REGISTER
            "READ_HOLDING_REGISTERS" -> ModbusAddressSpace.HOLDING_REGISTER_READ
            "WRITE_SINGLE_COIL",
            "WRITE_MULTIPLE_COILS" -> ModbusAddressSpace.COIL_WRITE
            "WRITE_SINGLE_REGISTER",
            "WRITE_MULTIPLE_REGISTERS" -> ModbusAddressSpace.HOLDING_REGISTER_WRITE
            else -> error("未知的 Modbus 功能码：$functionCodeName")
        }

    val registerSpan: Int =
        when (functionCodeName) {
            "WRITE_SINGLE_COIL",
            "WRITE_SINGLE_REGISTER" -> 1
            else -> quantity.coerceAtLeast(1)
        }
}

enum class ModbusWorkflowKind {
    FLASH_FIRMWARE,
}

data class ModbusWorkflowModel(
    val kind: ModbusWorkflowKind,
    val methodName: String,
    val workflowId: String,
    val requestClassName: String,
    val requestQualifiedName: String,
    val bytesParameterName: String,
    val returnType: ModbusReturnTypeModel,
    val doc: ModbusDocModel,
    val startMethodName: String,
    val chunkMethodName: String,
    val commitMethodName: String,
    val resetMethodName: String?,
)

/**
 * 服务定义。
 */
data class ModbusServiceModel(
    val interfacePackage: String,
    val interfaceSimpleName: String,
    val interfaceQualifiedName: String,
    val serviceId: String,
    val summary: String,
    val basePath: String,
    val transport: ModbusTransportKind,
    val doc: ModbusDocModel,
    val operations: List<ModbusOperationModel>,
    val workflows: List<ModbusWorkflowModel> = emptyList(),
) {
    val gatewayClassName: String =
        "${interfaceSimpleName}Generated${transport.transportId.replaceFirstChar(Char::uppercase)}Gateway"
    val configProviderClassName: String =
        "${interfaceSimpleName}Generated${transport.transportId.replaceFirstChar(Char::uppercase)}ConfigProvider"
    val cServiceName: String =
        serviceId
            .replace(Regex("[^A-Za-z0-9]+"), "_")
            .trim('_')
            .ifBlank { interfaceSimpleName.lowercase() }
}

/**
 * 渲染后的文件。
 */
data class GeneratedArtifact(
    val packageName: String?,
    val fileName: String,
    val extensionName: String,
    val content: String,
)

/**
 * 面向协议套件的生成上下文。
 */
data class ModbusProtocolSuiteModel(
    val protocolId: String = "modbus",
    val transport: ModbusTransportKind,
    val services: List<ModbusServiceModel>,
    val transportDefaults: ModbusTransportDefaults = ModbusTransportDefaults(),
)

data class ModbusRtuTransportDefaults(
    val portPath: String = "/dev/ttyUSB0",
    val unitId: Int = 1,
    val baudRate: Int = 9600,
    val dataBits: Int = 8,
    val stopBits: Int = 1,
    val parity: String = "none",
    val timeoutMs: Long = 1_000,
    val retries: Int = 2,
)

data class ModbusTcpTransportDefaults(
    val host: String = "127.0.0.1",
    val port: Int = 502,
    val unitId: Int = 1,
    val timeoutMs: Long = 1_000,
    val retries: Int = 2,
)

data class ModbusMqttTransportDefaults(
    val brokerUrl: String = "tcp://127.0.0.1:1883",
    val clientId: String = "modbus-mqtt-client",
    val requestTopic: String = "modbus/request",
    val responseTopic: String = "modbus/response",
    val qos: Int = 1,
    val timeoutMs: Long = 1_000,
    val retries: Int = 2,
)

data class ModbusTransportDefaults(
    val rtu: ModbusRtuTransportDefaults = ModbusRtuTransportDefaults(),
    val tcp: ModbusTcpTransportDefaults = ModbusTcpTransportDefaults(),
    val mqtt: ModbusMqttTransportDefaults = ModbusMqttTransportDefaults(),
)

enum class ModbusArtifactKind {
    KOTLIN_GATEWAY,
    C_SERVICE_CONTRACT,
    C_TRANSPORT_CONTRACT,
    MARKDOWN_PROTOCOL,
}

enum class ModbusServerRouteMode {
    DIRECT_KTOR,
    SPRING_SOURCE,
}

data class ModbusArtifactRenderContext(
    val kind: ModbusArtifactKind,
    val suite: ModbusProtocolSuiteModel,
    val service: ModbusServiceModel? = null,
    val serverRouteMode: ModbusServerRouteMode = ModbusServerRouteMode.DIRECT_KTOR,
)

/**
 * 数据库存储和跨进程传递使用的 metadata 载荷。
 */
@Serializable
data class ModbusMetadataEnvelope(
    val services: List<ModbusMetadataServicePayload>,
)

@Serializable
data class ModbusMetadataServicePayload(
    val interfacePackage: String,
    val interfaceSimpleName: String,
    val interfaceQualifiedName: String? = null,
    val serviceId: String = "",
    val summary: String = "",
    val basePath: String = "/api/modbus",
    val transport: String,
    val doc: ModbusMetadataDocPayload = ModbusMetadataDocPayload(),
    val operations: List<ModbusMetadataOperationPayload> = emptyList(),
    val workflows: List<ModbusMetadataWorkflowPayload> = emptyList(),
)

@Serializable
data class ModbusMetadataOperationPayload(
    val methodName: String,
    val operationId: String = "",
    val functionCodeName: String = "",
    val address: Int,
    val quantity: Int = -1,
    val requestClassName: String = "",
    val requestQualifiedName: String = "",
    val parameters: List<ModbusMetadataParameterPayload> = emptyList(),
    val returnType: ModbusMetadataReturnTypePayload,
    val doc: ModbusMetadataDocPayload = ModbusMetadataDocPayload(),
)

@Serializable
data class ModbusMetadataWorkflowPayload(
    val kind: String = "FLASH_FIRMWARE",
    val methodName: String,
    val workflowId: String = "",
    val requestClassName: String = "",
    val requestQualifiedName: String = "",
    val bytesParameterName: String = "bytes",
    val returnType: ModbusMetadataReturnTypePayload,
    val doc: ModbusMetadataDocPayload = ModbusMetadataDocPayload(),
    val startMethodName: String = "firmwareStart",
    val chunkMethodName: String = "firmwareChunk",
    val commitMethodName: String = "firmwareCommit",
    val resetMethodName: String? = "resetDevice",
)

@Serializable
data class ModbusMetadataReturnTypePayload(
    val qualifiedName: String,
    val simpleName: String,
    val kind: String,
    val docSummary: String = "",
    val valueKind: String? = null,
    val codecName: String = "AUTO",
    val length: Int = 1,
    val registerWidth: Int? = null,
    val properties: List<ModbusMetadataPropertyPayload> = emptyList(),
)

@Serializable
data class ModbusMetadataPropertyPayload(
    val name: String,
    val qualifiedType: String,
    val valueKind: String,
    val field: ModbusMetadataFieldPayload? = null,
    val doc: String = "",
)

@Serializable
data class ModbusMetadataFieldPayload(
    val codecName: String = "AUTO",
    val registerOffset: Int = -1,
    val bitOffset: Int = -1,
    val length: Int = 1,
    val registerWidth: Int? = null,
)

@Serializable
data class ModbusMetadataParameterPayload(
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
data class ModbusMetadataDocPayload(
    val summary: String = "",
    val descriptionLines: List<String> = emptyList(),
    val parameterDocs: Map<String, String> = emptyMap(),
)
