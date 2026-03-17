package site.addzero.biz.spec.iot.spi

import site.addzero.biz.spec.iot.IotThingRef
import java.util.ServiceLoader

/**
 * ServiceLoader facade for [IotPropertySpecProvider].
 */
object IotPropertySpecProviders {

    @Volatile
    private var cachedProviders: List<IotPropertySpecProvider>? = null

    @JvmStatic
    fun loadAll(): List<IotPropertySpecProvider> {
        return providers()
    }

    @JvmStatic
    fun load(thingRef: IotThingRef): IotPropertySpecProvider {
        val matches = providers().filter { it.supports(thingRef) }
        if (matches.isEmpty()) {
            throw IllegalStateException("No IotPropertySpecProvider found for thing $thingRef")
        }
        if (matches.size > 1) {
            throw IllegalStateException(
                "Multiple IotPropertySpecProviders found for thing $thingRef: ${providerNames(matches)}",
            )
        }
        return matches.first()
    }

    @JvmStatic
    @Synchronized
    fun reload(): List<IotPropertySpecProvider> {
        cachedProviders = discoverProviders()
        return cachedProviders.orEmpty()
    }

    private fun providers(): List<IotPropertySpecProvider> {
        val current = cachedProviders
        if (current != null) {
            return current
        }
        return synchronized(this) {
            cachedProviders ?: discoverProviders().also { cachedProviders = it }
        }
    }

    private fun discoverProviders(): List<IotPropertySpecProvider> {
        val loader = ServiceLoader.load(
            IotPropertySpecProvider::class.java,
            Thread.currentThread().contextClassLoader,
        )
        return loader.toList()
    }

    private fun providerNames(providers: List<IotPropertySpecProvider>): String {
        return providers.joinToString(separator = ", ") { it.name }
    }
}
