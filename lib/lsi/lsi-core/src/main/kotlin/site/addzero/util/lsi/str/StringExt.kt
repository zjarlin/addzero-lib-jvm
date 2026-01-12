package site.addzero.util.lsi.str

/**
 * 字符串扩展函数工具类
 */

/**
 * 将字符串转换为下划线命名格式（小写）
 * 例如：userName -> user_name
 * firstName -> first_name
 * userID -> user_id
 */
@Deprecated(message = "",replaceWith = ReplaceWith("site.addzero.util.str.toUnderlineLowerCase()"))
fun String.toSnakeCaseLowerCase(): String {
    if (this.isBlank()) return this

    return this.mapIndexed { index, c ->
        when {
            c.isUpperCase() && index > 0 -> "_${c.lowercase()}"
            else -> c.lowercase()
        }
    }.joinToString("")
}