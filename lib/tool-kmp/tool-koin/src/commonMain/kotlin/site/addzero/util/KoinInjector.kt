package site.addzero.util

import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinApplication
import org.koin.mp.KoinPlatformTools

@org.koin.core.annotation.KoinApplication
object DefaultKoinApp
//
//@org.koin.core.annotation.Module
//@Configuration
//@ComponentScan("site.addzero")
//class AppModule

/**
 * 获取 Koin KSP 生成的默认模块（平台相关）
 */
internal expect fun loadGeneratedKoinModule(): List<Module>

/**
 * 提供跨模块的 Koin 注入能力。
 *
 * - 库作者可以注册默认模块，业务方可追加自定义模块
 * - 优先复用宿主通过 `startKoin` 初始化的上下文
 * - 如宿主未使用 Koin，则内部创建独立的 KoinApplication
 */
object KoinInjector {

    var registeredModules = loadGeneratedKoinModule()

    private var embeddedKoinApp: KoinApplication? = null
    private var lastSyncedGlobalKoin: Koin? = null

    /**
     * 注册 Koin 模块（可多次调用）
     */
    fun registerModules(vararg modules: Module) {
        if (modules.isEmpty()) return

        registeredModules += modules
        loadModules(modules.toList())
    }

    /**
     * 注入单个实例
     */
    inline fun <reified T : Any> inject(): T {
        return resolveKoin().get()
    }

    /**
     * 注入实例列表
     */
    inline fun <reified T : Any> injectList(): List<T> {
        return resolveKoin().getAll()
    }

    /**
     * 根据条件筛选实例
     */
    inline fun <reified T : Any> getSupportStrategtyOrNull(predicate: (T) -> Boolean): T? {
        return injectList<T>().firstOrNull(predicate)
    }

    inline fun <reified T : Any> getSupportStrategty(
        msg: String = "No element matching predicate found",
        predicate: (T) -> Boolean
    ): T {
        val supportStrategtyOrNull = getSupportStrategtyOrNull<T>(predicate)
            ?: throw UnsupportedOperationException(msg)
        return supportStrategtyOrNull
    }

    @PublishedApi
    internal fun resolveKoin(): Koin {
        val global = globalKoin()
        if (global != null) {
            syncModulesToGlobal(global)
            embeddedKoinApp = null
            return global
        }
        return ensureEmbeddedApp().koin
    }

    private fun loadModules(modules: List<Module>) {
        val global = globalKoin()
        if (global != null) {
            global.loadModules(modules)
            lastSyncedGlobalKoin = global
        } else {
            ensureEmbeddedApp().modules(modules)
        }
    }

    private fun syncModulesToGlobal(global: Koin) {
        if (lastSyncedGlobalKoin === global) {
            return
        }
        if (registeredModules.isNotEmpty()) {
            global.loadModules(registeredModules)
        }
        lastSyncedGlobalKoin = global
    }

    private fun globalKoin(): Koin? {
        return runCatching { KoinPlatformTools.defaultContext().getOrNull() }.getOrNull()
    }

    private fun ensureEmbeddedApp(): KoinApplication {
        val cached = embeddedKoinApp
        if (cached != null) {
            return cached
        }

        val app = koinApplication {}
        if (registeredModules.isNotEmpty()) {
            app.modules(registeredModules)
        }
        embeddedKoinApp = app
        return app
    }
}
