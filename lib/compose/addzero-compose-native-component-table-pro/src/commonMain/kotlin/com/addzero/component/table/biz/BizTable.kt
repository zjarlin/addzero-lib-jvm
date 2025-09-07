package com.addzero.component.table.biz

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.addzero.component.button.AddIconButton
import com.addzero.component.search_bar.AddSearchBar
import com.addzero.component.table.original.TableOriginal
import com.addzero.component.table.original.entity.ColumnConfig
import com.addzero.component.table.original.entity.TableLayoutConfig

@Composable
context(
    tableSelectedViewModel: TableSelectedViewModel<*>, bizTableViewModel: BizTableViewModel<*, *>, tableButtonViewModel: TableButtonViewModel, tableSortViewModel: TableSortViewModel, tableFilterViewModel: TableFilterViewModel<*>, tablePaginationViewModel: TablePaginationViewModel
)
inline fun <reified T, C> BizTable(
    data: List<T>,
    columns: List<C>,
    noinline getColumnKey: (C) -> String,
    noinline getRowId: ((T) -> Any)? = null,
    columnConfigs: List<ColumnConfig> = emptyList(),
    layoutConfig: TableLayoutConfig = TableLayoutConfig(),
    noinline getColumnLabel: @Composable (C) -> Unit,
    noinline topSlot: (@Composable () -> Unit)? = null,
    noinline bottomSlot: (@Composable () -> Unit)? = null,
    noinline emptyContentSlot: (@Composable () -> Unit)? = null,
    noinline getCellContent: (@Composable (item: T, column: C) -> Unit)? = null,
    // 行左侧插槽（如复选框）
    noinline rowLeftSlot: (@Composable (item: T, index: Int) -> Unit)? = null,
    noinline rowActionSlot: (@Composable (item: T) -> Unit)? = null,
    modifier: Modifier = Modifier,
    noinline columnRightSlot: @Composable ((C) -> Unit)? = null, noinline buttonSlot: @Composable () -> Unit
) {

    TableOriginal(
        data = data,
        columns = columns,
        getColumnKey = getColumnKey,
        getRowId = getRowId,
        columnConfigs = columnConfigs,
        layoutConfig = layoutConfig,
        getColumnLabel = getColumnLabel,
        topSlot = topSlot ?: {
            AddSearchBar(
                keyword = bizTableViewModel.keyword,
                onKeyWordChanged = { bizTableViewModel.keyword = it },
                onSearch = { bizTableViewModel.onSearch() },
                leftSloat = {
                    RenderButtons(buttonSlot)
                })
            RenderSelectContent()
        },
        bottomSlot = bottomSlot ?: {
            RenderPagination()
        },
        emptyContentSlot = emptyContentSlot,
        getCellContent = getCellContent,
        rowLeftSlot = rowLeftSlot ?: { item, index ->
            RenderCheckbox()
        },
        rowActionSlot = rowActionSlot ?: {
            AddIconButton(
                text = "编辑", imageVector = Icons.Default.Edit, onClick = { bizTableViewModel.onEditClick() })

            AddIconButton(
                text = "删除", imageVector = Icons.Default.Delete, onClick = {
                    bizTableViewModel.onDeleteClick()
                }

            )

        },
        modifier = modifier,
        columnRightSlot = columnRightSlot ?: {
            RenderSort(it, getColumnKey)
            RenderAdvSearchDrawer()
        }
    )
}
