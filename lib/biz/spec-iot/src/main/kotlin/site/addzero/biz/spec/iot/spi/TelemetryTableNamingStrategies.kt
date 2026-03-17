package site.addzero.biz.spec.iot.spi

import site.addzero.biz.spec.iot.IotThingRef
import java.util.ServiceLoader

/**
 * ServiceLoader facade for [TelemetryTableNamingStrategy].
 */
object TelemetryTableNamingStrategies {

    private val defaultStrategy: TelemetryTableNamingStrategy = DefaultTelemetryTableNamingStrategy()

    @Volatile
    private var cachedProviders: List<TelemetryTableNamingStrategy>? = null

    @JvmStatic
    fun load(schemaRef: IotThingRef): TelemetryTableNamingStrategy {
        val matches = providers().filter { it.supports(schemaRef) }
        if (matches.size > 1) {
            throw IllegalStateException(
                "Multiple TelemetryTableNamingStrategies found for thing $schemaRef: ${strategyNames(matches)}",
            )
        }
        return matches.singleOrNull() ?: defaultStrategy
    }

    @JvmStatic
    fun loadAll(): List<TelemetryTableNamingStrategy> {
        val all = ArrayList(providers())
        all += defaultStrategy
        return all.toList()
    }

    @JvmStatic
    @Synchronized
    fun reload(): List<TelemetryTableNamingStrategy> {
        cachedProviders = discoverProviders()
        return loadAll()
    }

    private fun providers(): List<TelemetryTableNamingStrategy> {
        val current = cachedProviders
        if (current != null) {
            return current
        }
        return synchronized(this) {
            cachedProviders ?: discoverProviders().also { cachedProviders = it }
        }
    }

    private fun discoverProviders(): List<TelemetryTableNamingStrategy> {
        val loader = ServiceLoader.load(
            TelemetryTableNamingStrategy::class.java,
            Thread.currentThread().contextClassLoader,
        )
        return loader.toList()
    }

    private fun strategyNames(strategies: List<TelemetryTableNamingStrategy>): String {
        return strategies.joinToString(separator = ", ") { it.name }
    }

    private class DefaultTelemetryTableNamingStrategy : TelemetryTableNamingStrategy {

        override val name: String = "default"

        override fun supports(schemaRef: IotThingRef): Boolean {
            return true
        }

        override fun stableTableName(schemaRef: IotThingRef): String {
            return "telemetry_${normalize(schemaRef.kind)}_${normalize(schemaRef.id)}"
        }

        override fun subTableName(schemaRef: IotThingRef, sourceRef: IotThingRef): String {
            return "telemetry_${normalize(sourceRef.kind)}_${normalize(sourceRef.id)}"
        }

        private fun normalize(value: String?): String {
            val lower = value?.trim()?.lowercase().orEmpty()
            val builder = StringBuilder()
            lower.forEach { current ->
                if ((current in 'a'..'z') || (current in '0'..'9') || current == '_') {
                    builder.append(current)
                } else {
                    builder.append('_')
                }
            }
            if (builder.isEmpty() || builder.first().isDigit()) {
                builder.insert(0, "t_")
            }
            return builder.toString()
        }
    }
}
