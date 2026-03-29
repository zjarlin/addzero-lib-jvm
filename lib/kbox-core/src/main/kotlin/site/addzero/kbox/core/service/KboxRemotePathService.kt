package site.addzero.kbox.core.service

import org.koin.core.annotation.Single
import site.addzero.kbox.core.KBOX_APP_NAME
import site.addzero.kbox.core.model.KboxRemoteOs
import site.addzero.kbox.core.model.KboxSshConfig

@Single
class KboxRemotePathService(
    private val pathService: KboxPathService,
) {
    fun remoteAppDataDir(
        config: KboxSshConfig,
    ): String {
        val remotePath = config.remotePath
        val appName = remotePath.appName.ifBlank { KBOX_APP_NAME }
        return when (remotePath.os) {
            KboxRemoteOs.MACOS -> {
                joinRemote(
                    remoteUserHome(config),
                    "Library",
                    "Application Support",
                    appName,
                )
            }

            KboxRemoteOs.WINDOWS -> {
                val baseDir = remotePath.localAppData
                    .ifBlank { remotePath.appData }
                    .ifBlank { joinRemote(remoteUserHome(config), "AppData", "Local") }
                joinRemote(baseDir, appName)
            }

            KboxRemoteOs.LINUX -> {
                val baseDir = remotePath.xdgDataHome
                    .ifBlank { joinRemote(remoteUserHome(config), ".local", "share") }
                joinRemote(baseDir, appName)
            }
        }
    }

    fun remoteAbsolutePath(
        config: KboxSshConfig,
        relativePath: String,
    ): String {
        return joinRemote(
            remoteAppDataDir(config),
            relativePath,
        )
    }

    fun remoteAbsolutePathForFile(
        config: KboxSshConfig,
        absoluteSourcePath: String,
    ): String {
        val sourceFile = java.io.File(absoluteSourcePath)
        return remoteAbsolutePath(
            config = config,
            relativePath = pathService.offloadRelativePath(sourceFile),
        )
    }

    private fun remoteUserHome(
        config: KboxSshConfig,
    ): String {
        val remotePath = config.remotePath
        if (remotePath.userHome.isNotBlank()) {
            return normalizeRemotePath(remotePath.userHome)
        }
        val username = config.username.ifBlank { "user" }
        return when (remotePath.os) {
            KboxRemoteOs.MACOS -> "/Users/$username"
            KboxRemoteOs.WINDOWS -> "C:/Users/$username"
            KboxRemoteOs.LINUX -> "/home/$username"
        }
    }

    private fun joinRemote(
        base: String,
        vararg parts: String,
    ): String {
        val normalizedBase = normalizeRemotePath(base).trimEnd('/')
        val normalizedParts = parts
            .map(::normalizeRemotePath)
            .map { it.trim('/') }
            .filter { it.isNotBlank() }
        return buildString {
            append(normalizedBase)
            normalizedParts.forEach { part ->
                if (isNotEmpty() && !endsWith("/")) {
                    append("/")
                }
                append(part)
            }
        }
    }

    private fun normalizeRemotePath(
        path: String,
    ): String {
        return path.replace('\\', '/')
            .replace(Regex("/{2,}"), "/")
            .ifBlank { "/" }
    }
}
