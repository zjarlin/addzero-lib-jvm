package site.addzero.rustfs.api

/**
 * 简单结果封装类（临时实现，避免依赖问题）
 */
class SimpleResult(
    val success: Boolean,
    val message: String,
    val error: Throwable? = null
) {
    companion object {
        fun success(message: String = "Success"): SimpleResult =
            SimpleResult(true, message)

        fun failure(message: String, error: Throwable? = null): SimpleResult =
            SimpleResult(false, message, error)

        inline fun catch(errorMessage: String, block: () -> String): SimpleResult {
            return try {
                val result = block()
                SimpleResult(true, result)
            } catch (e: Exception) {
                SimpleResult(false, "$errorMessage: ${e.message}", e)
            }
        }

        inline fun <T> catchResult(errorMessage: String, block: () -> T): Result<T> {
            return try {
                Result.success(block())
            } catch (e: Exception) {
                Result.failure("$errorMessage: ${e.message}", e)
            }
        }
    }

    fun isSuccess(): Boolean = success
    fun isFailure(): Boolean = !success
}

/**
 * 带数据的结果类
 */
class Result<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String = "",
    val error: Throwable? = null
) {
    companion object {
        fun <T> success(data: T, message: String = "Success"): Result<T> =
            Result(true, data, message)

        fun <T> failure(message: String, error: Throwable? = null): Result<T> =
            Result(false, null, message, error)

        inline fun <T> catch(message: String = "", block: () -> T): Result<T> {
            return try {
                success(block())
            } catch (e: Exception) {
                failure(message.ifEmpty { e.message ?: "Unknown error" }, e)
            }
        }
    }

    fun getOrNull(): T? = data
    fun getOrThrow(): T = data ?: throw error ?: RuntimeException(message)
    fun isSuccess(): Boolean = success
    fun isFailure(): Boolean = !success

    fun <R> map(transform: (T) -> R): Result<R> {
        return if (success && data != null) {
            success(transform(data))
        } else {
            Result(false, null, message, error)
        }
    }
}
