package site.addzero.device.protocol.modbus.ksp.core

import kotlin.test.Test
import kotlin.test.assertEquals
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
        assertTrue(content.contains("该 gateway 由 KSP 自动生成"))
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
        assertTrue(source.content.contains("return flash_generated_firmware_chunk(input_registers, quantity, &service_result);").not())
        assertTrue(source.content.contains("const bool handled = flash_generated_firmware_chunk(input_registers, quantity, &service_result);"))
        assertTrue(source.content.contains("case SELF_DEV_BOARD_READ_INFO_ADDRESS:"))
        assertTrue(source.content.contains("const bool input_coils[1] = {value};"))
    }

    @Test
    fun renderSemanticCoilDtoArtifactsUseReadCoilsAndBoolFields() {
        val service = semanticCoilService()
        val gateway =
            ModbusArtifactRenderer
                .renderServerArtifacts(ModbusTransportKind.RTU, listOf(service))
                .single()
                .content
        val contractArtifacts = ModbusArtifactRenderer.renderContractArtifacts(service)
        val generatedSource =
            contractArtifacts
                .first { artifact -> artifact.fileName == "device_generated" && artifact.extensionName == "c" }
                .content
        val generatedHeader =
            contractArtifacts
                .first { artifact -> artifact.fileName == "device_generated" && artifact.extensionName == "h" }
                .content
        val dispatch =
            ModbusArtifactRenderer
                .renderTransportContractArtifacts(ModbusTransportKind.RTU, listOf(service))
                .first { artifact -> artifact.extensionName == "c" }
                .content

        assertTrue(gateway.contains("executor.readCoils(resolvedConfig, 0, 24)"))
        assertTrue(gateway.contains("ch24 = ModbusCodecSupport.decodeBoolean(ModbusCodec.BOOL_COIL, registers, 23, 0)"))
        assertTrue(generatedHeader.contains("#define DEVICE_GET_DEVICE_INFO_QUANTITY 24"))
        assertTrue(generatedHeader.contains("bool ch1;"))
        assertTrue(generatedHeader.contains("bool ch24;"))
        assertTrue(generatedSource.contains("out_coils[0] = response.ch1;"))
        assertTrue(generatedSource.contains("out_coils[23] = response.ch24;"))
        assertTrue(dispatch.contains("case DEVICE_GET_DEVICE_INFO_ADDRESS:"))
        assertTrue(dispatch.contains("return device_generated_get_device_info(out_coils, quantity);"))
    }

    @Test
    fun renderRegisterStringArtifactsUseUtf8PackingOnBothSides() {
        val service = semanticRegisterStringService()
        val gateway =
            ModbusArtifactRenderer
                .renderServerArtifacts(ModbusTransportKind.RTU, listOf(service))
                .single()
                .content
        val contractArtifacts = ModbusArtifactRenderer.renderContractArtifacts(service)
        val generatedHeader =
            contractArtifacts
                .first { artifact -> artifact.fileName == "device_generated" && artifact.extensionName == "h" }
                .content
        val generatedSource =
            contractArtifacts
                .first { artifact -> artifact.fileName == "device_generated" && artifact.extensionName == "c" }
                .content
        val protocolDoc =
            contractArtifacts
                .first { artifact -> artifact.fileName == "device.rtu.protocol" && artifact.extensionName == "md" }
                .content

        assertTrue(gateway.contains("executor.readInputRegisters(resolvedConfig, 100, 20)"))
        assertTrue(gateway.contains("deviceName = ModbusCodecSupport.decodeString(ModbusCodec.STRING_UTF8, registers, 4, 16)"))
        assertTrue(generatedHeader.contains("#define DEVICE_GET_DEVICE_RUNTIME_INFO_QUANTITY 20"))
        assertTrue(generatedHeader.contains("/* 设备运行信息。 */"))
        assertTrue(generatedHeader.contains("char device_name[33];"))
        assertTrue(generatedSource.contains("device_generated_encode_string_registers(response.device_name, out_registers, 4, 16);"))
        assertTrue(protocolDoc.contains("| `deviceName` | `String` | `STRING_UTF8` | `4` | `0` | `16` | 设备名称。 |"))
    }

    @Test
    fun renderScalarStringArtifactsUseStringBridgeSignature() {
        val service = scalarStringService()
        val gateway =
            ModbusArtifactRenderer
                .renderServerArtifacts(ModbusTransportKind.RTU, listOf(service))
                .single()
                .content
        val contractArtifacts = ModbusArtifactRenderer.renderContractArtifacts(service)
        val generatedHeader =
            contractArtifacts
                .first { artifact -> artifact.fileName == "device_generated" && artifact.extensionName == "h" }
                .content
        val generatedSource =
            contractArtifacts
                .first { artifact -> artifact.fileName == "device_generated" && artifact.extensionName == "c" }
                .content
        val bridgeHeader =
            contractArtifacts
                .first { artifact -> artifact.fileName == "device_bridge" && artifact.extensionName == "h" }
                .content

        assertTrue(gateway.contains("executor.readInputRegisters(resolvedConfig, 140, 16)"))
        assertTrue(gateway.contains("return ModbusCodecSupport.decodeString(ModbusCodec.STRING_UTF8, registers, 0, 16)"))
        assertTrue(generatedHeader.contains("#define DEVICE_GET_DEVICE_DISPLAY_NAME_QUANTITY 16"))
        assertTrue(generatedSource.contains("char value[33] = {0};"))
        assertTrue(generatedSource.contains("device_bridge_get_device_display_name(value, sizeof(value))"))
        assertTrue(generatedSource.contains("device_generated_encode_string_registers(value, out_registers, 0u, 16);"))
        assertTrue(bridgeHeader.contains("bool device_bridge_get_device_display_name(char *out_value, size_t out_capacity);"))
    }

    @Test
    fun codegenModeAcceptsGatewayAlias() {
        assertEquals(setOf(ModbusCodegenMode.SERVER), ModbusCodegenMode.parse("gateway"))
        assertEquals(setOf(ModbusCodegenMode.SERVER), ModbusCodegenMode.parse("client_gateway"))
        assertEquals(setOf(ModbusCodegenMode.SERVER, ModbusCodegenMode.CONTRACT), ModbusCodegenMode.parse("gateway,contract"))
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

internal fun semanticCoilService(): ModbusServiceModel =
    ModbusServiceModel(
        interfacePackage = "site.addzero.device.api.internal",
        interfaceSimpleName = "DeviceService",
        interfaceQualifiedName = "site.addzero.device.api.internal.DeviceService",
        serviceId = "device",
        summary = "读取 24 路数字输入状态。",
        basePath = "/api/modbus",
        transport = ModbusTransportKind.RTU,
        doc = ModbusDocModel(summary = "设备状态业务接口。"),
        operations =
            listOf(
                ModbusOperationModel(
                    methodName = "getDeviceInfo",
                    operationId = "get-device-info",
                    functionCodeName = "READ_COILS",
                    address = 0,
                    quantity = 24,
                    requestClassName = "DeviceServiceRtuGetDeviceInfoRequest",
                    requestQualifiedName = "site.addzero.generated.DeviceServiceRtuGetDeviceInfoRequest",
                    parameters = emptyList(),
                    returnType =
                        ModbusReturnTypeModel(
                            qualifiedName = "site.addzero.device.api.internal.DeviceInfo24",
                            simpleName = "DeviceInfo24",
                            kind = ModbusReturnKind.DTO,
                            properties =
                                (1..24).map { index ->
                                    ModbusPropertyModel(
                                        name = "ch$index",
                                        qualifiedType = "kotlin.Boolean",
                                        valueKind = ModbusValueKind.BOOLEAN,
                                        field =
                                            ModbusFieldModel(
                                                codecName = "BOOL_COIL",
                                                registerOffset = index - 1,
                                                bitOffset = 0,
                                                length = 1,
                                                registerWidth = 1,
                                            ),
                                        doc = "通道 $index 当前状态。",
                                    )
                                },
                        ),
                    doc = ModbusDocModel(summary = "读取设备信息。"),
                )
            ),
    )

internal fun semanticRegisterStringService(): ModbusServiceModel =
    ModbusServiceModel(
        interfacePackage = "site.addzero.device.api.internal",
        interfaceSimpleName = "DeviceService",
        interfaceQualifiedName = "site.addzero.device.api.internal.DeviceService",
        serviceId = "device",
        summary = "读取设备运行信息。",
        basePath = "/api/modbus",
        transport = ModbusTransportKind.RTU,
        doc = ModbusDocModel(summary = "设备运行信息业务接口。"),
        operations =
            listOf(
                ModbusOperationModel(
                    methodName = "getDeviceRuntimeInfo",
                    operationId = "get-device-runtime-info",
                    functionCodeName = "READ_INPUT_REGISTERS",
                    address = 100,
                    quantity = 20,
                    requestClassName = "DeviceServiceRtuGetDeviceRuntimeInfoRequest",
                    requestQualifiedName = "site.addzero.generated.DeviceServiceRtuGetDeviceRuntimeInfoRequest",
                    parameters = emptyList(),
                    returnType =
                    ModbusReturnTypeModel(
                        qualifiedName = "site.addzero.device.api.internal.DeviceRuntimeInfo",
                        simpleName = "DeviceRuntimeInfo",
                        kind = ModbusReturnKind.DTO,
                        docSummary = "设备运行信息。",
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
                                        name = "channelCount",
                                        qualifiedType = "kotlin.Int",
                                        valueKind = ModbusValueKind.INT,
                                        field = ModbusFieldModel(codecName = "U16", registerOffset = 1, bitOffset = 0, length = 1, registerWidth = 1),
                                        doc = "通道总数。",
                                    ),
                                    ModbusPropertyModel(
                                        name = "unitId",
                                        qualifiedType = "kotlin.Int",
                                        valueKind = ModbusValueKind.INT,
                                        field = ModbusFieldModel(codecName = "U16", registerOffset = 2, bitOffset = 0, length = 1, registerWidth = 1),
                                        doc = "从站地址。",
                                    ),
                                    ModbusPropertyModel(
                                        name = "baudRateCode",
                                        qualifiedType = "kotlin.Int",
                                        valueKind = ModbusValueKind.INT,
                                        field = ModbusFieldModel(codecName = "U16", registerOffset = 3, bitOffset = 0, length = 1, registerWidth = 1),
                                        doc = "波特率编码。",
                                    ),
                                    ModbusPropertyModel(
                                        name = "deviceName",
                                        qualifiedType = "kotlin.String",
                                        valueKind = ModbusValueKind.STRING,
                                        field = ModbusFieldModel(codecName = "STRING_UTF8", registerOffset = 4, bitOffset = 0, length = 16, registerWidth = 16),
                                        doc = "设备名称。",
                                    ),
                                ),
                        ),
                    doc = ModbusDocModel(summary = "读取设备运行信息。"),
                )
            ),
    )

