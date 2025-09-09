package com.addzero.component.table.biz

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.addzero.component.button.AddEditDeleteButton
import com.addzero.component.search_bar.AddSearchBar
import com.addzero.component.table.original.TableOriginal
import com.addzero.component.table.original.entity.ColumnConfig
import com.addzero.component.table.original.entity.TableLayoutConfig
import com.addzero.component.table.vm.TableFilterViewModel
import com.addzero.component.table.vm.koin.*

@Composable
inline fun <reified T, reified C> BizTable(
    data: List<T>,
    columns: List<C>,
    noinline getColumnKey: (C) -> String,
    noinline getRowId: ((T) -> Any)? = null,
    columnConfigs: List<ColumnConfig> = emptyList(),
    layoutConfig: TableLayoutConfig = TableLayoutConfig(),
    noinline getColumnLabel: (@Composable (C) -> Unit)? = null,
    noinline topSlot: (@Composable () -> Unit)? = null,
    noinline bottomSlot: (@Composable () -> Unit)? = null,
    noinline emptyContentSlot: (@Composable () -> Unit)? = null,
    noinline getCellContent: (@Composable (item: T, column: C) -> Unit)? = null,
    // 行左侧插槽（如复选框）
    noinline rowLeftSlot: (@Composable (item: T, index: Int) -> Unit)? = null,
    noinline rowActionSlot: (@Composable (item: T) -> Unit)? = null,
    modifier: Modifier = Modifier,
    noinline columnRightSlot: @Composable ((C) -> Unit)? = null, noinline buttonSlot: @Composable () -> Unit = {}
) {
    val tableFilterViewModel = TableFilterViewModel(getColumnKey, columnConfigs)
    val tableSelectedViewModel = TableSelectedViewModel<T>()
//    val tableButtonViewModel = TableButtonViewModel()
    val tablePaginationViewModel = TablePaginationViewModel()
    val tableSortViewModel = TableSortViewModel()
    val bizTableViewModel = BizTableViewModel<T>().apply {
        _data = data
    }

    context(
        bizTableViewModel,
        tableFilterViewModel,
//        tableButtonViewModel,
        tablePaginationViewModel,
        tableSelectedViewModel,
        tableSortViewModel,
        columnConfigs,
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
                RenderCheckbox(item)
            },
            rowActionSlot = rowActionSlot ?: {

                AddEditDeleteButton(
                    showDelete = true, showEdit = true, onEditClick = {

                        bizTableViewModel.onEditClick()
                    },
                    { bizTableViewModel.onDeleteClick() }
                )

            },
            modifier = modifier,
            columnRightSlot = columnRightSlot ?: {
                RenderSortButton(it, getColumnKey)
                RenderFilterButton(it, getColumnKey)
            }
        )

        //右侧的高级搜索面板
        RenderAdvSearchDrawer()


    }


}
