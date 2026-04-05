package site.addzero.device.protocol.modbus.ksp.gateway

import site.addzero.device.protocol.modbus.ksp.core.GeneratedArtifact
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactGenerator
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactKind
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactRenderContext
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactTemplates

class ModbusKotlinGatewayArtifactGenerator : ModbusArtifactGenerator {
    override val kind = ModbusArtifactKind.KOTLIN_GATEWAY

    override fun render(context: ModbusArtifactRenderContext): List<GeneratedArtifact> =
        ModbusArtifactTemplates.renderGatewayArtifacts(
            transport = context.suite.transport,
            services = context.suite.services,
            transportDefaults = context.suite.transportDefaults,
            serverRouteMode = context.serverRouteMode,
        )
}
