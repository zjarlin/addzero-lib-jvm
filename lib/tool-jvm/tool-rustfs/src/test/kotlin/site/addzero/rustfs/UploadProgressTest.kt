package site.addzero.rustfs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach

class UploadProgressTest {

    @Test
    fun `UploadProgressData has correct properties`() {
        val data = UploadProgressData(
            uploaded = 1024,
            total = 2048,
            percent = 50.0,
            partNumber = 1,
            totalParts = 4
        )

        assertEquals(1024, data.uploaded)
        assertEquals(2048, data.total)
        assertEquals(50.0, data.percent)
        assertEquals(1, data.partNumber)
        assertEquals(4, data.totalParts)
    }

    @Test
    fun `UploadProgressData with null part info`() {
        val data = UploadProgressData(
            uploaded = 512,
            total = 1024,
            percent = 50.0
        )

        assertEquals(512, data.uploaded)
        assertEquals(1024, data.total)
        assertEquals(50.0, data.percent)
        assertNull(data.partNumber)
        assertNull(data.totalParts)
    }

    @Test
    fun `MultipartUploadConfig default values`() {
        val config = MultipartUploadConfig.default()

        assertEquals(MultipartUploadConfig.DEFAULT_PART_SIZE, config.partSize)
        assertEquals(MultipartUploadConfig.DEFAULT_CONCURRENCY, config.concurrency)
        assertEquals(MultipartUploadConfig.DEFAULT_MAX_RETRIES, config.maxRetries)
        assertEquals(MultipartUploadConfig.DEFAULT_TIMEOUT_SECONDS, config.timeoutSeconds)
        assertNull(config.progressListener)
    }

    @Test
    fun `MultipartUploadConfig custom values`() {
        val listener = UploadProgressListener {}
        val config = MultipartUploadConfig(
            partSize = 10 * 1024 * 1024,
            concurrency = 5,
            maxRetries = 5,
            timeoutSeconds = 60,
            progressListener = listener
        )

        assertEquals(10 * 1024 * 1024, config.partSize)
        assertEquals(5, config.concurrency)
        assertEquals(5, config.maxRetries)
        assertEquals(60, config.timeoutSeconds)
        assertEquals(listener, config.progressListener)
    }

    @Test
    fun `PartInfo default values`() {
        val part = PartInfo(
            partNumber = 1,
            start = 0,
            end = 5 * 1024 * 1024,
            size = 5 * 1024 * 1024
        )

        assertEquals(1, part.partNumber)
        assertEquals(0, part.start)
        assertEquals(5 * 1024 * 1024, part.end)
        assertEquals(5 * 1024 * 1024, part.size)
        assertEquals(PartStatus.PENDING, part.status)
        assertNull(part.etag)
    }

    @Test
    fun `PartInfo with completed status`() {
        val part = PartInfo(
            partNumber = 2,
            start = 5 * 1024 * 1024,
            end = 10 * 1024 * 1024,
            size = 5 * 1024 * 1024,
            etag = "abc123",
            status = PartStatus.COMPLETED
        )

        assertEquals(PartStatus.COMPLETED, part.status)
        assertEquals("abc123", part.etag)
    }

    @Test
    fun `UploadStatus calculates progress correctly`() {
        val status = UploadStatus(
            uploadId = "test-upload-id",
            bucketName = "test-bucket",
            objectKey = "test-key",
            fileSize = 1000,
            uploadedSize = 500,
            progress = 0.0,
            parts = emptyList(),
            status = UploadStatusType.IN_PROGRESS
        )

        val calculated = status.calculateProgress()
        assertEquals(50.0, calculated)
    }

    @Test
    fun `UploadStatus with zero file size returns zero progress`() {
        val status = UploadStatus(
            uploadId = "test-upload-id",
            bucketName = "test-bucket",
            objectKey = "test-key",
            fileSize = 0,
            uploadedSize = 0,
            progress = 0.0,
            parts = emptyList(),
            status = UploadStatusType.INITIALIZED
        )

        val calculated = status.calculateProgress()
        assertEquals(0.0, calculated)
    }

    @Test
    fun `UploadStatus clamps progress at 100 percent`() {
        val status = UploadStatus(
            uploadId = "test-upload-id",
            bucketName = "test-bucket",
            objectKey = "test-key",
            fileSize = 100,
            uploadedSize = 150, // More than total
            progress = 0.0,
            parts = emptyList(),
            status = UploadStatusType.IN_PROGRESS
        )

        val calculated = status.calculateProgress()
        assertEquals(100.0, calculated)
    }

