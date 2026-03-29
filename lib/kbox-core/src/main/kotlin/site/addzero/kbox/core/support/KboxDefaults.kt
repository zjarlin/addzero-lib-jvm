package site.addzero.kbox.core.support

import site.addzero.kbox.core.KBOX_APP_NAME
import site.addzero.kbox.core.KBOX_DEFAULT_LARGE_FILE_THRESHOLD_BYTES
import site.addzero.kbox.core.model.KboxRemoteOs
import site.addzero.kbox.core.model.KboxRemotePathConfig
import site.addzero.kbox.core.model.KboxSettings
import site.addzero.kbox.core.model.KboxSshConfig
import java.io.File

object KboxDefaults {
    fun defaultSettings(): KboxSettings {
        val userHome = File(System.getProperty("user.home").orEmpty())
        return KboxSettings(
            installerScanRoots = defaultInstallerRoots(userHome),
            largeFileScanRoots = defaultLargeFileRoots(userHome),
            installerRules = KboxInstallerCatalog.defaultRules(),
            largeFileThresholdBytes = KBOX_DEFAULT_LARGE_FILE_THRESHOLD_BYTES,
            ssh = KboxSshConfig(
                remotePath = KboxRemotePathConfig(
                    os = currentRemoteOs(),
                    appName = KBOX_APP_NAME,
                ),
            ),
        )
    }

    fun normalize(settings: KboxSettings): KboxSettings {
        val defaults = defaultSettings()
        return settings.copy(
            localAppDataOverride = settings.localAppDataOverride.trim(),
            installerScanRoots = settings.installerScanRoots
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .ifEmpty { defaults.installerScanRoots },
            largeFileScanRoots = settings.largeFileScanRoots
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .ifEmpty { defaults.largeFileScanRoots },
            installerRules = settings.installerRules
                .filter { it.extension.isNotBlank() }
                .ifEmpty { defaults.installerRules },
            largeFileThresholdBytes = settings.largeFileThresholdBytes
                .takeIf { it > 0 }
                ?: defaults.largeFileThresholdBytes,
            ssh = settings.ssh.copy(
                port = settings.ssh.port.takeIf { it > 0 } ?: defaults.ssh.port,
                remotePath = settings.ssh.remotePath.copy(
                    appName = settings.ssh.remotePath.appName.ifBlank { KBOX_APP_NAME },
                ),
            ),
        )
    }

    private fun defaultInstallerRoots(
        userHome: File,
    ): List<String> {
        return buildList {
            addIfDirectory(userHome.resolve("Downloads"))
            addIfDirectory(userHome.resolve("Desktop"))
            addIfDirectory(userHome.resolve("Documents"))
        }
    }

    private fun defaultLargeFileRoots(
        userHome: File,
    ): List<String> {
        return buildList {
            addIfDirectory(userHome.resolve("Downloads"))
            addIfDirectory(userHome.resolve("Movies"))
            addIfDirectory(userHome.resolve("Desktop"))
            addIfDirectory(userHome.resolve("Documents"))
        }
    }

    private fun currentRemoteOs(): KboxRemoteOs {
        val osName = System.getProperty("os.name").orEmpty()
        return when {
            osName.contains("Mac", ignoreCase = true) -> KboxRemoteOs.MACOS
            osName.contains("Windows", ignoreCase = true) -> KboxRemoteOs.WINDOWS
            else -> KboxRemoteOs.LINUX
        }
    }

    private fun MutableList<String>.addIfDirectory(
        directory: File,
    ) {
        if (directory.isDirectory) {
            add(directory.absolutePath)
        }
    }
}
