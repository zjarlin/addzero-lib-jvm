package site.addzero.device.protocol.modbus.ksp.core

/**
 * 默认 Kotlin 接口元数据提供者。
 */
class ModbusInterfaceMetadataProvider : ModbusMetadataProvider {
    override val providerId: String = "interfaces"

    override fun isEnabled(context: ModbusMetadataCollectionContext): Boolean =
        context.contractPackages.isNotEmpty()

    override fun collect(context: ModbusMetadataCollectionContext): List<CollectedModbusService> =
        ModbusSymbolCollector(context.environment.logger).collect(
            resolver = context.resolver,
            transport = context.transport,
            contractPackages = context.contractPackages,
        )
}
