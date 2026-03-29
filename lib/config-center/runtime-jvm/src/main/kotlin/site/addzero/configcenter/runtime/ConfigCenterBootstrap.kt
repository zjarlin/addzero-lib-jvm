package site.addzero.configcenter.runtime

import java.io.File

data class ConfigCenterBootstrapOptions(
    val dbPath: String? = null,
    val masterKey: String? = null,
    val profile: String? = null,
    val appId: String? = null,
)

class ConfigCenterBootstrap(
    private val options: ConfigCenterBootstrapOptions = ConfigCenterBootstrapOptions(),
) {
    companion object {
        const val DB_PATH_KEY = "CONFIG_CENTER_DB_PATH"
        const val MASTER_KEY = "CONFIG_CENTER_MASTER_KEY"
        const val PROFILE_KEY = "CONFIG_CENTER_PROFILE"
        const val APP_ID_KEY = "CONFIG_CENTER_APP_ID"

        private const val DEFAULT_PROFILE = "default"
        private const val DEFAULT_APP_ID = "kcloud"
        private const val DEFAULT_REPO_DB_RELATIVE_PATH = "apps/kcloud/config-center.sqlite"
    }

    val dbFile: File by lazy {
        resolveDbFile().absoluteFile
    }

    val profile: String
        get() = options.profile
            ?.trim()
            ?.ifBlank { null }
            ?: readSetting(PROFILE_KEY)
            ?: DEFAULT_PROFILE

    val appId: String
        get() = options.appId
            ?.trim()
            ?.ifBlank { null }
            ?: readSetting(APP_ID_KEY)
            ?: DEFAULT_APP_ID

    val masterKey: String?
        get() = options.masterKey
            ?.trim()
            ?.ifBlank { null }
            ?: readSetting(MASTER_KEY)

    fun readBootstrapValue(
        key: String,
    ): String? {
        return when (key) {
            DB_PATH_KEY -> dbFile.absolutePath
            MASTER_KEY -> masterKey
            PROFILE_KEY -> profile
            APP_ID_KEY -> appId
            else -> null
        }
    }

    private fun resolveDbFile(): File {
        options.dbPath
            ?.trim()
            ?.ifBlank { null }
            ?.let { explicitPath ->
                return File(explicitPath)
            }

        readSetting(DB_PATH_KEY)?.let { configuredPath ->
            return File(configuredPath)
        }

        locateRepoTrackedDatabase()?.let { repoFile ->
            return repoFile
        }

        return fallbackWritableDatabase()
    }

    private fun locateRepoTrackedDatabase(): File? {
        val startingPoints = buildList {
            add(File(System.getProperty("user.dir").orEmpty()).absoluteFile)
            add(File(".").absoluteFile)
        }.distinctBy { it.absolutePath }

        startingPoints.forEach { start ->
            var current: File? = start
            while (current != null) {
                val candidate = File(current, DEFAULT_REPO_DB_RELATIVE_PATH)
                if (candidate.exists() && candidate.isFile) {
                    return candidate.absoluteFile
                }
                current = current.parentFile
            }
        }

        return null
    }

    private fun fallbackWritableDatabase(): File {
        val userHome = System.getProperty("user.home").orEmpty()
        val baseDirectory = File(userHome, ".kcloud").absoluteFile
        if (!baseDirectory.exists()) {
            baseDirectory.mkdirs()
        }
        return File(baseDirectory, "config-center.sqlite").absoluteFile
    }

    private fun readSetting(
        key: String,
    ): String? {
        return System.getProperty(key)
            ?.trim()
            ?.ifBlank { null }
            ?: System.getenv(key)
                ?.trim()
                ?.ifBlank { null }
    }
}

