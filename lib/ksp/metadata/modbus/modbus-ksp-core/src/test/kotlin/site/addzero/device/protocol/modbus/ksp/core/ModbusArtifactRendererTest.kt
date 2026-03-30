package site.addzero.device.protocol.modbus.ksp.core

import kotlin.test.Test
import kotlin.test.assertTrue

class ModbusArtifactRendererTest {
    @Test
    fun renderContractArtifactsKeepsChineseComments() {
        val artifacts = ModbusArtifactRenderer.renderContractArtifacts(sampleService())
        val header = artifacts.first { artifact -> artifact.fileName == "self_dev_board_generated" && artifact.extensionName == "h" }

        assertTrue(header.content.contains("读取设备信息。"))
        assertTrue(header.content.contains("固件同事"))
    }

    @Test
    fun renderContractArtifactsEmitsMarkdownTables() {
        val artifacts =
            ModbusArtifactRenderer.renderContractArtifacts(
                sampleService(operations = sampleService().operations + sampleSetLedOperation())
            )
        val protocolDoc = artifacts.first { artifact -> artifact.fileName == "self-dev-board.rtu.protocol" && artifact.extensionName == "md" }

        assertTrue(protocolDoc.content.contains("| Key | Value |"))
        assertTrue(protocolDoc.content.contains("| Operation ID | Method | Function Code | Address | Quantity | Return | Summary |"))
        assertTrue(protocolDoc.content.contains("| Name | Type | Codec | Register Offset | Bit Offset | Width | Description |"))
        assertTrue(protocolDoc.content.contains("| `on` | `Boolean` | `BOOL_COIL` | `0` | `0` | `1` | LED 是否点亮。 |"))
        assertTrue(protocolDoc.content.contains("| `protocolVersion` | `Int` | `U16` | `0` | `0` | `1` | 协议版本。 |"))
    }

    @Test
    fun renderServerArtifactsContainsTypedRoute() {
        val content =
            ModbusArtifactRenderer
                .renderServerArtifacts(ModbusTransportKind.RTU, listOf(sampleService()))
                .single()
                .content

        assertTrue(content.contains("post(\"/api/modbus/rtu/self-dev-board/read-info\")"))
        assertTrue(content.contains("class SelfDevBoardApiGeneratedRtuGateway"))
        assertTrue(content.contains("@Module"))
        assertTrue(content.contains("class GeneratedModbusRtuKoinModule"))
        assertTrue(content.contains("fun modbusRtuConfigRegistry("))
    }

    @Test
    fun renderTransportContractArtifactsEmitsDispatcher() {
        val artifacts =
            ModbusArtifactRenderer.renderTransportContractArtifacts(
                transport = ModbusTransportKind.RTU,
                services =
                    listOf(
                        sampleService(operations = listOf(sampleReadInfoOperation(), sampleSetLedOperation())),
                        sampleFlashService(),
                    ),
            )
        val header = artifacts.first { artifact -> artifact.fileName == "modbus_rtu_dispatch" && artifact.extensionName == "h" }
        val source = artifacts.first { artifact -> artifact.fileName == "modbus_rtu_dispatch" && artifact.extensionName == "c" }

        assertTrue(header.content.contains("bool modbus_rtu_dispatch_read_coils("))
        assertTrue(header.content.contains("bool modbus_rtu_dispatch_write_multiple_coils("))
        assertTrue(header.content.contains("bool modbus_rtu_dispatch_write_multiple_registers("))
        assertTrue(source.content.contains("case FLASH_FIRMWARE_CHUNK_ADDRESS:"))
        assertTrue(source.content.contains("return flash_handle_firmware_chunk(input_registers, quantity, &service_result);").not())
        assertTrue(source.content.contains("const bool handled = flash_handle_firmware_chunk(input_registers, quantity, &service_result);"))
        assertTrue(source.content.contains("case SELF_DEV_BOARD_READ_INFO_ADDRESS:"))
        assertTrue(source.content.contains("const bool input_coils[1] = {value};"))
    }

    @Test
    fun renderContractArtifactsUseStrictInputRegisterValidation() {
        val source =
            ModbusArtifactRenderer
                .renderContractArtifacts(sampleFlashService())
                .first { artifact -> artifact.fileName == "flash_generated" && artifact.extensionName == "c" }
                .content

        assertTrue(source.contains("if (out_result == NULL || input_registers == NULL || register_count < 4) {"))
        assertTrue(source.contains("if (out_result == NULL || input_registers == NULL || register_count < 10) {"))
    }
}

