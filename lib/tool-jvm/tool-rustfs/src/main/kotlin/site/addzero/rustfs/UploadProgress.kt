package site.addzero.rustfs

import java.io.Serializable

/**
 * 上传进度回调接口
 */
fun interface UploadProgressListener {
    /**
     * 上传进度回调
     * @param progress 进度数据
     */
    fun onProgress(progress: UploadProgressData)
}

/**
 * 上传进度数据
 */
data class UploadProgressData(
    /** 已上传字节数 */
    val uploaded: Long,

    /** 总字节数 */
    val total: Long,

    /** 进度百分比 (0-100) */
    val percent: Double,

    /** 当前分片号（分片上传时有效） */
    val partNumber: Int? = null,

    /** 总分片数（分片上传时有效） */
    val totalParts: Int? = null
)

/**
 * 分片上传配置
 */
data class MultipartUploadConfig(
    /** 分片大小（字节），默认 5MB */
    val partSize: Long = DEFAULT_PART_SIZE,

    /** 并发上传数，默认 3 */
    val concurrency: Int = DEFAULT_CONCURRENCY,

    /** 重试次数，默认 3 */
    val maxRetries: Int = DEFAULT_MAX_RETRIES,

    /** 超时时间（秒），默认 30 */
    val timeoutSeconds: Int = DEFAULT_TIMEOUT_SECONDS,

    /** 进度监听器 */
    val progressListener: UploadProgressListener? = null
) {
    companion object {
        const val DEFAULT_PART_SIZE = 5 * 1024 * 1024L  // 5MB
        const val DEFAULT_CONCURRENCY = 3
        const val DEFAULT_MAX_RETRIES = 3
        const val DEFAULT_TIMEOUT_SECONDS = 30

        fun default() = MultipartUploadConfig()
    }
}

/**
 * 分片信息
 */
data class PartInfo(
    val partNumber: Int,
    val start: Long,
    val end: Long,
    val size: Long,
    val etag: String? = null,
    val status: PartStatus = PartStatus.PENDING
) : Serializable

/**
 * 分片状态
 */
enum class PartStatus {
    PENDING,    // 待上传
    UPLOADING,  // 上传中
    COMPLETED,  // 已完成
    FAILED      // 失败
}

/**
 * 上传状态信息
 */
data class UploadStatus(
    val uploadId: String,
    val bucketName: String,
    val objectKey: String,

    /** 文件总大小 */
    val fileSize: Long,

    /** 已上传字节数 */
    val uploadedSize: Long,

    /** 进度百分比 (0-100) */
    val progress: Double,

    /** 分片信息列表 */
    val parts: List<PartInfo>,

    /** 状态 */
    val status: UploadStatusType,

    /** 错误信息（如果有） */
    val error: String? = null,

    /** 创建时间 */
    val createdAt: Long = System.currentTimeMillis(),

    /** 更新时间 */
    val updatedAt: Long = System.currentTimeMillis()
) : Serializable {
    /** 计算进度百分比 */
    fun calculateProgress(): Double {
        if (fileSize <= 0) return 0.0
        return (uploadedSize.toDouble() / fileSize * 100).coerceAtMost(100.0)
    }
}

/**
 * 上传状态类型
 */
enum class UploadStatusType {
    INITIALIZED,    // 已初始化
    IN_PROGRESS,    // 上传中
    COMPLETED,      // 已完成
    FAILED,         // 失败
    CANCELLED       // 已取消
}

/**
 * 分片上传结果
 */
sealed class MultipartUploadResult {
    /** 上传成功 */
    data class Success(
        val bucketName: String,
        val objectKey: String,
        val uploadId: String,
        val etag: String,
        val fileSize: Long,
        val partsCount: Int
    ) : MultipartUploadResult()

    /** 上传失败 */
    data class Failed(
        val bucketName: String,
        val objectKey: String,
        val uploadId: String?,
        val error: String,
        val cause: Throwable? = null
    ) : MultipartUploadResult()

    /** 上传中（断点续传用） */
    data class InProgress(
        val uploadId: String,
        val status: UploadStatus
    ) : MultipartUploadResult()
}

