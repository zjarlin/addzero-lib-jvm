package site.addzero.kcloud.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ServerConfig(
    val id: String = generateId(),
    val name: String,
    val host: String,
    val port: Int = 22,
    val username: String,
    val authType: AuthType = AuthType.PASSWORD,
    val password: String? = null,
    val privateKeyPath: String? = null,
    val passphrase: String? = null,
    val remoteRootPath: String = "/home/$username/kcloud",
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds()
)

@Serializable
enum class AuthType {
    PASSWORD,
    PRIVATE_KEY
}

@Serializable
data class StorageStrategy(
    val organizeByExtension: Boolean = true,
    val organizeByDate: Boolean = false,
    val organizeByTags: Boolean = false,
    val defaultTags: List<String> = emptyList(),
    val conflictStrategy: ConflictStrategy = ConflictStrategy.RENAME
)

@Serializable
enum class ConflictStrategy {
    OVERWRITE,
    SKIP,
    RENAME
}

@Serializable
data class LocalStrategy(
    val deleteAfterTransfer: Boolean = false,
    val createShortcut: Boolean = true,
    val shortcutType: ShortcutType = ShortcutType.ALIAS
)

@Serializable
enum class ShortcutType {
    ALIAS,
    SYMLINK
}

@Serializable
data class RemoteFile(
    val name: String,
    val path: String,
    val size: Long,
    val isDirectory: Boolean,
    val modifiedTime: Long,
    val extension: String? = null,
    val tags: List<String> = emptyList(),
    val isMounted: Boolean = false
)

@Serializable
data class TransferTask(
    val id: String = generateId(),
    val sourcePath: String,
    val targetPath: String,
    val fileName: String,
    val fileSize: Long,
    val serverId: String,
    val status: TransferStatus = TransferStatus.PENDING,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val errorMessage: String? = null
)

@Serializable
enum class TransferStatus {
    PENDING,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

@Serializable
data class TransferRecord(
    val id: String = generateId(),
    val taskId: String,
    val sourcePath: String,
    val targetPath: String,
    val fileName: String,
    val fileSize: Long,
    val serverId: String,
    val status: TransferStatus,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val completedAt: Long? = null,
    val errorMessage: String? = null
)

@Serializable
data class AppSettings(
    val servers: List<ServerConfig> = emptyList(),
    val storageStrategy: StorageStrategy = StorageStrategy(),
    val localStrategy: LocalStrategy = LocalStrategy(),
    val theme: Theme = Theme.SYSTEM,
    val language: String = "zh-CN",
    val autoStart: Boolean = false,
    val showNotifications: Boolean = true,
    val maxConcurrentTransfers: Int = 3,
    val updateSettings: UpdateSettings = UpdateSettings(),
    val ai: AiSettings = AiSettings()
)

object AiProviderIds {
    const val OLLAMA = "ollama"
    const val OPENAI = "openai"
    const val ANTHROPIC = "anthropic"
}

@Serializable
data class AiSettings(
    val enabled: Boolean = false,
    val selectedProviderId: String = AiProviderIds.OLLAMA,
    val ollama: OllamaAiSettings = OllamaAiSettings(),
    val openAi: OpenAiAiSettings = OpenAiAiSettings(),
    val anthropic: AnthropicAiSettings = AnthropicAiSettings()
)

@Serializable
data class OllamaAiSettings(
    val baseUrl: String = "http://127.0.0.1:11434",
    val model: String = "llama3.2",
    val timeoutMillis: Long = 30_000
)

@Serializable
data class OpenAiAiSettings(
    val baseUrl: String = "https://api.openai.com/v1",
    val model: String = "",
    val apiKey: String = ""
)

@Serializable
data class AnthropicAiSettings(
    val baseUrl: String = "https://api.anthropic.com",
    val model: String = "",
    val apiKey: String = ""
)

@Serializable
data class UpdateSettings(
    val checkUpdatesAutomatically: Boolean = true,
    val updateChannel: UpdateChannel = UpdateChannel.STABLE,
    val downloadUpdatesAutomatically: Boolean = false,
    val installUpdatesAutomatically: Boolean = false,
    val lastCheckTime: Long = 0,
    val skipVersion: String? = null
)

@Serializable
enum class UpdateChannel {
    STABLE,      // 稳定版
    BETA,        // 测试版
    DEV          // 开发版
}

@Serializable
data class VersionInfo(
    val version: String,
    val versionCode: Int,
    val releaseDate: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val mandatory: Boolean = false,
    val minVersion: String? = null
)

/**
 * 冲突信息 - 用于UI显示
 */
@Serializable
data class Conflict(
    val id: String = generateId(),
    val path: String,
    val localVersion: String,
    val remoteVersion: String,
    val localSize: Long = 0,
    val remoteSize: Long = 0,
    val localMtime: Long = 0,
    val remoteMtime: Long = 0,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)

@Serializable
enum class Theme {
    LIGHT,
    DARK,
    SYSTEM
}

fun generateId(): String {
    return "${Clock.System.now().toEpochMilliseconds()}_${(1000..9999).random()}"
}

val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}
