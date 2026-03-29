package site.addzero.kbox.plugin.api

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import org.koin.core.module.Module

const val KBOX_PLUGIN_API_VERSION = "1.0"

@Serializable
enum class KboxPluginCapability {
    SCREEN,
}

@Serializable
data class KboxPluginManifest(
    val pluginId: String,
    val name: String,
    val version: String,
    val hostApiVersion: String = KBOX_PLUGIN_API_VERSION,
    val entryClass: String,
    val description: String = "",
    val capabilities: List<KboxPluginCapability> = listOf(KboxPluginCapability.SCREEN),
)

data class KboxRouteContribution(
    val pluginId: String,
    val sceneName: String,
    val title: String,
    val routePath: String,
    val parentName: String = "",
    val iconName: String = "Extension",
    val sceneIconName: String = "Extension",
    val order: Double = 0.0,
    val sceneOrder: Int = Int.MAX_VALUE,
    val defaultInScene: Boolean = false,
    val content: @Composable () -> Unit,
)

interface KboxPluginContext {
    val manifest: KboxPluginManifest
    val appDataDir: String
    val pluginDir: String

    fun log(
        message: String,
        error: Throwable? = null,
    )
}

interface KboxRuntimePlugin {
    fun koinModules(
        context: KboxPluginContext,
    ): List<Module> = emptyList()

    fun routes(
        context: KboxPluginContext,
    ): List<KboxRouteContribution> = emptyList()

    fun onLoad(
        context: KboxPluginContext,
    ) {
    }

    fun onUnload(
        context: KboxPluginContext,
    ) {
    }
}

@Serializable
enum class KboxInstalledPluginState {
    LOADED,
    DISABLED,
    FAILED,
    PENDING_DELETE,
    NOT_LOADED,
}

@Serializable
data class KboxInstalledPluginSnapshot(
    val pluginId: String,
    val name: String,
    val version: String,
    val pluginDir: String,
    val enabled: Boolean,
    val state: KboxInstalledPluginState,
    val hasScreen: Boolean,
    val lastError: String = "",
)

@Serializable
data class KboxPluginOperationResult(
    val pluginId: String,
    val success: Boolean,
    val message: String,
    val snapshot: KboxInstalledPluginSnapshot? = null,
)

interface KboxDynamicRouteRegistry {
    val dynamicRoutes: StateFlow<List<KboxRouteContribution>>
}

interface KboxPluginInstallerService {
    suspend fun installFromDirectory(
        sourceDir: String,
    ): KboxPluginOperationResult
}

interface KboxPluginManagerService : KboxDynamicRouteRegistry, KboxPluginInstallerService {
    val installedPlugins: StateFlow<List<KboxInstalledPluginSnapshot>>

    suspend fun refresh()

    suspend fun enable(
        pluginId: String,
    ): KboxPluginOperationResult

    suspend fun disable(
        pluginId: String,
    ): KboxPluginOperationResult

    suspend fun uninstall(
        pluginId: String,
    ): KboxPluginOperationResult
}
