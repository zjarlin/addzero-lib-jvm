package site.addzero.util

import io.minio.MinioClient
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.nio.file.Files
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MinioUtilTest {

    private lateinit var config: MinioTestConfig
    private val testPrefix = "test/${UUID.randomUUID()}/"

    private lateinit var client: MinioClient
    private var initialized = false

    private val bucketName: String
        get() = config.bucketName

    @BeforeAll
    fun setup() {
        config = MinioTestConfig.default()
        client = MinioUtil.createClient(config.endpoint, config.accessKey, config.secretKey)

        val ensureResult = MinioUtil.ensureBucket(client, bucketName)
        if (ensureResult is MinioResult.Error) {
            val resultError = ensureResult as MinioResult.Error
            error("MinIO integration tests require bucket $bucketName to be accessible: ${resultError.message}")
        }

        initialized = true
    }

    @AfterAll
    fun cleanup() {
        if (!initialized) {
            return
        }
        val objects = MinioUtil.listObjects(client, bucketName, testPrefix)
        if (objects.isNotEmpty()) {
            MinioUtil.deleteObjects(client, bucketName, objects.map { it.objectName })
        }
    }

    @Test
    fun `test bucket exists`() {
        val exists = MinioUtil.bucketExists(client, bucketName)
        assertTrue(exists, "Bucket $bucketName should exist")

        val notExists = MinioUtil.bucketExists(client, "non-existent-bucket-12345")
        assertFalse(notExists, "Non-existent bucket should return false")
    }

    @Test
    fun `test list buckets`() {
        val buckets = MinioUtil.listBuckets(client)
        assertNotNull(buckets)
        assertTrue(buckets.isNotEmpty(), "Should have at least one bucket")

        val boxunBucket = buckets.find { it.name() == bucketName }
        assertNotNull(boxunBucket, "Bucket $bucketName should be in the list")
    }

    @Test
    fun `test ensure bucket`() {
        val testBucket = "test-bucket-${UUID.randomUUID()}"

        try {
            val result = MinioUtil.ensureBucket(client, testBucket)
            assertTrue(result is MinioResult.Success)

            val exists = MinioUtil.bucketExists(client, testBucket)
            assertTrue(exists, "Bucket should exist after ensure")

            val ensureAgain = MinioUtil.ensureBucket(client, testBucket)
            assertTrue(ensureAgain is MinioResult.Success)
        } finally {
            MinioUtil.deleteBucket(client, testBucket, force = true)
        }
    }

    @Test
    fun `test put object from bytes`() {
        val objectName = "${testPrefix}test-bytes.txt"
        val content = "Hello MinIO from Kotlin!".toByteArray()

        val result = MinioUtil.putObject(client, bucketName, objectName, content, "text/plain")
        assertTrue(result is MinioResult.Success)

        val exists = MinioUtil.objectExists(client, bucketName, objectName)
        assertTrue(exists, "Object should exist after upload")
    }

    @Test
    fun `test put and get object`() {
        val objectName = "${testPrefix}test-get-put.txt"
        val originalContent = "Test content for put and get".toByteArray()

        MinioUtil.putObject(client, bucketName, objectName, originalContent)

        val retrieved = MinioUtil.getObject(client, bucketName, objectName)
        assertNotNull(retrieved)
        assertArrayEquals(originalContent, retrieved!!, "Retrieved content should match original")
    }

    @Test
    fun `test stat object`() {
        val objectName = "${testPrefix}test-stat.txt"
        val content = "Content for stat test".toByteArray()

        MinioUtil.putObject(client, bucketName, objectName, content)

        val info = MinioUtil.statObject(client, bucketName, objectName)
        assertNotNull(info)
        assertEquals(objectName, info!!.objectName)
        assertEquals(content.size.toLong(), info.size)
        assertNotNull(info.etag)
    }

    @Test
    fun `test object exists`() {
        val objectName = "${testPrefix}test-exists.txt"
        val content = "Exists test".toByteArray()

        MinioUtil.putObject(client, bucketName, objectName, content)

        assertTrue(MinioUtil.objectExists(client, bucketName, objectName))
        assertFalse(MinioUtil.objectExists(client, bucketName, "${testPrefix}non-existent.txt"))
    }

    @Test
    fun `test list objects`() {
        val prefix = "${testPrefix}list-test/"
        val names = listOf("file1.txt", "file2.txt", "file3.txt")

        names.forEach { name ->
            MinioUtil.putObject(client, bucketName, "$prefix$name", "content".toByteArray())
        }

        val objects = MinioUtil.listObjects(client, bucketName, prefix)
        assertTrue(objects.size >= names.size, "Should list at least ${names.size} objects")

        val foundNames = objects.map { it.objectName.substringAfterLast('/') }.toSet()
        names.forEach { name ->
            assertTrue(foundNames.contains(name), "File $name should be in the list")
        }
    }

    @Test
    fun `test delete object`() {
        val objectName = "${testPrefix}test-delete.txt"
        val content = "To be deleted".toByteArray()

        MinioUtil.putObject(client, bucketName, objectName, content)
        assertTrue(MinioUtil.objectExists(client, bucketName, objectName))

        val result = MinioUtil.deleteObject(client, bucketName, objectName)
        assertTrue(result is MinioResult.Success)

        assertFalse(MinioUtil.objectExists(client, bucketName, objectName))
    }

    @Test
    fun `test delete objects batch`() {
        val prefix = "${testPrefix}batch-delete/"
        val names = (1..5).map { "file$it.txt" }

        names.forEach { name ->
            MinioUtil.putObject(client, bucketName, "$prefix$name", "content".toByteArray())
        }

        val objects = MinioUtil.listObjects(client, bucketName, prefix)
        val objectNames = objects.map { it.objectName }

        val result = MinioUtil.deleteObjects(client, bucketName, objectNames)
        assertTrue(result is MinioResult.Success)

        val remaining = MinioUtil.listObjects(client, bucketName, prefix)
        assertEquals(0, remaining.size, "All objects should be deleted")
    }

    @Test
    fun `test copy object`() {
        val sourceObject = "${testPrefix}source.txt"
        val targetObject = "${testPrefix}target.txt"
        val content = "Content to copy".toByteArray()

        MinioUtil.putObject(client, bucketName, sourceObject, content)

        val result = MinioUtil.copyObject(client, bucketName, sourceObject, bucketName, targetObject)
        assertTrue(result is MinioResult.Success)

        val sourceData = MinioUtil.getObject(client, bucketName, sourceObject)
        val targetData = MinioUtil.getObject(client, bucketName, targetObject)

        assertArrayEquals(sourceData, targetData, "Copied content should match original")
    }

    @Test
    fun `test put object from file`() {
        val objectName = "${testPrefix}from-file.txt"
        val tempFile = Files.createTempFile("minio-test", ".txt").toFile()
        tempFile.writeText("Content from file upload")

        try {
            val result = MinioUtil.putObject(client, bucketName, objectName, tempFile)
            assertTrue(result is MinioResult.Success)

            val retrieved = MinioUtil.getObject(client, bucketName, objectName)
            assertNotNull(retrieved)
            assertEquals(tempFile.readText(), String(retrieved!!))
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun `test get object to file`() {
        val objectName = "${testPrefix}download.txt"
        val content = "Content for download".toByteArray()

        MinioUtil.putObject(client, bucketName, objectName, content)

        val tempFile = Files.createTempFile("minio-download", ".txt").toFile()
        try {
            val result = MinioUtil.getObjectToFile(client, bucketName, objectName, tempFile)
            assertTrue(result is MinioResult.Success)

            assertTrue(tempFile.exists())
            assertArrayEquals(content, tempFile.readBytes())
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun `test get presigned URL`() {
        val objectName = "${testPrefix}presigned.txt"
        val content = "Content for presigned URL".toByteArray()

        MinioUtil.putObject(client, bucketName, objectName, content)

        val url = MinioUtil.getPresignedObjectUrl(client, bucketName, objectName, 3600)
        assertNotNull(url)
        assertTrue(url!!.contains(bucketName))
        assertTrue(url.contains(objectName))
    }

    @Test
    fun `test content type detection`() {
        val testCases = mapOf(
            "${testPrefix}image.jpg" to "image/jpeg",
            "${testPrefix}image.png" to "image/png",
            "${testPrefix}doc.pdf" to "application/pdf",
            "${testPrefix}data.json" to "application/json",
            "${testPrefix}page.html" to "text/html"
        )

        testCases.forEach { (name, expectedType) ->
            val result = MinioUtil.putObject(client, bucketName, name, "test".toByteArray(), expectedType)
            assertTrue(result is MinioResult.Success)

            val info = MinioUtil.statObject(client, bucketName, name)
            assertNotNull(info)
            assertEquals(expectedType, info!!.contentType, "Content type mismatch for $name")
        }
    }

    @Test
    fun `test upload binary data`() {
        val objectName = "${testPrefix}binary.dat"
        val binaryData = ByteArray(1024) { it.toByte() }

        val result = MinioUtil.putObject(client, bucketName, objectName, binaryData)
        assertTrue(result is MinioResult.Success)

        val retrieved = MinioUtil.getObject(client, bucketName, objectName)
        assertNotNull(retrieved)
        assertArrayEquals(binaryData, retrieved!!)
    }

    @Test
    fun `test empty object`() {
        val objectName = "${testPrefix}empty.txt"
        val emptyData = ByteArray(0)

        val result = MinioUtil.putObject(client, bucketName, objectName, emptyData)
        assertTrue(result is MinioResult.Success)

        val info = MinioUtil.statObject(client, bucketName, objectName)
        assertNotNull(info)
        assertEquals(0L, info!!.size)
    }

    @Test
    fun `test special characters in object name`() {
        val specialNames = listOf(
            "${testPrefix}file with spaces.txt",
            "${testPrefix}文件.txt",
            "${testPrefix}file-with-dashes.txt"
        )

        specialNames.forEach { name ->
            val content = "Special char test".toByteArray()
            val result = MinioUtil.putObject(client, bucketName, name, content)
            assertTrue(result is MinioResult.Success, "Failed to upload: $name")

            val retrieved = MinioUtil.getObject(client, bucketName, name)
            assertNotNull(retrieved, "Failed to retrieve: $name")
            assertArrayEquals(content, retrieved!!, "Content mismatch for: $name")
        }
    }

    @Test
    fun `test list objects with recursive false`() {
        val prefix = "${testPrefix}recursive-test/"
        val files = listOf(
            "file1.txt",
            "subdir/file2.txt",
            "subdir/deep/file3.txt"
        )

        files.forEach { name ->
            MinioUtil.putObject(client, bucketName, "$prefix$name", "content".toByteArray())
        }

        val nonRecursive = MinioUtil.listObjects(client, bucketName, prefix, recursive = false)
        val recursive = MinioUtil.listObjects(client, bucketName, prefix, recursive = true)

        assertTrue(recursive.size >= nonRecursive.size, "Recursive should return more or equal items")
    }

    private data class MinioTestConfig(
        val endpoint: String,
        val accessKey: String,
        val secretKey: String,
        val bucketName: String
    ) {
        companion object {
            fun default(): MinioTestConfig {
                return MinioTestConfig(
                    endpoint = "http://addzero.site:9091",
                    accessKey = "zjarlin",
                    secretKey = "zhou9955",
                    bucketName = "boxun"
                )
            }
        }
    }
}
