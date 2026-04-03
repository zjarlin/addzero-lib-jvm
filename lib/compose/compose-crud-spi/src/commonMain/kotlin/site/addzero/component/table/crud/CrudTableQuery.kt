package site.addzero.component.table.crud

import site.addzero.component.table.original.entity.StatePagination
import site.addzero.entity.low_table.StateSearch
import site.addzero.entity.low_table.StateSort

/**
 * 服务端列表查询条件。
 *
 * 这一层对齐 TanStack Table + Query 常见的查询状态：
 * 关键字、筛选、排序、分页统一收口成一个不可变对象。
 */
data class CrudTableQuery(
    val keyword: String = "",
    val filters: Set<StateSearch> = emptySet(),
    val sorts: Set<StateSort> = emptySet(),
    val pagination: StatePagination = StatePagination(),
) {
    /**
     * 回到第一页，适合关键字或筛选条件变化后的重新查询。
     */
    fun resetToFirstPage(): CrudTableQuery {
        return copy(
            pagination = pagination.copy(currentPage = 1),
        )
    }
}