internal fun sampleSetLedOperation(): ModbusOperationModel =
    ModbusOperationModel(
        methodName = "setLed",
        operationId = "set-led",
        functionCodeName = "WRITE_SINGLE_COIL",
        address = 0,
        quantity = 1,
        requestClassName = "SampleSetLedRequest",
        requestQualifiedName = "site.addzero.generated.SampleSetLedRequest",
        parameters =
            listOf(
                ModbusParameterModel(
                    name = "on",
                    qualifiedType = "kotlin.Boolean",
                    valueKind = ModbusValueKind.BOOLEAN,
                    order = 0,
                    codecName = "BOOL_COIL",
                    registerOffset = 0,
                    bitOffset = 0,
                    registerWidth = 1,
                    doc = "LED 是否点亮。",
                )
            ),
        returnType =
            ModbusReturnTypeModel(
                qualifiedName = "site.addzero.device.protocol.modbus.model.ModbusCommandResult",
                simpleName = "ModbusCommandResult",
                kind = ModbusReturnKind.COMMAND_RESULT,
            ),
        doc = ModbusDocModel(summary = "设置板载 LED 状态。"),
    )

internal fun sampleFirmwareChunkOperation(): ModbusOperationModel =
    ModbusOperationModel(
        methodName = "firmwareChunk",
        operationId = "firmware-chunk",
        functionCodeName = "WRITE_MULTIPLE_REGISTERS",
        address = 42,
        quantity = 10,
        requestClassName = "SampleFirmwareChunkRequest",
        requestQualifiedName = "site.addzero.generated.SampleFirmwareChunkRequest",
        parameters =
            listOf(
                ModbusParameterModel(
                    name = "sequence",
                    qualifiedType = "kotlin.Int",
                    valueKind = ModbusValueKind.INT,
                    order = 0,
                    codecName = "U16",
                    registerOffset = 0,
                    bitOffset = 0,
                    registerWidth = 1,
                    doc = "块序号。",
                ),
                ModbusParameterModel(
                    name = "usedBytes",
                    qualifiedType = "kotlin.Int",
                    valueKind = ModbusValueKind.INT,
                    order = 1,
                    codecName = "U16",
                    registerOffset = 1,
                    bitOffset = 0,
                    registerWidth = 1,
                    doc = "有效字节数。",
                ),
                ModbusParameterModel(
                    name = "word0",
                    qualifiedType = "kotlin.Int",
                    valueKind = ModbusValueKind.INT,
                    order = 2,
                    codecName = "U32_BE",
                    registerOffset = 2,
                    bitOffset = 0,
                    registerWidth = 2,
                    doc = "数据字 0。",
                ),
                ModbusParameterModel(
                    name = "word1",
                    qualifiedType = "kotlin.Int",
                    valueKind = ModbusValueKind.INT,
                    order = 3,
                    codecName = "U32_BE",
                    registerOffset = 4,
                    bitOffset = 0,
                    registerWidth = 2,
                    doc = "数据字 1。",
                ),
                ModbusParameterModel(
                    name = "word2",
                    qualifiedType = "kotlin.Int",
                    valueKind = ModbusValueKind.INT,
                    order = 4,
                    codecName = "U32_BE",
                    registerOffset = 6,
                    bitOffset = 0,
                    registerWidth = 2,
                    doc = "数据字 2。",
                ),
                ModbusParameterModel(
                    name = "word3",
                    qualifiedType = "kotlin.Int",
                    valueKind = ModbusValueKind.INT,
                    order = 5,
                    codecName = "U32_BE",
                    registerOffset = 8,
                    bitOffset = 0,
                    registerWidth = 2,
                    doc = "数据字 3。",
                ),
            ),
        returnType =
            ModbusReturnTypeModel(
                qualifiedName = "site.addzero.device.protocol.modbus.model.ModbusCommandResult",
                simpleName = "ModbusCommandResult",
                kind = ModbusReturnKind.COMMAND_RESULT,
            ),
        doc = ModbusDocModel(summary = "发送一块固件数据。"),
    )

