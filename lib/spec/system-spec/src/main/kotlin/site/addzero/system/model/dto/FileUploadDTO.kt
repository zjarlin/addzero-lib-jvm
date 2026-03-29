package site.addzero.system.model.dto

import site.addzero.system.enums.StorageType
import site.addzero.system.model.common.PageQuery
import java.time.Instant

/**
 * 文件上传请求
 */
data class FileUploadRequest(
    val filename: String,
    val contentType: String? = null,
    val size: Long? = null,
    val bucket: String = "default",
    val path: String? = null,
    val metadata: Map<String, String> = emptyMap(),
    val uploaderId: String? = null,
    val bizType: String? = null,
    val bizId: String? = null
)

/**
 * 文件存储结果
 */
data class FileStorageResult(
    val fileId: String,
    val originalFilename: String,
    val storagePath: String,
    val storageType: StorageType,
    val size: Long,
    val contentType: String?,
    val storedAt: Instant
)

/**
 * 文件元数据
 */
data class FileMetadata(
    val fileId: String,
    val filename: String,
    val size: Long,
    val contentType: String?,
    val hash: String?,
    val lastModified: Instant,
    val customMetadata: Map<String, String> = emptyMap()
)

/**
 * 文件记录DTO
 */
data class FileRecordDTO(
    val id: String,
    val fileId: String,
    val originalFilename: String,
    val storagePath: String,
    val storageType: StorageType,
    val bucket: String,
    val size: Long,
    val contentType: String?,
    val hash: String?,
    val uploaderId: String?,
    val bizType: String?,
    val bizId: String?,
    val status: FileStatus,
    val createdAt: Instant,
    val updatedAt: Instant
)

enum class FileStatus {
    UPLOADING,   // 上传中
    ACTIVE,      // 正常
    EXPIRED,     // 已过期
    DELETED      // 已删除
}

data class FileRecordCreateRequest(
    val fileId: String,
    val originalFilename: String,
    val storagePath: String,
    val storageType: StorageType,
    val bucket: String,
    val size: Long,
    val contentType: String? = null,
    val hash: String? = null,
    val uploaderId: String? = null,
    val bizType: String? = null,
    val bizId: String? = null
)

data class FileRecordUpdateRequest(
    val originalFilename: String? = null,
    val bizType: String? = null,
    val bizId: String? = null,
    val status: FileStatus? = null
)

data class FileRecordQuery(
    override val pageNum: Int = 1,
    override val pageSize: Int = 10,
    val keyword: String? = null,
    val bucket: String? = null,
    val bizType: String? = null,
    val bizId: String? = null,
    val uploaderId: String? = null,
    val status: FileStatus? = null,
    val startTime: Instant? = null,
    val endTime: Instant? = null
) : PageQuery(pageNum, pageSize)

data class StorageStats(
    val bucket: String,
    val totalFiles: Long,
    val totalSize: Long,
    val avgFileSize: Long
)
