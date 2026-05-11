package site.addzero.device.protocol.modbus.ksp.core

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import java.io.File
import java.util.ServiceLoader

/**
 * 由消费方提供的 C 工程集成 SPI。
 *
 * 目标：
 * - Kotlin / metadata 侧的 KSP 选项仍然留在 processor 自己的 mustMap schema 中
 * - 固件工程目录、bridge 实现目录、Keil / CubeMX 同步目标这类强消费方绑定配置交给消费方 SPI
 */
interface ModbusConsumerCArtifactsProvider {
    val providerId: String

    val order: Int
        get() = 0

    fun isEnabled(context: ModbusConsumerCArtifactsContext): Boolean = true

    fun provide(context: ModbusConsumerCArtifactsContext): ModbusConsumerCArtifactsConfig?
}

data class ModbusConsumerCArtifactsContext(
    val environment: SymbolProcessorEnvironment,
    val transport: ModbusTransportKind? = null,
)

data class ModbusConsumerCArtifactsConfig(
    val firmwareProjectDir: File,
    val bridgeImplTargetPath: String = "Core/Src/modbus",
    val markdownOutputPath: String = "Docs/generated/modbus",
    val keilUvprojxPath: String? = null,
    val keilTargetName: String = "",
    val keilGroupName: String = "Core/modbus",
    val mxprojectPath: String? = null,
)

object ModbusConsumerCArtifactsSupport {
    fun resolve(
        environment: SymbolProcessorEnvironment,
        transport: ModbusTransportKind? = null,
    ): ModbusConsumerCArtifactsConfig? {
        val context = ModbusConsumerCArtifactsContext(environment = environment, transport = transport)
        val providers =
            ServiceLoader
                .load(ModbusConsumerCArtifactsProvider::class.java, ModbusConsumerCArtifactsSupport::class.java.classLoader)
                .toList()
                .filter { provider -> provider.isEnabled(context) }
                .sortedWith(compareBy(ModbusConsumerCArtifactsProvider::order, ModbusConsumerCArtifactsProvider::providerId))
        if (providers.isEmpty()) {
            return null
        }
        check(providers.size == 1) {
            "检测到多个启用中的 ModbusConsumerCArtifactsProvider: ${
                providers.joinToString { provider -> provider.providerId }
            }"
        }
        val provider = providers.single()
        environment.logger.logging("Using Modbus consumer C artifacts provider: ${provider.providerId}")
        return provider.provide(context)
    }
}
