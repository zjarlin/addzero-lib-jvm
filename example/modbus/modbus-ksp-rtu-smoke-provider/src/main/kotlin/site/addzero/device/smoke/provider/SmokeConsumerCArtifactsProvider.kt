package site.addzero.device.smoke.provider

import java.io.File
import site.addzero.device.protocol.modbus.ksp.core.ModbusConsumerCArtifactsConfig
import site.addzero.device.protocol.modbus.ksp.core.ModbusConsumerCArtifactsContext
import site.addzero.device.protocol.modbus.ksp.core.ModbusConsumerCArtifactsProvider

class SmokeConsumerCArtifactsProvider : ModbusConsumerCArtifactsProvider {
    override val providerId: String = "smoke-c-artifacts"

    override fun isEnabled(context: ModbusConsumerCArtifactsContext): Boolean =
        SmokeModbusProviderSupport.resolveSqliteFile(context.environment) != null

    override fun provide(context: ModbusConsumerCArtifactsContext): ModbusConsumerCArtifactsConfig? {
        val buildDir = SmokeModbusProviderSupport.resolveBuildDir(context.environment) ?: return null
        return ModbusConsumerCArtifactsConfig(
            firmwareProjectDir = buildDir.resolve("smoke/external-project"),
            bridgeImplTargetPath = "Core/Src/modbus",
            keilUvprojxPath = "MDK-ARM/test1.uvprojx",
            keilTargetName = "test1",
            keilGroupName = "Core/modbus/${context.transport?.transportId ?: "rtu"}",
            mxprojectPath = ".mxproject",
        )
    }
}

internal object SmokeModbusProviderSupport {
    const val SQLITE_PATH_OPTION: String = "addzero.modbus.smoke.sqlite.path"

    fun resolveSqliteFile(environment: com.google.devtools.ksp.processing.SymbolProcessorEnvironment): File? =
        environment.options[SQLITE_PATH_OPTION]
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?.let(::File)
            ?.absoluteFile

    fun resolveBuildDir(environment: com.google.devtools.ksp.processing.SymbolProcessorEnvironment): File? =
        resolveSqliteFile(environment)?.parentFile?.parentFile
}
