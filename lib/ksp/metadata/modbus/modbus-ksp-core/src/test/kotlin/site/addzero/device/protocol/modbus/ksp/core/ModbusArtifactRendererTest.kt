package site.addzero.device.protocol.modbus.ksp.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
        val bridgeSample = artifacts.first { artifact -> artifact.fileName == "self_dev_board_bridge_sample" && artifact.extensionName == "c" }

        assertTrue(protocolDoc.content.contains("| 项目 | 内容 |"))
        assertTrue(protocolDoc.content.contains("## 联调说明"))
        assertTrue(protocolDoc.content.contains("固件侧需要实现的入口是"))
        assertTrue(protocolDoc.content.contains("已经存在，重新生成时不会覆盖"))
        assertTrue(protocolDoc.content.contains("bridge_sample.c"))
        assertTrue(protocolDoc.content.contains("联调时以上表 `address`、`quantity`、`function code`、标准码值为准。"))
        assertTrue(protocolDoc.content.contains("`STRING_UTF8` 字段的 `Width` 表示寄存器个数"))
        assertTrue(protocolDoc.content.contains("## 仿真软件怎么填"))
        assertTrue(protocolDoc.content.contains("| 操作标识 | 方法 | 标准功能码 | 标准码值 | 标准含义 | 地址 | 数量 | 返回类型 | 说明 |"))
        assertTrue(protocolDoc.content.contains("### RTU 报文示例"))
        assertTrue(protocolDoc.content.contains("### RTU 请求帧拆解"))
        assertTrue(protocolDoc.content.contains("| 名称 | 类型 | 编码 | 寄存器偏移 | 位偏移 | 宽度 | 说明 |"))
        assertTrue(protocolDoc.content.contains("| `on` | `Boolean` | `BOOL_COIL` | `0` | `0` | `1` | LED 是否点亮。 |"))
        assertTrue(protocolDoc.content.contains("| `protocolVersion` | `Int` | `U16` | `0` | `0` | `1` | 协议版本。 |"))
        assertTrue(bridgeSample.content.contains("请勿手动修改此文件。"))
        assertTrue(bridgeSample.content.contains("更新日期："))
        assertTrue(bridgeSample.content.contains("self_dev_board_bridge_impl.c"))
        assertTrue(bridgeSample.content.contains("#include \"self_dev_board/self_dev_board_bridge.h\""))
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
        val bridgeImpl =
            contractArtifacts
                .first { artifact -> artifact.fileName == "device_bridge_impl" && artifact.extensionName == "c" }
                .content
        val protocolDoc =
            contractArtifacts
                .first { artifact -> artifact.fileName == "device.rtu.protocol" && artifact.extensionName == "md" }
                .content
        val bridgeSample =
            contractArtifacts
                .first { artifact -> artifact.fileName == "device_bridge_sample" && artifact.extensionName == "c" }
                .content

        assertTrue(gateway.contains("executor.readInputRegisters(resolvedConfig, 100, 20)"))
        assertTrue(gateway.contains("deviceName = ModbusCodecSupport.decodeString(ModbusCodec.STRING_UTF8, registers, 4, 16)"))
        assertTrue(generatedHeader.contains("#define DEVICE_GET_DEVICE_RUNTIME_INFO_QUANTITY 20"))
        assertTrue(generatedHeader.contains("/* 设备运行信息。 */"))
        assertTrue(generatedHeader.contains("char device_name[33];"))
        assertTrue(generatedSource.contains("device_generated_encode_string_registers(response.device_name, out_registers, 4, 16);"))
        assertTrue(bridgeImpl.contains("#include <string.h>"))
        assertTrue(bridgeImpl.contains("static void device_bridge_copy_text(char *out_text, size_t out_capacity, const char *input) {"))
        assertTrue(bridgeImpl.contains("device_name 字符串：codec=STRING_UTF8，寄存器宽度=16，最多 32 个字节，缓冲区容量 33（含 '\\0'）。"))
        assertTrue(bridgeImpl.contains(" * device_bridge_copy_text(out_response->device_name, sizeof(out_response->device_name), \"XXXXXXXX-XXXXX\");"))
        assertTrue(bridgeImpl.contains("device_bridge_copy_text(out_response->device_name, sizeof(out_response->device_name), \"\");"))
        assertFalse(bridgeImpl.contains("out_response->device_name[0] = '\\0';"))
        assertFalse(bridgeImpl.contains("out_response->device_name[32] = '\\0';"))
        assertTrue(protocolDoc.contains("## 联调说明"))
        assertTrue(protocolDoc.contains("`device_bridge_impl.c`"))
        assertTrue(protocolDoc.contains("`device_bridge_sample.c`"))
        assertFalse(protocolDoc.contains("这份文档由 Modbus contract KSP 自动生成。"))
        assertTrue(protocolDoc.contains("| `deviceName` | `String` | `STRING_UTF8` | `4` | `0` | `16` | 设备名称。 |"))
        assertTrue(bridgeSample.contains("更新日期："))
        assertTrue(bridgeSample.contains("device_bridge_copy_text(out_response->device_name, sizeof(out_response->device_name), \"\");"))
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
        val bridgeImpl =
            contractArtifacts
                .first { artifact -> artifact.fileName == "device_bridge_impl" && artifact.extensionName == "c" }
                .content

        assertTrue(gateway.contains("executor.readInputRegisters(resolvedConfig, 140, 16)"))
        assertTrue(gateway.contains("return ModbusCodecSupport.decodeString(ModbusCodec.STRING_UTF8, registers, 0, 16)"))
        assertTrue(generatedHeader.contains("#define DEVICE_GET_DEVICE_DISPLAY_NAME_QUANTITY 16"))
        assertTrue(generatedSource.contains("char value[33] = {0};"))
        assertTrue(generatedSource.contains("device_bridge_get_device_display_name(value, sizeof(value))"))
        assertTrue(generatedSource.contains("device_generated_encode_string_registers(value, out_registers, 0u, 16);"))
        assertTrue(bridgeHeader.contains("bool device_bridge_get_device_display_name(char *out_value, size_t out_capacity);"))
        assertTrue(bridgeImpl.contains("返回值字符串：codec=STRING_UTF8，寄存器宽度=16，最多 32 个字节，out_capacity 包含结尾 '\\0'。"))
        assertTrue(bridgeImpl.contains("/* 示例：device_bridge_copy_text(out_value, out_capacity, \"XXXXXXXX-XXXXX\"); */"))
        assertTrue(bridgeImpl.contains("device_bridge_copy_text(out_value, out_capacity, \"\");"))
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

    @Test
    fun renderFlashWorkflowGatewayAndDocs() {
        val service = sampleFlashWorkflowService()
        val gateway =
            ModbusArtifactRenderer
                .renderServerArtifacts(ModbusTransportKind.RTU, listOf(service))
                .single()
                .content
        val contractArtifacts = ModbusArtifactRenderer.renderContractArtifacts(service)
        val bridgeHeader =
            contractArtifacts
                .first { artifact -> artifact.fileName == "flash_bridge" && artifact.extensionName == "h" }
                .content
        val bridgeImpl =
            contractArtifacts
                .first { artifact -> artifact.fileName == "flash_bridge_impl" && artifact.extensionName == "c" }
                .content
        val markdown =
            contractArtifacts
                .first { artifact -> artifact.fileName == "flash.rtu.protocol" && artifact.extensionName == "md" }
                .content

        assertTrue(gateway.contains("override suspend fun flashFirmware(bytes: ByteArray): site.addzero.device.contract.FlashResult"))
        assertTrue(gateway.contains("require(bytes.isNotEmpty()) { \"flashFirmware bytes must not be empty\" }"))
        assertTrue(gateway.contains("val firmwareCrc32 = generatedModbusCrc32(bytes)"))
        assertTrue(gateway.contains("val maxPayloadBytesPerChunk = 16"))
        assertTrue(gateway.contains("val startResult = firmwareStart(config = resolvedConfig, totalBytes = totalBytes, crc32 = firmwareCrc32)"))
        assertTrue(gateway.contains("val chunkResult = firmwareChunk(config = resolvedConfig, sequence = sequence, usedBytes = chunkBytes.size, word0 = word0, word1 = word1, word2 = word2, word3 = word3)"))
        assertTrue(gateway.contains("val commitResult = firmwareCommit(config = resolvedConfig, totalChunks = totalChunks)"))
        assertTrue(gateway.contains("val resetResult = resetDevice(config = resolvedConfig, trigger = true)"))
        assertTrue(gateway.contains("crc32 = firmwareCrc32"))
        assertTrue(bridgeHeader.contains("高层工作流：flashFirmware(bytes)"))
        assertTrue(bridgeHeader.contains("属于高层 flashFirmware(bytes) 工作流的低层步骤。"))
        assertTrue(bridgeImpl.contains("Kotlin 上位机会自动计算 CRC32、切片并顺序调用 begin/chunk/commit/reset"))
        assertTrue(markdown.contains("## 联调说明"))
        assertTrue(markdown.contains("联调时以上表 `address`、`quantity`、`function code`、标准码值为准。"))
        assertTrue(markdown.contains("## 工作流总览"))
        assertTrue(markdown.contains("## 工作流 `flash-firmware`"))
        assertTrue(markdown.contains("### RTU 报文示例"))
        assertTrue(markdown.contains("单片最大字节数: `16`"))
        assertTrue(markdown.contains("CRC32: 由上位机自动计算并通过 firmwareStart 下发"))
    }

    @Test
    fun renderFlashWorkflowWithoutCrcUsesNullResultField() {
        val gateway =
            ModbusArtifactRenderer
                .renderServerArtifacts(ModbusTransportKind.RTU, listOf(sampleFlashWorkflowServiceWithoutCrc()))
                .single()
                .content

        assertTrue(gateway.contains("val startResult = firmwareStart(config = resolvedConfig, totalBytes = totalBytes)"))
        assertTrue(gateway.contains("crc32 = null"))
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

internal fun sampleFirmwareStartOperationWithoutCrc(): ModbusOperationModel =
    sampleFirmwareStartOperation().copy(
        quantity = 2,
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
            ),
    )

