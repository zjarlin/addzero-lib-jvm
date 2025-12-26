package site.addzero.rustfs

/**
 * RustFS 操作结果
 */
sealed class RustfsResult {
    /** 操作成功 */
    data class Success(
        val message: String,
        val data: Map<String, Any?> = emptyMap()
    ) : RustfsResult()

    /** 操作失败 */
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : RustfsResult()

    /** 操作进行中（用于异步操作或断点续传） */
    data class InProgress(
        val message: String,
        val progress: UploadProgress,
        val uploadId: String? = null
    ) : RustfsResult()
}

/**
 * 上传进度信息
 */
data class UploadProgress(
    /** 总字节数 */
    val totalBytes: Long,

    /** 已上传字节数 */
    val uploadedBytes: Long,

    /** 进度百分比 (0-100) */
    val percent: Double,

    /** 当前分片号（分片上传时有效） */
    val currentPart: Int? = null,

    /** 总分片数（分片上传时有效） */
    val totalParts: Int? = null,

    /** 速度（字节/秒） */
    val speed: Long? = null,

    /** 预计剩余时间（秒） */
    val remainingSeconds: Long? = null
) {
    /** 是否完成 */
    val isComplete: Boolean
        get() = percent >= 100.0

    /** 格式化的进度字符串 */
    val formatted: String
        get() = buildString {
            append(String.format("%.2f%%", percent))
            if (currentPart != null && totalParts != null) {
                append(" ($currentPart/$totalParts)")
            }
            append(" (${formatBytes(uploadedBytes)}/${formatBytes(totalBytes)})")
            speed?.let {
                append(" @ ${formatBytes(it)}/s")
            }
            remainingSeconds?.let {
                append(" ${formatSeconds(it)} remaining")
            }
        }

    companion object {
        fun formatBytes(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> String.format("%.2f KB", bytes / 1024.0)
                bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024))
                else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
            }
        }

        fun formatSeconds(seconds: Long): String {
            return when {
                seconds < 60 -> "${seconds}s"
                seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
                else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
            }
        }
    }
}
