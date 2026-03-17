package site.addzero.biz.spec.iot.spi

import site.addzero.biz.spec.iot.IotPropertySpec
import site.addzero.biz.spec.iot.IotValueType
import site.addzero.biz.spec.iot.tdengine.TdColumnSpec
import site.addzero.biz.spec.iot.tdengine.TdengineSchemaDefaults
import java.util.Locale
import java.util.ServiceLoader

/**
 * ServiceLoader facade for [TdengineTypeMappingProvider].
 */
object TdengineTypeMappings {

    private val defaultProvider: TdengineTypeMappingProvider = DefaultTdengineTypeMappingProvider()

    @Volatile
    private var cachedProviders: List<TdengineTypeMappingProvider>? = null

    @JvmStatic
    fun toColumnSpec(propertySpec: IotPropertySpec): TdColumnSpec {
        return load(propertySpec.valueType).toColumnSpec(propertySpec)
    }

    @JvmStatic
    fun load(valueType: IotValueType): TdengineTypeMappingProvider {
        val matches = providers().filter { it.supports(valueType) }
        if (matches.size > 1) {
            throw IllegalStateException(
                "Multiple TdengineTypeMappingProviders found for value type $valueType: ${providerNames(matches)}",
            )
        }
        return matches.singleOrNull() ?: defaultProvider
    }

    @JvmStatic
    fun loadAll(): List<TdengineTypeMappingProvider> {
        val all = ArrayList(providers())
        all += defaultProvider
        return all.toList()
    }

    @JvmStatic
    @Synchronized
    fun reload(): List<TdengineTypeMappingProvider> {
        cachedProviders = discoverProviders()
        return loadAll()
    }

    private fun providers(): List<TdengineTypeMappingProvider> {
        val current = cachedProviders
        if (current != null) {
            return current
        }
        return synchronized(this) {
            cachedProviders ?: discoverProviders().also { cachedProviders = it }
        }
    }

    private fun discoverProviders(): List<TdengineTypeMappingProvider> {
        val loader = ServiceLoader.load(
            TdengineTypeMappingProvider::class.java,
            Thread.currentThread().contextClassLoader,
        )
        return loader.toList()
    }

    private fun providerNames(providers: List<TdengineTypeMappingProvider>): String {
        return providers.joinToString(separator = ", ") { it.name }
    }

    private class DefaultTdengineTypeMappingProvider : TdengineTypeMappingProvider {

        override val name: String = "default"

        override fun supports(valueType: IotValueType): Boolean {
            return valueType == IotValueType.BOOLEAN ||
                valueType == IotValueType.INT32 ||
                valueType == IotValueType.FLOAT32
        }

        override fun toColumnSpec(propertySpec: IotPropertySpec): TdColumnSpec {
            val identifier = propertySpec.identifier.trim().lowercase(Locale.ROOT)
            return when (propertySpec.valueType) {
                IotValueType.BOOLEAN -> TdColumnSpec(identifier, TdColumnSpec.TYPE_BOOL, null, null)
                IotValueType.INT32 -> TdColumnSpec(identifier, TdColumnSpec.TYPE_INT, null, null)
                IotValueType.FLOAT32 -> TdColumnSpec(identifier, TdColumnSpec.TYPE_FLOAT, null, null)
            }
        }
    }
}
