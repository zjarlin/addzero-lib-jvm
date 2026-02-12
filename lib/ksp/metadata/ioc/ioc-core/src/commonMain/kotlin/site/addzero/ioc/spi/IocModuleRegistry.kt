package site.addzero.ioc.spi

/**
 * Central registry for collecting IocModuleProviders from all modules.
 *
 * Usage pattern (generated code calls this):
 *   IocModuleRegistry.register(MyModuleProvider)
 *
 * Then at app startup:
 *   IocModuleRegistry.applyAll(registry)
 */
object IocModuleRegistry {
    private val providers = mutableListOf<IocModuleProvider>()

    fun register(provider: IocModuleProvider) {
        if (providers.none { it.moduleName == provider.moduleName }) {
            providers.add(provider)
        }
    }

    fun getProviders(): List<IocModuleProvider> = providers.toList()

    fun clear() {
        providers.clear()
    }
}
