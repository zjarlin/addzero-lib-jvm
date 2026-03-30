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
 * - `contract`
 * - `server,contract`
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
                    "server" -> resolved += SERVER
                    "contract" -> resolved += CONTRACT
                    else -> {
                        error(
                            "未知的 Modbus 代码生成模式：$rawValue；可选值为 server、contract，支持逗号分隔多选。",
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
}

/**
 * 返回类型分类。
 */
enum class ModbusReturnKind {
    UNIT,
    BOOLEAN,
    INT,
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
    val capabilityKey: String,
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
) {
    val gatewayClassName: String = "${interfaceSimpleName}Generated${transport.transportId.replaceFirstChar(Char::uppercase)}Gateway"
    val configProviderClassName: String =
        "${interfaceSimpleName}Generated${transport.transportId.replaceFirstChar(Char::uppercase)}ConfigProvider"
    val cServiceName: String = serviceId.replace(Regex("[^A-Za-z0-9]+"), "_").trim('_').ifBlank { interfaceSimpleName.lowercase() }
}

/**
 * 采集结果。
 */
data class CollectedModbusService(
    val model: ModbusServiceModel,
    val originatingFiles: List<KSFile>,
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
