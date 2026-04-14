package site.addzero.device.protocol.modbus.ksp.gateway

import site.addzero.device.protocol.modbus.ksp.core.GeneratedArtifact
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactGenerator
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactKind
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactRenderContext

class ModbusKotlinGatewayArtifactGenerator : ModbusArtifactGenerator {
    override val kind = ModbusArtifactKind.KOTLIN_GATEWAY

    override fun render(context: ModbusArtifactRenderContext): List<GeneratedArtifact> =
        ModbusKotlinGatewayArtifactTemplates.renderGatewayArtifacts(
            transport = context.suite.transport,
            services = context.suite.services,
            transportDefaults = context.suite.transportDefaults,
            serverRouteMode = context.serverRouteMode,
        )
}
