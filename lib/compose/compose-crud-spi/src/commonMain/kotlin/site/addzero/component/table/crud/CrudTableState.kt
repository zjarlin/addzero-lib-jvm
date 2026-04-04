package site.addzero.component.table.crud

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import site.addzero.component.table.original.entity.StatePagination
import site.addzero.entity.PageResult
import site.addzero.entity.low_table.EnumSortDirection
import site.addzero.entity.low_table.StateSearch
import site.addzero.entity.low_table.StateSort

/**
 * 表格的可控查询状态。
 *
 * 这一层只关心列表交互，不处理任何网络、副作用或消息提示。
 */
@Stable
class CrudTableState<ID : Any> internal constructor(
    keyword: String,
    pagination: StatePagination,
    sorts: Set<StateSort>,
    filters: Set<StateSearch>,
    editModeEnabled: Boolean,
    selectedRowIds: Set<ID>,
) {
    var keyword by mutableStateOf(keyword)
        private set

    var pagination by mutableStateOf(pagination)
        private set

    var sorts by mutableStateOf(sorts)
        private set

    var filters by mutableStateOf(filters)
        private set

    var editModeEnabled by mutableStateOf(editModeEnabled)
        private set

    var selectedRowIds by mutableStateOf(selectedRowIds)
        private set

    val currentQuery
        get() = CrudTableQuery(
            keyword = keyword,
            filters = filters,
            sorts = sorts,
            pagination = pagination,
        )

    fun updateKeyword(nextKeyword: String) {
        keyword = nextKeyword
    }

    fun replacePagination(nextPagination: StatePagination) {
        pagination = nextPagination
    }

    fun replaceSorts(nextSorts: Set<StateSort>) {
        sorts = nextSorts
    }

    fun replaceFilters(nextFilters: Set<StateSearch>) {
        filters = nextFilters
    }

    fun applyQuery(query: CrudTableQuery) {
        keyword = query.keyword
        filters = query.filters
        sorts = query.sorts
        pagination = query.pagination
    }

    /**
     * 切换某一列的排序方向。
     */
    fun toggleSort(columnKey: String) {
        val existingSort = sorts.find { it.columnKey == columnKey }
        val nextDirection = when (existingSort?.direction) {
            EnumSortDirection.ASC -> EnumSortDirection.DESC
            EnumSortDirection.DESC -> EnumSortDirection.NONE
            else -> EnumSortDirection.ASC
        }

        val nextSorts = sorts
            .filterNot { it.columnKey == columnKey }
            .toMutableSet()

        if (nextDirection != EnumSortDirection.NONE) {
            nextSorts += StateSort(columnKey = columnKey, direction = nextDirection)
        }

        sorts = nextSorts
    }

    fun currentFilter(columnKey: String): StateSearch? {
        return filters.find { it.columnKey == columnKey }
    }

    fun upsertFilter(search: StateSearch) {
        filters = filters
            .filterNot { it.columnKey == search.columnKey }
            .toSet() + search
    }

    fun removeFilter(columnKey: String) {
        filters = filters.filterNot { it.columnKey == columnKey }.toSet()
    }

    /**
     * 切换多选模式。
     *
     * 退出多选时直接清空选择，避免旧状态残留。
     */
    fun toggleEditMode() {
        val nextEnabled = !editModeEnabled
        editModeEnabled = nextEnabled
        if (!nextEnabled) {
            clearSelection()
        }
    }

    fun setEditMode(enabled: Boolean) {
        editModeEnabled = enabled
        if (!enabled) {
            clearSelection()
        }
    }

    fun updateSelection(
        rowId: ID,
        checked: Boolean,
    ) {
        selectedRowIds = if (checked) {
            selectedRowIds + rowId
        } else {
            selectedRowIds - rowId
        }
    }

    fun removeSelection(ids: Set<ID>) {
        selectedRowIds = selectedRowIds - ids
    }

    fun retainSelection(ids: Set<ID>) {
        selectedRowIds = selectedRowIds.intersect(ids)
    }

    fun clearSelection() {
        selectedRowIds = emptySet()
    }

    /**
     * 把服务端分页结果回填到本地分页状态。
     */
    fun applyPageResult(pageResult: PageResult<*>) {
        val nextTotalItems = pageResult.totalRowCount
            .coerceIn(0, Int.MAX_VALUE.toLong())
            .toInt()
        val nextPageIndex = if (pageResult.pageIndex > 0) {
            pageResult.pageIndex
        } else {
            pagination.currentPage
        }
        val nextPageSize = if (pageResult.pageSize > 0) {
            pageResult.pageSize
        } else {
            pagination.pageSize
        }
        pagination = pagination.copy(
            currentPage = nextPageIndex,
            pageSize = nextPageSize,
            totalItems = nextTotalItems,
        )
    }
}

@Composable
fun <ID : Any> rememberCrudTableState(
    initialQuery: CrudTableQuery = CrudTableQuery(),
    initialEditModeEnabled: Boolean = false,
    initialSelection: Set<ID> = emptySet(),
): CrudTableState<ID> {
    return remember(
        initialQuery,
        initialEditModeEnabled,
        initialSelection,
    ) {
        CrudTableState(
            keyword = initialQuery.keyword,
            pagination = initialQuery.pagination,
            sorts = initialQuery.sorts,
            filters = initialQuery.filters,
            editModeEnabled = initialEditModeEnabled,
            selectedRowIds = initialSelection,
        )
    }
}
