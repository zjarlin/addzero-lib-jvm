package site.addzero.kbox.core.model

import kotlinx.serialization.Serializable
import site.addzero.kbox.core.KBOX_APP_NAME
import site.addzero.kbox.core.KBOX_DEFAULT_LARGE_FILE_THRESHOLD_BYTES

@Serializable
enum class KboxInstallerPlatform {
    WINDOWS,
    MACOS,
    LINUX,
    ANDROID,
    IOS,
    OTHER,
}

@Serializable
data class KboxInstallerRule(
    val extension: String = "",
    val platform: KboxInstallerPlatform = KboxInstallerPlatform.OTHER,
    val bucket: String = "",
)

@Serializable
enum class KboxSshAuthMode {
    PASSWORD,
    PRIVATE_KEY,
}

@Serializable
enum class KboxRemoteOs {
    MACOS,
    WINDOWS,
    LINUX,
}

@Serializable
data class KboxRemotePathConfig(
    val os: KboxRemoteOs = KboxRemoteOs.MACOS,
    val userHome: String = "",
    val localAppData: String = "",
    val appData: String = "",
    val xdgDataHome: String = "",
    val appName: String = KBOX_APP_NAME,
)

@Serializable
data class KboxSshConfig(
    val enabled: Boolean = false,
    val host: String = "",
    val port: Int = 22,
    val username: String = "",
    val authMode: KboxSshAuthMode = KboxSshAuthMode.PASSWORD,
    val password: String = "",
    val privateKeyPath: String = "",
    val privateKeyPassphrase: String = "",
    val strictHostKeyChecking: Boolean = false,
    val remotePath: KboxRemotePathConfig = KboxRemotePathConfig(),
)

@Serializable
data class KboxSettings(
    val localAppDataOverride: String = "",
    val installerScanRoots: List<String> = emptyList(),
    val largeFileScanRoots: List<String> = emptyList(),
    val installerRules: List<KboxInstallerRule> = emptyList(),
    val largeFileThresholdBytes: Long = KBOX_DEFAULT_LARGE_FILE_THRESHOLD_BYTES,
    val ssh: KboxSshConfig = KboxSshConfig(),
)

data class KboxInstallerCandidate(
    val sourcePath: String,
    val fileName: String,
    val sizeBytes: Long,
    val lastModifiedMillis: Long,
    val platform: KboxInstallerPlatform,
    val extension: String,
    val bucket: String,
    val destinationRelativePath: String,
    val destinationAbsolutePath: String,
)

data class KboxLargeFileCandidate(
    val sourcePath: String,
    val fileName: String,
    val sizeBytes: Long,
    val lastModifiedMillis: Long,
    val remoteRelativePath: String,
)

@Serializable
data class KboxInstallerArchiveRecord(
    val sourcePath: String = "",
    val destinationPath: String = "",
    val destinationRelativePath: String = "",
    val sizeBytes: Long = 0,
    val platform: KboxInstallerPlatform = KboxInstallerPlatform.OTHER,
    val extension: String = "",
    val archivedAtMillis: Long = 0,
)

@Serializable
data class KboxOffloadRecord(
    val sourcePath: String = "",
    val remotePath: String = "",
    val remoteRelativePath: String = "",
    val sizeBytes: Long = 0,
    val deletedLocalSource: Boolean = false,
    val offloadedAtMillis: Long = 0,
)

data class KboxInstallerCollectResult(
    val archived: List<KboxInstallerArchiveRecord>,
    val skipped: List<String>,
)

data class KboxOffloadResult(
    val uploaded: List<KboxOffloadRecord>,
    val skipped: List<String>,
)
