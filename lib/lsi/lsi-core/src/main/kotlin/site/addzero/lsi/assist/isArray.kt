package site.addzero.lsi.assist

/**
 * 判断字符串是否为数组类型
 *
 * 注意：此扩展函数与 TypeChecker.kt 中的实现保持一致
 */
fun String.isArray(): Boolean {
    return this.startsWith("Array<") || this.endsWith("[]")
}