    @Test
    fun `UploadProgressStorage generateKey with bucket and object key`() {
        val key = UploadProgressStorage.generateKey("my-bucket", "path/to/file.txt")

        assertEquals("upload:progress:my-bucket:path/to/file.txt", key)
    }

    @Test
    fun `UploadProgressStorage generateKey with uploadId`() {
        val key = UploadProgressStorage.generateKey("upload-12345")

        assertEquals("upload:progress:id:upload-12345", key)
    }

    @Test
    fun `MultipartUploadResult Success contains correct data`() {
        val result = MultipartUploadResult.Success(
            bucketName = "test-bucket",
            objectKey = "test-key",
            uploadId = "upload-id",
            etag = "etag-123",
            fileSize = 1024,
            partsCount = 3
        )

        assertTrue(result is MultipartUploadResult.Success)
        val success = result as MultipartUploadResult.Success
        assertEquals("test-bucket", success.bucketName)
        assertEquals("test-key", success.objectKey)
        assertEquals("upload-id", success.uploadId)
        assertEquals("etag-123", success.etag)
        assertEquals(1024, success.fileSize)
        assertEquals(3, success.partsCount)
    }

    @Test
    fun `MultipartUploadResult Failed contains error info`() {
        val exception = RuntimeException("Connection failed")
        val result = MultipartUploadResult.Failed(
            bucketName = "test-bucket",
            objectKey = "test-key",
            uploadId = null,
            error = "Upload failed",
            cause = exception
        )

        assertTrue(result is MultipartUploadResult.Failed)
        val failed = result as MultipartUploadResult.Failed
        assertEquals("test-bucket", failed.bucketName)
        assertEquals("test-key", failed.objectKey)
        assertNull(failed.uploadId)
        assertEquals("Upload failed", failed.error)
        assertEquals(exception, failed.cause)
    }

    @Test
    fun `MultipartUploadResult InProgress contains status`() {
        val status = UploadStatus(
            uploadId = "upload-id",
            bucketName = "test-bucket",
            objectKey = "test-key",
            fileSize = 2048,
            uploadedSize = 1024,
            progress = 50.0,
            parts = emptyList(),
            status = UploadStatusType.IN_PROGRESS
        )
        val result = MultipartUploadResult.InProgress("upload-id", status)

        assertTrue(result is MultipartUploadResult.InProgress)
        val inProgress = result as MultipartUploadResult.InProgress
        assertEquals("upload-id", inProgress.uploadId)
        assertEquals(status, inProgress.status)
    }

    @Test
    fun `PartStatus enum has all expected values`() {
        val statuses = PartStatus.entries

        assertTrue(statuses.contains(PartStatus.PENDING))
        assertTrue(statuses.contains(PartStatus.UPLOADING))
        assertTrue(statuses.contains(PartStatus.COMPLETED))
        assertTrue(statuses.contains(PartStatus.FAILED))
        assertEquals(4, statuses.size)
    }

    @Test
    fun `UploadStatusType enum has all expected values`() {
        val types = UploadStatusType.entries

        assertTrue(types.contains(UploadStatusType.INITIALIZED))
        assertTrue(types.contains(UploadStatusType.IN_PROGRESS))
        assertTrue(types.contains(UploadStatusType.COMPLETED))
        assertTrue(types.contains(UploadStatusType.FAILED))
        assertTrue(types.contains(UploadStatusType.CANCELLED))
        assertEquals(5, types.size)
    }

    @Test
    fun `InMemoryUploadProgressStorage save and retrieve status`() {
        val storage = InMemoryUploadProgressStorage()
        val status = UploadStatus(
            uploadId = "test-id",
            bucketName = "bucket",
            objectKey = "key",
            fileSize = 1000,
            uploadedSize = 500,
            progress = 50.0,
            parts = listOf(
                PartInfo(1, 0, 500, 500, status = PartStatus.COMPLETED),
                PartInfo(2, 500, 1000, 500, status = PartStatus.PENDING)
            ),
            status = UploadStatusType.IN_PROGRESS
        )

        val key = "test-key"
        val saved = storage.saveStatus(key, status)

        assertTrue(saved)

        val retrieved = storage.getStatus(key)
        assertNotNull(retrieved)
        assertEquals("test-id", retrieved?.uploadId)
        assertEquals(50.0, retrieved?.progress)
        assertEquals(2, retrieved?.parts?.size)
    }

