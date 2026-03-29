package site.addzero.kbox.core

import kotlinx.serialization.json.Json
import site.addzero.kbox.core.model.KboxDetectedPackageManager
import site.addzero.kbox.core.model.KboxPackageImportEntryResult
import site.addzero.kbox.core.service.KboxPackageManagerAdapter
import site.addzero.kbox.core.service.KboxPackageProfileService
import site.addzero.kbox.core.service.KboxPathService
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KboxPackageProfileServiceTest {
    @Test
    fun `import should only install missing packages`() {
        val tempRoot = createTempDirectory("kbox-package-profile").toFile()
        val adapter = FakePackageManagerAdapter()
        val pathService = object : KboxPathService() {
            override fun defaultAppDataDir(): File {
                return File(tempRoot, "kbox-data")
            }
        }
        val service = KboxPackageProfileService(
            json = Json {
                prettyPrint = true
                encodeDefaults = true
                ignoreUnknownKeys = true
            },
            pathService = pathService,
            adapters = listOf(adapter),
        )

        val summary = service.exportProfile("desktop")
        adapter.reportedInstalledPackages = linkedSetOf("pkg-a")
        val profile = service.readProfile(summary.fileName)
        val diff = service.diffProfile(profile).single()
        val importResult = service.importProfile(summary.fileName)

        assertEquals(listOf("pkg-b"), diff.missingPackages)
        assertEquals(listOf("pkg-b"), adapter.installedBatches.single())
        assertTrue(importResult.entries.single().success)
        assertEquals(listOf("pkg-a"), importResult.entries.single().skippedPackages)
    }

    private class FakePackageManagerAdapter : KboxPackageManagerAdapter {
        override val managerId: String = "fake"
        override val displayName: String = "Fake Manager"

        var reportedInstalledPackages = linkedSetOf("pkg-a", "pkg-b")
        val installedBatches = mutableListOf<List<String>>()

        override fun detect(): KboxDetectedPackageManager {
            return KboxDetectedPackageManager(
                managerId = managerId,
                displayName = displayName,
                available = true,
                detail = "可用",
            )
        }

        override fun exportInstalledPackages(): List<String> {
            return reportedInstalledPackages.toList().sorted()
        }

        override fun installMissingPackages(
            packages: List<String>,
        ): KboxPackageImportEntryResult {
            installedBatches += packages
            reportedInstalledPackages.addAll(packages)
            return KboxPackageImportEntryResult(
                managerId = managerId,
                displayName = displayName,
                attemptedPackages = packages,
                installedPackages = packages,
                output = "ok",
            )
        }
    }
}
