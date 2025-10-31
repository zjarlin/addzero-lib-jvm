package site.addzero.entity

import site.addzero.enums.ErrorEnum


@Deprecated(
    message = "Res is deprecated, use ProblemDetail instead",
    replaceWith = ReplaceWith("ProblemDetail", "site.addzero.entity.ProblemDetail")
)

data class Res<T>(
    val code: Int = 200,
    val message: String = "请求成功",
    val data: T? = null
) {
    companion object {
        // 成功响应
        fun <T> success(data: T?) = Res(data = data)
        fun <T> success(message: String, data: T?) = Res(message = message, data = data)
        fun success(message: String) = Res<String>(message = message)

        // 错误响应
        fun fail(message: String?) = Res<String>(400, message ?: "error")
        fun fail(code: Int, message: String) = Res<Nothing>(code, message)
        fun fail(errorEnum: ErrorEnum) = Res<Nothing>(errorEnum.code, errorEnum.msg)
        fun unauthorized(message: String) = Res<Nothing>(401, message)
        fun forbidden(message: String) = Res<Nothing>(403, message)
    }
}
fun ErrorEnum.fail() = Res.fail(this)
