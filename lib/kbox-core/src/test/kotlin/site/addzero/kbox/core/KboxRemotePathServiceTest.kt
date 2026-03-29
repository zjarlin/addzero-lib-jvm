package site.addzero.kbox.core

import kotlin.test.Test
import kotlin.test.assertEquals
import site.addzero.kbox.core.model.KboxRemoteOs
import site.addzero.kbox.core.model.KboxRemotePathConfig
import site.addzero.kbox.core.model.KboxSshConfig
import site.addzero.kbox.core.service.KboxPathService
import site.addzero.kbox.core.service.KboxRemotePathService
import java.io.File
import java.nio.file.Files

class KboxRemotePathServiceTest {
    @Test
    fun `remote app data dir should follow windows rule`() {
        val service = KboxRemotePathService(pathService = KboxPathService())
        val config = KboxSshConfig(
            username = "zjarlin",
            remotePath = KboxRemotePathConfig(
                os = KboxRemoteOs.WINDOWS,
            ),
        )

        val resolved = service.remoteAppDataDir(config)

        assertEquals("C:/Users/zjarlin/AppData/Local/KBox", resolved)
    }

    @Test
    fun `remote absolute path should preserve app data relative file`() {
        val tempRoot = Files.createTempDirectory("kbox-remote-path").toFile()
        val pathService = object : KboxPathService() {
            override fun appDataDir(): File {
                return File(tempRoot, "KBox").apply { mkdirs() }
            }
        }
        val service = KboxRemotePathService(pathService = pathService)
        val localFile = File(pathService.appDataDir(), "packages/macos/dmg/demo.dmg").apply {
            parentFile.mkdirs()
            writeText("demo")
        }
        val config = KboxSshConfig(
            username = "backup",
            remotePath = KboxRemotePathConfig(
                os = KboxRemoteOs.MACOS,
            ),
        )

        val resolved = service.remoteAbsolutePathForFile(config, localFile.absolutePath)

        assertEquals(
            "/Users/backup/Library/Application Support/KBox/packages/macos/dmg/demo.dmg",
            resolved,
        )
    }
}
