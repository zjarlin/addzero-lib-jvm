package site.addzero.kbox.runtime

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import org.koin.mp.KoinPlatform
import site.addzero.kbox.core.service.KboxPathService
import site.addzero.kbox.plugin.api.KBOX_PLUGIN_API_VERSION
import site.addzero.kbox.plugin.api.KboxDynamicRouteRegistry
import site.addzero.kbox.plugin.api.KboxInstalledPluginSnapshot
import site.addzero.kbox.plugin.api.KboxInstalledPluginState
import site.addzero.kbox.plugin.api.KboxPluginContext
import site.addzero.kbox.plugin.api.KboxPluginInstallerService
import site.addzero.kbox.plugin.api.KboxPluginManagerService
import site.addzero.kbox.plugin.api.KboxPluginManifest
import site.addzero.kbox.plugin.api.KboxPluginOperationResult
import site.addzero.kbox.plugin.api.KboxRouteContribution
import site.addzero.kbox.plugin.api.KboxRuntimePlugin
import java.io.File
import java.net.URLClassLoader

@Single(
    binds = [
        KboxPluginManagerService::class,
        KboxPluginInstallerService::class,
        KboxDynamicRouteRegistry::class,
    ],
)
class KboxRuntimePluginManager(
    private val pathService: KboxPathService,
    private val json: Json,
) : KboxPluginManagerService {
    override val dynamicRoutes: StateFlow<List<KboxRouteContribution>>
        get() = dynamicRoutesState.asStateFlow()

    override val installedPlugins: StateFlow<List<KboxInstalledPluginSnapshot>>
        get() = installedPluginsState.asStateFlow()

    private val lock = Mutex()
    private val dynamicRoutesState = MutableStateFlow<List<KboxRouteContribution>>(emptyList())
    private val installedPluginsState = MutableStateFlow<List<KboxInstalledPluginSnapshot>>(emptyList())
    private val loadedPlugins = linkedMapOf<String, LoadedPluginHandle>()
    private val lastErrors = linkedMapOf<String, String>()

    override suspend fun refresh() {
        lock.withLock {
            cleanupPendingDeleteEntries()
            val scanResult = scanInstalledPlugins()
            val discoveredIds = scanResult.discovered.map { plugin -> plugin.pluginId }.toSet()
            loadedPlugins.keys.filterNot(discoveredIds::contains)
                .toList()
                .forEach(::unloadLoadedPlugin)

            scanResult.discovered.forEach { plugin ->
                val state = currentState(plugin.pluginId)
                if (state.enabled) {
                    loadPluginIfNecessary(plugin)
                } else {
                    unloadLoadedPlugin(plugin.pluginId)
                }
            }
            publishState(scanResult)
        }
    }

    override suspend fun installFromDirectory(
        sourceDir: String,
    ): KboxPluginOperationResult {
        return lock.withLock {
            val source = File(sourceDir.trim())
            require(source.isDirectory) {
                "插件目录不存在：${source.absolutePath}"
            }
            val manifest = parseManifest(source.resolve("plugin.json"))
            require(manifest.hostApiVersion == KBOX_PLUGIN_API_VERSION) {
                "插件 API 版本不兼容：${manifest.hostApiVersion}"
            }
            val targetDir = pluginDir(manifest.pluginId)
            unloadLoadedPlugin(manifest.pluginId)
            if (source.canonicalFile != targetDir.canonicalFile) {
                if (targetDir.exists() && !targetDir.deleteRecursively()) {
                    return@withLock failureResult(manifest.pluginId, "旧插件目录删除失败：${targetDir.absolutePath}")
                }
                targetDir.parentFile?.mkdirs()
                source.copyRecursively(targetDir, overwrite = true)
            }
            saveState(
                manifest.pluginId,
                currentState(manifest.pluginId).copy(
                    enabled = true,
                    pendingDelete = false,
                ),
            )
            val scanResult = scanInstalledPlugins()
            val plugin = scanResult.discovered.first { item -> item.pluginId == manifest.pluginId }
            val loadError = loadPluginIfNecessary(plugin)
            publishState(scanResult)
            if (loadError == null) {
                successResult(plugin.pluginId, "插件已安装并加载")
            } else {
                failureResult(plugin.pluginId, loadError)
            }
        }
    }

    override suspend fun enable(
        pluginId: String,
    ): KboxPluginOperationResult {
        return lock.withLock {
            val plugin = requireInstalled(pluginId)
            saveState(pluginId, currentState(pluginId).copy(enabled = true, pendingDelete = false))
            val loadError = loadPluginIfNecessary(plugin)
            publishState(scanInstalledPlugins())
            if (loadError == null) {
                successResult(pluginId, "插件已启用")
            } else {
                failureResult(pluginId, loadError)
            }
        }
    }

    override suspend fun disable(
        pluginId: String,
    ): KboxPluginOperationResult {
        return lock.withLock {
            requireInstalled(pluginId)
            saveState(pluginId, currentState(pluginId).copy(enabled = false))
            unloadLoadedPlugin(pluginId)
            publishState(scanInstalledPlugins())
            successResult(pluginId, "插件已停用")
        }
    }

    override suspend fun uninstall(
        pluginId: String,
    ): KboxPluginOperationResult {
        return lock.withLock {
            requireInstalled(pluginId)
            unloadLoadedPlugin(pluginId)
            val pluginDir = pluginDir(pluginId)
            val deleted = pluginDir.deleteRecursively()
            saveState(
                pluginId,
                currentState(pluginId).copy(
                    enabled = false,
                    pendingDelete = !deleted,
                ),
            )
            if (deleted) {
                removeState(pluginId)
            }
            publishState(scanInstalledPlugins())
            if (deleted) {
                successResult(pluginId, "插件目录已删除")
            } else {
                failureResult(pluginId, "插件已卸载，但目录仍被占用，重启后可再次清理")
            }
        }
    }

    private fun publishState(
        scanResult: PluginScanResult,
    ) {
        dynamicRoutesState.value = loadedPlugins.values
            .sortedBy { handle -> handle.manifest.name }
            .flatMap { handle -> handle.routes }

        val discoveredSnapshots = scanResult.discovered.map { plugin ->
            val savedState = currentState(plugin.pluginId)
            val loaded = loadedPlugins[plugin.pluginId]
            val errorMessage = lastErrors[plugin.pluginId].orEmpty()
            val state = when {
                savedState.pendingDelete -> KboxInstalledPluginState.PENDING_DELETE
                !savedState.enabled -> KboxInstalledPluginState.DISABLED
                loaded != null -> KboxInstalledPluginState.LOADED
                errorMessage.isNotBlank() -> KboxInstalledPluginState.FAILED
                else -> KboxInstalledPluginState.NOT_LOADED
            }
            KboxInstalledPluginSnapshot(
                pluginId = plugin.pluginId,
                name = plugin.manifest.name,
                version = plugin.manifest.version,
                pluginDir = plugin.directory.absolutePath,
                enabled = savedState.enabled,
                state = state,
                hasScreen = plugin.manifest.capabilities.any { capability ->
                    capability.name == "SCREEN"
                },
                lastError = errorMessage,
            )
        }
        val failedSnapshots = scanResult.failures.map { failure ->
            KboxInstalledPluginSnapshot(
                pluginId = failure.pluginId,
                name = failure.pluginId,
                version = "-",
                pluginDir = failure.directory.absolutePath,
                enabled = false,
                state = KboxInstalledPluginState.FAILED,
                hasScreen = false,
                lastError = failure.message,
            )
        }
        installedPluginsState.value = (discoveredSnapshots + failedSnapshots)
            .sortedBy { snapshot -> snapshot.name.lowercase() }
    }

    private fun loadPluginIfNecessary(
        plugin: DiscoveredPlugin,
    ): String? {
        if (loadedPlugins.containsKey(plugin.pluginId)) {
            return null
        }
        return runCatching {
            require(plugin.manifest.hostApiVersion == KBOX_PLUGIN_API_VERSION) {
                "插件 API 版本不兼容：${plugin.manifest.hostApiVersion}"
            }
            val jarFiles = plugin.directory.resolve("lib")
                .listFiles()
                ?.filter { file -> file.extension == "jar" }
                .orEmpty()
            require(jarFiles.isNotEmpty()) {
                "插件 lib 目录没有可加载的 jar：${plugin.directory.absolutePath}"
            }
            val classLoader = URLClassLoader(
                jarFiles.map { file -> file.toURI().toURL() }.toTypedArray(),
                javaClass.classLoader,
            )
            val pluginClass = classLoader.loadClass(plugin.manifest.entryClass)
            require(KboxRuntimePlugin::class.java.isAssignableFrom(pluginClass)) {
                "插件入口未实现 KboxRuntimePlugin：${plugin.manifest.entryClass}"
            }
            val pluginInstance = pluginClass.getDeclaredConstructor().newInstance() as KboxRuntimePlugin
            val context = DefaultKboxPluginContext(
                manifest = plugin.manifest,
                appDataDir = pathService.appDataDir().absolutePath,
                pluginDir = plugin.directory.absolutePath,
            )
            val modules = pluginInstance.koinModules(context)
            if (modules.isNotEmpty()) {
                KoinPlatform.getKoin().loadModules(modules)
            }
            val routes = pluginInstance.routes(context)
            pluginInstance.onLoad(context)
            loadedPlugins[plugin.pluginId] = LoadedPluginHandle(
                manifest = plugin.manifest,
                plugin = pluginInstance,
                classLoader = classLoader,
                modules = modules,
                routes = routes,
                context = context,
            )
            lastErrors.remove(plugin.pluginId)
        }.exceptionOrNull()?.let { error ->
            unloadLoadedPlugin(plugin.pluginId)
            val message = error.message ?: "插件加载失败"
            lastErrors[plugin.pluginId] = message
            message
        }
    }

    private fun unloadLoadedPlugin(
        pluginId: String,
    ) {
        val handle = loadedPlugins.remove(pluginId) ?: return
        runCatching {
            handle.plugin.onUnload(handle.context)
        }
        if (handle.modules.isNotEmpty()) {
            runCatching {
                KoinPlatform.getKoin().unloadModules(handle.modules)
            }
        }
        runCatching {
            handle.classLoader.close()
        }
    }

    private fun cleanupPendingDeleteEntries() {
        val updatedStates = loadState().toMutableMap()
        updatedStates.values.filter { state -> state.pendingDelete }
            .forEach { state ->
                val pluginDir = pluginDir(state.pluginId)
                if (!pluginDir.exists() || pluginDir.deleteRecursively()) {
                    updatedStates.remove(state.pluginId)
                }
            }
        saveStateFile(updatedStates)
    }

    private fun requireInstalled(
        pluginId: String,
    ): DiscoveredPlugin {
        return scanInstalledPlugins().discovered.firstOrNull { plugin -> plugin.pluginId == pluginId }
            ?: throw IllegalArgumentException("插件不存在：$pluginId")
    }

    private fun scanInstalledPlugins(): PluginScanResult {
        val pluginRoot = pluginsRoot()
        if (!pluginRoot.isDirectory) {
            return PluginScanResult()
        }
        val discovered = mutableListOf<DiscoveredPlugin>()
        val failures = mutableListOf<FailedPluginDirectory>()
        pluginRoot.listFiles()
            .orEmpty()
            .filter(File::isDirectory)
            .forEach { directory ->
                runCatching {
                    parseManifest(directory.resolve("plugin.json"))
                }.onSuccess { manifest ->
                    discovered += DiscoveredPlugin(
                        pluginId = manifest.pluginId.trim(),
                        manifest = manifest,
                        directory = directory,
                    )
                    lastErrors.remove(manifest.pluginId)
                    lastErrors.remove(directory.name)
                }.onFailure { error ->
                    val message = error.message ?: "插件清单解析失败"
                    lastErrors[directory.name] = message
                    failures += FailedPluginDirectory(
                        pluginId = directory.name,
                        directory = directory,
                        message = message,
                    )
                }
            }
        return PluginScanResult(
            discovered = discovered.sortedBy { plugin -> plugin.manifest.name.lowercase() },
            failures = failures.sortedBy { failure -> failure.pluginId.lowercase() },
        )
    }

    private fun parseManifest(
        manifestFile: File,
    ): KboxPluginManifest {
        require(manifestFile.isFile) {
            "插件清单不存在：${manifestFile.absolutePath}"
        }
        val manifest = json.decodeFromString<KboxPluginManifest>(manifestFile.readText())
        require(manifest.pluginId.isNotBlank()) {
            "插件 ID 不能为空"
        }
        require(manifest.entryClass.isNotBlank()) {
            "插件入口类不能为空"
        }
        return manifest
    }

    private fun currentState(
        pluginId: String,
    ): PersistedPluginState {
        return loadState()[pluginId] ?: PersistedPluginState(pluginId = pluginId)
    }

    private fun saveState(
        pluginId: String,
        state: PersistedPluginState,
    ) {
        val updated = loadState().toMutableMap()
        updated[pluginId] = state.copy(pluginId = pluginId)
        saveStateFile(updated)
    }

    private fun removeState(
        pluginId: String,
    ) {
        val updated = loadState().toMutableMap()
        updated.remove(pluginId)
        saveStateFile(updated)
    }

    private fun loadState(): Map<String, PersistedPluginState> {
        val file = stateFile()
        if (!file.isFile) {
            return emptyMap()
        }
        return json.decodeFromString<PersistedPluginStateStore>(file.readText())
            .plugins
            .associateBy { state -> state.pluginId }
    }

    private fun saveStateFile(
        states: Map<String, PersistedPluginState>,
    ) {
        val file = stateFile()
        file.parentFile?.mkdirs()
        file.writeText(
            json.encodeToString(
                PersistedPluginStateStore(
                    plugins = states.values.sortedBy { state -> state.pluginId },
                ),
            ),
        )
    }

    private fun pluginsRoot(): File {
        return File(pathService.appDataDir(), "plugins").apply { mkdirs() }
    }

    private fun pluginDir(
        pluginId: String,
    ): File {
        return File(pluginsRoot(), pluginId)
    }

    private fun stateFile(): File {
        return File(pluginsRoot(), "runtime-state.json")
    }

    private fun successResult(
        pluginId: String,
        message: String,
    ): KboxPluginOperationResult {
        return KboxPluginOperationResult(
            pluginId = pluginId,
            success = true,
            message = message,
            snapshot = installedPluginsState.value.firstOrNull { snapshot -> snapshot.pluginId == pluginId },
        )
    }

    private fun failureResult(
        pluginId: String,
        message: String,
    ): KboxPluginOperationResult {
        return KboxPluginOperationResult(
            pluginId = pluginId,
            success = false,
            message = message,
            snapshot = installedPluginsState.value.firstOrNull { snapshot -> snapshot.pluginId == pluginId },
        )
    }
}

