package site.addzero.device.protocol.modbus.ksp.c

import site.addzero.device.protocol.modbus.ksp.core.GeneratedArtifact
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactGenerator
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactKind
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactRenderContext
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactTemplates

class ModbusCServiceContractArtifactGenerator : ModbusArtifactGenerator {
    override val kind: ModbusArtifactKind = ModbusArtifactKind.C_SERVICE_CONTRACT

    override fun render(context: ModbusArtifactRenderContext): List<GeneratedArtifact> =
        ModbusArtifactTemplates.renderServiceContractArtifacts(
            service = requireNotNull(context.service) { "C service contract generation requires a single service context." },
            transportDefaults = context.suite.transportDefaults,
        )
}

class ModbusCTransportContractArtifactGenerator : ModbusArtifactGenerator {
    override val kind: ModbusArtifactKind = ModbusArtifactKind.C_TRANSPORT_CONTRACT

    override fun render(context: ModbusArtifactRenderContext): List<GeneratedArtifact> =
        ModbusArtifactTemplates.renderTransportContractArtifacts(
            transport = context.suite.transport,
            services = context.suite.services,
            transportDefaults = context.suite.transportDefaults,
        )
}
