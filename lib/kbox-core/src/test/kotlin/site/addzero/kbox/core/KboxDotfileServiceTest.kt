package site.addzero.kbox.core

import kotlinx.serialization.json.Json
import site.addzero.kbox.core.model.KboxDotfileStatus
import site.addzero.kbox.core.service.KboxDotfileService
import site.addzero.kbox.core.service.KboxPathService
import java.io.File
import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KboxDotfileServiceTest {
    @Test
    fun `import should copy local file into canonical storage and replace target with symlink`() {
        val previousHome = System.getProperty("user.home")
        val tempHome = createTempDirectory("kbox-dotfile-import").toFile()
        System.setProperty("user.home", tempHome.absolutePath)
        try {
            val pathService = object : KboxPathService() {
                override fun defaultAppDataDir(): File {
                    return File(tempHome, "kbox-data")
                }
            }
            val service = KboxDotfileService(
                json = Json {
                    prettyPrint = true
                    encodeDefaults = true
                    ignoreUnknownKeys = true
                },
                pathService = pathService,
            )
            val targetFile = File(tempHome, ".zshrc").apply {
                writeText("export TEST=1")
            }

            val candidate = service.importTarget(targetFile.absolutePath)

            assertTrue(Files.isSymbolicLink(targetFile.toPath()))
            assertEquals(KboxDotfileStatus.MANAGED, candidate.status)
            assertTrue(File(candidate.canonicalPath).isFile)
            assertEquals("export TEST=1", File(candidate.canonicalPath).readText())
        } finally {
            restoreUserHome(previousHome)
        }
    }

    @Test
    fun `remove managed target should restore local copy and delete canonical storage`() {
        val previousHome = System.getProperty("user.home")
        val tempHome = createTempDirectory("kbox-dotfile-remove").toFile()
        System.setProperty("user.home", tempHome.absolutePath)
        try {
            val pathService = object : KboxPathService() {
                override fun defaultAppDataDir(): File {
                    return File(tempHome, "kbox-data")
                }
            }
            val service = KboxDotfileService(
                json = Json {
                    prettyPrint = true
                    encodeDefaults = true
                    ignoreUnknownKeys = true
                },
                pathService = pathService,
            )
            val targetFile = File(tempHome, ".gitconfig").apply {
                writeText("[user]\nname = zjarlin")
            }
            val candidate = service.importTarget(targetFile.absolutePath)

            service.removeManagedTarget(targetFile.absolutePath)

            assertFalse(Files.isSymbolicLink(targetFile.toPath()))
            assertTrue(targetFile.isFile)
            assertEquals("[user]\nname = zjarlin", targetFile.readText())
            assertFalse(File(candidate.canonicalPath).exists())
            assertTrue(service.listEntries().isEmpty())
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
