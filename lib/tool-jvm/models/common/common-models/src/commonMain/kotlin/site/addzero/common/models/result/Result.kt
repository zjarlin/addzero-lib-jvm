package site.addzero.common.models.result

import kotlin.math.round

/**
 * 通用的操作结果类型
 *
 * @param T 成功时携带的数据类型
 */
sealed class Result<out T> {
    /**
     * 操作成功
     * @param message 成功消息
     * @param data 成功时携带的数据
     */
    data class Success<T>(
        val message: String,
        val data: T? = null,
        val code: Int = 200
    ) : Result<T>()

    /**
     * 操作失败
     * @param message 错误消息
     * @param cause 异常原因
     */
    data class Error<T>(
        val message: String,
        val cause: Throwable? = null,
        val code: Int = 400
    ) : Result<T>()

    /**
     * 操作进行中（用于异步操作或断点续传）
     * @param message 进行中消息
     * @param progress 进度信息
     * @param extra 额外信息（如 uploadId）
     */
    data class InProgress<T>(
        val message: String,
        val progress: Progress? = null,
        val extra: String? = null,
        val code: Int = 202
    ) : Result<T>()

    /**
     * 是否成功
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * 是否失败
     */
    val isError: Boolean
        get() = this is Error

    /**
     * 是否进行中
     */
    val isInProgress: Boolean
        get() = this is InProgress

    /**
     * 映射成功时的数据
     */
    inline fun <R> mapData(transform: (T?) -> R?): Result<R> = when (this) {
        is Success -> Success(message, transform(this.data), code)
        is Error -> Error(message, this.cause, code)
        is InProgress -> InProgress(message, progress, extra, code)
    }

    /**
     * 映射消息
     */
    fun mapMessage(transform: (String) -> String): Result<T> = when (this) {
        is Success -> Success(transform(message), this.data, code)
        is Error -> Error(transform(message), this.cause, code)
        is InProgress -> InProgress(transform(message), progress, extra, code)
    }

    companion object {
        /**
         * 创建成功的 Result
         */
        fun <T> success(message: String, data: T? = null, code: Int = 200): Result<T> = Success(message, data, code)

        /**
         * 创建失败的 Result
         */
        fun <T> error(message: String, cause: Throwable? = null, code: Int = 400): Result<T> = Error(message, cause, code)

        /**
         * 创建进行中的 Result
         */
        fun <T> inProgress(message: String, progress: Progress? = null, extra: String? = null, code: Int = 202): Result<T> =
            InProgress(message, progress, extra, code)

        /**
         * 捕获异常并返回 Result
         */
        inline fun <T> catch(message: String, block: () -> T): Result<T> = try {
            success(message, block())
        } catch (e: Exception) {
            error(message, e)
        }
    }
}

/**
 * 进度信息
 */
data class Progress(
    /** 总字节数 */
    val totalBytes: Long,

    /** 已完成字节数 */
    val completedBytes: Long,

    /** 进度百分比 (0-100) */
    val percent: Double,

    /** 当前项目（如当前分片号） */
    val current: Int? = null,

    /** 总项目数（如总分片数） */
    val total: Int? = null,

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
            append("${round(percent * 100) / 100}%")
            if (current != null && total != null) {
                append(" ($current/$total)")
            }
            append(" (${formatBytes(completedBytes)}/${formatBytes(totalBytes)})")
            speed?.let {
                append(" @ ${formatBytes(it)}/s")
            }
            remainingSeconds?.let {
                append(" ${formatSeconds(it)} remaining")
            }
        }

    companion object {
        fun formatBytes(bytes: Long): String {
            val value = when {
                bytes < 1024 -> bytes.toDouble()
                bytes < 1024 * 1024 -> bytes / 1024.0
                bytes < 1024L * 1024 * 1024 -> bytes / (1024.0 * 1024)
                else -> bytes / (1024.0 * 1024 * 1024)
            }
            val unit = when {
                bytes < 1024 -> "B"
                bytes < 1024 * 1024 -> "KB"
                bytes < 1024L * 1024 * 1024 -> "MB"
                else -> "GB"
            }
            val formattedValue = if (value == value.toLong().toDouble()) value.toLong().toString() else (round(value * 100) / 100).toString()
            return "$formattedValue $unit"
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

/**
 * 无数据的 Result 类型别名
 */
typealias SimpleResult = Result<Unit>
