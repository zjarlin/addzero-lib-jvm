package site.addzero.kcloud.db

import site.addzero.kcloud.model.ConflictStrategy
import kotlinx.coroutines.flow.Flow

/**
 * 文件同步状态
 */
enum class SyncState {
    SYNCED,         // 已同步
    PENDING_UPLOAD, // 等待上传
    PENDING_DOWNLOAD, // 等待下载
    CONFLICT,       // 冲突
    ERROR           // 错误
}

/**
 * 同步操作类型
 */
enum class SyncOperation {
    UPLOAD,
    DOWNLOAD,
    DELETE_LOCAL,
    DELETE_REMOTE
}

/**
 * 同步队列状态
 */
enum class QueueStatus {
    PENDING,
    RUNNING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * 文件记录 - 数据库实体
 */
data class FileRecord(
    val id: Long = 0,
    val path: String,                    // 相对路径（唯一）
    val localMtime: Long?,               // 本地修改时间
    val localSize: Long?,                // 本地大小
    val localHash: String?,              // 本地内容哈希（SHA-256）
    val remoteEtag: String?,             // 远程ETag
    val remoteVersionId: String?,        // S3版本ID
    val remoteMtime: Long?,              // 远程修改时间
    val remoteSize: Long?,               // 远程大小
    val syncState: SyncState,            // 同步状态
    val lastSyncTime: Long?,             // 上次成功同步时间
    val conflictStrategy: ConflictStrategy? // 冲突解决策略
)

/**
 * 同步队列项
 */
data class SyncQueueItem(
    val id: Long = 0,
    val fileId: Long,                    // 关联的文件ID
    val operation: SyncOperation,        // 操作类型
    val status: QueueStatus,             // 队列状态
    val progressBytes: Long,             // 已传输字节（断点续传）
    val totalBytes: Long,                // 总字节数
    val retryCount: Int,                 // 重试次数
    val errorMessage: String?,           // 错误信息
    val createdAt: Long,                 // 创建时间
    val updatedAt: Long                  // 更新时间
)

/**
 * 文件变更类型（用于扫描结果）
 */
sealed class FileChange {
    abstract val path: String

    data class NewLocal(override val path: String, val size: Long, val mtime: Long) : FileChange()
    data class ModifiedLocal(override val path: String, val size: Long, val mtime: Long, val oldHash: String?) : FileChange()
    data class DeletedLocal(override val path: String) : FileChange()
    data class NewRemote(
        override val path: String,
        val size: Long,
        val etag: String,
        val mtime: Long
    ) : FileChange()

    data class ModifiedRemote(
        override val path: String,
        val size: Long,
        val etag: String,
        val oldEtag: String?,
        val mtime: Long
    ) : FileChange()
    data class DeletedRemote(override val path: String) : FileChange()
    data class Conflict(
        override val path: String,
        val localMtime: Long,
        val remoteMtime: Long,
        val localSize: Long,
        val remoteSize: Long
    ) : FileChange()
}

/**
 * 数据库接口 - 定义所有数据库操作
 */
interface Database {

    // ========== 初始化 ==========
    fun initialize()
    fun close()

    // ========== 文件记录操作 ==========

    /**
     * 获取或创建文件记录
     */
    fun getOrCreateFileRecord(path: String): FileRecord

    /**
     * 根据路径获取文件记录
     */
    fun getFileRecord(path: String): FileRecord?

    /**
     * 获取所有文件记录
     */
    fun getAllFileRecords(): List<FileRecord>

    /**
     * 获取指定状态的文件记录
     */
    fun getFileRecordsByState(state: SyncState): List<FileRecord>

    /**
     * 更新本地文件信息
     */
    fun updateLocalInfo(
        path: String,
        mtime: Long,
        size: Long,
        hash: String?
    ): Boolean

    /**
     * 更新远程文件信息
     */
    fun updateRemoteInfo(
        path: String,
        etag: String,
        versionId: String?,
        mtime: Long?,
        size: Long?
    ): Boolean

    /**
     * 更新同步状态
     */
    fun updateSyncState(path: String, state: SyncState, lastSyncTime: Long? = null): Boolean

    /**
     * 设置冲突解决策略
     */
    fun setConflictStrategy(path: String, strategy: ConflictStrategy): Boolean

    /**
     * 删除文件记录
     */
    fun deleteFileRecord(path: String): Boolean

    /**
     * 标记冲突已解决
     */
    fun resolveConflict(path: String, resolution: ConflictResolution): Boolean

    // ========== 同步队列操作 ==========

    /**
     * 添加同步任务到队列
     */
    fun enqueueSync(
        path: String,
        operation: SyncOperation,
        totalBytes: Long
    ): Long

    /**
     * 获取队列中的所有任务
     */
    fun getQueueItems(): List<SyncQueueItem>

    /**
     * 获取待处理的任务
     */
    fun getPendingQueueItems(limit: Int = 10): List<SyncQueueItem>

    /**
     * 获取正在运行的任务
     */
    fun getRunningQueueItems(): List<SyncQueueItem>

    /**
     * 更新任务状态
     */
    fun updateQueueStatus(
        queueId: Long,
        status: QueueStatus,
        progressBytes: Long? = null,
        errorMessage: String? = null
    ): Boolean

    /**
     * 增加重试次数
     */
    fun incrementRetryCount(queueId: Long): Boolean

    /**
     * 完成任务
     */
    fun completeQueueItem(queueId: Long): Boolean

    /**
     * 删除队列项
     */
    fun deleteQueueItem(queueId: Long): Boolean

    /**
     * 清空已完成/失败的队列项
     */
    fun clearCompletedQueue(): Int

    // ========== 统计查询 ==========

    /**
     * 获取统计信息
     */
    fun getStats(): DatabaseStats

    /**
     * 监听文件记录变化（用于UI实时更新）
     */
    fun observeFileRecords(): Flow<List<FileRecord>>

    /**
     * 监听队列变化
     */
    fun observeQueue(): Flow<List<SyncQueueItem>>
}

/**
 * 冲突解决结果
 */
enum class ConflictResolution {
    USE_LOCAL,
    USE_REMOTE,
    KEEP_BOTH,
    MERGE           // 尝试合并（文本文件）
}

/**
 * 数据库统计信息
 */
data class DatabaseStats(
    val totalFiles: Int,
    val syncedFiles: Int,
    val pendingUploads: Int,
    val pendingDownloads: Int,
    val conflicts: Int,
    val queuePending: Int,
    val queueRunning: Int,
    val queueFailed: Int
)

/**
 * 数据库异常
 */
class DatabaseException(message: String, cause: Throwable? = null) : Exception(message, cause)
