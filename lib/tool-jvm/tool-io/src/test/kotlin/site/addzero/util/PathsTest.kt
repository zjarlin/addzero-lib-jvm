package site.addzero.util

import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PathsTest {

    @Test
    fun `configuredDirectory prefers system property override`() {
        val root = Files.createTempDirectory("path-util-system-property").toFile()
        try {
            val overrideDir = File(root, "override")
            val defaultDir = File(root, "default")

            withSystemProperty("site.addzero.test.workspace.dir", overrideDir.absolutePath) {
                val result = PathUtil.configuredDirectory(
                    defaultDirectory = defaultDir,
                    systemProperty = "site.addzero.test.workspace.dir",
                )

                assertEquals(overrideDir.absolutePath, result.absolutePath)
            }
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `configuredDirectory creates default directory when requested`() {
        val root = Files.createTempDirectory("path-util-default-directory").toFile()
        try {
            val defaultDir = File(root, "workspace")

            val result = PathUtil.configuredDirectory(
                defaultDirectory = defaultDir,
                createDirectories = true,
            )

            assertEquals(defaultDir.absolutePath, result.absolutePath)
            assertTrue(result.exists())
            assertTrue(result.isDirectory)
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `appDataDir uses macOS application support path`() {
        val result = PathUtil.appDataDir(
            appName = "demo-app",
            createDirectories = false,
            osName = "Mac OS X",
            userHome = File("/Users/demo"),
            localAppData = null,
            appData = null,
            xdgDataHome = null,
        )

        assertEquals(
            File("/Users/demo/Library/Application Support/demo-app").path,
            result.path,
        )
    }

    @Test
    fun `appDataDir uses windows local app data path`() {
        val result = PathUtil.appDataDir(
            appName = "demo-app",
            createDirectories = false,
            osName = "Windows 11",
            userHome = File("C:/Users/demo"),
            localAppData = "C:/Users/demo/AppData/Local",
            appData = "C:/Users/demo/AppData/Roaming",
            xdgDataHome = null,
        )

        assertEquals(
            File("C:/Users/demo/AppData/Local", "demo-app").path,
            result.path,
        )
    }

    @Test
    fun `appDataDir uses xdg data home on linux`() {
        val result = PathUtil.appDataDir(
            appName = "demo-app",
            createDirectories = false,
            osName = "Linux",
            userHome = File("/home/demo"),
            localAppData = null,
            appData = null,
            xdgDataHome = "/data/xdg",
        )

        assertEquals(
            File("/data/xdg", "demo-app").path,
            result.path,
        )
    }

    @Test
    fun `child creates nested directories when requested`() {
        val root = Files.createTempDirectory("path-util-child").toFile()
        try {
            val result = PathUtil.child(
                root,
                "level-1",
                "level-2",
                createDirectories = true,
            )

            assertTrue(result.exists())
            assertTrue(result.isDirectory)
            assertEquals(File(root, "level-1/level-2").absolutePath, result.absolutePath)
        } finally {
            root.deleteRecursively()
        }
    }

    private fun withSystemProperty(
        name: String,
        value: String?,
        block: () -> Unit,
    ) {
        val originalValue = System.getProperty(name)
        try {
            if (value == null) {
                System.clearProperty(name)
            } else {
                System.setProperty(name, value)
            }
            block()
        } finally {
            if (originalValue == null) {
                System.clearProperty(name)
            } else {
                System.setProperty(name, originalValue)
            }
        }
    }
}
