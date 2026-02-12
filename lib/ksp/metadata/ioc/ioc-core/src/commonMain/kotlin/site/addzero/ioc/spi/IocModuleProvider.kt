package site.addzero.ioc.spi

import site.addzero.ioc.registry.MutableBeanRegistry

/**
 * SPI interface for IoC module registration.
 *
 * Each KSP-processed module generates an implementation of this interface.
 * At startup, all implementations are collected and applied to the central registry.
 *
 * KMP-friendly: no reflection, no Class.forName, no ServiceLoader.
 * Each module generates a concrete object implementing this interface,
 * and a top-level function to register it.
 */
interface IocModuleProvider {
    /** unique module identifier */
    val moduleName: String

    /** register all beans from this module into the registry */
    fun register(registry: MutableBeanRegistry)
}
