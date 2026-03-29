package site.addzero.system.model.common

/**
 * 通用分页结果
 */
data class PageResult<T>(
    val list: List<T>,
    val total: Long,
    val pageNum: Int,
    val pageSize: Int
) {
    companion object {
        fun <T> empty(pageNum: Int = 1, pageSize: Int = 10): PageResult<T> {
            return PageResult(emptyList(), 0, pageNum, pageSize)
        }
    }
}
