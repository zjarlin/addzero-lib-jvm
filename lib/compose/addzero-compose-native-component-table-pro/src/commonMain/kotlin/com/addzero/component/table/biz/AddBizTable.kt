package com.addzero.component.table.biz

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.addzero.component.button.AddIconButton
import com.addzero.component.search_bar.AddSearchBar
import com.addzero.component.table.biz.renders.RenderButtons
import com.addzero.component.table.biz.renders.RenderCheckbox
import com.addzero.component.table.biz.renders.RenderPagination
import com.addzero.component.table.biz.renders.RenderSelectContent
import com.addzero.component.table.biz.renders.RenderSort
import com.addzero.component.table.original.TableOriginal
import com.addzero.component.table.original.entity.ColumnConfig
import com.addzero.component.table.original.entity.StatePagination
import com.addzero.component.table.original.entity.TableLayoutConfig
import com.addzero.entity.low_table.EnumSortDirection
import com.addzero.entity.low_table.StateSearch

@Composable
inline fun <reified T, C> AddBizTable(
    data: List<T>,
    columns: List<C>,
    noinline getColumnKey: (C) -> String,
    noinline getRowId: ((T) -> Any)? = null,
    columnConfigs: List<ColumnConfig> = emptyList(),
    layoutConfig: TableLayoutConfig = TableLayoutConfig(),
    noinline getColumnLabel: (@Composable (C) -> Unit)?=null,
    noinline topSlot: (@Composable () -> Unit)? = null,
    noinline bottomSlot: (@Composable () -> Unit)? = null,
    noinline emptyContentSlot: (@Composable () -> Unit)? = null,
    noinline getCellContent: (@Composable (item: T, column: C) -> Unit)? = null,
    noinline rowActionSlot: (@Composable (item: T) -> Unit)? = null,
    modifier: Modifier = Modifier,
    noinline columnRightSlot: @Composable ((C) -> Unit)? = null,
    noinline buttonSlot: @Composable () -> Unit = {},
    // AddBizTable 特有参数
    showPagination: Boolean = true,
    showSearchBar: Boolean = true,
    showBatchActions: Boolean = true,
    showRowSelection: Boolean = true,
    showDefaultRowActions: Boolean = true,
    enableSorting: Boolean = true,
    enableAdvancedSearch: Boolean = true
) {
    // 内部状态管理
    var keyword by remember { mutableStateOf("") }
    var editModeFlag by remember { mutableStateOf(false) }
    var selectedItemIds by remember { mutableStateOf(setOf<Any>()) }
    var pageState by remember { mutableStateOf(StatePagination()) }
    var sortState by remember { mutableStateOf(mapOf<String, EnumSortDirection>()) }
    var filterStateMap by remember { mutableStateOf(mapOf<String, StateSearch>()) }
    var showFieldAdvSearch by remember { mutableStateOf(false) }
    var currentColumnKey by remember { mutableStateOf("") }
    var currentStateSearch by remember { mutableStateOf(StateSearch(
        columnKey = currentColumnKey
    )) }

    // 计算当前页的ID
    val currentPageIds = remember(data) {
        data.map { getRowId?.invoke(it) ?: it.hashCode() }
    }

    // 检查当前页是否全选
    val isPageAllSelected = remember(selectedItemIds, currentPageIds) {
        currentPageIds.isNotEmpty() && selectedItemIds.containsAll(currentPageIds)
    }

    // 获取列的排序方向
    val getSortDirection = { columnKey: String ->
        sortState[columnKey] ?: EnumSortDirection.NONE
    }

    // 默认的顶部插槽
    val defaultTopSlot: @Composable () -> Unit = {
        if (showSearchBar) {
            AddSearchBar(
                keyword = keyword,
                onKeyWordChanged = { keyword = it },
                onSearch = { /* 搜索逻辑 */ },
                leftSloat = {
                    RenderButtons(
                        buttonSlot = buttonSlot,
                        editModeFlag = editModeFlag,
                        onEditModeFlagChange = { editModeFlag = it },
                        onSaveClick = { /* 保存逻辑 */ },
                        onImportClick = { /* 导入逻辑 */ },
                        onExportClick = { /* 导出逻辑 */ }
                    )
                }
            )
        }

        if (showBatchActions) {
            RenderSelectContent(
                editModeFlag = editModeFlag,
                selectedItemIds = selectedItemIds,
                onClearSelectedItems = { selectedItemIds = emptySet() },
                onBatchDelete = { /* 批量删除逻辑 */ },
                onBatchExport = { /* 批量导出逻辑 */ }
            )
        }
    }

    // 默认的底部插槽
    val defaultBottomSlot: @Composable () -> Unit = {
        if (showPagination) {
            RenderPagination(
                showPagination = showPagination,
                pageState = pageState,
                onPageSizeChange = {
                    pageState = pageState.copy(pageSize = it, currentPage = 1)
                },
                onGoFirstPage = {
                    pageState = pageState.copy(currentPage = 1)
                },
                onPreviousPage = {
                    if (pageState.hasPreviousPage) {
                        pageState = pageState.copy(currentPage = pageState.currentPage - 1)
                    }
                },
                onGoToPage = {
                    if (it in 1..pageState.totalPages) {
                        pageState = pageState.copy(currentPage = it)
                    }
                },
                onNextPage = {
                    if (pageState.hasNextPage) {
                        pageState = pageState.copy(currentPage = pageState.currentPage + 1)
                    }
                },
                onGoLastPage = {
                    pageState = pageState.copy(currentPage = pageState.totalPages)
                }
            )
        }
    }

    // 默认的行左侧插槽
    val defaultRowLeftSlot: @Composable (item: T, index: Int) -> Unit = { item, index ->
        if (showRowSelection && editModeFlag) {
            RenderCheckbox(
                editModeFlag = editModeFlag,
                isPageAllSelected = isPageAllSelected,
                onTogglePageSelection = {
                    if (isPageAllSelected) {
                        // 取消全选
                        selectedItemIds = selectedItemIds.filter { it !in currentPageIds }.toSet()
                    } else {
                        // 全选
                        selectedItemIds = selectedItemIds + currentPageIds
                    }
                }
            )
        }
    }

    // 默认的行操作插槽
    val defaultRowActionSlot: @Composable (item: T) -> Unit = { item ->
        if (showDefaultRowActions) {
            AddIconButton(
                text = "编辑",
                imageVector = Icons.Default.Edit,
                onClick = { /* 编辑逻辑 */ }
            )
            AddIconButton(
                text = "删除",
                imageVector = Icons.Default.Delete,
                onClick = { /* 删除逻辑 */ }
            )
        } else {
            rowActionSlot?.invoke(item)
        }
    }

    // 默认的列右侧插槽
    val defaultColumnRightSlot: @Composable (C) -> Unit = { column ->
        if (enableSorting) {
            val columnKey = getColumnKey(column)
            RenderSort(
                columnKey = columnKey,
                sortDirection = getSortDirection(columnKey),
                onSortChange = { key ->
                    val currentDirection = sortState[key] ?: EnumSortDirection.NONE
                    val newDirection = when (currentDirection) {
                        EnumSortDirection.NONE -> EnumSortDirection.ASC
                        EnumSortDirection.ASC -> EnumSortDirection.DESC
                        EnumSortDirection.DESC -> EnumSortDirection.NONE
                    }
                    sortState = sortState + (key to newDirection)
                }
            )
        }
        columnRightSlot?.invoke(column)
    }

    TableOriginal(
        data = data,
        columns = columns,
        getColumnKey = getColumnKey,
        getRowId = getRowId,
        columnConfigs = columnConfigs,
        layoutConfig = layoutConfig,
        getColumnLabel = getColumnLabel,
        topSlot = topSlot ?: defaultTopSlot,
        bottomSlot = bottomSlot ?: defaultBottomSlot,
        emptyContentSlot = emptyContentSlot,
        getCellContent = getCellContent,
        rowLeftSlot = defaultRowLeftSlot,
        rowActionSlot = if (rowActionSlot != null) rowActionSlot else defaultRowActionSlot,
        modifier = modifier,
        columnRightSlot = defaultColumnRightSlot
    )
}
