package site.addzero.rustfs

import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import java.io.File
import java.util.concurrent.atomic.AtomicLong

/**
 * RustFS 上传辅助工具
 */
object RustfsUploadHelper {

    /**
     * 创建带速度计算的进度监听器
     *
     * @param progressStorage 进度存储（可选，默认使用 Caffeine）
     * @param bucketName 存储桶名称
     * @param objectKey 对象键
     * @param onUpdate 进度更新回调
     * @return UploadProgressListener
     */
    fun createProgressListener(
        progressStorage: UploadProgressStorage? = CaffeineUploadProgressStorage.create(),
        bucketName: String? = null,
        objectKey: String? = null,
        onUpdate: (UploadProgress) -> Unit = {}
    ): UploadProgressListener {
        return SpeedTrackingProgressListener(progressStorage, bucketName, objectKey, onUpdate)
    }

    /**
     * 创建 Caffeine 缓存进度监听器（默认）
     *
     * @param bucketName 存储桶名称
     * @param objectKey 对象键
     * @param onUpdate 进度更新回调
     * @return UploadProgressListener
     */
    fun createCaffeineProgressListener(
        bucketName: String,
        objectKey: String,
        onUpdate: (UploadProgress) -> Unit = {}
    ): UploadProgressListener {
        val storage = CaffeineUploadProgressStorage.create()
        return SpeedTrackingProgressListener(storage, bucketName, objectKey, onUpdate)
    }

    /**
     * 创建 Redis 进度监听器（直接写入 Redis）
     *
     * @param progressStorage Redis 进度存储
     * @param bucketName 存储桶名称
     * @param objectKey 对象键
     * @return UploadProgressListener
     */
    fun createRedisProgressListener(
        progressStorage: UploadProgressStorage,
        bucketName: String,
        objectKey: String
    ): UploadProgressListener {
        return RedisProgressListener(progressStorage, bucketName, objectKey)
    }

