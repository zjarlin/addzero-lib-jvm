package site.addzero.common.models.result

import kotlinx.serialization.Serializable
import kotlin.math.round

/**
 * 通用的操作结果类型
 *
 * @param T 成功时携带的数据类型
 */
@Serializable
sealed class Result<out T> {
  /**
   * 操作成功
   * @param message 成功消息
   * @param data 成功时携带的数据
   */
   @Serializable
   data class Success<T>(
     val code: String,
     val message: String? = null,
     val data: T? = null,
   ) : Result<T>()

  /**
   * 操作失败
   * @param message 错误消息
   */
   @Serializable
   data class Error<T>(
     val code: String,
     val message: String? = null,
   ) : Result<T>()

  /**
   * 操作进行中（用于异步操作或断点续传）
   * @param message 进行中消息
   * @param progress 进度信息
   * @param extra 额外信息（如 uploadId）
   */
   @Serializable
   data class InProgress<T>(
     val code: String = "202",
     val message: String? = null,
     val progress: Progress? = null,
     val extra: String? = null,
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
     is Success -> Success(code, message, transform(this.data))
     is Error -> Error(code, message)
     is InProgress -> InProgress(code, message, progress, extra)
   }

  /**
   * 映射消息
   */
   fun mapMessage(transform: (String) -> String): Result<T> = when (this) {
     is Success -> Success(code, message?.let(transform), this.data)
     is Error -> Error(code, message?.let(transform))
     is InProgress -> InProgress(code, message?.let(transform), progress, extra)
   }

  companion object {
    /**
     * 创建成功的 Result
     */
     fun <T> success(message: String? = null, data: T? = null, code: String = "200"): Result<T> = Success(code, message, data)

    /**
     * 创建失败的 Result
     */
     fun <T> error(message: String? = null, code: String = "400"): Result<T> = Error(code, message)

    /**
     * 创建进行中的 Result
     */
     fun <T> inProgress(message: String? = null, progress: Progress? = null, extra: String? = null, code: String = "202"): Result<T> =
       InProgress(code, message, progress, extra)

    /**
     * 捕获异常并返回 Result
     */
     inline fun <T> catch(message: String? = null, block: () -> T): Result<T> = try {
       success(message, block())
     } catch (e: Exception) {
       error(message)
     }
  }
}

/**
 * 进度信息
 */
@Serializable
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
  val remainingSeconds: Long? = null,
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
