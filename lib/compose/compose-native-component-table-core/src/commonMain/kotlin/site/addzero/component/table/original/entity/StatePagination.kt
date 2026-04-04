package site.addzero.component.table.original.entity

/**
 * 分页状态。
 */
data class StatePagination(
    var currentPage: Int = 1,
    var pageSize: Int = 10,
    var totalItems: Int = 0,
) {
    val totalPages get() = if (totalItems == 0) 1 else (totalItems + pageSize - 1) / pageSize
    val hasPreviousPage get() = currentPage > 1
    val hasNextPage get() = currentPage < totalPages
    val startItem get() = (currentPage - 1) * pageSize + 1
    val endItem get() = minOf(currentPage * pageSize, totalItems)
}
