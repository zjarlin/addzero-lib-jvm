package site.addzero.kbox.core

import site.addzero.kbox.core.service.KboxAppDataMigrationService
import site.addzero.kbox.core.service.KboxPathService
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith

class KboxAppDataMigrationServiceTest {
    @Test
    fun `migrate should move whole app data directory to target`() {
        val previousHome = System.getProperty("user.home")
        val tempHome = createTempDirectory("kbox-migration-service").toFile()
        System.setProperty("user.home", tempHome.absolutePath)
        try {
            val pathService = object : KboxPathService() {
                override fun defaultAppDataDir(): File {
                    return File(tempHome, "default-data")
                }
            }
            val sourceDir = pathService.appDataDir()
            File(sourceDir, "packages/macos/demo.pkg").apply {
                parentFile.mkdirs()
                writeText("pkg")
            }
            File(sourceDir, "history/offload-history.json").apply {
                parentFile.mkdirs()
                writeText("[]")
            }
            val service = KboxAppDataMigrationService(pathService)
            val targetDir = File(tempHome, "custom-data")

            val result = service.migrate(targetDir.absolutePath)

            assertTrue(result.migrated)
            assertFalse(sourceDir.exists())
            assertTrue(File(targetDir, "packages/macos/demo.pkg").isFile)
            assertTrue(File(targetDir, "history/offload-history.json").isFile)
        } finally {
            restoreUserHome(previousHome)
        }
    }

    @Test
    fun `migrate should reject non-empty target directory`() {
        val previousHome = System.getProperty("user.home")
        val tempHome = createTempDirectory("kbox-migration-conflict").toFile()
        System.setProperty("user.home", tempHome.absolutePath)
        try {
            val pathService = object : KboxPathService() {
                override fun defaultAppDataDir(): File {
                    return File(tempHome, "default-data")
                }
            }
            File(pathService.appDataDir(), "config/settings.json").apply {
                parentFile.mkdirs()
                writeText("{}")
            }
            val targetDir = File(tempHome, "conflict-data").apply {
                mkdirs()
                resolve("exists.txt").writeText("conflict")
            }
            val service = KboxAppDataMigrationService(pathService)

            assertFailsWith<IllegalStateException> {
                service.migrate(targetDir.absolutePath)
            }
        } finally {
            restoreUserHome(previousHome)
        }
    }

    private fun restoreUserHome(
        previousHome: String?,
    ) {
        if (previousHome.isNullOrBlank()) {
            System.clearProperty("user.home")
        } else {
            System.setProperty("user.home", previousHome)
        }
    }
}