internal fun sampleFirmwareCommitOperation(): ModbusOperationModel =
    ModbusOperationModel(
        methodName = "firmwareCommit",
        operationId = "firmware-commit",
        functionCodeName = "WRITE_SINGLE_REGISTER",
        address = 60,
        quantity = 1,
        requestClassName = "SampleFirmwareCommitRequest",
        requestQualifiedName = "site.addzero.generated.SampleFirmwareCommitRequest",
        parameters =
            listOf(
                ModbusParameterModel(
                    name = "totalChunks",
                    qualifiedType = "kotlin.Int",
                    valueKind = ModbusValueKind.INT,
                    order = 0,
                    codecName = "U16",
                    registerOffset = 0,
                    bitOffset = 0,
                    registerWidth = 1,
                    doc = "总分片数。",
                ),
            ),
        returnType =
            ModbusReturnTypeModel(
                qualifiedName = "site.addzero.device.protocol.modbus.model.ModbusCommandResult",
                simpleName = "ModbusCommandResult",
                kind = ModbusReturnKind.COMMAND_RESULT,
            ),
        doc = ModbusDocModel(summary = "提交烧录结果。"),
    )

internal fun sampleResetDeviceOperation(): ModbusOperationModel =
    ModbusOperationModel(
        methodName = "resetDevice",
        operationId = "reset-device",
        functionCodeName = "WRITE_SINGLE_COIL",
        address = 64,
        quantity = 1,
        requestClassName = "SampleResetDeviceRequest",
        requestQualifiedName = "site.addzero.generated.SampleResetDeviceRequest",
        parameters =
            listOf(
                ModbusParameterModel(
                    name = "trigger",
                    qualifiedType = "kotlin.Boolean",
                    valueKind = ModbusValueKind.BOOLEAN,
                    order = 0,
                    codecName = "BOOL_COIL",
                    registerOffset = 0,
                    bitOffset = 0,
                    registerWidth = 1,
                    doc = "写入 true 时触发复位。",
                ),
            ),
        returnType =
            ModbusReturnTypeModel(
                qualifiedName = "site.addzero.device.protocol.modbus.model.ModbusCommandResult",
                simpleName = "ModbusCommandResult",
                kind = ModbusReturnKind.COMMAND_RESULT,
            ),
        doc = ModbusDocModel(summary = "触发设备复位。"),
    )

