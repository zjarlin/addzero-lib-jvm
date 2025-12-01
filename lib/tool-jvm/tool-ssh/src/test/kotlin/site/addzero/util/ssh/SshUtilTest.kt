package site.addzero.util.ssh

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SshUtilTest {

    private val testConfig = SshConfig(
        host = "1.194.161.16",
        username = "root",
        password = "zljkj@!20250331",
       port = 8022
    )

    @Test
    fun testExecuteSync() {
        val result = SshUtil.executeSync(testConfig, "echo hello")
        assertEquals(0, result.exitCode)
        assertTrue(result.stdout.contains("hello"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun testExecuteStream() = runBlocking {
        val lines = SshUtil.executeStream(testConfig, "echo -e 'line1\nline2\nline3'").toList()
        assertEquals(3, lines.size)
        assertEquals("line1", lines[0])
        assertEquals("line2", lines[1])
        assertEquals("line3", lines[2])
    }

    @Test
    fun testSessionReuse() {
        SshUtil.use(testConfig) { session ->
            val result1 = session.executeSync("pwd")
            assertTrue(result1.isSuccess)

            val result2 = session.executeSync("whoami")
            assertTrue(result2.isSuccess)
            assertEquals(testConfig.username, result2.stdout.trim())
        }
    }

    @Test
    fun testUploadAndDownload() {
        val localTestFile = java.io.File.createTempFile("ssh_test_", ".txt")
        localTestFile.writeText("SSH测试内容")

        val downloadedFile = java.io.File.createTempFile("ssh_download_", ".txt")

        try {
            SshUtil.use(testConfig) { session ->
                session.uploadFile(localTestFile.absolutePath, "/tmp/${localTestFile.name}")
                session.downloadFile("/tmp/${localTestFile.name}", downloadedFile.absolutePath)
            }
            assertEquals("SSH测试内容", downloadedFile.readText())
        } finally {
            localTestFile.delete()
            downloadedFile.delete()
            SshUtil.executeSync(testConfig, "rm -f /tmp/${localTestFile.name}")
        }
    }
}

