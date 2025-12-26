package site.addzero.rustfs

import org.slf4j.LoggerFactory
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.*
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * RustFS 分片上传工具类
 *
 * 支持功能：
 * - 分片上传：将大文件切分成多个分片并发上传
 * - 断点续传：记录上传进度，支持从中断处继续上传
 * - 进度回调：实时回调上传进度
 * - Redis 进度存储：支持通过 Redis 存储上传进度
 */
object RustfsMultipartUtil {

    private val logger = LoggerFactory.getLogger(RustfsMultipartUtil::class.java)

    // 最小分片大小（5MB），S3 规范要求
    private const val MIN_PART_SIZE = 5L * 1024 * 1024

    // 最大分片数量（S3 限制）
    private const val MAX_PARTS = 10000

    // 内存缓存上传状态，用于快速查询
    private val uploadCache = ConcurrentHashMap<String, UploadStatus>()

    /**
     * 初始化分片上传
     *
     * @param client S3 客户端
     * @param bucketName 存储桶名称
     * @param objectKey 对象键
     * @param file 要上传的文件
     * @param config 分片上传配置
     * @param progressStorage 进度存储（可选，默认使用 Caffeine）
     * @return 分片上传结果（成功返回 uploadId）
     */
    fun initMultipartUpload(
        client: S3Client,
        bucketName: String,
        objectKey: String,
        file: File,
        config: MultipartUploadConfig = MultipartUploadConfig.default(),
        progressStorage: UploadProgressStorage? = CaffeineUploadProgressStorage.create()
    ): MultipartUploadResult {
        return try {
            // 检查文件
            if (!file.exists()) {
                return MultipartUploadResult.Failed(
                    bucketName, objectKey, null,
                    "File not found: ${file.absolutePath}"
                )
            }

            val fileSize = file.length()
            if (fileSize <= 0) {
                return MultipartUploadResult.Failed(
                    bucketName, objectKey, null,
                    "File is empty: ${file.absolutePath}"
                )
            }

            // 检查是否有未完成的上传（断点续传）
            val storageKey = UploadProgressStorage.generateKey(bucketName, objectKey)
            val existingStatus = progressStorage?.getStatus(storageKey)
                ?: uploadCache[storageKey]

            if (existingStatus != null && existingStatus.status == UploadStatusType.IN_PROGRESS) {
                logger.info("Found existing upload for $bucketName/$objectKey, uploadId: ${existingStatus.uploadId}")
                return MultipartUploadResult.InProgress(existingStatus.uploadId, existingStatus)
            }

            // 计算分片数量
            val partSize = maxOf(config.partSize, MIN_PART_SIZE)
            val partsCount = calculatePartsCount(fileSize, partSize)

            if (partsCount > MAX_PARTS) {
                return MultipartUploadResult.Failed(
                    bucketName, objectKey, null,
                    "File too large, parts count ($partsCount) exceeds maximum ($MAX_PARTS)"
                )
            }

            // 初始化分片信息
            val parts = generatePartInfos(fileSize, partSize)

            // 创建 S3 分片上传
            val createRequest = CreateMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(guessContentType(file))
                .build()

            val createResponse = client.createMultipartUpload(createRequest)
            val uploadId = createResponse.uploadId()

            // 创建上传状态
            val status = UploadStatus(
                uploadId = uploadId,
                bucketName = bucketName,
                objectKey = objectKey,
                fileSize = fileSize,
                uploadedSize = 0L,
                progress = 0.0,
                parts = parts,
                status = UploadStatusType.INITIALIZED
            )

            // 保存状态
            uploadCache[storageKey] = status
            progressStorage?.saveStatus(storageKey, status)
            uploadCache[uploadId] = status

            logger.info("Initialized multipart upload: $bucketName/$objectKey, uploadId: $uploadId, parts: $partsCount")

            MultipartUploadResult.Success(bucketName, objectKey, uploadId, "", fileSize, partsCount)
        } catch (e: Exception) {
            logger.error("Failed to initialize multipart upload: $bucketName/$objectKey", e)
            MultipartUploadResult.Failed(bucketName, objectKey, null, e.message ?: "Unknown error", e)
        }
    }

