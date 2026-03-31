package site.addzero.device.protocol.modbus.ksp.markdown

import site.addzero.device.protocol.modbus.ksp.core.GeneratedArtifact
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactGenerator
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactKind
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactRenderContext
import site.addzero.device.protocol.modbus.ksp.core.ModbusArtifactTemplates

class ModbusMarkdownArtifactGenerator : ModbusArtifactGenerator {
    override val kind: ModbusArtifactKind = ModbusArtifactKind.MARKDOWN_PROTOCOL

    override fun render(context: ModbusArtifactRenderContext): List<GeneratedArtifact> =
        ModbusArtifactTemplates.renderMarkdownArtifacts(
            service = requireNotNull(context.service) { "Markdown generation requires a single service context." },
        )
}
