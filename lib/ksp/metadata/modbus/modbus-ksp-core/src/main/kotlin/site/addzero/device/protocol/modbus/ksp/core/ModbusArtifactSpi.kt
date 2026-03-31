package site.addzero.device.protocol.modbus.ksp.core

import java.util.ServiceLoader

/**
 * 协议套件的单类产物生成 SPI。
 *
 * 每个输出模块只负责一类产物：
 * - Kotlin gateway
 * - C service/transport exposure
 * - Markdown protocol docs
 */
interface ModbusArtifactGenerator {
    val kind: ModbusArtifactKind

    fun render(context: ModbusArtifactRenderContext): List<GeneratedArtifact>
}

/**
 * 面向 processor 的统一 facade。
 *
 * processor 不再直接依赖具体模板模块，而是只依赖 core 中的 suite metadata + SPI。
 */
object ModbusArtifactRenderer {
    fun renderGatewayArtifacts(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
    ): List<GeneratedArtifact> =
        render(
            context =
                ModbusArtifactRenderContext(
                    kind = ModbusArtifactKind.KOTLIN_GATEWAY,
                    suite = ModbusProtocolSuiteModel(transport = transport, services = services),
                ),
        )

    fun renderServerArtifacts(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
    ): List<GeneratedArtifact> = renderGatewayArtifacts(transport, services)

    fun renderServiceContractArtifacts(service: ModbusServiceModel): List<GeneratedArtifact> =
        render(
            context =
                ModbusArtifactRenderContext(
                    kind = ModbusArtifactKind.C_SERVICE_CONTRACT,
                    suite = ModbusProtocolSuiteModel(transport = service.transport, services = listOf(service)),
                    service = service,
                ),
        )

    fun renderMarkdownArtifacts(service: ModbusServiceModel): List<GeneratedArtifact> =
        render(
            context =
                ModbusArtifactRenderContext(
                    kind = ModbusArtifactKind.MARKDOWN_PROTOCOL,
                    suite = ModbusProtocolSuiteModel(transport = service.transport, services = listOf(service)),
                    service = service,
                ),
        )

    fun renderContractArtifacts(service: ModbusServiceModel): List<GeneratedArtifact> =
        renderServiceContractArtifacts(service) + renderMarkdownArtifacts(service)

    fun renderTransportContractArtifacts(
        transport: ModbusTransportKind,
        services: List<ModbusServiceModel>,
    ): List<GeneratedArtifact> =
        render(
            context =
                ModbusArtifactRenderContext(
                    kind = ModbusArtifactKind.C_TRANSPORT_CONTRACT,
                    suite = ModbusProtocolSuiteModel(transport = transport, services = services),
                ),
        )

    private fun render(context: ModbusArtifactRenderContext): List<GeneratedArtifact> =
        generators()
            .getValue(context.kind)
            .render(context)

    private fun generators(): Map<ModbusArtifactKind, ModbusArtifactGenerator> {
        val loaded =
            ServiceLoader
                .load(ModbusArtifactGenerator::class.java, ModbusArtifactRenderer::class.java.classLoader)
                .toList()
        check(loaded.isNotEmpty()) {
            "未通过 ServiceLoader 加载到任何 ModbusArtifactGenerator；请确认 kotlin/c/markdown generator 模块已加入 classpath。"
        }
        val grouped = loaded.groupBy(ModbusArtifactGenerator::kind)
        val duplicates = grouped.filterValues { generators -> generators.size > 1 }
        check(duplicates.isEmpty()) {
            "发现重复的 ModbusArtifactGenerator 注册：${duplicates.keys.joinToString()}"
        }
        return grouped.mapValues { (_, generators) -> generators.single() }
    }
}
