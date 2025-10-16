package site.addzero.app

/**
 * 参数处理工具类
 */
internal object ParamUtils {

    /**
     * 尝试将值转换为期望的类型
     */
    @Suppress("UNCHECKED_CAST")
    fun convertValueIfNeeded(value: Any?, expectedType: Any?): Any? {
        if (value == null) return null

        return when (expectedType) {
            String::class -> value.toString()
            Int::class -> when (value) {
                is Int -> value
                is String -> value.toIntOrNull() ?: throw IllegalArgumentException("无法将 '$value' 转换为 Int")
                is Number -> value.toInt()
                else -> throw IllegalArgumentException("无法将 ${value::class.simpleName} 转换为 Int")
            }
            Double::class -> when (value) {
                is Double -> value
                is String -> value.toDoubleOrNull() ?: throw IllegalArgumentException("无法将 '$value' 转换为 Double")
                is Number -> value.toDouble()
                else -> throw IllegalArgumentException("无法将 ${value::class.simpleName} 转换为 Double")
            }
            Boolean::class -> when (value) {
                is Boolean -> value
                is String -> when (value.lowercase()) {
                    "y", "yes", "true" -> true
                    "n", "no", "false" -> false
                    else -> throw IllegalArgumentException("无法将 '$value' 转换为 Boolean")
                }
                else -> throw IllegalArgumentException("无法将 ${value::class.simpleName} 转换为 Boolean")
            }
            else -> value // 对于其他类型，直接返回原值
        }
    }
}
