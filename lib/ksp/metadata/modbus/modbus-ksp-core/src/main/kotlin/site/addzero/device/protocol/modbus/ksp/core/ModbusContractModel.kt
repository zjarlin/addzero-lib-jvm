package site.addzero.device.protocol.modbus.ksp.core

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSFile

/**
 * Modbus 注解的限定名常量。
 */
object ModbusAnnotationNames {
    const val generateRtuServer = "site.addzero.device.protocol.modbus.annotation.GenerateModbusRtuServer"
    const val generateTcpServer = "site.addzero.device.protocol.modbus.annotation.GenerateModbusTcpServer"
    const val operation = "site.addzero.device.protocol.modbus.annotation.ModbusOperation"
    const val param = "site.addzero.device.protocol.modbus.annotation.ModbusParam"
    const val field = "site.addzero.device.protocol.modbus.annotation.ModbusField"
}

/**
 * 生成模式。
 *
 * 支持通过 `addzero.modbus.codegen.mode` 传单值或多值：
 * - `server`
 * - `gateway`
 * - `contract`
 * - `server,contract`
 * - `gateway,contract`
 */
enum class ModbusCodegenMode {
    CONTRACT,
    SERVER;

    companion object {
        fun from(environment: SymbolProcessorEnvironment): Set<ModbusCodegenMode> =
            parse(environment.options["addzero.modbus.codegen.mode"])

        fun parse(rawValue: String?): Set<ModbusCodegenMode> {
            val tokens =
                rawValue
                    ?.split(',', ';', '\n')
                    ?.map(String::trim)
                    ?.filter(String::isNotBlank)
                    .orEmpty()
            if (tokens.isEmpty()) {
                return linkedSetOf(SERVER)
            }

            val resolved = linkedSetOf<ModbusCodegenMode>()
            tokens.forEach { token ->
                when (token.lowercase()) {
                    "server", "gateway", "client_gateway" -> resolved += SERVER
                    "contract" -> resolved += CONTRACT
                    else -> {
                        error(
                            "未知的 Modbus 代码生成模式：$rawValue；可选值为 server、gateway、client_gateway、contract，支持逗号分隔多选。",
                        )
                    }
                }
            }
            return resolved
        }
    }
}

/**
 * 传输类型。
 */
enum class ModbusTransportKind(
    val markerAnnotationName: String,
    val transportId: String,
    val displayName: String,
    val generatedPackage: String,
    val generatedFileName: String,
) {
    RTU(
        markerAnnotationName = ModbusAnnotationNames.generateRtuServer,
        transportId = "rtu",
        displayName = "Modbus RTU",
        generatedPackage = "site.addzero.esp32_host_computer.generated.modbus.rtu",
        generatedFileName = "GeneratedModbusRtu",
    ),
    TCP(
        markerAnnotationName = ModbusAnnotationNames.generateTcpServer,
        transportId = "tcp",
        displayName = "Modbus TCP",
        generatedPackage = "site.addzero.esp32_host_computer.generated.modbus.tcp",
        generatedFileName = "GeneratedModbusTcp",
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
    val fullText =
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
    val isReadOperation = functionCodeName.startsWith("READ_")

    val usesCoilBits =
        functionCodeName in
            setOf(
                "READ_COILS",
                "READ_DISCRETE_INPUTS",
                "WRITE_SINGLE_COIL",
                "WRITE_MULTIPLE_COILS",
            )

    val addressSpace =
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

    val registerSpan =
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
    val gatewayClassName = "${interfaceSimpleName}Generated${transport.transportId.replaceFirstChar(Char::uppercase)}Gateway"
    val configProviderClassName =
        "${interfaceSimpleName}Generated${transport.transportId.replaceFirstChar(Char::uppercase)}ConfigProvider"
    val cServiceName = serviceId.replace(Regex("[^A-Za-z0-9]+"), "_").trim('_').ifBlank { interfaceSimpleName.lowercase() }
}

/**
 * 采集结果。
 */
data class CollectedModbusService(
    val model: ModbusServiceModel,
    val originatingFiles: List<KSFile> = emptyList(),
)

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
 *
 * 这里把“语义契约 -> 多产物协议暴露”的输入统一建模，
 * 方便后续在 Modbus / MQTT 间复用同一套 suite 级 SPI 设计。
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

data class ModbusTransportDefaults(
    val rtu: ModbusRtuTransportDefaults = ModbusRtuTransportDefaults(),
    val tcp: ModbusTcpTransportDefaults = ModbusTcpTransportDefaults(),
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