    /**
     * 上传分片
     *
     * @param client S3 客户端
     * @param bucketName 存储桶名称
     * @param objectKey 对象键
     * @param uploadId 上传 ID
     * @param file 要上传的文件
     * @param partNumber 分片号（从 1 开始）
     * @param config 分片上传配置
     * @return 分片上传结果（etag）
     */
    fun uploadPart(
        client: S3Client,
        bucketName: String,
        objectKey: String,
        uploadId: String,
        file: File,
        partNumber: Int,
        config: MultipartUploadConfig = MultipartUploadConfig.default()
    ): Result<String> {
        return runCatching {
            val partSize = maxOf(config.partSize, MIN_PART_SIZE)
            val parts = generatePartInfos(file.length(), partSize)

            if (partNumber < 1 || partNumber > parts.size) {
                throw IllegalArgumentException("Invalid part number: $partNumber, total parts: ${parts.size}")
            }

            val partInfo = parts[partNumber - 1]

            // 读取分片数据
            val partData = readPartData(file, partInfo.start, partInfo.size)

            // 上传分片
            val request = UploadPartRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .contentLength(partInfo.size)
                .build()

            val response = client.uploadPart(request, RequestBody.fromBytes(partData))
            val etag = response.eTag()

            logger.debug("Uploaded part $partNumber/${parts.size} for $bucketName/$objectKey, etag: $etag")
            etag
        }.onFailure { e ->
            logger.error("Failed to upload part $partNumber for $bucketName/$objectKey", e)
        }
    }

    /**
     * 执行完整的分片上传（并发上传所有分片）
     *
     * @param client S3 客户端
     * @param bucketName 存储桶名称
     * @param objectKey 对象键
     * @param file 要上传的文件
     * @param config 分片上传配置
     * @param progressStorage 进度存储（可选，默认使用 Caffeine）
     * @return 分片上传结果
     */
    fun uploadMultipart(
        client: S3Client,
        bucketName: String,
        objectKey: String,
        file: File,
        config: MultipartUploadConfig = MultipartUploadConfig.default(),
        progressStorage: UploadProgressStorage? = CaffeineUploadProgressStorage.create()
    ): MultipartUploadResult {
        // 先初始化
        val initResult = initMultipartUpload(
            client, bucketName, objectKey, file, config, progressStorage
        )

        if (initResult !is MultipartUploadResult.Success) {
            return initResult
        }

        val uploadId = initResult.uploadId
        val storageKey = UploadProgressStorage.generateKey(bucketName, objectKey)

        return try {
            val partSize = maxOf(config.partSize, MIN_PART_SIZE)
            val parts = generatePartInfos(file.length(), partSize)
            val uploadedBytes = AtomicLong(0L)

            // 创建线程池
            val executor = Executors.newFixedThreadPool(config.concurrency)

            // 提交上传任务
            val futures = parts.map { partInfo ->
                executor.submit<PartInfo> {
                    var retries = 0
                    var lastError: Exception? = null

                    while (retries <= config.maxRetries) {
                        try {
                            // 更新状态为上传中
                            progressStorage?.updatePartStatus(
                                storageKey, partInfo.partNumber,
                                PartStatus.UPLOADING, null
                            )

                            // 读取分片数据
                            val partData = readPartData(file, partInfo.start, partInfo.size)

                            // 上传分片
                            val request = UploadPartRequest.builder()
                                .bucket(bucketName)
                                .key(objectKey)
                                .uploadId(uploadId)
                                .partNumber(partInfo.partNumber)
                                .contentLength(partInfo.size)
                                .build()

                            val response = client.uploadPart(request, RequestBody.fromBytes(partData))
                            val etag = response.eTag()

                            // 更新状态为已完成
                            val completedPart = partInfo.copy(
                                status = PartStatus.COMPLETED,
                                etag = etag
                            )
                            progressStorage?.updatePartStatus(
                                storageKey, partInfo.partNumber,
                                PartStatus.COMPLETED, etag
                            )

                            // 更新总进度
                            val newUploaded = uploadedBytes.addAndGet(partInfo.size)
                            config.progressListener?.onProgress(
                                UploadProgressData(
                                    uploaded = newUploaded,
                                    total = file.length(),
                                    percent = newUploaded.toDouble() / file.length() * 100,
                                    partNumber = partInfo.partNumber,
                                    totalParts = parts.size
                                )
                            )

                            progressStorage?.updateUploadedSize(storageKey, newUploaded)

                            logger.debug(
                                "Uploaded part ${partInfo.partNumber}/${parts.size}, " +
                                        "progress: ${String.format("%.2f%%", newUploaded.toDouble() / file.length() * 100)}"
                            )

                            return@submit completedPart
                        } catch (e: Exception) {
                            lastError = e
                            retries++
                            if (retries <= config.maxRetries) {
                                logger.warn("Upload part ${partInfo.partNumber} failed, retrying ($retries/${config.maxRetries})", e)
                                Thread.sleep(1000L * retries)
                            }
                        }
                    }

                    // 重试次数用完，标记为失败
                    progressStorage?.updatePartStatus(
                        storageKey, partInfo.partNumber,
                        PartStatus.FAILED, null
                    )

                    throw lastError ?: Exception("Upload failed after ${config.maxRetries} retries")
                }
            }

            // 等待所有分片上传完成
            val completedParts = mutableListOf<CompletedPart>()
            var failedPart: PartInfo? = null

            for ((index, future) in futures.withIndex()) {
                try {
                    val part = future.get()
                    completedParts.add(
                        CompletedPart.builder()
                            .partNumber(part.partNumber)
                            .eTag(part.etag!!)
                            .build()
                    )
                } catch (e: Exception) {
                    logger.error("Failed to upload part ${index + 1}", e)
                    failedPart = parts[index]
                    break
                }
            }

            executor.shutdown()
            executor.awaitTermination(1, TimeUnit.MINUTES)

            // 如果有失败的分片，取消上传
            if (failedPart != null) {
                abortMultipartUpload(client, bucketName, objectKey, uploadId)
                updateStatus(storageKey, UploadStatusType.FAILED, "Failed to upload part ${failedPart.partNumber}", progressStorage)
                return MultipartUploadResult.Failed(
                    bucketName, objectKey, uploadId,
                    "Failed to upload part ${failedPart.partNumber}"
                )
            }

            // 合并分片
            val completedPartsSorted = completedParts.sortedBy { it.partNumber() }
            val completeRequest = CompleteMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .uploadId(uploadId)
                .multipartUpload { m -> m.parts(completedPartsSorted) }
                .build()

            val completeResponse = client.completeMultipartUpload(completeRequest)
            // location() 和 eTag() 在某些版本可能不可用，使用 try-catch 处理
            val location = try { completeResponse.location() } catch (e: Exception) { null }
            val etag = try { completeResponse.eTag() } catch (e: Exception) { "" }

            // 更新状态为已完成
            updateStatus(storageKey, UploadStatusType.COMPLETED, null, progressStorage)

            logger.info("Multipart upload completed: $bucketName/$objectKey, uploadId: $uploadId, etag: $etag")

            MultipartUploadResult.Success(
                bucketName, objectKey, uploadId,
                etag, file.length(), parts.size
            )

        } catch (e: Exception) {
            logger.error("Failed to complete multipart upload: $bucketName/$objectKey", e)
            abortMultipartUpload(client, bucketName, objectKey, uploadId)
            updateStatus(storageKey, UploadStatusType.FAILED, e.message, progressStorage)
            MultipartUploadResult.Failed(bucketName, objectKey, uploadId, e.message ?: "Unknown error", e)
        }
    }

