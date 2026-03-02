package site.addzero.util.bool

/**
 * 布尔值加法操作符，用于权重计算
 * 将布尔值视为 0 和 1 进行加法运算
 */
operator fun Boolean.plus(other: Boolean): Int = this.toInt() + other.toInt()

/**
 * 布尔值转整数
 */
fun Boolean.toInt(): Int = if (this) 1 else 0

/**
 * 整数与布尔值加法操作符
 */
operator fun Int.plus(boolean: Boolean): Int = this + boolean.toInt()
