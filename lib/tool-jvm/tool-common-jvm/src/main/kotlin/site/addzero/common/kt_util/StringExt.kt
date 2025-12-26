package site.addzero.common.kt_util

/**
 * 检查字符串是否包含任意指定的子串（忽略大小写）
 */
fun String.containsAnyIgnoreCase(vararg substrings: String): Boolean {
    val lower = this.lowercase()
    return substrings.any { lower.contains(it.lowercase()) }
}

/**
 * 检查字符串是否包含任意指定的字符
 */
fun CharSequence.containsAny(vararg chars: Char): Boolean {
    return chars.any { it in this }
}

/**
 * 检查字符串是否包含任意指定的子串
 */
fun String.containsAny(vararg substrings: String): Boolean {
    return substrings.any { this.contains(it) }
}