    @Test
    fun `InMemoryUploadProgressStorage delete status`() {
        val storage = InMemoryUploadProgressStorage()
        val status = UploadStatus(
            uploadId = "test-id",
            bucketName = "bucket",
            objectKey = "key",
            fileSize = 1000,
            uploadedSize = 0,
            progress = 0.0,
            parts = emptyList(),
            status = UploadStatusType.INITIALIZED
        )

        val key = "test-key"
        storage.saveStatus(key, status)

        assertTrue(storage.deleteStatus(key))
        assertNull(storage.getStatus(key))
    }

    @Test
    fun `InMemoryUploadProgressStorage update part status`() {
        val storage = InMemoryUploadProgressStorage()
        val status = UploadStatus(
            uploadId = "test-id",
            bucketName = "bucket",
            objectKey = "key",
            fileSize = 1000,
            uploadedSize = 0,
            progress = 0.0,
            parts = listOf(
                PartInfo(1, 0, 500, 500, status = PartStatus.PENDING),
                PartInfo(2, 500, 1000, 500, status = PartStatus.PENDING)
            ),
            status = UploadStatusType.IN_PROGRESS
        )

        val key = "test-key"
        storage.saveStatus(key, status)

        // Update first part to completed
        val updated = storage.updatePartStatus(key, 1, PartStatus.COMPLETED, "etag-123")

        assertTrue(updated)

        val retrieved = storage.getStatus(key)
        assertEquals(PartStatus.COMPLETED, retrieved?.parts?.get(0)?.status)
        assertEquals("etag-123", retrieved?.parts?.get(0)?.etag)
        assertEquals(PartStatus.PENDING, retrieved?.parts?.get(1)?.status)
    }

    @Test
    fun `InMemoryUploadProgressStorage update uploaded size`() {
        val storage = InMemoryUploadProgressStorage()
        val status = UploadStatus(
            uploadId = "test-id",
            bucketName = "bucket",
            objectKey = "key",
            fileSize = 1000,
            uploadedSize = 0,
            progress = 0.0,
            parts = listOf(
                PartInfo(1, 0, 500, 500, status = PartStatus.COMPLETED),
                PartInfo(2, 500, 1000, 500, status = PartStatus.PENDING)
            ),
            status = UploadStatusType.IN_PROGRESS
        )

        val key = "test-key"
        storage.saveStatus(key, status)

        val updated = storage.updateUploadedSize(key, 500)

        assertTrue(updated)

        val retrieved = storage.getStatus(key)
        assertEquals(500, retrieved?.uploadedSize)
        assertEquals(50.0, retrieved?.progress)
    }

    @Test
    fun `InMemoryUploadProgressStorage clear all`() {
        val storage = InMemoryUploadProgressStorage()
        val status = UploadStatus(
            uploadId = "test-id",
            bucketName = "bucket",
            objectKey = "key",
            fileSize = 1000,
            uploadedSize = 0,
            progress = 0.0,
            parts = emptyList(),
            status = UploadStatusType.INITIALIZED
        )

        storage.saveStatus("key1", status)
        storage.saveStatus("key2", status)

        storage.clear()

        assertNull(storage.getStatus("key1"))
        assertNull(storage.getStatus("key2"))
    }

    @Test
    fun `CaffeineUploadProgressStorage create instance`() {
        val storage = CaffeineUploadProgressStorage.create()

        assertNotNull(storage)
    }

    @Test
    fun `CaffeineUploadProgressStorage custom config`() {
        val storage = CaffeineUploadProgressStorage(
            maximumSize = 500,
            expireAfterWriteSeconds = 3600
        )

        assertNotNull(storage)
    }

    @Test
    fun `CaffeineUploadProgressStorage save and retrieve status`() {
        val storage = CaffeineUploadProgressStorage.create()
        val status = UploadStatus(
            uploadId = "test-id",
            bucketName = "bucket",
            objectKey = "key",
            fileSize = 1000,
            uploadedSize = 500,
            progress = 50.0,
            parts = emptyList(),
            status = UploadStatusType.IN_PROGRESS
        )

        val key = "test-key"
        val saved = storage.saveStatus(key, status)

        assertTrue(saved)

        val retrieved = storage.getStatus(key)
        assertNotNull(retrieved)
        assertEquals("test-id", retrieved?.uploadId)
        assertEquals(50.0, retrieved?.progress)
    }

