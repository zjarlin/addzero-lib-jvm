package site.addzero.device.protocol.modbus.ksp.core

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import java.util.ServiceLoader

/**
 * Modbus 元数据输入 SPI。
 *
 * processor 只负责生命周期和产物写出，不再直接依赖某一种输入来源；
 * 具体可以来自 Kotlin 接口、数据库，或后续其他协议上下文源。
 */
interface ModbusMetadataProvider {
    val providerId: String

    val order: Int
        get() = 0

    fun isEnabled(context: ModbusMetadataCollectionContext): Boolean = true

    fun collect(context: ModbusMetadataCollectionContext): List<CollectedModbusService>
}

data class ModbusMetadataCollectionContext(
    val environment: SymbolProcessorEnvironment,
    val resolver: Resolver,
    val transport: ModbusTransportKind,
    val contractPackages: List<String>,
)

object ModbusMetadataCollector {
    fun collect(
        environment: SymbolProcessorEnvironment,
        resolver: Resolver,
        transport: ModbusTransportKind,
        contractPackages: List<String>,
    ): List<CollectedModbusService> {
        val context =
            ModbusMetadataCollectionContext(
                environment = environment,
                resolver = resolver,
                transport = transport,
                contractPackages = contractPackages,
            )
        val providers = resolveProviders(context)
        val collected = linkedMapOf<String, Pair<String, CollectedModbusService>>()
        providers.forEach { provider ->
            environment.logger.logging("Collecting Modbus metadata via provider: ${provider.providerId}")
            provider.collect(context).forEach { service ->
                val interfaceQualifiedName = service.model.interfaceQualifiedName
                val existing = collected.putIfAbsent(interfaceQualifiedName, provider.providerId to service)
                if (existing != null) {
                    error("Duplicate Modbus metadata for $interfaceQualifiedName from providers ${existing.first} and ${provider.providerId}.")
                }
            }
        }
        return collected.values.map { (_, service) -> service }
    }

    fun availableProviderIds(): List<String> =
        loadProviders()
            .map(ModbusMetadataProvider::providerId)
            .sorted()

    private fun resolveProviders(
        context: ModbusMetadataCollectionContext,
    ): List<ModbusMetadataProvider> {
        val loaded = loadProviders()
        check(loaded.isNotEmpty()) {
            "未通过 ServiceLoader 加载到任何 ModbusMetadataProvider；请确认默认 provider 已加入 classpath。"
        }
        val configuredProviderIds = context.environment.resolveMetadataProviderIds()
        val selected =
            if (configuredProviderIds.isEmpty()) {
                loaded
            } else {
                val providersById = loaded.associateBy(ModbusMetadataProvider::providerId)
                val unknownIds = configuredProviderIds - providersById.keys
                check(unknownIds.isEmpty()) {
                    "未知的 Modbus metadata provider: ${unknownIds.joinToString()}，当前可选值：${providersById.keys.sorted().joinToString()}."
                }
                configuredProviderIds.map { providerId -> providersById.getValue(providerId) }
            }
        return selected
            .filter { provider -> provider.isEnabled(context) }
            .sortedWith(compareBy(ModbusMetadataProvider::order, ModbusMetadataProvider::providerId))
    }

    private fun loadProviders(): List<ModbusMetadataProvider> =
        ServiceLoader
            .load(ModbusMetadataProvider::class.java, ModbusMetadataCollector::class.java.classLoader)
            .toList()
}