    /**
     * 断点续传 - 继续未完成的上传
     *
     * @param client S3 客户端
     * @param bucketName 存储桶名称
     * @param objectKey 对象键
     * @param file 要上传的文件
     * @param config 分片上传配置
     * @param progressStorage 进度存储
     * @return 分片上传结果
     */
    fun resumeUpload(
        client: S3Client,
        bucketName: String,
        objectKey: String,
        file: File,
        config: MultipartUploadConfig = MultipartUploadConfig.default(),
        progressStorage: UploadProgressStorage
    ): MultipartUploadResult {
        val storageKey = UploadProgressStorage.generateKey(bucketName, objectKey)
        val status = progressStorage.getStatus(storageKey)
            ?: return MultipartUploadResult.Failed(
                bucketName, objectKey, null,
                "No existing upload found for $bucketName/$objectKey"
            )

        if (status.status != UploadStatusType.IN_PROGRESS && status.status != UploadStatusType.INITIALIZED) {
            return MultipartUploadResult.Failed(
                bucketName, objectKey, status.uploadId,
                "Upload status is ${status.status}, cannot resume"
            )
        }

        logger.info("Resuming upload: $bucketName/$objectKey, uploadId: ${status.uploadId}")

        return try {
            val partSize = maxOf(config.partSize, MIN_PART_SIZE)
            val parts = status.parts
            val uploadedBytes = AtomicLong(
                parts.filter { it.status == PartStatus.COMPLETED }.sumOf { it.size }
            )

            // 创建线程池
            val executor = Executors.newFixedThreadPool(config.concurrency)

            // 只上传未完成的分片
            val pendingParts = parts.filter { it.status != PartStatus.COMPLETED }

            val futures = pendingParts.map { partInfo ->
                executor.submit<PartInfo> {
                    var retries = 0
                    var lastError: Exception? = null

                    while (retries <= config.maxRetries) {
                        try {
                            progressStorage?.updatePartStatus(
                                storageKey, partInfo.partNumber,
                                PartStatus.UPLOADING, null
                            )

                            val partData = readPartData(file, partInfo.start, partInfo.size)

                            val request = UploadPartRequest.builder()
                                .bucket(bucketName)
                                .key(objectKey)
                                .uploadId(status.uploadId)
                                .partNumber(partInfo.partNumber)
                                .contentLength(partInfo.size)
                                .build()

                            val response = client.uploadPart(request, RequestBody.fromBytes(partData))
                            val etag = response.eTag()

                            progressStorage?.updatePartStatus(
                                storageKey, partInfo.partNumber,
                                PartStatus.COMPLETED, etag
                            )

                            val newUploaded = uploadedBytes.addAndGet(partInfo.size)
                            config.progressListener?.onProgress(
                                UploadProgressData(
                                    uploaded = newUploaded,
                                    total = file.length(),
                                    percent = newUploaded.toDouble() / file.length() * 100,
                                    partNumber = partInfo.partNumber,
                                    totalParts = parts.size
                                )
                            )

                            progressStorage?.updateUploadedSize(storageKey, newUploaded)

                            return@submit partInfo.copy(
                                status = PartStatus.COMPLETED,
                                etag = etag
                            )
                        } catch (e: Exception) {
                            lastError = e
                            retries++
                            if (retries <= config.maxRetries) {
                                logger.warn("Upload part ${partInfo.partNumber} failed, retrying", e)
                                Thread.sleep(1000L * retries)
                            }
                        }
                    }

                    progressStorage?.updatePartStatus(
                        storageKey, partInfo.partNumber,
                        PartStatus.FAILED, null
                    )

                    throw lastError ?: Exception("Upload failed after ${config.maxRetries} retries")
                }
            }

            // 收集已完成的分片和新上传的分片
            val completedParts = mutableListOf<CompletedPart>()

            // 已完成的分片
            parts.filter { it.status == PartStatus.COMPLETED && it.etag != null }
                .forEach { part ->
                    completedParts.add(
                        CompletedPart.builder()
                            .partNumber(part.partNumber)
                            .eTag(part.etag!!)
                            .build()
                    )
                }

            // 等待新上传的分片
            var failedPart: PartInfo? = null
            for (future in futures) {
                try {
                    val part = future.get()
                    completedParts.add(
                        CompletedPart.builder()
                            .partNumber(part.partNumber)
                            .eTag(part.etag!!)
                            .build()
                    )
                } catch (e: Exception) {
                    logger.error("Failed to upload part", e)
                    failedPart = parts.find { it.status == PartStatus.FAILED }
                    break
                }
            }

            executor.shutdown()
            executor.awaitTermination(1, TimeUnit.MINUTES)

            if (failedPart != null) {
                updateStatus(storageKey, UploadStatusType.FAILED, "Failed to upload part ${failedPart.partNumber}", progressStorage)
                return MultipartUploadResult.Failed(
                    bucketName, objectKey, status.uploadId,
                    "Failed to upload part ${failedPart.partNumber}"
                )
            }

            // 合并分片
            val completedPartsSorted = completedParts.sortedBy { it.partNumber() }
            val completeRequest = CompleteMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .uploadId(status.uploadId)
                .multipartUpload { m -> m.parts(completedPartsSorted) }
                .build()

            val completeResponse = client.completeMultipartUpload(completeRequest)
            val etag = try { completeResponse.eTag() } catch (e: Exception) { "" }

            updateStatus(storageKey, UploadStatusType.COMPLETED, null, progressStorage)

            MultipartUploadResult.Success(
                bucketName, objectKey, status.uploadId,
                etag ?: "", file.length(), parts.size
            )

        } catch (e: Exception) {
            logger.error("Failed to resume upload: $bucketName/$objectKey", e)
            updateStatus(storageKey, UploadStatusType.FAILED, e.message, progressStorage)
            MultipartUploadResult.Failed(bucketName, objectKey, status.uploadId, e.message ?: "Unknown error", e)
        }
    }

