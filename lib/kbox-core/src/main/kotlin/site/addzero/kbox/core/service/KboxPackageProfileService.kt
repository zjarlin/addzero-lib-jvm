package site.addzero.kbox.core.service

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import site.addzero.kbox.core.model.KboxDetectedPackageManager
import site.addzero.kbox.core.model.KboxPackageDiff
import site.addzero.kbox.core.model.KboxPackageImportEntryResult
import site.addzero.kbox.core.model.KboxPackageImportResult
import site.addzero.kbox.core.model.KboxPackageProfile
import site.addzero.kbox.core.model.KboxPackageProfileEntry
import site.addzero.kbox.core.model.KboxPackageProfileSummary
import site.addzero.kbox.core.support.sanitizeFileName
import java.io.File

@Single
class KboxPackageProfileService(
    private val json: Json,
    private val pathService: KboxPathService,
    private val adapters: List<KboxPackageManagerAdapter>,
) {
    fun detectManagers(): List<KboxDetectedPackageManager> {
        return adapters.map { adapter -> adapter.detect() }
            .sortedBy { manager -> manager.displayName.lowercase() }
    }

    fun exportProfile(
        profileName: String,
    ): KboxPackageProfileSummary {
        val normalizedProfileName = profileName.trim().ifBlank {
            "profile-${System.currentTimeMillis()}"
        }
        val managerEntries = adapters.mapNotNull { adapter ->
            val detection = adapter.detect()
            if (!detection.available) {
                return@mapNotNull null
            }
            KboxPackageProfileEntry(
                managerId = adapter.managerId,
                displayName = adapter.displayName,
                packages = adapter.exportInstalledPackages(),
            )
        }
        val profile = KboxPackageProfile(
            profileName = normalizedProfileName,
            createdAtMillis = System.currentTimeMillis(),
            managers = managerEntries,
        )
        val outputFile = profileFile(normalizedProfileName)
        outputFile.parentFile?.mkdirs()
        outputFile.writeText(json.encodeToString(profile))
        return toSummary(outputFile, profile)
    }

    fun listProfiles(): List<KboxPackageProfileSummary> {
        return pathService.packageProfilesDir()
            .listFiles { file -> file.isFile && file.extension == "json" }
            .orEmpty()
            .mapNotNull { file ->
                runCatching {
                    val profile = json.decodeFromString<KboxPackageProfile>(file.readText())
                    toSummary(file, profile)
                }.getOrNull()
            }
            .sortedByDescending { profile -> profile.createdAtMillis }
    }

    fun readProfile(
        fileName: String,
    ): KboxPackageProfile {
        val file = File(pathService.packageProfilesDir(), fileName)
        require(file.isFile) {
            "Profile 不存在：$fileName"
        }
        return json.decodeFromString<KboxPackageProfile>(file.readText())
    }

    fun diffProfile(
        profile: KboxPackageProfile,
    ): List<KboxPackageDiff> {
        val adapterById = adapters.associateBy { adapter -> adapter.managerId }
        return profile.managers.map { entry ->
            val adapter = adapterById[entry.managerId]
            val detection = adapter?.detect()
            val installedPackages = if (adapter != null && detection?.available == true) {
                adapter.exportInstalledPackages().toSet()
            } else {
                emptySet()
            }
            val requestedPackages = entry.packages.distinct()
            val missingPackages = requestedPackages.filterNot(installedPackages::contains)
            KboxPackageDiff(
                managerId = entry.managerId,
                displayName = entry.displayName,
                available = detection?.available == true,
                requestedPackages = requestedPackages.size,
                installedPackages = requestedPackages.size - missingPackages.size,
                missingPackages = missingPackages,
            )
        }
    }

    fun importProfile(
        fileName: String,
    ): KboxPackageImportResult {
        val profile = readProfile(fileName)
        val adapterById = adapters.associateBy { adapter -> adapter.managerId }
        val entries = profile.managers.map { profileEntry ->
            val adapter = adapterById[profileEntry.managerId]
            if (adapter == null) {
                return@map KboxPackageImportEntryResult(
                    managerId = profileEntry.managerId,
                    displayName = profileEntry.displayName,
                    failedPackages = profileEntry.packages,
                    output = "当前宿主缺少适配器：${profileEntry.managerId}",
                )
            }
            val detection = adapter.detect()
            if (!detection.available) {
                return@map KboxPackageImportEntryResult(
                    managerId = profileEntry.managerId,
                    displayName = profileEntry.displayName,
                    failedPackages = profileEntry.packages,
                    output = detection.detail,
                )
            }
            val installedPackages = adapter.exportInstalledPackages().toSet()
            val missingPackages = profileEntry.packages
                .distinct()
                .filterNot(installedPackages::contains)
            val skippedPackages = profileEntry.packages
                .distinct()
                .filter(installedPackages::contains)
            val installedResult = adapter.installMissingPackages(missingPackages)
            installedResult.copy(
                skippedPackages = skippedPackages,
            )
        }
        return KboxPackageImportResult(entries = entries)
    }

    private fun profileFile(
        profileName: String,
    ): File {
        val safeName = sanitizeFileName(profileName)
        return File(pathService.packageProfilesDir(), "$safeName.json")
    }

    private fun toSummary(
        file: File,
        profile: KboxPackageProfile,
    ): KboxPackageProfileSummary {
        return KboxPackageProfileSummary(
            fileName = file.name,
            profileName = profile.profileName,
            createdAtMillis = profile.createdAtMillis,
            packageManagerCount = profile.managers.size,
            packageCount = profile.managers.sumOf { manager -> manager.packages.size },
        )
    }
}
