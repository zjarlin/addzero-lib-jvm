package site.addzero.kbox.core.service

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import site.addzero.kbox.core.KBOX_APP_NAME
import site.addzero.kbox.core.model.KboxInstallerPlatform
import site.addzero.kbox.core.model.KboxInstallerRule
import site.addzero.kbox.core.support.normalizeSegments
import site.addzero.kbox.core.support.sanitizeFileName
import site.addzero.kbox.core.support.stableShortHash
import java.io.File

@Single
open class KboxPathService {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
    }

    open fun defaultAppDataDir(): File {
        val userHome = File(requiredSystemProperty("user.home"))
        val osName = System.getProperty("os.name").orEmpty()
        return when {
            osName.contains("Mac", ignoreCase = true) -> {
                File(userHome, "Library/Application Support/$KBOX_APP_NAME")
            }

            osName.contains("Windows", ignoreCase = true) -> {
                val baseDir = System.getenv("LOCALAPPDATA")
                    ?.takeIf { it.isNotBlank() }
                    ?: System.getenv("APPDATA")?.takeIf { it.isNotBlank() }
                    ?: userHome.absolutePath
                File(baseDir, KBOX_APP_NAME)
            }

            else -> {
                val baseDir = System.getenv("XDG_DATA_HOME")
                    ?.takeIf { it.isNotBlank() }
                    ?: File(userHome, ".local/share").absolutePath
                File(baseDir, KBOX_APP_NAME)
            }
        }
    }

    open fun appDataDir(): File {
        return resolveAppDataDir(
            localAppDataOverride = currentAppDataOverride(),
            createDirectories = true,
        )
    }

    fun resolveAppDataDir(
        localAppDataOverride: String,
        createDirectories: Boolean = true,
    ): File {
        val normalizedOverride = normalizeOverride(localAppDataOverride)
        val resolved = if (normalizedOverride.isBlank()) {
            defaultAppDataDir()
        } else {
            File(normalizedOverride)
        }.absoluteFile
        if (createDirectories) {
            resolved.mkdirs()
        }
        return resolved
    }

    fun currentAppDataOverride(): String {
        val locatorFile = appDataLocatorFile()
        if (!locatorFile.isFile) {
            return ""
        }
        return runCatching {
            json.decodeFromString(KboxAppDataLocator.serializer(), locatorFile.readText())
                .localAppDataOverride
                .trim()
        }.getOrDefault("")
    }

    fun writeAppDataOverride(
        localAppDataOverride: String,
    ) {
        val normalizedOverride = normalizeOverride(localAppDataOverride)
        val locatorFile = appDataLocatorFile()
        if (normalizedOverride.isBlank()) {
            locatorFile.delete()
            return
        }
        locatorFile.parentFile?.mkdirs()
        locatorFile.writeText(
            json.encodeToString(
                KboxAppDataLocator(
                    localAppDataOverride = normalizedOverride,
                ),
            ),
        )
    }

    fun settingsFile(): File {
        return settingsFileForAppDataDir(appDataDir())
    }

    fun settingsFileForOverride(
        localAppDataOverride: String,
    ): File {
        return settingsFileForAppDataDir(
            resolveAppDataDir(
                localAppDataOverride = localAppDataOverride,
                createDirectories = true,
            ),
        )
    }

    fun settingsFileForAppDataDir(
        appDataDir: File,
    ): File {
        return File(configDir(appDataDir), "settings.json")
    }

    fun installerHistoryFile(): File {
        return File(historyDir(), "installer-history.json")
    }

    fun offloadHistoryFile(): File {
        return File(historyDir(), "offload-history.json")
    }

    fun dotfileRegistryFile(): File {
        return File(configDir(), "dotfiles.json")
    }

    fun composeProjectsRegistryFile(): File {
        return File(configDir(), "compose-projects.json")
    }

    fun packageTarget(
        rule: KboxInstallerRule,
        sourceFile: File,
    ): File {
        val platformDir = when (rule.platform) {
            KboxInstallerPlatform.WINDOWS -> "windows"
            KboxInstallerPlatform.MACOS -> "macos"
            KboxInstallerPlatform.LINUX -> "linux"
            KboxInstallerPlatform.ANDROID -> "android"
            KboxInstallerPlatform.IOS -> "ios"
            KboxInstallerPlatform.OTHER -> "other"
        }
        val extension = sourceFile.extension.lowercase()
        val fileName = buildDestinationFileName(sourceFile)
        return File(packagesDir(), "$platformDir/${rule.bucket.ifBlank { extension }}/$fileName")
    }

    fun offloadRelativePath(
        sourceFile: File,
    ): String {
        val appDataRoot = appDataDir().canonicalFile
        val canonicalSource = sourceFile.canonicalFile
        val relativeFromAppData = canonicalSource.toRelativeStringOrNull(appDataRoot)
        if (relativeFromAppData != null) {
            return relativeFromAppData.replace(File.separatorChar, '/')
        }
        return listOf("offload")
            .plus(normalizeSegments(canonicalSource.absolutePath))
            .joinToString("/")
    }

    fun packagesDir(): File {
        return packagesDir(appDataDir())
    }

    fun packagesDir(
        appDataDir: File,
    ): File {
        return File(appDataDir, "packages").apply { mkdirs() }
    }

    fun historyDir(): File {
        return historyDir(appDataDir())
    }

    fun historyDir(
        appDataDir: File,
    ): File {
        return File(appDataDir, "history").apply { mkdirs() }
    }

    fun configDir(): File {
        return configDir(appDataDir())
    }

    fun configDir(
        appDataDir: File,
    ): File {
        return File(appDataDir, "config").apply { mkdirs() }
    }

    fun dotfilesDir(): File {
        return dotfilesDir(appDataDir())
    }

    fun dotfilesDir(
        appDataDir: File,
    ): File {
        return File(appDataDir, "dotfiles").apply { mkdirs() }
    }

    fun dotfilesBackupDir(): File {
        return dotfilesBackupDir(appDataDir())
    }

    fun dotfilesBackupDir(
        appDataDir: File,
    ): File {
        return File(dotfilesDir(appDataDir), "_backup").apply { mkdirs() }
    }

    fun packageProfilesDir(): File {
        return packageProfilesDir(appDataDir())
    }

    fun packageProfilesDir(
        appDataDir: File,
    ): File {
        return File(appDataDir, "package-profiles").apply { mkdirs() }
    }

    private fun buildDestinationFileName(
        sourceFile: File,
    ): String {
        val hash = stableShortHash(sourceFile.absolutePath)
        val baseName = sanitizeFileName(sourceFile.nameWithoutExtension)
        val extension = sourceFile.extension.lowercase()
        return if (extension.isBlank()) {
            "${baseName}__${hash}"
        } else {
            "${baseName}__${hash}.${extension}"
        }
    }

    private fun File.toRelativeStringOrNull(
        baseDir: File,
    ): String? {
        return runCatching {
            val sourcePath = this.toPath().normalize()
            val basePath = baseDir.toPath().normalize()
            if (!sourcePath.startsWith(basePath)) {
                null
            } else {
                basePath.relativize(sourcePath).toString()
            }
        }.getOrNull()
    }

    private fun appDataLocatorFile(): File {
        return File(
            requiredSystemProperty("user.home"),
            ".kbox-app-data.json",
        )
    }

    private fun normalizeOverride(
        localAppDataOverride: String,
    ): String {
        val trimmed = localAppDataOverride.trim()
        if (trimmed.isBlank()) {
            return ""
        }
        val expanded = if (trimmed == "~") {
            requiredSystemProperty("user.home")
        } else if (trimmed.startsWith("~/")) {
            File(requiredSystemProperty("user.home"), trimmed.removePrefix("~/")).path
        } else {
            trimmed
        }
        return runCatching {
            File(expanded).absoluteFile.canonicalPath
        }.getOrElse {
            File(expanded).absoluteFile.path
        }
    }

    private fun requiredSystemProperty(
        name: String,
    ): String {
        val value = System.getProperty(name)?.trim()
        check(!value.isNullOrEmpty()) {
            "系统属性缺失：$name"
        }
        return value
    }

    @Serializable
    private data class KboxAppDataLocator(
        val localAppDataOverride: String = "",
    )
}