    /**
     * 取消分片上传
     *
     * @param client S3 客户端
     * @param bucketName 存储桶名称
     * @param objectKey 对象键
     * @param uploadId 上传 ID
     * @return 是否成功
     */
    fun abortMultipartUpload(
        client: S3Client,
        bucketName: String,
        objectKey: String,
        uploadId: String
    ): Boolean {
        return try {
            val request = AbortMultipartUploadRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .uploadId(uploadId)
                .build()
            client.abortMultipartUpload(request)

            val storageKey = UploadProgressStorage.generateKey(bucketName, objectKey)
            uploadCache.remove(storageKey)
            uploadCache.remove(uploadId)

            logger.info("Aborted multipart upload: $bucketName/$objectKey, uploadId: $uploadId")
            true
        } catch (e: Exception) {
            logger.error("Failed to abort multipart upload: $bucketName/$objectKey", e)
            false
        }
    }

    /**
     * 获取上传状态
     *
     * @param bucketName 存储桶名称
     * @param objectKey 对象键
     * @param progressStorage 进度存储
     * @return 上传状态
     */
    fun getUploadStatus(
        bucketName: String,
        objectKey: String,
        progressStorage: UploadProgressStorage
    ): UploadStatus? {
        val storageKey = UploadProgressStorage.generateKey(bucketName, objectKey)
        return progressStorage.getStatus(storageKey)
            ?: uploadCache[storageKey]
    }