    /**
     * 检查对象是否存在并返回元数据
     *
     * @param client S3 客户端
     * @param bucketName 存储桶名称
     * @param objectKey 对象键
     * @return 对象元数据，不存在返回 null
     */
    fun headObject(
        client: S3Client,
        bucketName: String,
        objectKey: String
    ): HeadObjectResponse? {
        return try {
            val request = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build()
            client.headObject(request)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 判断是否需要使用分片上传
     *
     * @param fileSize 文件大小
     * @param threshold 分片上传阈值（默认 100MB）
     * @return 是否需要分片上传
     */
    fun shouldUseMultipartUpload(fileSize: Long, threshold: Long = 100 * 1024 * 1024): Boolean {
        return fileSize >= threshold
    }

    /**
     * 计算建议的分片大小
     *
     * @param fileSize 文件大小
     * @return 建议的分片大小（字节）
     */
    fun calculateOptimalPartSize(fileSize: Long): Long {
        return when {
            fileSize <= 100 * 1024 * 1024 -> 5 * 1024 * 1024L  // 5MB
            fileSize <= 1024 * 1024 * 1024 -> 10 * 1024 * 1024L  // 10MB
            fileSize <= 10 * 1024 * 1024 * 1024L -> 50 * 1024 * 1024L  // 50MB
            else -> 100 * 1024 * 1024L  // 100MB
        }
    }

    /**
     * 智能上传 - 自动选择普通上传或分片上传
     *
     * @param client S3 客户端
     * @param bucketName 存储桶名称
     * @param objectKey 对象键
     * @param file 要上传的文件
     * @param config 分片上传配置
     * @param progressStorage 进度存储（可选，默认使用 Caffeine）
     * @return 上传结果
     */
    fun smartUpload(
        client: S3Client,
        bucketName: String,
        objectKey: String,
        file: File,
        config: MultipartUploadConfig = MultipartUploadConfig.default(),
        progressStorage: UploadProgressStorage? = CaffeineUploadProgressStorage.create()
    ): RustfsResult {
        return if (shouldUseMultipartUpload(file.length())) {
            // 使用分片上传
            val result = RustfsMultipartUtil.uploadMultipart(
                client, bucketName, objectKey, file, config, progressStorage
            )
            when (result) {
                is MultipartUploadResult.Success -> RustfsResult.Success(
                    "File uploaded successfully: $bucketName/$objectKey",
                    mapOf(
                        "uploadId" to result.uploadId,
                        "etag" to result.etag,
                        "fileSize" to result.fileSize,
                        "partsCount" to result.partsCount
                    )
                )
                is MultipartUploadResult.Failed -> RustfsResult.Error(
                    result.error,
                    result.cause
                )
                is MultipartUploadResult.InProgress -> RustfsResult.InProgress(
                    "Upload in progress",
                    UploadProgress(
                        totalBytes = result.status.fileSize,
                        uploadedBytes = result.status.uploadedSize,
                        percent = result.status.progress,
                        currentPart = null,
                        totalParts = null
                    ),
                    result.uploadId
                )
            }
        } else {
            // 使用普通上传
            val contentType = guessContentType(file)
            val putResult = RustfsUtil.putObject(client, bucketName, objectKey, file, contentType)
            when (putResult) {
                is RustfsResult.Success -> RustfsResult.Success(
                    "File uploaded successfully: $bucketName/$objectKey",
                    mapOf("fileSize" to file.length())
                )
                is RustfsResult.Error -> putResult
                else -> RustfsResult.Error("Unknown error")
            }
        }
    }

    /**
     * 断点续传 - 自动判断是否需要续传
     *
     * @param client S3 客户端
     * @param bucketName 存储桶名称
     * @param objectKey 对象键
     * @param file 要上传的文件
     * @param config 分片上传配置
     * @param progressStorage 进度存储
     * @return 上传结果
     */
    fun resumeOrUpload(
        client: S3Client,
        bucketName: String,
        objectKey: String,
        file: File,
        config: MultipartUploadConfig = MultipartUploadConfig.default(),
        progressStorage: UploadProgressStorage
    ): RustfsResult {
        // 检查是否有未完成的上传
        val existingStatus = progressStorage.getStatus(
            UploadProgressStorage.generateKey(bucketName, objectKey)
        )

        return if (existingStatus != null &&
            (existingStatus.status == UploadStatusType.IN_PROGRESS ||
             existingStatus.status == UploadStatusType.INITIALIZED)) {
            // 续传
            val result = RustfsMultipartUtil.resumeUpload(
                client, bucketName, objectKey, file, config, progressStorage
            )
            when (result) {
                is MultipartUploadResult.Success -> RustfsResult.Success(
                    "File uploaded successfully (resumed): $bucketName/$objectKey",
                    mapOf(
                        "uploadId" to result.uploadId,
                        "etag" to result.etag,
                        "fileSize" to result.fileSize,
                        "resumed" to true
                    )
                )
                is MultipartUploadResult.Failed -> RustfsResult.Error(
                    result.error,
                    result.cause
                )
                is MultipartUploadResult.InProgress -> RustfsResult.InProgress(
                    "Upload in progress",
                    UploadProgress(
                        totalBytes = result.status.fileSize,
                        uploadedBytes = result.status.uploadedSize,
                        percent = result.status.progress
                    ),
                    result.uploadId
                )
            }
        } else {
            // 新上传
            smartUpload(client, bucketName, objectKey, file, config, progressStorage)
        }
    }

    /**
     * 猜测文件 Content-Type
     */
    private fun guessContentType(file: File): String {
        val name = file.name.lowercase()
        return when {
            name.endsWith(".jpg") || name.endsWith(".jpeg") -> "image/jpeg"
            name.endsWith(".png") -> "image/png"
            name.endsWith(".gif") -> "image/gif"
            name.endsWith(".webp") -> "image/webp"
            name.endsWith(".svg") -> "image/svg+xml"
            name.endsWith(".pdf") -> "application/pdf"
            name.endsWith(".txt") -> "text/plain; charset=utf-8"
            name.endsWith(".html") || name.endsWith(".htm") -> "text/html; charset=utf-8"
            name.endsWith(".css") -> "text/css"
            name.endsWith(".js") -> "application/javascript"
            name.endsWith(".json") -> "application/json"
            name.endsWith(".xml") -> "application/xml"
            name.endsWith(".zip") -> "application/zip"
            name.endsWith(".tar") -> "application/x-tar"
            name.endsWith(".tar.gz") -> "application/tar+gzip"
            name.endsWith(".gz") -> "application/gzip"
            name.endsWith(".rar") -> "application/vnd.rar"
            name.endsWith(".7z") -> "application/x-7z-compressed"
            name.endsWith(".mp4") -> "video/mp4"
            name.endsWith(".avi") -> "video/x-msvideo"
            name.endsWith(".mov") -> "video/quicktime"
            name.endsWith(".wmv") -> "video/x-ms-wmv"
            name.endsWith(".flv") -> "video/x-flv"
            name.endsWith(".mkv") -> "video/x-matroska"
            name.endsWith(".mp3") -> "audio/mpeg"
            name.endsWith(".wav") -> "audio/wav"
            name.endsWith(".ogg") -> "audio/ogg"
            name.endsWith(".flac") -> "audio/flac"
            name.endsWith(".m4a") -> "audio/mp4"
            name.endsWith(".aac") -> "audio/aac"
            name.endsWith(".doc") -> "application/msword"
            name.endsWith(".docx") -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            name.endsWith(".xls") -> "application/vnd.ms-excel"
            name.endsWith(".xlsx") -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            name.endsWith(".ppt") -> "application/vnd.ms-powerpoint"
            name.endsWith(".pptx") -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            name.endsWith(".csv") -> "text/csv"
            name.endsWith(".md") -> "text/markdown"
            name.endsWith(".yaml") || name.endsWith(".yml") -> "application/x-yaml"
            else -> "application/octet-stream"
        }
    }
}

/**
 * 带速度计算的进度监听器
 */
class SpeedTrackingProgressListener(
    private val progressStorage: UploadProgressStorage? = null,
    private val bucketName: String? = null,
    private val objectKey: String? = null,
    private val onUpdate: (UploadProgress) -> Unit = {}
) : UploadProgressListener {

    private var startTime = System.currentTimeMillis()
    private var lastUpdateTime = startTime
    private var lastUploadedBytes = 0L
    private val totalBytes = AtomicLong(0L)
    private val uploadedBytes = AtomicLong(0L)

    override fun onProgress(progress: UploadProgressData) {
        totalBytes.set(progress.total)
        uploadedBytes.set(progress.uploaded)

        val now = System.currentTimeMillis()
        val elapsed = (now - startTime) / 1000.0 // 秒
        val speed = if (elapsed > 0) {
            (progress.uploaded / elapsed).toLong()
        } else {
            0L
        }

        val remaining = if (speed > 0) {
            ((progress.total - progress.uploaded) / speed).toLong()
        } else {
            null
        }

        val uploadProgress = UploadProgress(
            totalBytes = progress.total,
            uploadedBytes = progress.uploaded,
            percent = progress.percent,
            currentPart = progress.partNumber,
            totalParts = progress.totalParts,
            speed = speed,
            remainingSeconds = remaining
        )

        // 持久化到存储
        if (progressStorage != null && bucketName != null && objectKey != null) {
            val key = UploadProgressStorage.generateKey(bucketName, objectKey)
            progressStorage.updateUploadedSize(key, progress.uploaded)
        }

        // 回调
        onUpdate(uploadProgress)
    }

    fun reset() {
        startTime = System.currentTimeMillis()
        lastUpdateTime = startTime
        lastUploadedBytes = 0L
        uploadedBytes.set(0L)
    }
}

/**
 * Redis 进度监听器（直接写入 Redis）
 */
class RedisProgressListener(
    private val progressStorage: UploadProgressStorage,
    private val bucketName: String,
    private val objectKey: String
) : UploadProgressListener {

    private val key = UploadProgressStorage.generateKey(bucketName, objectKey)

    override fun onProgress(progress: UploadProgressData) {
        progressStorage.updateUploadedSize(key, progress.uploaded)
    }
}
