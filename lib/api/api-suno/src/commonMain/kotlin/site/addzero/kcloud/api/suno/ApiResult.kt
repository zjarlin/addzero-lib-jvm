package site.addzero.kcloud.api.suno

import kotlinx.serialization.Serializable

/**
 * 后端统一响应包装，对应 site.addzero.common.models.result.Result
 */
@Serializable
data class ApiResult<T>(
    val code: Int = 0,
    val msg: String? = null,
    val data: T? = null,
) {
    val isSuccess get() = code == 200 || code == 0

    fun getOrThrow(): T {
        if (data != null) return data
        throw RuntimeException(msg ?: "API 返回空数据 (code=$code)")
    }

    fun getOrNull(): T? = data
}
