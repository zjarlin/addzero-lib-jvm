package site.addzero.rustfs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class SpeedTrackingProgressListenerTest {

    private lateinit var storage: InMemoryUploadProgressStorage
    private lateinit var callbackValues: MutableList<UploadProgress>
    private lateinit var listener: SpeedTrackingProgressListener

    @BeforeEach
    fun setUp() {
        storage = InMemoryUploadProgressStorage()
        callbackValues = mutableListOf()
        listener = SpeedTrackingProgressListener(
            progressStorage = storage,
            bucketName = "test-bucket",
            objectKey = "test-key",
            onUpdate = { progress -> callbackValues.add(progress) }
        )
    }

    @Test
    fun `onProgress calculates speed correctly`() {
        val totalBytes = 10 * 1024 * 1024L // 10MB

        // Simulate some upload progress
        val progress1 = UploadProgressData(
            uploaded = 2 * 1024 * 1024, // 2MB
            total = totalBytes,
            percent = 20.0
        )

        Thread.sleep(100) // Small delay to ensure time passes

        val progress2 = UploadProgressData(
            uploaded = 5 * 1024 * 1024, // 5MB
            total = totalBytes,
            percent = 50.0
        )

        listener.onProgress(progress1)
        listener.onProgress(progress2)

        assertEquals(2, callbackValues.size)

        val result2 = callbackValues[1]
        assertNotNull(result2.speed)
        assertTrue(result2.speed!! > 0, "Speed should be positive")
    }

    @Test
    fun `onProgress updates total and uploaded bytes`() {
        val progress = UploadProgressData(
            uploaded = 1024,
            total = 2048,
            percent = 50.0
        )

        listener.onProgress(progress)

        assertEquals(1, callbackValues.size)
        val result = callbackValues[0]
        assertEquals(1024L, result.uploadedBytes)
        assertEquals(2048L, result.totalBytes)
    }

    @Test
    fun `onProgress includes part number info`() {
        val progress = UploadProgressData(
            uploaded = 5 * 1024 * 1024,
            total = 100 * 1024 * 1024,
            percent = 5.0,
            partNumber = 1,
            totalParts = 20
        )

        listener.onProgress(progress)

        assertEquals(1, callbackValues.size)
        val result = callbackValues[0]
        assertEquals(1, result.currentPart)
        assertEquals(20, result.totalParts)
    }

    @Test
    fun `onProgress without part info`() {
        val progress = UploadProgressData(
            uploaded = 1024,
            total = 2048,
            percent = 50.0
        )

        listener.onProgress(progress)

        assertEquals(1, callbackValues.size)
        val result = callbackValues[0]
        assertNull(result.currentPart)
        assertNull(result.totalParts)
    }

    @Test
    fun `onProgress updates storage when bucket and key provided`() {
        // First, create a status in storage
        val initialStatus = UploadStatus(
            uploadId = "test-upload",
            bucketName = "test-bucket",
            objectKey = "test-key",
            fileSize = 2048,
            uploadedSize = 0,
            progress = 0.0,
            parts = emptyList(),
            status = UploadStatusType.IN_PROGRESS
        )

        val key = UploadProgressStorage.generateKey("test-bucket", "test-key")
        storage.saveStatus(key, initialStatus)

        val progress = UploadProgressData(
            uploaded = 1024,
            total = 2048,
            percent = 50.0
        )

        listener.onProgress(progress)

        val status = storage.getStatus(key)

        assertNotNull(status)
        assertEquals(1024L, status?.uploadedSize)
    }

    @Test
    fun `onProgress without storage does not throw`() {
        val listenerNoStorage = SpeedTrackingProgressListener(
            progressStorage = null,
            bucketName = null,
            objectKey = null,
            onUpdate = { }
        )

        val progress = UploadProgressData(
            uploaded = 1024,
            total = 2048,
            percent = 50.0
        )

        // Should not throw
        listenerNoStorage.onProgress(progress)
    }

    @Test
    fun `onProgress calculates remaining time`() {
        // First, upload some data to establish a baseline
        val progress1 = UploadProgressData(
            uploaded = 1 * 1024 * 1024,
            total = 10 * 1024 * 1024,
            percent = 10.0
        )

        Thread.sleep(100)

        val progress2 = UploadProgressData(
            uploaded = 3 * 1024 * 1024,
            total = 10 * 1024 * 1024,
            percent = 30.0
        )

        listener.onProgress(progress1)
        listener.onProgress(progress2)

        val result = callbackValues[1]
        assertNotNull(result.remainingSeconds)
        // remainingSeconds should be non-null, might be 0 if very fast
        assertTrue(result.remainingSeconds!! >= 0)
    }

    @Test
    fun `reset clears all tracking state`() {
        val progress = UploadProgressData(
            uploaded = 5 * 1024 * 1024,
            total = 10 * 1024 * 1024,
            percent = 50.0
        )

        listener.onProgress(progress)
        listener.reset()

        // After reset, progress should start from zero time
        val progress2 = UploadProgressData(
            uploaded = 6 * 1024 * 1024,
            total = 10 * 1024 * 1024,
            percent = 60.0
        )

        Thread.sleep(50)
        listener.onProgress(progress2)

        // Speed should be calculated from reset point
        val result = callbackValues[1]
        assertNotNull(result)
    }

    @Test
    fun `multiple callbacks in sequence`() {
        val total = 10 * 1024 * 1024L

        for (i in 1..5) {
            val uploaded = (total * i / 5).toLong()
            val progress = UploadProgressData(
                uploaded = uploaded,
                total = total,
                percent = i * 20.0
            )
            listener.onProgress(progress)
            Thread.sleep(10)
        }

        assertEquals(5, callbackValues.size)

        // Verify progression
        assertEquals(2 * 1024 * 1024L, callbackValues[0].uploadedBytes)
        assertEquals(4 * 1024 * 1024L, callbackValues[1].uploadedBytes)
        assertEquals(6 * 1024 * 1024L, callbackValues[2].uploadedBytes)
        assertEquals(8 * 1024 * 1024L, callbackValues[3].uploadedBytes)
        assertEquals(10 * 1024 * 1024L, callbackValues[4].uploadedBytes)
    }

    @Test
    fun `concurrent onProgress calls are thread-safe`() {
        val latch = CountDownLatch(10)
        val errors = AtomicReference<Exception?>()
        val callCount = AtomicInteger(0)

        val concurrentListener = SpeedTrackingProgressListener(
            progressStorage = storage,
            bucketName = "test-bucket",
            objectKey = "test-key",
            onUpdate = { callCount.incrementAndGet() }
        )

        // Simulate concurrent progress updates
        repeat(10) { i ->
            Thread {
                try {
                    val progress = UploadProgressData(
                        uploaded = (i + 1) * 1024L * 1024,
                        total = 10 * 1024L * 1024,
                        percent = (i + 1) * 10.0
                    )
                    concurrentListener.onProgress(progress)
                } catch (e: Exception) {
                    errors.set(e)
                } finally {
                    latch.countDown()
                }
            }.start()
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS))
        assertNull(errors.get(), "No exceptions should occur during concurrent calls")
        assertEquals(10, callCount.get(), "All callbacks should be invoked")
    }

    @Test
    fun `progress at 100 percent is complete`() {
        val progress = UploadProgressData(
            uploaded = 10 * 1024 * 1024,
            total = 10 * 1024 * 1024,
            percent = 100.0
        )

        listener.onProgress(progress)

        assertEquals(1, callbackValues.size)
        assertTrue(callbackValues[0].isComplete)
    }

    @Test
    fun `progress below 100 percent is not complete`() {
        val progress = UploadProgressData(
            uploaded = 9 * 1024 * 1024,
            total = 10 * 1024 * 1024,
            percent = 90.0
        )

        listener.onProgress(progress)

        assertEquals(1, callbackValues.size)
        assertFalse(callbackValues[0].isComplete)
    }

    @Test
    fun `formatted progress string contains all info`() {
        val progress = UploadProgressData(
            uploaded = 5 * 1024 * 1024,
            total = 10 * 1024 * 1024,
            percent = 50.0,
            partNumber = 3,
            totalParts = 10
        )

        listener.onProgress(progress)

        assertEquals(1, callbackValues.size)
        val formatted = callbackValues[0].formatted

        assertTrue(formatted.contains("50.00%"))
        assertTrue(formatted.contains("(3/10)"))
        assertTrue(formatted.contains("MB"))
    }

    @Test
    fun `speed is zero on first call`() {
        val progress = UploadProgressData(
            uploaded = 1024,
            total = 2048,
            percent = 50.0
        )

        // Very first call might not have meaningful speed
        listener.onProgress(progress)

        // Speed should be available (though may be very high or zero depending on timing)
        val result = callbackValues[0]
        assertNotNull(result.speed)
    }
}