internal fun sampleFirmwareStartOperation(): ModbusOperationModel =
    ModbusOperationModel(
        methodName = "firmwareStart",
        operationId = "firmware-start",
        functionCodeName = "WRITE_MULTIPLE_REGISTERS",
        address = 24,
        quantity = 4,
        requestClassName = "SampleFirmwareStartRequest",
        requestQualifiedName = "site.addzero.generated.SampleFirmwareStartRequest",
        parameters =
            listOf(
                ModbusParameterModel(
                    name = "totalBytes",
                    qualifiedType = "kotlin.Int",
                    valueKind = ModbusValueKind.INT,
                    order = 0,
                    codecName = "U32_BE",
                    registerOffset = 0,
                    bitOffset = 0,
                    registerWidth = 2,
                    doc = "固件总长度。",
                ),
                ModbusParameterModel(
                    name = "crc32",
                    qualifiedType = "kotlin.Int",
                    valueKind = ModbusValueKind.INT,
                    order = 1,
                    codecName = "U32_BE",
                    registerOffset = 2,
                    bitOffset = 0,
                    registerWidth = 2,
                    doc = "CRC32 校验值。",
                ),
            ),
        returnType =
            ModbusReturnTypeModel(
                qualifiedName = "site.addzero.device.protocol.modbus.model.ModbusCommandResult",
                simpleName = "ModbusCommandResult",
                kind = ModbusReturnKind.COMMAND_RESULT,
            ),
        doc = ModbusDocModel(summary = "初始化一次烧录会话。"),
    )

internal fun sampleReadInfoOperation(): ModbusOperationModel = sampleService().operations.single()

internal fun sampleFlashService(): ModbusServiceModel =
    ModbusServiceModel(
        interfacePackage = "site.addzero.device.api.internal",
        interfaceSimpleName = "FlashApi",
        interfaceQualifiedName = "site.addzero.device.api.internal.FlashApi",
        serviceId = "flash",
        summary = "烧录动作。",
        basePath = "/api/modbus",
        transport = ModbusTransportKind.RTU,
        doc = ModbusDocModel(summary = "固件烧录接口。"),
        operations = listOf(sampleFirmwareStartOperation(), sampleFirmwareChunkOperation()),
    )

internal fun sampleService(
    operations: List<ModbusOperationModel> =
        listOf(
            ModbusOperationModel(
                methodName = "readInfo",
                operationId = "read-info",
                functionCodeName = "READ_INPUT_REGISTERS",
                address = 0,
                quantity = 4,
                requestClassName = "SampleReadInfoRequest",
                requestQualifiedName = "site.addzero.generated.SampleReadInfoRequest",
                parameters = emptyList(),
                returnType =
                    ModbusReturnTypeModel(
                        qualifiedName = "site.addzero.device.api.external.SelfDevBoardInfo",
                        simpleName = "SelfDevBoardInfo",
                        kind = ModbusReturnKind.DTO,
                        properties =
                            listOf(
                                ModbusPropertyModel(
                                    name = "protocolVersion",
                                    qualifiedType = "kotlin.Int",
                                    valueKind = ModbusValueKind.INT,
                                    field = ModbusFieldModel(codecName = "U16", registerOffset = 0, bitOffset = 0, length = 1, registerWidth = 1),
                                    doc = "协议版本。",
                                ),
                                ModbusPropertyModel(
                                    name = "ledEnabled",
                                    qualifiedType = "kotlin.Boolean",
                                    valueKind = ModbusValueKind.BOOLEAN,
                                    field = ModbusFieldModel(codecName = "BIT_FLAG", registerOffset = 3, bitOffset = 0, length = 1, registerWidth = 1),
                                    doc = "LED 是否点亮。",
                                ),
                            ),
                    ),
                doc = ModbusDocModel(summary = "读取设备信息。"),
            )
        ),
): ModbusServiceModel =
    ModbusServiceModel(
        interfacePackage = "site.addzero.device.api.internal",
        interfaceSimpleName = "SelfDevBoardApi",
        interfaceQualifiedName = "site.addzero.device.api.internal.SelfDevBoardApi",
        serviceId = "self-dev-board",
        summary = "自研控制板的基础探测、控制与升级能力。",
        basePath = "/api/modbus",
        transport = ModbusTransportKind.RTU,
        doc =
            ModbusDocModel(
                summary = "自研控制板业务接口。",
                descriptionLines = listOf("固件同事可以基于这里继续完善业务实现。"),
            ),
        operations = operations,
    )
