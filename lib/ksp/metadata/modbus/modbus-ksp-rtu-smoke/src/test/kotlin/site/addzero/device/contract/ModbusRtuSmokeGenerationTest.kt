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
    private val addressLockFile = File(requireNotNull(System.getProperty("modbus.smoke.addressLockPath")))
    private val generatedKotlinDir = projectDir.resolve("build/generated/ksp/main/kotlin")
    private val generatedResourceDir = projectDir.resolve("build/generated/ksp/main/resources/generated/modbus/rtu")
    private val generatedMarkdownDir = projectDir.resolve("build/generated/ksp/main/resources/generated/modbus/protocols")
    private val externalHeaderDir = externalProjectDir.resolve("Core/Inc/generated/modbus")
    private val externalSourceDir = externalProjectDir.resolve("Core/Src/generated/modbus")

    @Test
    fun realAnnotatedContractGeneratesGatewayAndCBridgeArtifacts() {
        val gatewayKt =
            generatedKotlinDir.resolve(
                "site/addzero/esp32_host_computer/generated/modbus/rtu/GeneratedModbusRtu.kt",
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
        val flashProtocolMarkdown = generatedMarkdownDir.resolve("flash.rtu.protocol.md")
        val externalGeneratedHeader = externalHeaderDir.resolve("device_generated.h")
        val externalBridgeHeader = externalHeaderDir.resolve("device_bridge.h")
        val externalFlashGeneratedHeader = externalHeaderDir.resolve("flash_generated.h")
        val externalFlashBridgeHeader = externalHeaderDir.resolve("flash_bridge.h")
        val externalDispatchHeader = externalHeaderDir.resolve("modbus_rtu_dispatch.h")
        val externalAdapterHeader = externalHeaderDir.resolve("modbus_rtu_agile_slave_adapter.h")
        val externalGeneratedSource = externalSourceDir.resolve("device_generated.c")
        val externalFlashGeneratedSource = externalSourceDir.resolve("flash_generated.c")
        val externalGeneratedBridgeSource = externalSourceDir.resolve("device_bridge.c")
        val externalFlashGeneratedBridgeSource = externalSourceDir.resolve("flash_bridge.c")
        val externalDispatchSource = externalSourceDir.resolve("modbus_rtu_dispatch.c")
        val externalAdapterSource = externalSourceDir.resolve("modbus_rtu_agile_slave_adapter.c")
        val externalFlashBridgeImplFile = externalProjectDir.resolve("Core/Src/modbus/flash_bridge_impl.c")

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
            flashProtocolMarkdown,
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
            addressLockFile,
        ).forEach { file ->
            assertTrue(file.isFile, "Expected generated file to exist: ${file.absolutePath}")
        }

        assertTrue(gatewayKt.readText().contains("executor.readCoils(resolvedConfig, 0, 24)"))
        assertTrue(gatewayKt.readText().contains("executor.readInputRegisters(resolvedConfig, 100, 20)"))
        assertTrue(gatewayKt.readText().contains("executor.readInputRegisters(resolvedConfig, 140, 16)"))
        assertTrue(gatewayKt.readText().contains("class DeviceApiGeneratedRtuGateway"))
        assertTrue(gatewayKt.readText().contains("suspend fun getDeviceRuntimeInfo("))
        assertTrue(gatewayKt.readText().contains("suspend fun getDeviceDisplayName("))
        assertTrue(gatewayKt.readText().contains("suspend fun resetDevice("))
        assertTrue(gatewayKt.readText().contains("suspend fun firmwareStart("))
        assertTrue(gatewayKt.readText().contains("suspend fun firmwareChunk("))
        assertTrue(gatewayKt.readText().contains("suspend fun firmwareCommit("))
        assertTrue(gatewayKt.readText().contains("deviceName = ModbusCodecSupport.decodeString(ModbusCodec.STRING_UTF8, registers, 4, 16)"))
        assertTrue(gatewayKt.readText().contains("return ModbusCodecSupport.decodeString(ModbusCodec.STRING_UTF8, registers, 0, 16)"))
        assertTrue(gatewayKt.readText().contains("executor.writeSingleCoil(resolvedConfig, 64, trigger)"))
        assertTrue(gatewayKt.readText().contains("executor.writeMultipleRegisters(resolvedConfig, 512, encodedValues)"))
        assertTrue(gatewayKt.readText().contains("executor.writeMultipleRegisters(resolvedConfig, 520, encodedValues)"))
        assertTrue(gatewayKt.readText().contains("executor.writeSingleRegister(resolvedConfig, 530, registerValue)"))

        assertTrue(generatedHeader.readText().contains("请勿手动修改此文件。"))
        assertTrue(generatedHeader.readText().contains("#define DEVICE_GET_DEVICE_INFO_QUANTITY 24"))
        assertTrue(generatedHeader.readText().contains("#define DEVICE_GET_DEVICE_RUNTIME_INFO_ADDRESS 100"))
        assertTrue(generatedHeader.readText().contains("#define DEVICE_GET_DEVICE_RUNTIME_INFO_QUANTITY 20"))
        assertTrue(generatedHeader.readText().contains("#define DEVICE_GET_DEVICE_DISPLAY_NAME_ADDRESS 140"))
        assertTrue(generatedHeader.readText().contains("#define DEVICE_GET_DEVICE_DISPLAY_NAME_QUANTITY 16"))
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
        assertTrue(flashGeneratedHeader.readText().contains("/* 触发设备复位。"))
        assertTrue(flashGeneratedSource.readText().contains("request.total_bytes = (int)(((uint32_t)input_registers[0] << 16) | input_registers[1]);"))
        assertTrue(flashGeneratedSource.readText().contains("request.word3 = (int)(((uint32_t)input_registers[8] << 16) | input_registers[9]);"))
        assertTrue(flashGeneratedSource.readText().contains("return flash_bridge_firmware_start(&request, out_result);"))
        assertTrue(flashGeneratedSource.readText().contains("return flash_bridge_reset_device(&request, out_result);"))

        assertTrue(generatedSource.readText().contains("device_bridge_get_device_info"))
        assertTrue(generatedSource.readText().contains("device_bridge_get_device_runtime_info"))
        assertTrue(generatedSource.readText().contains("device_bridge_get_device_display_name"))
        assertTrue(generatedSource.readText().contains("请勿手动修改此文件。"))
        assertTrue(generatedSource.readText().contains("device_generated_encode_string_registers(response.device_name, out_registers, 4, 16);"))
        assertTrue(generatedSource.readText().contains("device_generated_encode_string_registers(value, out_registers, 0u, 16);"))

        assertTrue(bridgeHeader.readText().contains("请勿手动修改此文件。"))
        assertTrue(bridgeHeader.readText().contains("在你的板级/业务 .c 文件中 #include \"device_bridge.h\""))
        assertTrue(bridgeHeader.readText().contains("device_bridge_get_device_info"))
        assertTrue(bridgeHeader.readText().contains("device_bridge_get_device_runtime_info"))
        assertTrue(bridgeHeader.readText().contains("device_bridge_get_device_display_name"))
        assertTrue(bridgeHeader.readText().contains("参数："))
        assertTrue(bridgeHeader.readText().contains("- out_response: device_get_device_runtime_info_response_t 输出对象。"))

        assertTrue(bridgeImplSource.readText().contains("要改哪里："))
        assertTrue(bridgeImplSource.readText().contains("在下面这些 device_bridge_* 函数体里接入 GPIO / ADC / 状态机 / Flash / 传感器驱动"))
        assertTrue(bridgeImplSource.readText().contains("不要修改函数签名"))
        assertTrue(bridgeImplSource.readText().contains("这个文件建议放在 Core/Src/modbus 或你通过 KSP 参数指定的业务目录"))

        assertTrue(flashBridgeHeader.readText().contains("请勿手动修改此文件。"))
        assertTrue(flashBridgeHeader.readText().contains("参数："))
        assertTrue(flashBridgeHeader.readText().contains("- request: flash_firmware_start_request_t 输入参数对象。"))
        assertTrue(flashBridgeHeader.readText().contains("- out_result: 命令处理结果输出。"))
        assertTrue(flashBridgeImplSource.readText().contains("输入参数："))
        assertTrue(flashBridgeImplSource.readText().contains("request->total_bytes: 本次固件总字节数。"))
        assertTrue(flashBridgeImplSource.readText().contains("request->crc32: 本次固件镜像的 CRC32。"))

        assertTrue(dispatchHeader.readText().contains("请勿手动修改此文件。"))
        assertTrue(dispatchSource.readText().contains("case DEVICE_GET_DEVICE_INFO_ADDRESS:"))
        assertTrue(dispatchSource.readText().contains("return device_generated_get_device_info(out_coils, quantity);"))
        assertTrue(dispatchSource.readText().contains("case DEVICE_GET_DEVICE_RUNTIME_INFO_ADDRESS:"))
        assertTrue(dispatchSource.readText().contains("return device_generated_get_device_runtime_info(out_registers, quantity);"))
        assertTrue(dispatchSource.readText().contains("case DEVICE_GET_DEVICE_DISPLAY_NAME_ADDRESS:"))
        assertTrue(dispatchSource.readText().contains("return device_generated_get_device_display_name(out_registers, quantity);"))
        assertTrue(dispatchSource.readText().contains("case FLASH_RESET_DEVICE_ADDRESS:"))
        assertTrue(dispatchSource.readText().contains("case FLASH_FIRMWARE_START_ADDRESS:"))
        assertTrue(dispatchSource.readText().contains("case FLASH_FIRMWARE_CHUNK_ADDRESS:"))
        assertTrue(dispatchSource.readText().contains("case FLASH_FIRMWARE_COMMIT_ADDRESS:"))

        assertTrue(adapterHeader.readText().contains("请勿手动修改此文件。"))
        assertTrue(adapterSource.readText().contains("请勿手动修改此文件。"))
        assertTrue(adapterSource.readText().contains("modbus_rtu_dispatch_read_coils"))

        assertTrue(protocolMarkdown.readText().contains("| `get-device-info` | `getDeviceInfo` | `READ_COILS` | `0` | `24` | `DeviceInfo24` | 读取 24 路输出通道状态。 |"))
        assertTrue(protocolMarkdown.readText().contains("| `ch24` | `Boolean` | `BOOL_COIL` | `23` | `0` | `1` | 通道 24。 |"))
        assertTrue(protocolMarkdown.readText().contains("| `get-device-runtime-info` | `getDeviceRuntimeInfo` | `READ_INPUT_REGISTERS` | `100` | `20` | `DeviceRuntimeInfo` | 读取设备运行信息。 |"))
        assertTrue(protocolMarkdown.readText().contains("| `protocolVersion` | `Int` | `U16` | `0` | `0` | `1` | 协议版本。 |"))
        assertTrue(protocolMarkdown.readText().contains("| `deviceName` | `String` | `STRING_UTF8` | `4` | `0` | `16` | 设备名称。 |"))
        assertTrue(protocolMarkdown.readText().contains("| `get-device-display-name` | `getDeviceDisplayName` | `READ_INPUT_REGISTERS` | `140` | `16` | `String` | 读取设备显示名称。 |"))
        assertTrue(flashProtocolMarkdown.readText().contains("| `reset-device` | `resetDevice` | `WRITE_SINGLE_COIL` | `64` | `1` | `ModbusCommandResult` | 触发设备复位。 |"))
        assertTrue(flashProtocolMarkdown.readText().contains("| `firmware-start` | `firmwareStart` | `WRITE_MULTIPLE_REGISTERS` | `512` | `4` | `ModbusCommandResult` | 初始化一次烧录会话。 |"))
        assertTrue(flashProtocolMarkdown.readText().contains("| `totalBytes` | `Int` | `U32_BE` | `0` | `0` | `2` | 本次固件总字节数。 |"))
        assertTrue(flashProtocolMarkdown.readText().contains("| `firmware-chunk` | `firmwareChunk` | `WRITE_MULTIPLE_REGISTERS` | `520` | `10` | `ModbusCommandResult` | 写入一帧固件数据。 |"))
        assertTrue(flashProtocolMarkdown.readText().contains("| `firmware-commit` | `firmwareCommit` | `WRITE_SINGLE_REGISTER` | `530` | `1` | `ModbusCommandResult` | 提交烧录结果。 |"))

        val addressLock = addressLockFile.readText()
        assertTrue(addressLock.contains("meta.transport=rtu"))
        assertTrue(addressLock.contains("op.device|get-device-info|READ_COILS|COIL_READ=0|24|24"))
        assertTrue(addressLock.contains("op.device|get-device-runtime-info|READ_INPUT_REGISTERS|INPUT_REGISTER=100|20|20"))
        assertTrue(addressLock.contains("op.device|get-device-display-name|READ_INPUT_REGISTERS|INPUT_REGISTER=140|16|16"))
        assertTrue(addressLock.contains("op.flash|reset-device|WRITE_SINGLE_COIL|COIL_WRITE=64|1|1"))
        assertTrue(addressLock.contains("op.flash|firmware-start|WRITE_MULTIPLE_REGISTERS|HOLDING_REGISTER_WRITE=512|4|4"))
        assertTrue(addressLock.contains("op.flash|firmware-chunk|WRITE_MULTIPLE_REGISTERS|HOLDING_REGISTER_WRITE=520|10|10"))
        assertTrue(addressLock.contains("op.flash|firmware-commit|WRITE_SINGLE_REGISTER|HOLDING_REGISTER_WRITE=530|1|1"))

        assertTrue(externalGeneratedHeader.readText().contains("请勿手动修改此文件。"))
        assertTrue(externalBridgeHeader.readText().contains("device_bridge_get_device_info"))
        assertTrue(externalBridgeHeader.readText().contains("device_bridge_get_device_runtime_info"))
        assertTrue(externalBridgeHeader.readText().contains("device_bridge_get_device_display_name"))
        assertTrue(externalFlashGeneratedHeader.readText().contains("FLASH_FIRMWARE_START_ADDRESS"))
        assertTrue(externalFlashBridgeHeader.readText().contains("flash_bridge_firmware_start"))
        assertTrue(externalDispatchHeader.readText().contains("modbus_rtu_dispatch_read_coils"))
        assertTrue(externalDispatchHeader.readText().contains("modbus_rtu_dispatch_read_input_registers"))
        assertTrue(externalAdapterHeader.readText().contains("generated_modbus_rtu_agile_slave_callback"))
        assertTrue(externalGeneratedSource.readText().contains("device_bridge_get_device_info"))
        assertTrue(externalGeneratedSource.readText().contains("device_bridge_get_device_runtime_info"))
        assertTrue(externalGeneratedSource.readText().contains("device_bridge_get_device_display_name"))
        assertTrue(externalFlashGeneratedSource.readText().contains("flash_bridge_firmware_start"))
        assertTrue(externalBridgeImplFile.readText().contains("要改哪里："))
        assertTrue(externalBridgeImplFile.readText().contains("device_bridge_get_device_runtime_info"))
        assertTrue(externalBridgeImplFile.readText().contains("device_bridge_get_device_display_name"))
        assertTrue(externalFlashBridgeImplFile.readText().contains("request->total_bytes: 本次固件总字节数。"))
        assertFalse(externalGeneratedBridgeSource.exists(), "Generated directory should not contain editable bridge source")
        assertFalse(externalFlashGeneratedBridgeSource.exists(), "Generated directory should not contain editable flash bridge source")
        assertTrue(externalDispatchSource.readText().contains("DEVICE_GET_DEVICE_INFO_ADDRESS"))
        assertTrue(externalDispatchSource.readText().contains("DEVICE_GET_DEVICE_RUNTIME_INFO_ADDRESS"))
        assertTrue(externalDispatchSource.readText().contains("DEVICE_GET_DEVICE_DISPLAY_NAME_ADDRESS"))
        assertTrue(externalDispatchSource.readText().contains("FLASH_FIRMWARE_START_ADDRESS"))
        assertTrue(externalAdapterSource.readText().contains("modbus_rtu_dispatch_read_coils"))

        val uvprojx = keilUvprojxFile.readText()
        assertTrue(uvprojx.contains("<GroupName>Core/modbus</GroupName>"))
        assertTrue(uvprojx.contains("<FileName>device_generated.c</FileName>"))
        assertTrue(uvprojx.contains("<FilePath>..\\Core\\Src\\generated\\modbus\\device_generated.c</FilePath>"))
        assertTrue(uvprojx.contains("<FileName>device_bridge_impl.c</FileName>"))
        assertTrue(uvprojx.contains("<FilePath>..\\Core\\Src\\modbus\\device_bridge_impl.c</FilePath>"))
        assertTrue(uvprojx.contains("<FileName>flash_generated.c</FileName>"))
        assertTrue(uvprojx.contains("<FileName>flash_bridge_impl.c</FileName>"))
        assertTrue(uvprojx.contains("<FileName>modbus_rtu_dispatch.c</FileName>"))
        assertTrue(uvprojx.contains("<FileName>modbus_rtu_agile_slave_adapter.c</FileName>"))
        assertFalse(uvprojx.contains("generated\\modbus\\device_bridge.c"))
        assertFalse(uvprojx.contains("generated\\modbus\\flash_bridge.c"))
    }
}
