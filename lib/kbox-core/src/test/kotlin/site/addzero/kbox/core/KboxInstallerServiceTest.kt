package site.addzero.kbox.core

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import site.addzero.kbox.core.model.KboxSettings
import site.addzero.kbox.core.service.KboxHistoryStore
import site.addzero.kbox.core.service.KboxInstallerService
import site.addzero.kbox.core.service.KboxPathService
import site.addzero.kbox.core.support.KboxDefaults
import java.io.File
import java.nio.file.Files

class KboxInstallerServiceTest {
    @Test
    fun `scan should match installer extensions and plan app data target`() {
        val tempRoot = Files.createTempDirectory("kbox-installer-scan").toFile()
        val installerFile = File(tempRoot, "Downloads/sample.pkg").apply {
            parentFile.mkdirs()
            writeText("pkg")
        }

        val pathService = object : KboxPathService() {
            override fun appDataDir(): File {
                return File(tempRoot, "AppData").apply { mkdirs() }
            }
        }
        val historyStore = KboxHistoryStore(
            json = Json {
                prettyPrint = true
                encodeDefaults = true
            },
            pathService = pathService,
        )
        val service = KboxInstallerService(
            pathService = pathService,
            historyStore = historyStore,
        )
        val settings = KboxDefaults.defaultSettings().copy(
            installerScanRoots = listOf(File(tempRoot, "Downloads").absolutePath),
        )

        val candidates = service.scan(settings)

        assertEquals(1, candidates.size)
        assertTrue(candidates.single().destinationAbsolutePath.contains("packages/macos/pkg"))
    }
}