private class DefaultKboxPluginContext(
    override val manifest: KboxPluginManifest,
    override val appDataDir: String,
    override val pluginDir: String,
) : KboxPluginContext {
    override fun log(
        message: String,
        error: Throwable?,
    ) {
        println("[KBox:${manifest.pluginId}] $message")
        error?.printStackTrace()
    }
}

private data class LoadedPluginHandle(
    val manifest: KboxPluginManifest,
    val plugin: KboxRuntimePlugin,
    val classLoader: URLClassLoader,
    val modules: List<org.koin.core.module.Module>,
    val routes: List<KboxRouteContribution>,
    val context: KboxPluginContext,
)

private data class DiscoveredPlugin(
    val pluginId: String,
    val manifest: KboxPluginManifest,
    val directory: File,
)

private data class FailedPluginDirectory(
    val pluginId: String,
    val directory: File,
    val message: String,
)

private data class PluginScanResult(
    val discovered: List<DiscoveredPlugin> = emptyList(),
    val failures: List<FailedPluginDirectory> = emptyList(),
)

@Serializable
private data class PersistedPluginStateStore(
    val plugins: List<PersistedPluginState> = emptyList(),
)

@Serializable
private data class PersistedPluginState(
    val pluginId: String = "",
    val enabled: Boolean = true,
    val pendingDelete: Boolean = false,
)
