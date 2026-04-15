package site.addzero.device.contract

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ModbusRtuSmokeGenerationTest {
    private val projectDir = File(requireNotNull(System.getProperty("modbus.smoke.projectDir")))
    private val externalProjectDir = File(requireNotNull(System.getProperty("modbus.smoke.externalProjectDir")))
    private val externalBridgeImplFile = externalProjectDir.resolve(requireNotNull(System.getProperty("modbus.smoke.externalBridgeImplPath")))
    private val keilUvprojxFile = File(requireNotNull(System.getProperty("modbus.smoke.keilUvprojxPath")))
    private val mxprojectFile = File(requireNotNull(System.getProperty("modbus.smoke.mxprojectPath")))
    private val addressLockFile = File(requireNotNull(System.getProperty("modbus.smoke.addressLockPath")))
    private val springRouteOutputDir = File(requireNotNull(System.getProperty("modbus.smoke.springRouteOutputDir")))
    private val generatedKotlinDir = projectDir.resolve("build/generated/ksp/main/kotlin")
    private val generatedResourceDir = projectDir.resolve("build/generated/ksp/main/resources/generated/modbus/rtu")
    private val generatedMarkdownDir = projectDir.resolve("build/generated/ksp/main/resources/generated/modbus/protocols")
    private val externalHeaderDir = externalProjectDir.resolve("Core/Inc/generated/modbus/rtu")
    private val externalSourceDir = externalProjectDir.resolve("Core/Src/generated/modbus/rtu")
    private val externalMarkdownDir = externalProjectDir.resolve("Docs/generated/modbus/rtu")

    @Test
    fun realAnnotatedContractGeneratesGatewayAndCBridgeArtifacts() {
        val gatewayKt =
            generatedKotlinDir.resolve(
                "site/addzero/generated/modbus/rtu/GeneratedModbusRtu.kt",
            )
        val generatedHeader = generatedResourceDir.resolve("device_generated.h")
        val generatedSource = generatedResourceDir.resolve("device_generated.c")
        val bridgeHeader = generatedResourceDir.resolve("device_bridge.h")
        val bridgeImplSource = generatedResourceDir.resolve("device_bridge_impl.c")
        val flashGeneratedHeader = generatedResourceDir.resolve("flash_generated.h")
        val flashGeneratedSource = generatedResourceDir.resolve("flash_generated.c")
        val flashBridgeHeader = generatedResourceDir.resolve("flash_bridge.h")
        val flashBridgeImplSource = generatedResourceDir.resolve("flash_bridge_impl.c")
        val dispatchHeader = generatedResourceDir.resolve("modbus_rtu_dispatch.h")
        val dispatchSource = generatedResourceDir.resolve("modbus_rtu_dispatch.c")
        val adapterHeader = generatedResourceDir.resolve("modbus_rtu_agile_slave_adapter.h")
        val adapterSource = generatedResourceDir.resolve("modbus_rtu_agile_slave_adapter.c")
        val protocolMarkdown = generatedMarkdownDir.resolve("device.rtu.protocol.md")
        val bridgeSampleSource = generatedResourceDir.resolve("device_bridge_sample.c")
        val flashProtocolMarkdown = generatedMarkdownDir.resolve("flash.rtu.protocol.md")
        val flashBridgeSampleSource = generatedResourceDir.resolve("flash_bridge_sample.c")
        val generatedSpringRouteSource =
            springRouteOutputDir.resolve(
                "site/addzero/generated/modbus/rtu/GeneratedModbusRtuSpringRoutesSource.kt",
            )
        val externalProtocolMarkdown = externalMarkdownDir.resolve("device.rtu.protocol.md")
        val externalBridgeSampleSource = externalMarkdownDir.resolve("device_bridge_sample.c")
        val externalFlashProtocolMarkdown = externalMarkdownDir.resolve("flash.rtu.protocol.md")
        val externalFlashBridgeSampleSource = externalMarkdownDir.resolve("flash_bridge_sample.c")
        val externalGeneratedHeader = externalHeaderDir.resolve("device/device_generated.h")
        val externalBridgeHeader = externalHeaderDir.resolve("device/device_bridge.h")
        val externalFlashGeneratedHeader = externalHeaderDir.resolve("flash/flash_generated.h")
        val externalFlashBridgeHeader = externalHeaderDir.resolve("flash/flash_bridge.h")
        val externalDispatchHeader = externalHeaderDir.resolve("transport/modbus_rtu_dispatch.h")
        val externalAdapterHeader = externalHeaderDir.resolve("transport/modbus_rtu_agile_slave_adapter.h")
        val externalGeneratedSource = externalSourceDir.resolve("device/device_generated.c")
        val externalFlashGeneratedSource = externalSourceDir.resolve("flash/flash_generated.c")
        val externalGeneratedBridgeSource = externalSourceDir.resolve("device/device_bridge.c")
        val externalFlashGeneratedBridgeSource = externalSourceDir.resolve("flash/flash_bridge.c")
        val externalDispatchSource = externalSourceDir.resolve("transport/modbus_rtu_dispatch.c")
        val externalAdapterSource = externalSourceDir.resolve("transport/modbus_rtu_agile_slave_adapter.c")
        val externalFlashBridgeImplFile = externalProjectDir.resolve("Core/Src/modbus/rtu/flash/flash_bridge_impl.c")

        listOf(
            gatewayKt,
            generatedHeader,
            generatedSource,
            bridgeHeader,
            bridgeImplSource,
            flashGeneratedHeader,
            flashGeneratedSource,
            flashBridgeHeader,
            flashBridgeImplSource,
            dispatchHeader,
            dispatchSource,
            adapterHeader,
            adapterSource,
            protocolMarkdown,
            bridgeSampleSource,
            flashProtocolMarkdown,
            flashBridgeSampleSource,
            generatedSpringRouteSource,
            externalProtocolMarkdown,
            externalBridgeSampleSource,
            externalFlashProtocolMarkdown,
            externalFlashBridgeSampleSource,
            externalGeneratedHeader,
            externalBridgeHeader,
            externalFlashGeneratedHeader,
            externalFlashBridgeHeader,
            externalDispatchHeader,
            externalAdapterHeader,
            externalGeneratedSource,
            externalFlashGeneratedSource,
            externalBridgeImplFile,
            externalFlashBridgeImplFile,
            externalDispatchSource,
            externalAdapterSource,
            mxprojectFile,
            addressLockFile,
        ).forEach { file ->
            assertTrue(file.isFile, "Expected generated file to exist: ${file.absolutePath}")
        }

        assertTrue(gatewayKt.readText().contains("executor.readCoils(resolvedConfig, 0, 24)"))
        assertTrue(gatewayKt.readText().contains("executor.readInputRegisters(resolvedConfig, 100, 20)"))
        assertTrue(gatewayKt.readText().contains("class DeviceApiGeneratedRtuGateway"))
        assertTrue(gatewayKt.readText().contains("private val configuredDefaultConfig: ModbusRtuEndpointConfig"))
        assertTrue(gatewayKt.readText().contains("fun defaultConfig(): ModbusRtuEndpointConfig = configuredDefaultConfig"))
        assertFalse(gatewayKt.readText().contains("class DeviceApiGeneratedRtuConfigProvider"))
        assertFalse(gatewayKt.readText().contains("fun modbusRtuConfigRegistry("))
        assertFalse(gatewayKt.readText().contains("fun Route.registerGeneratedModbusRtuRoutes()"))
        assertTrue(gatewayKt.readText().contains("fun deviceApi("))
        assertTrue(gatewayKt.readText().contains("): site.addzero.device.contract.DeviceApi = gateway"))
        assertTrue(gatewayKt.readText().contains("suspend fun getDeviceRuntimeInfo("))
        assertTrue(gatewayKt.readText().contains("suspend fun resetDevice("))
        assertTrue(gatewayKt.readText().contains("suspend fun firmwareStart("))
        assertTrue(gatewayKt.readText().contains("suspend fun firmwareChunk("))
        assertTrue(gatewayKt.readText().contains("suspend fun firmwareCommit("))
        assertTrue(gatewayKt.readText().contains("override suspend fun flashFirmware(bytes: ByteArray): site.addzero.device.contract.FlashResult"))
        assertTrue(gatewayKt.readText().contains("require(bytes.isNotEmpty()) { \"flashFirmware bytes must not be empty\" }"))
        assertTrue(gatewayKt.readText().contains("val firmwareCrc32 = generatedModbusCrc32(bytes)"))
        assertTrue(gatewayKt.readText().contains("val startResult = firmwareStart(config = resolvedConfig, totalBytes = totalBytes, crc32 = firmwareCrc32)"))
        assertTrue(gatewayKt.readText().contains("val chunkResult = firmwareChunk(config = resolvedConfig, sequence = sequence, usedBytes = chunkBytes.size, word0 = word0, word1 = word1, word2 = word2, word3 = word3)"))
        assertTrue(gatewayKt.readText().contains("val commitResult = firmwareCommit(config = resolvedConfig, totalChunks = totalChunks)"))
        assertTrue(gatewayKt.readText().contains("val resetResult = resetDevice(config = resolvedConfig, trigger = true)"))
        assertTrue(gatewayKt.readText().contains("return site.addzero.device.contract.FlashResult("))
        assertTrue(gatewayKt.readText().contains("deviceName = ModbusCodecSupport.decodeString(ModbusCodec.STRING_UTF8, registers, 4, 16)"))
        assertTrue(gatewayKt.readText().contains("executor.writeSingleCoil(resolvedConfig, 64, trigger)"))
        assertTrue(gatewayKt.readText().contains("executor.writeMultipleRegisters(resolvedConfig, 512, encodedValues)"))
        assertTrue(gatewayKt.readText().contains("executor.writeMultipleRegisters(resolvedConfig, 520, encodedValues)"))
        assertTrue(gatewayKt.readText().contains("executor.writeSingleRegister(resolvedConfig, 530, registerValue)"))

        assertTrue(generatedHeader.readText().contains("请勿手动修改此文件。"))
        assertTrue(generatedHeader.readText().contains("#define DEVICE_GET_DEVICE_INFO_QUANTITY 24"))
        assertTrue(generatedHeader.readText().contains("#define DEVICE_GET_DEVICE_RUNTIME_INFO_ADDRESS 100"))
        assertTrue(generatedHeader.readText().contains("#define DEVICE_GET_DEVICE_RUNTIME_INFO_QUANTITY 20"))
        assertTrue(generatedHeader.readText().contains("/* 设备运行信息。 */"))
        assertTrue(generatedHeader.readText().contains("bool ch24;"))
        assertTrue(generatedHeader.readText().contains("int32_t protocol_version;"))
        assertTrue(generatedHeader.readText().contains("char device_name[33];"))

        assertTrue(flashGeneratedHeader.readText().contains("请勿手动修改此文件。"))
        assertTrue(flashGeneratedHeader.readText().contains("#define FLASH_RESET_DEVICE_ADDRESS 64"))
        assertTrue(flashGeneratedHeader.readText().contains("#define FLASH_FIRMWARE_START_ADDRESS 512"))
        assertTrue(flashGeneratedHeader.readText().contains("#define FLASH_FIRMWARE_CHUNK_QUANTITY 10"))
        assertTrue(flashGeneratedHeader.readText().contains("/* 初始化一次烧录会话。 请求参数。 */"))
        assertTrue(flashGeneratedHeader.readText().contains("/* 本次固件总字节数。 */"))
        assertTrue(flashGeneratedHeader.readText().contains("int32_t total_bytes;"))
        assertTrue(flashGeneratedHeader.readText().contains("属于高层 flashFirmware(bytes) 工作流的低层步骤。"))
        assertTrue(flashGeneratedHeader.readText().contains("/* 触发设备复位。"))
        assertTrue(flashGeneratedSource.readText().contains("request.total_bytes = (int)(((uint32_t)input_registers[0] << 16) | input_registers[1]);"))
        assertTrue(flashGeneratedSource.readText().contains("request.word3 = (int)(((uint32_t)input_registers[8] << 16) | input_registers[9]);"))
        assertTrue(flashGeneratedSource.readText().contains("return flash_bridge_firmware_start(&request, out_result);"))
        assertTrue(flashGeneratedSource.readText().contains("return flash_bridge_reset_device(&request, out_result);"))

        assertTrue(generatedSource.readText().contains("device_bridge_get_device_info"))
        assertTrue(generatedSource.readText().contains("device_bridge_get_device_runtime_info"))
        assertTrue(generatedSource.readText().contains("请勿手动修改此文件。"))
        assertTrue(generatedSource.readText().contains("device_generated_encode_string_registers(response.device_name, out_registers, 4, 16);"))

        assertTrue(bridgeHeader.readText().contains("请勿手动修改此文件。"))
        assertTrue(bridgeHeader.readText().contains("在你的板级/业务 .c 文件中 #include \"device/device_bridge.h\""))
        assertTrue(bridgeHeader.readText().contains("device_bridge_get_device_info"))
        assertTrue(bridgeHeader.readText().contains("device_bridge_get_device_runtime_info"))
        assertTrue(bridgeHeader.readText().contains("参数："))
        assertTrue(bridgeHeader.readText().contains("- out_response: device_get_device_runtime_info_response_t 输出对象。"))

        assertTrue(bridgeImplSource.readText().contains("要改哪里："))
        assertTrue(bridgeImplSource.readText().contains("在下面这些 device_bridge_* 函数体里接入 GPIO / ADC / 状态机 / Flash / 传感器驱动"))
        assertTrue(bridgeImplSource.readText().contains("不要修改函数签名"))
        assertTrue(bridgeImplSource.readText().contains("这个文件建议放在 Core/Src/modbus/<transport>/<service>，例如 Core/Src/modbus/rtu/device；也可以放在你通过 KSP 参数指定的业务目录"))
        assertTrue(bridgeImplSource.readText().contains("#include <string.h>"))
        assertTrue(bridgeImplSource.readText().contains("static void device_bridge_copy_text(char *out_text, size_t out_capacity, const char *input) {"))
        assertTrue(bridgeImplSource.readText().contains("device_name 字符串：codec=STRING_UTF8，寄存器宽度=16，最多 32 个字节，缓冲区容量 33（含 '\\0'）。"))
        assertTrue(bridgeImplSource.readText().contains(" * device_bridge_copy_text(out_response->device_name, sizeof(out_response->device_name), \"XXXXXXXX-XXXXX\");"))
        assertTrue(bridgeImplSource.readText().contains("device_bridge_copy_text(out_response->device_name, sizeof(out_response->device_name), \"\");"))
        assertFalse(bridgeImplSource.readText().contains("out_response->device_name[0] = '\\0';"))
        assertFalse(bridgeImplSource.readText().contains("out_response->device_name[32] = '\\0';"))

        assertTrue(flashBridgeHeader.readText().contains("请勿手动修改此文件。"))
        assertTrue(flashBridgeHeader.readText().contains("参数："))
        assertTrue(flashBridgeHeader.readText().contains("- request: flash_firmware_start_request_t 输入参数对象。"))
        assertTrue(flashBridgeHeader.readText().contains("- out_result: 命令处理结果输出。"))
        assertTrue(flashBridgeHeader.readText().contains("高层工作流：flashFirmware(bytes)"))
        assertTrue(flashBridgeImplSource.readText().contains("输入参数："))
        assertTrue(flashBridgeImplSource.readText().contains("request->total_bytes: 本次固件总字节数。"))
        assertTrue(flashBridgeImplSource.readText().contains("request->crc32: 本次固件镜像的 CRC32。"))
        assertTrue(flashBridgeImplSource.readText().contains("Kotlin 上位机会自动计算 CRC32、切片并顺序调用 begin/chunk/commit/reset"))

        assertTrue(dispatchHeader.readText().contains("请勿手动修改此文件。"))
        assertTrue(dispatchSource.readText().contains("case DEVICE_GET_DEVICE_INFO_ADDRESS:"))
        assertTrue(dispatchSource.readText().contains("return device_generated_get_device_info(out_coils, quantity);"))
        assertTrue(dispatchSource.readText().contains("case DEVICE_GET_DEVICE_RUNTIME_INFO_ADDRESS:"))
        assertTrue(dispatchSource.readText().contains("return device_generated_get_device_runtime_info(out_registers, quantity);"))
        assertTrue(dispatchSource.readText().contains("case FLASH_RESET_DEVICE_ADDRESS:"))
        assertTrue(dispatchSource.readText().contains("case FLASH_FIRMWARE_START_ADDRESS:"))
        assertTrue(dispatchSource.readText().contains("case FLASH_FIRMWARE_CHUNK_ADDRESS:"))
        assertTrue(dispatchSource.readText().contains("case FLASH_FIRMWARE_COMMIT_ADDRESS:"))

        assertTrue(adapterHeader.readText().contains("请勿手动修改此文件。"))
        assertTrue(adapterSource.readText().contains("请勿手动修改此文件。"))
        assertTrue(adapterSource.readText().contains("modbus_rtu_dispatch_read_coils"))

        assertTrue(protocolMarkdown.readText().contains("| `get-device-info` | `getDeviceInfo` | `READ_COILS` | `0x01` | 读取线圈 | `0` | `24` | `DeviceInfo24` | 读取 24 路输出通道状态。 |"))
        assertTrue(protocolMarkdown.readText().contains("## 联调说明"))
        assertTrue(protocolMarkdown.readText().contains("固件侧需要实现的入口是 `device_bridge_impl.c` 里的 `device_bridge_*` 函数。"))
        assertTrue(protocolMarkdown.readText().contains("重新生成时不会覆盖"))
        assertTrue(protocolMarkdown.readText().contains("`device_bridge_sample.c`"))
        assertTrue(protocolMarkdown.readText().contains("## 仿真软件怎么填"))
        assertTrue(protocolMarkdown.readText().contains("| Baud Rate | `9600` |"))
        assertTrue(protocolMarkdown.readText().contains("| 波特率 | 默认 `9600`。 |"))
        assertTrue(protocolMarkdown.readText().contains("### RTU 报文示例"))
        assertTrue(protocolMarkdown.readText().contains("### RTU 请求帧拆解"))
        assertTrue(protocolMarkdown.readText().contains("请求 payload 示例：`00 00 00 18`"))
        assertTrue(protocolMarkdown.readText().contains("| 项目 | 从站地址 | 功能码 | payload: 起始地址 | payload: 数量 | CRC16 |"))
        assertTrue(protocolMarkdown.readText().contains("| 长度 | `1 byte` | `1 byte` | `2 byte` | `2 byte` | `2 byte` |"))
        assertTrue(protocolMarkdown.readText().contains("| 示例 | `01` | `01` | `00 00` | `00 18` | `3C 00` |"))
        assertTrue(protocolMarkdown.readText().contains("| `ch24` | `Boolean` | `BOOL_COIL` | `23` | `0` | `1` | 通道 24。 |"))
        assertTrue(protocolMarkdown.readText().contains("| `get-device-runtime-info` | `getDeviceRuntimeInfo` | `READ_INPUT_REGISTERS` | `0x04` | 读取输入寄存器 | `100` | `20` | `DeviceRuntimeInfo` | 读取设备运行信息。 |"))
        assertTrue(protocolMarkdown.readText().contains("| `protocolVersion` | `Int` | `U16` | `0` | `0` | `1` | 协议版本。 |"))
        assertTrue(protocolMarkdown.readText().contains("| `deviceName` | `String` | `STRING_UTF8` | `4` | `0` | `16` | 设备名称。 |"))
        assertTrue(bridgeSampleSource.readText().contains("请勿手动修改此文件。"))
        assertTrue(bridgeSampleSource.readText().contains("更新日期："))
        assertTrue(bridgeSampleSource.readText().contains("device_bridge_get_device_runtime_info"))
        assertTrue(flashProtocolMarkdown.readText().contains("| `reset-device` | `resetDevice` | `WRITE_SINGLE_COIL` | `0x05` | 写单个线圈 | `64` | `1` | `ModbusCommandResult` | 触发设备复位。 |"))
        assertTrue(flashProtocolMarkdown.readText().contains("| `firmware-start` | `firmwareStart` | `WRITE_MULTIPLE_REGISTERS` | `0x10` | 写多个寄存器 | `512` | `4` | `ModbusCommandResult` | 初始化一次烧录会话。 |"))
        assertTrue(flashProtocolMarkdown.readText().contains("| `totalBytes` | `Int` | `U32_BE` | `0` | `0` | `2` | 本次固件总字节数。 |"))
        assertTrue(flashProtocolMarkdown.readText().contains("| `firmware-chunk` | `firmwareChunk` | `WRITE_MULTIPLE_REGISTERS` | `0x10` | 写多个寄存器 | `520` | `10` | `ModbusCommandResult` | 写入一帧固件数据。 |"))
        assertTrue(flashProtocolMarkdown.readText().contains("| `firmware-commit` | `firmwareCommit` | `WRITE_SINGLE_REGISTER` | `0x06` | 写单个寄存器 | `530` | `1` | `ModbusCommandResult` | 提交烧录结果。 |"))
        assertTrue(flashProtocolMarkdown.readText().contains("## 工作流总览"))
        assertTrue(flashProtocolMarkdown.readText().contains("## 联调说明"))
        assertTrue(flashProtocolMarkdown.readText().contains("| `flash-firmware` | `flashFirmware` | `FlashResult` | 执行完整固件烧录工作流。 | `firmwareStart` -> `firmwareChunk` -> `firmwareCommit` -> `resetDevice` |"))
        assertTrue(flashProtocolMarkdown.readText().contains("### 固件工作流"))
        assertTrue(flashProtocolMarkdown.readText().contains("请求 payload 示例：`00 40 FF 00`"))
        assertTrue(flashProtocolMarkdown.readText().contains("| 项目 | 从站地址 | 功能码 | payload: 线圈地址 | payload: 线圈写入值 | CRC16 |"))
        assertTrue(flashProtocolMarkdown.readText().contains("| 示例 | `01` | `05` | `00 40` | `FF 00` | `8D EE` |"))
        assertTrue(flashProtocolMarkdown.readText().contains("CRC32: 由上位机自动计算并通过 firmwareStart 下发"))
        assertTrue(flashBridgeSampleSource.readText().contains("请勿手动修改此文件。"))
        assertTrue(flashBridgeSampleSource.readText().contains("flash_bridge_firmware_start"))
        assertTrue(externalProtocolMarkdown.readText().contains("## 联调说明"))
        assertTrue(externalBridgeSampleSource.readText().contains("更新日期："))
        assertTrue(externalFlashProtocolMarkdown.readText().contains("## 工作流总览"))
        assertTrue(externalFlashBridgeSampleSource.readText().contains("更新日期："))
        assertTrue(generatedSpringRouteSource.readText().contains("@file:site.addzero.springktor.runtime.RequestMapping(\"/api/modbus/rtu\")"))
        assertTrue(generatedSpringRouteSource.readText().contains("import org.koin.mp.KoinPlatform"))
        assertTrue(generatedSpringRouteSource.readText().contains("@PostMapping(\"/device/get-device-info\")"))
        assertTrue(generatedSpringRouteSource.readText().contains("val gateway = KoinPlatform.getKoin().get<DeviceApiGeneratedRtuGateway>()"))
        assertTrue(generatedSpringRouteSource.readText().contains("return gateway.getDeviceInfo(config = config)"))
        assertTrue(generatedSpringRouteSource.readText().contains("@PostMapping(\"/flash/flash-firmware\")"))

        val addressLock = addressLockFile.readText()
        assertTrue(addressLock.contains("meta.transport=rtu"))
        assertTrue(addressLock.contains("op.device|get-device-info|READ_COILS|COIL_READ=0|24|24"))
        assertTrue(addressLock.contains("op.device|get-device-runtime-info|READ_INPUT_REGISTERS|INPUT_REGISTER=100|20|20"))
        assertTrue(addressLock.contains("op.flash|reset-device|WRITE_SINGLE_COIL|COIL_WRITE=64|1|1"))
        assertTrue(addressLock.contains("op.flash|firmware-start|WRITE_MULTIPLE_REGISTERS|HOLDING_REGISTER_WRITE=512|4|4"))
        assertTrue(addressLock.contains("op.flash|firmware-chunk|WRITE_MULTIPLE_REGISTERS|HOLDING_REGISTER_WRITE=520|10|10"))
        assertTrue(addressLock.contains("op.flash|firmware-commit|WRITE_SINGLE_REGISTER|HOLDING_REGISTER_WRITE=530|1|1"))

        assertTrue(externalGeneratedHeader.readText().contains("请勿手动修改此文件。"))
        assertTrue(externalBridgeHeader.readText().contains("device_bridge_get_device_info"))
        assertTrue(externalBridgeHeader.readText().contains("device_bridge_get_device_runtime_info"))
        assertTrue(externalFlashGeneratedHeader.readText().contains("FLASH_FIRMWARE_START_ADDRESS"))
        assertTrue(externalFlashBridgeHeader.readText().contains("flash_bridge_firmware_start"))
        assertTrue(externalDispatchHeader.readText().contains("modbus_rtu_dispatch_read_coils"))
        assertTrue(externalDispatchHeader.readText().contains("modbus_rtu_dispatch_read_input_registers"))
        assertTrue(externalAdapterHeader.readText().contains("generated_modbus_rtu_agile_slave_callback"))
        assertTrue(externalGeneratedSource.readText().contains("device_bridge_get_device_info"))
        assertTrue(externalGeneratedSource.readText().contains("device_bridge_get_device_runtime_info"))
        assertTrue(externalFlashGeneratedSource.readText().contains("flash_bridge_firmware_start"))
        assertTrue(externalBridgeImplFile.readText().contains("要改哪里："))
        assertTrue(externalBridgeImplFile.readText().contains("device_bridge_get_device_runtime_info"))
        assertTrue(externalFlashBridgeImplFile.readText().contains("request->total_bytes: 本次固件总字节数。"))
        assertFalse(externalGeneratedBridgeSource.exists(), "Generated directory should not contain editable bridge source")
        assertFalse(externalFlashGeneratedBridgeSource.exists(), "Generated directory should not contain editable flash bridge source")
        assertTrue(externalDispatchSource.readText().contains("DEVICE_GET_DEVICE_INFO_ADDRESS"))
        assertTrue(externalDispatchSource.readText().contains("DEVICE_GET_DEVICE_RUNTIME_INFO_ADDRESS"))
        assertTrue(externalDispatchSource.readText().contains("FLASH_FIRMWARE_START_ADDRESS"))
        assertTrue(externalAdapterSource.readText().contains("modbus_rtu_dispatch_read_coils"))

        val uvprojx = keilUvprojxFile.readText()
        assertTrue(uvprojx.contains("<GroupName>Core/modbus/rtu/device</GroupName>"))
        assertTrue(uvprojx.contains("<GroupName>Core/modbus/rtu/flash</GroupName>"))
        assertTrue(uvprojx.contains("<GroupName>Core/modbus/rtu/transport</GroupName>"))
        assertTrue(uvprojx.contains("<FileName>device_generated.c</FileName>"))
        assertTrue(uvprojx.contains("<FilePath>..\\Core\\Src\\generated\\modbus\\rtu\\device\\device_generated.c</FilePath>"))
        assertTrue(uvprojx.contains("<FileName>device_bridge_impl.c</FileName>"))
        assertTrue(uvprojx.contains("<FilePath>..\\Core\\Src\\modbus\\rtu\\device\\device_bridge_impl.c</FilePath>"))
        assertTrue(uvprojx.contains("<FileName>flash_generated.c</FileName>"))
        assertTrue(uvprojx.contains("<FileName>flash_bridge_impl.c</FileName>"))
        assertTrue(uvprojx.contains("<FileName>modbus_rtu_dispatch.c</FileName>"))
        assertTrue(uvprojx.contains("<FileName>modbus_rtu_agile_slave_adapter.c</FileName>"))
        assertFalse(uvprojx.contains("generated\\modbus\\device_bridge.c"))
        assertFalse(uvprojx.contains("generated\\modbus\\flash_bridge.c"))

        val mxproject = mxprojectFile.readText()
        assertTrue(mxproject.contains("HeaderPath="))
        assertTrue(mxproject.contains("..\\Core\\Inc\\generated\\modbus\\rtu"))
        assertTrue(mxproject.contains("..\\Core\\Src\\generated\\modbus\\rtu\\device\\device_generated.c"))
        assertTrue(mxproject.contains("..\\Core\\Src\\modbus\\rtu\\device\\device_bridge_impl.c"))
        assertTrue(mxproject.contains("HeaderPath#1=..\\Core\\Inc\\generated\\modbus\\rtu"))
        assertTrue(mxproject.contains("SourcePath#1=..\\Core\\Src\\generated\\modbus\\rtu"))
        assertTrue(mxproject.contains("SourcePath#2=..\\Core\\Src\\modbus\\rtu"))
    }
}
