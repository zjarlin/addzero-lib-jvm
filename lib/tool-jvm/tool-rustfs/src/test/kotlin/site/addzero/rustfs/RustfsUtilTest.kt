package site.addzero.rustfs

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RustfsUtilTest {

    // 集成测试配置：连接到 RustFS 服务
    private val integrationConfig = RustfsConfig(
        endpoint = "http://addzero.site:9000",
        accessKey = "zjarlin",
        secretKey = "zhou9955",
        region = "us-east-1"
    )

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
        val config = RustfsConfig.default()
        val bucket = "demo"
        val key = "hello.txt"

        val url = RustfsUtil.getPresignedObjectUrl(config, bucket, key, expiresInSeconds = 60)

        assertNotNull(url)
        assertTrue(url!!.contains(bucket))
        assertTrue(url.contains(key))
    }

    @Test
    fun `upload and read object from boxun bucket`() {
        val client = RustfsUtil.createClient(integrationConfig)
        val bucket = "boxun"
        val testKey = "test/upload-read-test.txt"
        val testContent = "Hello from RustfsUtilTest! ${System.currentTimeMillis()}".toByteArray()

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
    fun `upload and list objects in boxun bucket`() {
        val client = RustfsUtil.createClient(integrationConfig)
        val bucket = "boxun"
        val prefix = "test/list-test/"
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
            RustfsUtil.putObject(client, bucket, key, testContent)
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
