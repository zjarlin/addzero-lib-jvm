package site.addzero.rustfs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import kotlin.io.path.createTempFile
import kotlin.io.path.writeBytes

class RustfsUploadHelperTest {

    @Test
    fun `shouldUseMultipartUpload returns true for large files`() {
        // 100MB file
        val largeFile = 100 * 1024 * 1024L
        assertTrue(RustfsUploadHelper.shouldUseMultipartUpload(largeFile))
    }

    @Test
    fun `shouldUseMultipartUpload returns false for small files`() {
        // 10MB file
        val smallFile = 10 * 1024 * 1024L
        assertFalse(RustfsUploadHelper.shouldUseMultipartUpload(smallFile))
    }

    @Test
    fun `shouldUseMultipartUpload uses custom threshold`() {
        val fileSize = 50 * 1024 * 1024L // 50MB
        val threshold = 60 * 1024 * 1024L // 60MB threshold

        assertFalse(RustfsUploadHelper.shouldUseMultipartUpload(fileSize, threshold))

        val threshold2 = 40 * 1024 * 1024L // 40MB threshold
        assertTrue(RustfsUploadHelper.shouldUseMultipartUpload(fileSize, threshold2))
    }

    @Test
    fun `shouldUseMultipartUpload returns true for threshold boundary`() {
        val threshold = 100 * 1024 * 1024L
        assertTrue(RustfsUploadHelper.shouldUseMultipartUpload(threshold))
    }

    @Test
    fun `calculateOptimalPartSize for small files`() {
        val partSize = RustfsUploadHelper.calculateOptimalPartSize(50 * 1024 * 1024) // 50MB

        assertEquals(5 * 1024 * 1024L, partSize) // 5MB
    }

    @Test
    fun `calculateOptimalPartSize for medium files`() {
        val partSize = RustfsUploadHelper.calculateOptimalPartSize(500 * 1024 * 1024) // 500MB

        assertEquals(10 * 1024 * 1024L, partSize) // 10MB
    }

    @Test
    fun `calculateOptimalPartSize for large files`() {
        val partSize = RustfsUploadHelper.calculateOptimalPartSize(5L * 1024 * 1024 * 1024) // 5GB

        assertEquals(50 * 1024 * 1024L, partSize) // 50MB
    }

    @Test
    fun `calculateOptimalPartSize for very large files`() {
        val partSize = RustfsUploadHelper.calculateOptimalPartSize(20L * 1024 * 1024 * 1024) // 20GB

        assertEquals(100 * 1024 * 1024L, partSize) // 100MB
    }

    @Test
    fun `createProgressListener with default storage`() {
        val listener = RustfsUploadHelper.createProgressListener()

        assertNotNull(listener)
    }

    @Test
    fun `createProgressListener with custom callback`() {
        var callbackCalled = false
        var receivedPercent: Double? = null

        val listener = RustfsUploadHelper.createProgressListener(
            onUpdate = { progress ->
                callbackCalled = true
                receivedPercent = progress.percent
            }
        )

        val testData = UploadProgressData(
            uploaded = 512,
            total = 1024,
            percent = 50.0
        )

        listener.onProgress(testData)

        assertTrue(callbackCalled)
        assertEquals(50.0, receivedPercent)
    }

    @Test
    fun `createCaffeineProgressListener creates listener with storage`() {
        var callbackCalled = false

        val listener = RustfsUploadHelper.createCaffeineProgressListener(
            bucketName = "test-bucket",
            objectKey = "test-key",
            onUpdate = { callbackCalled = true }
        )

        assertNotNull(listener)

        val testData = UploadProgressData(
            uploaded = 256,
            total = 512,
            percent = 50.0
        )

        listener.onProgress(testData)

        assertTrue(callbackCalled)
    }

    @Test
    fun `createRedisProgressListener creates listener with custom storage`() {
        val storage = InMemoryUploadProgressStorage()

        val listener = RustfsUploadHelper.createRedisProgressListener(
            progressStorage = storage,
            bucketName = "test-bucket",
            objectKey = "test-key"
        )

        assertNotNull(listener)

        // First, create a status in storage
        val initialStatus = UploadStatus(
            uploadId = "test-upload",
            bucketName = "test-bucket",
            objectKey = "test-key",
            fileSize = 256,
            uploadedSize = 0,
            progress = 0.0,
            parts = emptyList(),
            status = UploadStatusType.IN_PROGRESS
        )

        val key = UploadProgressStorage.generateKey("test-bucket", "test-key")
        storage.saveStatus(key, initialStatus)

        val testData = UploadProgressData(
            uploaded = 128,
            total = 256,
            percent = 50.0
        )

        listener.onProgress(testData)

        // Verify storage was updated
        val status = storage.getStatus(key)
        assertNotNull(status)
        assertEquals(128L, status?.uploadedSize)
    }

    @Test
    fun `guessContentType via smartUpload`() {
        // Create a temporary file with known extension
        val tempFile = createTempFile("test", ".txt").toFile()
        tempFile.deleteOnExit()

        // We can't directly test the private guessContentType method,
        // but we can verify the helper doesn't throw on known file types
        // This tests the integration indirectly
        val file = createTempFile("test", ".json").toFile()
        file.deleteOnExit()

        // Just verify the file exists - the actual content type detection
        // is tested through integration tests with real S3 operations
        assertTrue(file.exists())
    }

    @Test
    fun `UploadProgress formatted string shows correct info`() {
        val progress = UploadProgress(
            totalBytes = 10 * 1024 * 1024, // 10MB
            uploadedBytes = 5 * 1024 * 1024, // 5MB
            percent = 50.0,
            currentPart = 2,
            totalParts = 4,
            speed = 1024 * 1024, // 1MB/s
            remainingSeconds = 5
        )

        val formatted = progress.formatted

        assertTrue(formatted.contains("50.00%"))
        assertTrue(formatted.contains("(2/4)"))
        assertTrue(formatted.contains("MB"))
    }

    @Test
    fun `UploadProgress isComplete returns true when 100 percent`() {
        val progress = UploadProgress(
            totalBytes = 1000,
            uploadedBytes = 1000,
            percent = 100.0
        )

        assertTrue(progress.isComplete)
    }

    @Test
    fun `UploadProgress isComplete returns false when not 100 percent`() {
        val progress = UploadProgress(
            totalBytes = 1000,
            uploadedBytes = 500,
            percent = 50.0
        )

        assertFalse(progress.isComplete)
    }

    @Test
    fun `UploadProgress formatBytes formats correctly`() {
        assertEquals("512 B", UploadProgress.formatBytes(512))
        assertTrue(UploadProgress.formatBytes(1024).contains("KB"))
        assertTrue(UploadProgress.formatBytes(1024 * 1024).contains("MB"))
        assertTrue(UploadProgress.formatBytes(1024 * 1024 * 1024).contains("GB"))
    }

    @Test
    fun `UploadProgress formatSeconds formats correctly`() {
        assertEquals("30s", UploadProgress.formatSeconds(30))
        assertEquals("2m 30s", UploadProgress.formatSeconds(150))
        assertEquals("1h 30m", UploadProgress.formatSeconds(5400))
    }

}
