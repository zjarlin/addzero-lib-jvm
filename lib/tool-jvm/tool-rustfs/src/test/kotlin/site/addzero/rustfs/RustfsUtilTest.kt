package site.addzero.rustfs

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.net.HttpURLConnection
import java.net.URL

class RustfsUtilTest {

    companion object {
        // 使用默认配置进行集成测试
        private val integrationConfig = RustfsConfig.default()

        private var isServiceAvailable = false

        @BeforeAll
        @JvmStatic
        fun checkServiceAvailability() {
            try {
                val endpoint = integrationConfig.endpoint
                val connection = URL(endpoint).openConnection() as HttpURLConnection
                connection.connectTimeout = 2000
                connection.readTimeout = 2000
                connection.requestMethod = "GET"
                connection.connect()
                // 任何响应都说明服务可用（包括 401/403 等）
                isServiceAvailable = true
                connection.disconnect()
            } catch (e: Exception) {
                // 服务不可用，跳过集成测试
                isServiceAvailable = false
            }
        }
    }

    @Test
    fun `build list request applies prefix and delimiter`() {
        val request = RustfsUtil.buildListRequest("example", "data/", recursive = false)

        assertEquals("example", request.bucket())
        assertEquals("data/", request.prefix())
        assertEquals("/", request.delimiter())
        assertEquals(1000, request.maxKeys())
    }

    @Test
    fun `build list request recursive omits delimiter`() {
        val request = RustfsUtil.buildListRequest("bucket", prefix = null, recursive = true)

        assertEquals("bucket", request.bucket())
        assertNull(request.delimiter())
    }

    @Test
    fun `presigned URL contains bucket and key`() {
        val bucket = "demo"
        val key = "hello.txt"

        val url = RustfsUtil.getPresignedObjectUrl(integrationConfig, bucket, key, expiresInSeconds = 60)

        assertNotNull(url)
        assertTrue(url!!.contains(bucket))
        assertTrue(url.contains(key))
    }

    @Test
    fun `createClient with default config`() {
        val client = RustfsUtil.createClient()

        assertNotNull(client)
    }

    @Test
    fun `upload and read object`() {
        assumeTrue(isServiceAvailable, "RustFS service is not available")

        val client = RustfsUtil.createClient(integrationConfig)
        val bucket = "test"
        val testKey = "test/upload-read-test-${System.currentTimeMillis()}.txt"
        val testContent = "Hello from RustfsUtilTest!".toByteArray()

        // Ensure bucket exists
        val ensureResult = RustfsUtil.ensureBucket(client, bucket)
        assertTrue(ensureResult is RustfsResult.Success, "Bucket should exist: $ensureResult")

        // Upload object
        val putResult = RustfsUtil.putObject(client, bucket, testKey, testContent, "text/plain")
        assertTrue(putResult is RustfsResult.Success, "Upload should succeed: $putResult")

        // Verify object exists
        assertTrue(RustfsUtil.objectExists(client, bucket, testKey), "Object should exist after upload")

        // Read object back
        val downloaded = RustfsUtil.getObject(client, bucket, testKey)
        assertNotNull(downloaded, "Downloaded content should not be null")
        assertArrayEquals(testContent, downloaded, "Downloaded content should match uploaded content")

        // Cleanup
        RustfsUtil.deleteObject(client, bucket, testKey)
    }

    @Test
    fun `upload and list objects`() {
        assumeTrue(isServiceAvailable, "RustFS service is not available")

        val client = RustfsUtil.createClient(integrationConfig)
        val bucket = "test"
        val prefix = "test/list-test-${System.currentTimeMillis()}/"
        val testKeys = listOf(
            "${prefix}file1.txt",
            "${prefix}file2.txt",
            "${prefix}nested/file3.txt"
        )

        // Ensure bucket exists
        RustfsUtil.ensureBucket(client, bucket)

        // Upload test objects
        val testContent = "test content".toByteArray()
        for (key in testKeys) {
            val result = RustfsUtil.putObject(client, bucket, key, testContent)
            assertTrue(result is RustfsResult.Success, "Upload should succeed for $key: $result")
        }

        // List objects with prefix
        val objects = RustfsUtil.listObjects(client, bucket, prefix, recursive = true)

        // Extract just the keys from results
        val foundKeys = objects.map { it.key() }.toSet()

        // Verify all test keys were found
        assertEquals(testKeys.size, foundKeys.intersect(testKeys.toSet()).size, "All uploaded keys should be listed")

        // Cleanup
        RustfsUtil.deleteObjects(client, bucket, testKeys)
    }
}
