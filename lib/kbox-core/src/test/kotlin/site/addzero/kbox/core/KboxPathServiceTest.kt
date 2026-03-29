package site.addzero.kbox.core

import site.addzero.kbox.core.service.KboxPathService
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KboxPathServiceTest {
    @Test
    fun `app data dir should prefer saved override`() {
        val previousHome = System.getProperty("user.home")
        val tempHome = createTempDirectory("kbox-path-service").toFile()
        System.setProperty("user.home", tempHome.absolutePath)
        try {
            val pathService = object : KboxPathService() {
                override fun defaultAppDataDir(): File {
                    return File(tempHome, "default-data")
                }
            }
            val customDir = File(tempHome, "custom-data")

            pathService.writeAppDataOverride(customDir.absolutePath)

            assertEquals(customDir.canonicalPath, pathService.currentAppDataOverride())
            assertEquals(customDir.canonicalPath, pathService.appDataDir().canonicalPath)
            assertTrue(pathService.settingsFile().canonicalPath.startsWith(customDir.canonicalPath))
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
