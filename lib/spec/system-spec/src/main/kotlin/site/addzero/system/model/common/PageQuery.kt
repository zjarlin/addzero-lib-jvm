package site.addzero.system.model.common

/**
 * 通用分页查询参数
 */
open class PageQuery(
    open val pageNum: Int = 1,
    open val pageSize: Int = 10
) {
    fun offset(): Long = ((pageNum - 1).coerceAtLeast(0) * pageSize).toLong()
    fun limit(): Int = pageSize.coerceIn(1, 1000)
}
