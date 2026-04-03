package site.addzero.component.table.biz

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import site.addzero.component.table.original.entity.StatePagination
import site.addzero.entity.low_table.EnumSortDirection
import site.addzero.entity.low_table.StateSearch
import site.addzero.entity.low_table.StateSort

/**
 * `AddTable` 的内部状态持有者。
 *
 * 统一收口分页、排序、筛选、多选和当前编辑中的高级搜索条件，
 * 避免这些状态继续散落在入口函数里相互耦合。
 */
@Stable
internal class AddTableState<C> {
    var keyword by mutableStateOf("")
    var editModeEnabled by mutableStateOf(false)
    var selectedItemIds by mutableStateOf<Set<Any>>(emptySet())
    var pagination by mutableStateOf(StatePagination())
    var sortState by mutableStateOf<Set<StateSort>>(emptySet())
    var advancedSearchVisible by mutableStateOf(false)
    var filterStateMap by mutableStateOf<Map<String, StateSearch>>(emptyMap())
    var editingSearch by mutableStateOf(StateSearch())
    var currentColumn by mutableStateOf<C?>(null)

    val filters: Set<StateSearch>
        get() = filterStateMap.values.toSet()

    /**
     * 切换多选模式。
     *
     * 退出多选时顺便清空当前选择，避免旧选择状态残留到下一轮批量操作。
     */
    fun toggleEditMode() {
        val nextEnabled = !editModeEnabled
        editModeEnabled = nextEnabled
        if (!nextEnabled) {
            selectedItemIds = emptySet()
        }
    }

    /**
     * 更新单行选择结果。
     */
    fun updateSelection(
        itemId: Any,
        checked: Boolean,
    ) {
        selectedItemIds = if (checked) {
            selectedItemIds + itemId
        } else {
            selectedItemIds - itemId
        }
    }

    /**
     * 清空当前批量选择。
     */
    fun clearSelection() {
        selectedItemIds = emptySet()
    }

    /**
     * 切换某一列的排序方向。
     */
    fun toggleSort(columnKey: String) {
        val existingSort = sortState.find { it.columnKey == columnKey }
        val nextDirection = when (existingSort?.direction) {
            EnumSortDirection.ASC -> EnumSortDirection.DESC
            EnumSortDirection.DESC -> EnumSortDirection.NONE
            else -> EnumSortDirection.ASC
        }

        val nextSorts = sortState
            .filterNot { it.columnKey == columnKey }
            .toMutableSet()

        if (nextDirection != EnumSortDirection.NONE) {
            nextSorts += StateSort(columnKey = columnKey, direction = nextDirection)
        }

        sortState = nextSorts
    }

    /**
     * 打开某一列的高级搜索抽屉。
     */
    fun openAdvancedSearch(
        column: C,
        columnKey: String,
        existingSearch: StateSearch?,
    ) {
        currentColumn = column
        editingSearch = existingSearch ?: StateSearch(columnKey = columnKey)
        advancedSearchVisible = true
    }

    /**
     * 关闭高级搜索抽屉。
     */
    fun closeAdvancedSearch() {
        advancedSearchVisible = false
    }

    /**
     * 保存当前编辑中的高级搜索条件。
     */
    fun saveAdvancedSearch(columnKey: String) {
        filterStateMap = filterStateMap + (columnKey to editingSearch.copy(columnKey = columnKey))
        advancedSearchVisible = false
    }

    /**
     * 清空指定列的高级搜索条件。
     */
    fun clearAdvancedSearch(columnKey: String) {
        filterStateMap = filterStateMap - columnKey
    }
}

/**
 * 记住一个 `AddTable` 内部状态实例。
 */
@Composable
internal fun <C> rememberAddTableState(): AddTableState<C> {
    return remember { AddTableState() }
}
