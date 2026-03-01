package site.addzero.starter.statuspages

import kotlinx.serialization.Serializable

/** 错误响应 */
@Serializable
data class ErrorResponse(
    val code: Int,
    val message: String,
)