    @Test
    fun `CaffeineUploadProgressStorage delete status`() {
        val storage = CaffeineUploadProgressStorage.create()
        val status = UploadStatus(
            uploadId = "test-id",
            bucketName = "bucket",
            objectKey = "key",
            fileSize = 1000,
            uploadedSize = 0,
            progress = 0.0,
            parts = emptyList(),
            status = UploadStatusType.INITIALIZED
        )

        val key = "test-key"
        storage.saveStatus(key, status)

        assertTrue(storage.deleteStatus(key))
        assertNull(storage.getStatus(key))
    }

    @Test
    fun `CaffeineUploadProgressStorage update part status`() {
        val storage = CaffeineUploadProgressStorage.create()
        val status = UploadStatus(
            uploadId = "test-id",
            bucketName = "bucket",
            objectKey = "key",
            fileSize = 1000,
            uploadedSize = 0,
            progress = 0.0,
            parts = listOf(
                PartInfo(1, 0, 500, 500, status = PartStatus.PENDING),
                PartInfo(2, 500, 1000, 500, status = PartStatus.PENDING)
            ),
            status = UploadStatusType.IN_PROGRESS
        )

        val key = "test-key"
        storage.saveStatus(key, status)

        val updated = storage.updatePartStatus(key, 1, PartStatus.COMPLETED, "etag-123")

        assertTrue(updated)

        val retrieved = storage.getStatus(key)
        assertEquals(PartStatus.COMPLETED, retrieved?.parts?.get(0)?.status)
        assertEquals("etag-123", retrieved?.parts?.get(0)?.etag)
    }

    @Test
    fun `CaffeineUploadProgressStorage update uploaded size`() {
        val storage = CaffeineUploadProgressStorage.create()
        val status = UploadStatus(
            uploadId = "test-id",
            bucketName = "bucket",
            objectKey = "key",
            fileSize = 1000,
            uploadedSize = 0,
            progress = 0.0,
            parts = emptyList(),
            status = UploadStatusType.IN_PROGRESS
        )

        val key = "test-key"
        storage.saveStatus(key, status)

        val updated = storage.updateUploadedSize(key, 750)

        assertTrue(updated)

        val retrieved = storage.getStatus(key)
        assertEquals(750, retrieved?.uploadedSize)
        assertEquals(75.0, retrieved?.progress)
    }

    @Test
    fun `CaffeineUploadProgressStorage get all status`() {
        val storage = CaffeineUploadProgressStorage.create()
        val status1 = UploadStatus(
            uploadId = "id1",
            bucketName = "bucket",
            objectKey = "key1",
            fileSize = 1000,
            uploadedSize = 0,
            progress = 0.0,
            parts = emptyList(),
            status = UploadStatusType.IN_PROGRESS
        )
        val status2 = UploadStatus(
            uploadId = "id2",
            bucketName = "bucket",
            objectKey = "key2",
            fileSize = 2000,
            uploadedSize = 1000,
            progress = 50.0,
            parts = emptyList(),
            status = UploadStatusType.IN_PROGRESS
        )

        storage.saveStatus("key1", status1)
        storage.saveStatus("key2", status2)

        val all = storage.getAllStatus()

        assertEquals(2, all.size)
        assertTrue(all.containsKey("key1"))
        assertTrue(all.containsKey("key2"))
    }

    @Test
    fun `CaffeineUploadProgressStorage get status by upload id`() {
        val storage = CaffeineUploadProgressStorage.create()
        val status = UploadStatus(
            uploadId = "test-upload-id",
            bucketName = "bucket",
            objectKey = "key",
            fileSize = 1000,
            uploadedSize = 500,
            progress = 50.0,
            parts = emptyList(),
            status = UploadStatusType.IN_PROGRESS
        )

        val key = "test-key"
        storage.saveStatus(key, status)

        val retrieved = storage.getStatusByUploadId("test-upload-id")

        assertNotNull(retrieved)
        assertEquals("test-upload-id", retrieved?.uploadId)
    }

    @Test
    fun `CaffeineUploadProgressStorage clear all`() {
        val storage = CaffeineUploadProgressStorage.create()
        val status = UploadStatus(
            uploadId = "test-id",
            bucketName = "bucket",
            objectKey = "key",
            fileSize = 1000,
            uploadedSize = 0,
            progress = 0.0,
            parts = emptyList(),
            status = UploadStatusType.INITIALIZED
        )

        storage.saveStatus("key1", status)
        storage.saveStatus("key2", status)

        storage.clear()

        assertEquals(0, storage.getAllStatus().size)
    }
}