/**
 * 进度存储接口（用于 Redis 或其他存储）
 */
interface UploadProgressStorage {
    /**
     * 保存上传状态
     */
    fun saveStatus(key: String, status: UploadStatus): Boolean

    /**
     * 获取上传状态
     */
    fun getStatus(key: String): UploadStatus?

    /**
     * 删除上传状态
     */
    fun deleteStatus(key: String): Boolean

    /**
     * 更新分片状态
     */
    fun updatePartStatus(key: String, partNumber: Int, status: PartStatus, etag: String? = null): Boolean

    /**
     * 更新已上传大小
     */
    fun updateUploadedSize(key: String, uploadedSize: Long): Boolean

    companion object {
        /**
         * 生成存储 key
         */
        fun generateKey(bucketName: String, objectKey: String): String {
            return "upload:progress:$bucketName:$objectKey"
        }

        /**
         * 生成 uploadId 对应的 key
         */
        fun generateKey(uploadId: String): String {
            return "upload:progress:id:$uploadId"
        }
    }
}

/**
 * Redis 进度存储实现
 */
class RedisUploadProgressStorage(
    private val redisTemplate: RedisTemplate
) : UploadProgressStorage {

    override fun saveStatus(key: String, status: UploadStatus): Boolean {
        return try {
            redisTemplate.opsForValue().set(key, status, TTL_SECONDS)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun getStatus(key: String): UploadStatus? {
        return try {
            redisTemplate.opsForValue().get(key) as? UploadStatus
        } catch (e: Exception) {
            null
        }
    }

    override fun deleteStatus(key: String): Boolean {
        return try {
            redisTemplate.delete(key)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun updatePartStatus(key: String, partNumber: Int, status: PartStatus, etag: String?): Boolean {
        return try {
            val currentStatus = getStatus(key) ?: return false
            val updatedParts = currentStatus.parts.map { part ->
                if (part.partNumber == partNumber) {
                    part.copy(status = status, etag = etag ?: part.etag)
                } else {
                    part
                }
            }
            val newStatus = currentStatus.copy(
                parts = updatedParts,
                uploadedSize = updatedParts.filter { it.status == PartStatus.COMPLETED }
                    .sumOf { it.size },
                updatedAt = System.currentTimeMillis()
            )
            saveStatus(key, newStatus)
        } catch (e: Exception) {
            false
        }
    }

    override fun updateUploadedSize(key: String, uploadedSize: Long): Boolean {
        return try {
            val currentStatus = getStatus(key) ?: return false
            val newStatus = currentStatus.copy(
                uploadedSize = uploadedSize,
                progress = (uploadedSize.toDouble() / currentStatus.fileSize * 100).coerceAtMost(100.0),
                updatedAt = System.currentTimeMillis()
            )
            saveStatus(key, newStatus)
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        const val TTL_SECONDS = 86400L // 24小时
    }
}

/**
 * Redis 操作接口抽象（避免强依赖 Spring）
 */
interface RedisTemplate {
    fun opsForValue(): ValueOperations
    fun delete(key: String): Boolean

    interface ValueOperations {
        fun set(key: String, value: Any, timeout: Long): Boolean?
        fun get(key: String): Any?
    }
}

/**
 * Caffeine 缓存进度存储实现（默认实现，高性能本地缓存）
 */
class CaffeineUploadProgressStorage(
    /** 缓存最大条目数，默认 1000 */
    private val maximumSize: Long = DEFAULT_MAXIMUM_SIZE,

    /** 写入后过期时间（秒），默认 24 小时 */
    private val expireAfterWriteSeconds: Long = DEFAULT_EXPIRE_AFTER_WRITE_SECONDS
) : UploadProgressStorage {

    private val cache = com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
        .maximumSize(maximumSize)
        .expireAfterWrite(java.time.Duration.ofSeconds(expireAfterWriteSeconds))
        .build<String, UploadStatus>()

    override fun saveStatus(key: String, status: UploadStatus): Boolean {
        return try {
            cache.put(key, status)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun getStatus(key: String): UploadStatus? {
        return try {
            cache.getIfPresent(key)
        } catch (e: Exception) {
            null
        }
    }

    override fun deleteStatus(key: String): Boolean {
        return try {
            cache.invalidate(key)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun updatePartStatus(key: String, partNumber: Int, status: PartStatus, etag: String?): Boolean {
        return try {
            val currentStatus = cache.getIfPresent(key) ?: return false
            val updatedParts = currentStatus.parts.map { part ->
                if (part.partNumber == partNumber) {
                    part.copy(status = status, etag = etag ?: part.etag)
                } else {
                    part
                }
            }
            val newStatus = currentStatus.copy(
                parts = updatedParts,
                uploadedSize = updatedParts.filter { it.status == PartStatus.COMPLETED }
                    .sumOf { it.size },
                updatedAt = System.currentTimeMillis()
            )
            cache.put(key, newStatus)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun updateUploadedSize(key: String, uploadedSize: Long): Boolean {
        return try {
            val currentStatus = cache.getIfPresent(key) ?: return false
            val newStatus = currentStatus.copy(
                uploadedSize = uploadedSize,
                progress = (uploadedSize.toDouble() / currentStatus.fileSize * 100).coerceAtMost(100.0),
                updatedAt = System.currentTimeMillis()
            )
            cache.put(key, newStatus)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取所有缓存的上传状态
     */
    fun getAllStatus(): Map<String, UploadStatus> {
        return cache.asMap()
    }

    /**
     * 根据上传 ID 获取状态
     */
    fun getStatusByUploadId(uploadId: String): UploadStatus? {
        return cache.asMap().values.find { it.uploadId == uploadId }
    }

    /**
     * 清空所有缓存
     */
    fun clear() {
        cache.invalidateAll()
    }

    /**
     * 获取缓存统计信息
     */
    fun getStats(): com.github.benmanes.caffeine.cache.stats.CacheStats? {
        return cache.stats()
    }

    companion object {
        const val DEFAULT_MAXIMUM_SIZE = 1000L
        const val DEFAULT_EXPIRE_AFTER_WRITE_SECONDS = 86400L // 24小时

        /**
         * 创建默认配置的实例
         */
        fun create(): CaffeineUploadProgressStorage {
            return CaffeineUploadProgressStorage()
        }
    }
}

/**
 * 内存进度存储实现（用于测试或无 Caffeine 环境）
 */
class InMemoryUploadProgressStorage : UploadProgressStorage {
    private val storage = mutableMapOf<String, UploadStatus>()

    @Synchronized
    override fun saveStatus(key: String, status: UploadStatus): Boolean {
        storage[key] = status
        return true
    }

    @Synchronized
    override fun getStatus(key: String): UploadStatus? {
        return storage[key]
    }

    @Synchronized
    override fun deleteStatus(key: String): Boolean {
        return storage.remove(key) != null
    }

    @Synchronized
    override fun updatePartStatus(key: String, partNumber: Int, status: PartStatus, etag: String?): Boolean {
        val currentStatus = storage[key] ?: return false
        val updatedParts = currentStatus.parts.map { part ->
            if (part.partNumber == partNumber) {
                part.copy(status = status, etag = etag ?: part.etag)
            } else {
                part
            }
        }
        val newStatus = currentStatus.copy(
            parts = updatedParts,
            uploadedSize = updatedParts.filter { it.status == PartStatus.COMPLETED }
                .sumOf { it.size },
            updatedAt = System.currentTimeMillis()
        )
        storage[key] = newStatus
        return true
    }

    @Synchronized
    override fun updateUploadedSize(key: String, uploadedSize: Long): Boolean {
        val currentStatus = storage[key] ?: return false
        val newStatus = currentStatus.copy(
            uploadedSize = uploadedSize,
            progress = (uploadedSize.toDouble() / currentStatus.fileSize * 100).coerceAtMost(100.0),
            updatedAt = System.currentTimeMillis()
        )
        storage[key] = newStatus
        return true
    }

    fun clear() {
        storage.clear()
    }
}