internal fun sampleFlashWorkflowModel(): ModbusWorkflowModel =
    ModbusWorkflowModel(
        kind = ModbusWorkflowKind.FLASH_FIRMWARE,
        methodName = "flashFirmware",
        workflowId = "flash-firmware",
        requestClassName = "FlashApiRtuFlashFirmwareRequest",
        requestQualifiedName = "site.addzero.generated.FlashApiRtuFlashFirmwareRequest",
        bytesParameterName = "bytes",
        returnType =
            ModbusReturnTypeModel(
                qualifiedName = "site.addzero.device.contract.FlashResult",
                simpleName = "FlashResult",
                kind = ModbusReturnKind.DTO,
                properties =
                    listOf(
                        ModbusPropertyModel("accepted", "kotlin.Boolean", ModbusValueKind.BOOLEAN, field = null, doc = "是否成功。"),
                        ModbusPropertyModel("summary", "kotlin.String", ModbusValueKind.STRING, field = null, doc = "执行摘要。"),
                        ModbusPropertyModel("totalBytes", "kotlin.Int", ModbusValueKind.INT, field = null, doc = "总字节数。"),
                        ModbusPropertyModel("totalChunks", "kotlin.Int", ModbusValueKind.INT, field = null, doc = "总分片数。"),
                        ModbusPropertyModel("crc32", "kotlin.Int", ModbusValueKind.INT, field = null, doc = "CRC32。"),
                        ModbusPropertyModel("resetIssued", "kotlin.Boolean", ModbusValueKind.BOOLEAN, field = null, doc = "是否已复位。"),
                    ),
            ),
        doc =
            ModbusDocModel(
                summary = "执行完整固件烧录工作流。",
                parameterDocs = mapOf("bytes" to "待烧录的完整固件字节数组。"),
            ),
        startMethodName = "firmwareStart",
        chunkMethodName = "firmwareChunk",
        commitMethodName = "firmwareCommit",
        resetMethodName = "resetDevice",
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
        operations = listOf(sampleResetDeviceOperation(), sampleFirmwareStartOperation(), sampleFirmwareChunkOperation(), sampleFirmwareCommitOperation()),
    )

internal fun sampleFlashWorkflowService(): ModbusServiceModel =
    sampleFlashService().copy(
        workflows = listOf(sampleFlashWorkflowModel()),
    )

internal fun sampleFlashWorkflowServiceWithoutCrc(): ModbusServiceModel =
    sampleFlashService().copy(
        operations =
            listOf(
                sampleResetDeviceOperation(),
                sampleFirmwareStartOperationWithoutCrc(),
                sampleFirmwareChunkOperation(),
                sampleFirmwareCommitOperation(),
            ),
        workflows = listOf(sampleFlashWorkflowModel()),
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
