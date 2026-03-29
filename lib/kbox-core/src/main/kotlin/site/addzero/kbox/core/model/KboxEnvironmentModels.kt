package site.addzero.kbox.core.model

import kotlinx.serialization.Serializable

@Serializable
data class KboxAppDataMigrationPlan(
    val currentPath: String = "",
    val targetPath: String = "",
    val needsMigration: Boolean = false,
    val blockers: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
) {
    val canMigrate: Boolean
        get() = blockers.isEmpty()
}

data class KboxAppDataMigrationResult(
    val previousPath: String,
    val currentPath: String,
    val migrated: Boolean,
)

@Serializable
enum class KboxManagedFileKind {
    FILE,
    DIRECTORY,
}

@Serializable
data class KboxDotfileEntry(
    val logicalName: String = "",
    val targetPath: String = "",
    val localType: KboxManagedFileKind = KboxManagedFileKind.FILE,
    val canonicalRelativePath: String = "",
    val enabled: Boolean = true,
)

enum class KboxDotfileStatus {
    CANDIDATE,
    MANAGED,
    DRIFTED,
    CONFLICT,
    MISSING,
    DISABLED,
}

data class KboxDotfileCandidate(
    val logicalName: String,
    val targetPath: String,
    val localType: KboxManagedFileKind,
    val canonicalRelativePath: String = "",
    val canonicalPath: String = "",
    val status: KboxDotfileStatus = KboxDotfileStatus.CANDIDATE,
    val managed: Boolean = false,
    val message: String = "",
)

@Serializable
data class KboxComposeProjectConfig(
    val projectId: String = "",
    val name: String = "",
    val directory: String = "",
    val composeFiles: List<String> = emptyList(),
    val envFile: String = "",
    val enabled: Boolean = true,
)

enum class KboxComposeProjectAvailability {
    AVAILABLE,
    UNAVAILABLE,
}

data class KboxComposeProjectSnapshot(
    val config: KboxComposeProjectConfig,
    val services: List<String>,
    val availability: KboxComposeProjectAvailability,
    val availabilityMessage: String = "",
)

data class KboxComposeProjectFiles(
    val composeFileContent: Map<String, String>,
    val envContent: String,
)

data class KboxCommandSpec(
    val command: List<String>,
    val workingDirectory: String = "",
    val environment: Map<String, String> = emptyMap(),
    val timeoutMillis: Long = 5 * 60 * 1000,
)

data class KboxCommandResult(
    val commandLine: String,
    val workingDirectory: String,
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val timedOut: Boolean,
    val durationMillis: Long,
) {
    val success: Boolean
        get() = !timedOut && exitCode == 0

    val output: String
        get() = buildString {
            if (stdout.isNotBlank()) {
                append(stdout.trim())
            }
            if (stderr.isNotBlank()) {
                if (isNotEmpty()) {
                    appendLine()
                }
                append(stderr.trim())
            }
        }.trim()
}

@Serializable
data class KboxPackageProfile(
    val profileName: String = "",
    val createdAtMillis: Long = 0,
    val managers: List<KboxPackageProfileEntry> = emptyList(),
)

@Serializable
data class KboxPackageProfileEntry(
    val managerId: String = "",
    val displayName: String = "",
    val packages: List<String> = emptyList(),
)

data class KboxPackageProfileSummary(
    val fileName: String,
    val profileName: String,
    val createdAtMillis: Long,
    val packageManagerCount: Int,
    val packageCount: Int,
)

data class KboxDetectedPackageManager(
    val managerId: String,
    val displayName: String,
    val available: Boolean,
    val detail: String = "",
)

data class KboxPackageDiff(
    val managerId: String,
    val displayName: String,
    val available: Boolean,
    val requestedPackages: Int,
    val installedPackages: Int,
    val missingPackages: List<String>,
)

data class KboxPackageImportEntryResult(
    val managerId: String,
    val displayName: String,
    val attemptedPackages: List<String> = emptyList(),
    val installedPackages: List<String> = emptyList(),
    val skippedPackages: List<String> = emptyList(),
    val failedPackages: List<String> = emptyList(),
    val output: String = "",
) {
    val success: Boolean
        get() = failedPackages.isEmpty()
}

data class KboxPackageImportResult(
    val entries: List<KboxPackageImportEntryResult>,
)

data class KboxInstallerDeleteResult(
    val deleted: List<String>,
    val skipped: List<String>,
)
