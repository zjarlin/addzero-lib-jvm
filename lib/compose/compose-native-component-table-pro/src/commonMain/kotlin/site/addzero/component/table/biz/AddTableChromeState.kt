package site.addzero.component.table.biz

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import site.addzero.entity.low_table.StateSearch

/**
 * `AddTable` 的视觉状态持有者。
 *
 * 查询、排序、筛选、多选已经收敛到 `CrudTableState`，
 * 这里仅保留高级筛选抽屉本身的临时编辑态。
 */
@Stable
internal class AddTableChromeState<C> {
    var advancedSearchVisible by mutableStateOf(false)
    var editingSearch by mutableStateOf(StateSearch())
    var currentColumn by mutableStateOf<C?>(null)

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
        currentColumn = null
    }

    /**
     * 清理当前列的临时筛选草稿。
     */
    fun clearCurrentSearch(columnKey: String) {
        editingSearch = StateSearch(columnKey = columnKey)
    }
}

/**
 * 记住一个 `AddTable` 视觉状态实例。
 */
@Composable
internal fun <C> rememberAddTableChromeState(): AddTableChromeState<C> {
    return remember { AddTableChromeState<C>() }
}
