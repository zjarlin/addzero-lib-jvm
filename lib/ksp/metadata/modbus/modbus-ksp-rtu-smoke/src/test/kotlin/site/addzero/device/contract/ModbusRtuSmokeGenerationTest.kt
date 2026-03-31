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
        val dispatchHeader = generatedResourceDir.resolve("modbus_rtu_dispatch.h")
        val dispatchSource = generatedResourceDir.resolve("modbus_rtu_dispatch.c")
        val adapterHeader = generatedResourceDir.resolve("modbus_rtu_agile_slave_adapter.h")
        val adapterSource = generatedResourceDir.resolve("modbus_rtu_agile_slave_adapter.c")
        val protocolMarkdown = generatedMarkdownDir.resolve("device.rtu.protocol.md")
        val externalGeneratedHeader = externalHeaderDir.resolve("device_generated.h")
        val externalBridgeHeader = externalHeaderDir.resolve("device_bridge.h")
        val externalDispatchHeader = externalHeaderDir.resolve("modbus_rtu_dispatch.h")
        val externalAdapterHeader = externalHeaderDir.resolve("modbus_rtu_agile_slave_adapter.h")
        val externalGeneratedSource = externalSourceDir.resolve("device_generated.c")
        val externalGeneratedBridgeSource = externalSourceDir.resolve("device_bridge.c")
        val externalDispatchSource = externalSourceDir.resolve("modbus_rtu_dispatch.c")
        val externalAdapterSource = externalSourceDir.resolve("modbus_rtu_agile_slave_adapter.c")

        listOf(
            gatewayKt,
            generatedHeader,
            generatedSource,
            bridgeHeader,
            bridgeImplSource,
            dispatchHeader,
            dispatchSource,
            adapterHeader,
            adapterSource,
            protocolMarkdown,
            externalGeneratedHeader,
            externalBridgeHeader,
            externalDispatchHeader,
            externalAdapterHeader,
            externalGeneratedSource,
            externalBridgeImplFile,
            externalDispatchSource,
            externalAdapterSource,
        ).forEach { file ->
            assertTrue(file.isFile, "Expected generated file to exist: ${file.absolutePath}")
        }

        assertTrue(gatewayKt.readText().contains("executor.readCoils(resolvedConfig, 0, 24)"))
        assertTrue(gatewayKt.readText().contains("executor.readInputRegisters(resolvedConfig, 100, 20)"))
        assertTrue(gatewayKt.readText().contains("class DeviceApiGeneratedRtuGateway"))
        assertTrue(gatewayKt.readText().contains("suspend fun getDeviceRuntimeInfo("))
        assertTrue(gatewayKt.readText().contains("deviceName = ModbusCodecSupport.decodeString(ModbusCodec.STRING_UTF8, registers, 4, 16)"))

        assertTrue(generatedHeader.readText().contains("请勿手动修改此文件。"))
        assertTrue(generatedHeader.readText().contains("#define DEVICE_GET_DEVICE_INFO_QUANTITY 24"))
        assertTrue(generatedHeader.readText().contains("#define DEVICE_GET_DEVICE_RUNTIME_INFO_ADDRESS 100"))
        assertTrue(generatedHeader.readText().contains("#define DEVICE_GET_DEVICE_RUNTIME_INFO_QUANTITY 20"))
        assertTrue(generatedHeader.readText().contains("bool ch24;"))
        assertTrue(generatedHeader.readText().contains("int32_t protocol_version;"))
        assertTrue(generatedHeader.readText().contains("char device_name[33];"))

        assertTrue(generatedSource.readText().contains("device_bridge_get_device_info"))
        assertTrue(generatedSource.readText().contains("device_bridge_get_device_runtime_info"))
        assertTrue(generatedSource.readText().contains("请勿手动修改此文件。"))
        assertTrue(generatedSource.readText().contains("device_generated_encode_string_registers(response.device_name, out_registers, 4, 16);"))

        assertTrue(bridgeHeader.readText().contains("请勿手动修改此文件。"))
        assertTrue(bridgeHeader.readText().contains("在你的板级/业务 .c 文件中 #include \"device_bridge.h\""))
        assertTrue(bridgeHeader.readText().contains("device_bridge_get_device_info"))
        assertTrue(bridgeHeader.readText().contains("device_bridge_get_device_runtime_info"))

        assertTrue(bridgeImplSource.readText().contains("要改哪里："))
        assertTrue(bridgeImplSource.readText().contains("在下面这些 device_bridge_* 函数体里接入 GPIO / ADC / 状态机 / Flash / 传感器驱动"))
        assertTrue(bridgeImplSource.readText().contains("不要修改函数签名"))
        assertTrue(bridgeImplSource.readText().contains("这个文件建议放在 Core/Src/modbus 或你通过 KSP 参数指定的业务目录"))

        assertTrue(dispatchHeader.readText().contains("请勿手动修改此文件。"))
        assertTrue(dispatchSource.readText().contains("case DEVICE_GET_DEVICE_INFO_ADDRESS:"))
        assertTrue(dispatchSource.readText().contains("return device_generated_get_device_info(out_coils, quantity);"))
        assertTrue(dispatchSource.readText().contains("case DEVICE_GET_DEVICE_RUNTIME_INFO_ADDRESS:"))
        assertTrue(dispatchSource.readText().contains("return device_generated_get_device_runtime_info(out_registers, quantity);"))

        assertTrue(adapterHeader.readText().contains("请勿手动修改此文件。"))
        assertTrue(adapterSource.readText().contains("请勿手动修改此文件。"))
        assertTrue(adapterSource.readText().contains("modbus_rtu_dispatch_read_coils"))

        assertTrue(protocolMarkdown.readText().contains("| `get-device-info` | `getDeviceInfo` | `READ_COILS` | `0` | `24` | `DeviceInfo24` | 读取 24 路输出通道状态。 |"))
        assertTrue(protocolMarkdown.readText().contains("| `ch24` | `Boolean` | `BOOL_COIL` | `23` | `0` | `1` | 通道 24。 |"))
        assertTrue(protocolMarkdown.readText().contains("| `get-device-runtime-info` | `getDeviceRuntimeInfo` | `READ_INPUT_REGISTERS` | `100` | `20` | `DeviceRuntimeInfo` | 读取设备运行信息。 |"))
        assertTrue(protocolMarkdown.readText().contains("| `protocolVersion` | `Int` | `U16` | `0` | `0` | `1` | 协议版本。 |"))
        assertTrue(protocolMarkdown.readText().contains("| `deviceName` | `String` | `STRING_UTF8` | `4` | `0` | `16` | 设备名称。 |"))

        assertTrue(externalGeneratedHeader.readText().contains("请勿手动修改此文件。"))
        assertTrue(externalBridgeHeader.readText().contains("device_bridge_get_device_info"))
        assertTrue(externalBridgeHeader.readText().contains("device_bridge_get_device_runtime_info"))
        assertTrue(externalDispatchHeader.readText().contains("modbus_rtu_dispatch_read_coils"))
        assertTrue(externalDispatchHeader.readText().contains("modbus_rtu_dispatch_read_input_registers"))
        assertTrue(externalAdapterHeader.readText().contains("generated_modbus_rtu_agile_slave_callback"))
        assertTrue(externalGeneratedSource.readText().contains("device_bridge_get_device_info"))
        assertTrue(externalGeneratedSource.readText().contains("device_bridge_get_device_runtime_info"))
        assertTrue(externalBridgeImplFile.readText().contains("要改哪里："))
        assertTrue(externalBridgeImplFile.readText().contains("device_bridge_get_device_runtime_info"))
        assertFalse(externalGeneratedBridgeSource.exists(), "Generated directory should not contain editable bridge source")
        assertTrue(externalDispatchSource.readText().contains("DEVICE_GET_DEVICE_INFO_ADDRESS"))
        assertTrue(externalDispatchSource.readText().contains("DEVICE_GET_DEVICE_RUNTIME_INFO_ADDRESS"))
        assertTrue(externalAdapterSource.readText().contains("modbus_rtu_dispatch_read_coils"))

        val uvprojx = keilUvprojxFile.readText()
        assertTrue(uvprojx.contains("<GroupName>Core/modbus</GroupName>"))
        assertTrue(uvprojx.contains("<FileName>device_generated.c</FileName>"))
        assertTrue(uvprojx.contains("<FilePath>..\\Core\\Src\\generated\\modbus\\device_generated.c</FilePath>"))
        assertTrue(uvprojx.contains("<FileName>device_bridge_impl.c</FileName>"))
        assertTrue(uvprojx.contains("<FilePath>..\\Core\\Src\\modbus\\device_bridge_impl.c</FilePath>"))
        assertTrue(uvprojx.contains("<FileName>modbus_rtu_dispatch.c</FileName>"))
        assertTrue(uvprojx.contains("<FileName>modbus_rtu_agile_slave_adapter.c</FileName>"))
        assertFalse(uvprojx.contains("generated\\modbus\\device_bridge.c"))
    }
}