    /**
     * 列出所有未完成的分片上传
     *
     * @param client S3 客户端
     * @param bucketName 存储桶名称
     * @return 未完成的分片上传列表
     */
    fun listMultipartUploads(
        client: S3Client,
        bucketName: String
    ): List<MultipartUpload> {
        return try {
            val request = ListMultipartUploadsRequest.builder()
                .bucket(bucketName)
                .build()
            client.listMultipartUploads(request).uploads()
        } catch (e: Exception) {
            logger.error("Failed to list multipart uploads for $bucketName", e)
            emptyList()
        }
    }

    /**
     * 计算分片数量
     */
    private fun calculatePartsCount(fileSize: Long, partSize: Long): Int {
        return ((fileSize + partSize - 1) / partSize).toInt()
    }

    /**
     * 生成分片信息
     */
    private fun generatePartInfos(fileSize: Long, partSize: Long): List<PartInfo> {
        val partsCount = calculatePartsCount(fileSize, partSize)
        return (1..partsCount).map { partNumber ->
            val start = (partNumber - 1) * partSize
            val end = minOf(start + partSize, fileSize)
            PartInfo(
                partNumber = partNumber,
                start = start,
                end = end,
                size = end - start,
                status = PartStatus.PENDING
            )
        }
    }

    /**
     * 读取分片数据
     */
    private fun readPartData(file: File, start: Long, size: Long): ByteArray {
        RandomAccessFile(file, "r").use { raf ->
            raf.seek(start)
            val buffer = ByteArray(size.toInt())
            raf.readFully(buffer)
            return buffer
        }
    }

    /**
     * 更新上传状态
     */
    private fun updateStatus(
        storageKey: String,
        status: UploadStatusType,
        error: String?,
        progressStorage: UploadProgressStorage?
    ) {
        val currentStatus = progressStorage?.getStatus(storageKey)
            ?: uploadCache[storageKey] ?: return

        val newStatus = currentStatus.copy(
            status = status,
            error = error,
            updatedAt = System.currentTimeMillis()
        )

        uploadCache[storageKey] = newStatus
        progressStorage?.saveStatus(storageKey, newStatus)
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
            name.endsWith(".pdf") -> "application/pdf"
            name.endsWith(".txt") -> "text/plain"
            name.endsWith(".html") -> "text/html"
            name.endsWith(".css") -> "text/css"
            name.endsWith(".js") -> "application/javascript"
            name.endsWith(".json") -> "application/json"
            name.endsWith(".xml") -> "application/xml"
            name.endsWith(".zip") -> "application/zip"
            name.endsWith(".mp4") -> "video/mp4"
            name.endsWith(".mp3") -> "audio/mpeg"
            else -> "application/octet-stream"
        }
    }
}
