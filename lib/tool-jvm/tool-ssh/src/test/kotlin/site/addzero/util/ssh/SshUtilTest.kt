package site.addzero.util.ssh

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@EnabledIfEnvironmentVariable(named = "ENABLE_REAL_SSH_TEST", matches = "true")
class SshUtilTest {

    private val testConfig by lazy {
        SshConfig(
            host = requireNotNull(System.getenv("SSH_TEST_HOST")),
            username = requireNotNull(System.getenv("SSH_TEST_USERNAME")),
            password = System.getenv("SSH_TEST_PASSWORD"),
            privateKeyPath = System.getenv("SSH_TEST_PRIVATE_KEY"),
            port = System.getenv("SSH_TEST_PORT")?.toIntOrNull() ?: 22,
        )
    }

    @Test
    fun testExecuteSync() {
        val result = executeSync(testConfig, "echo hello")
        assertEquals(0, result.exitCode)
        assertTrue(result.stdout.contains("hello"))
        assertTrue(result.isSuccess)
    }

    @Test
    fun testExecuteStream() = runBlocking {
        val lines = executeStream(testConfig, "echo -e 'line1\nline2\nline3'").toList()
        assertEquals(3, lines.size)
        assertEquals("line1", lines[0])
        assertEquals("line2", lines[1])
        assertEquals("line3", lines[2])
    }

    @Test
    fun testSessionReuse() {
        use(testConfig) { session ->
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
            use(testConfig) { session ->
                session.uploadFile(localTestFile.absolutePath, "/tmp/${localTestFile.name}")
                session.downloadFile("/tmp/${localTestFile.name}", downloadedFile.absolutePath)
            }
            assertEquals("SSH测试内容", downloadedFile.readText())
        } finally {
            localTestFile.delete()
            downloadedFile.delete()
            executeSync(testConfig, "rm -f /tmp/${localTestFile.name}")
        }
    }
}
