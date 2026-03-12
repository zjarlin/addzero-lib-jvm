package site.addzero.network.call.payment.spi

import java.util.ServiceLoader

object PaymentProviders {

    @Volatile
    private var cachedProviders: Map<PaymentChannel, PaymentProvider>? = null

    fun loadAll(): List<PaymentProvider> {
        return providers().values.toList()
    }

    fun load(channel: PaymentChannel): PaymentProvider {
        return providers()[channel]
            ?: throw IllegalStateException("No payment provider found for channel: ${channel.code}")
    }

    fun load(channel: String): PaymentProvider {
        return load(PaymentChannel.fromValue(channel))
    }

    fun reload(): List<PaymentProvider> {
        synchronized(this) {
            cachedProviders = discoverProviders()
            return cachedProviders!!.values.toList()
        }
    }

    private fun providers(): Map<PaymentChannel, PaymentProvider> {
        val current = cachedProviders
        if (current != null) {
            return current
        }

        synchronized(this) {
            val reloaded = cachedProviders
            if (reloaded != null) {
                return reloaded
            }

            val discovered = discoverProviders()
            cachedProviders = discovered
            return discovered
        }
    }

    private fun discoverProviders(): Map<PaymentChannel, PaymentProvider> {
        val discovered = linkedMapOf<PaymentChannel, PaymentProvider>()
        val serviceLoader = ServiceLoader.load(PaymentProvider::class.java)
        for (provider in serviceLoader) {
            val previousProvider = discovered.putIfAbsent(provider.channel, provider)
            if (previousProvider != null) {
                throw IllegalStateException(
                    "Duplicate payment provider found for channel ${provider.channel.code}: " +
                        "${previousProvider::class.java.name} and ${provider::class.java.name}",
                )
            }
        }
        return discovered
    }
}