internal fun scalarStringService(): ModbusServiceModel =
    ModbusServiceModel(
        interfacePackage = "site.addzero.device.api.internal",
        interfaceSimpleName = "DeviceService",
        interfaceQualifiedName = "site.addzero.device.api.internal.DeviceService",
        serviceId = "device",
        summary = "读取设备显示名称。",
        basePath = "/api/modbus",
        transport = ModbusTransportKind.RTU,
        doc = ModbusDocModel(summary = "设备显示名称业务接口。"),
        operations =
            listOf(
                ModbusOperationModel(
                    methodName = "getDeviceDisplayName",
                    operationId = "get-device-display-name",
                    functionCodeName = "READ_INPUT_REGISTERS",
                    address = 140,
                    quantity = 16,
                    requestClassName = "DeviceServiceRtuGetDeviceDisplayNameRequest",
                    requestQualifiedName = "site.addzero.generated.DeviceServiceRtuGetDeviceDisplayNameRequest",
                    parameters = emptyList(),
                    returnType =
                        ModbusReturnTypeModel(
                            qualifiedName = "kotlin.String",
                            simpleName = "String",
                            kind = ModbusReturnKind.STRING,
                            valueKind = ModbusValueKind.STRING,
                            codecName = "STRING_UTF8",
                            length = 16,
                            registerWidth = 16,
                        ),
                    doc = ModbusDocModel(summary = "读取设备显示名称。"),
                )
            ),
    )
