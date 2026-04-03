package site.addzero.component.table.original.render

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import site.addzero.component.table.original.entity.ColumnConfig
import site.addzero.component.table.original.entity.TableLayoutConfig

/**
 * 渲染可滚动内容区域。
 */
@Composable
fun <T, C> RenderTableScrollableContent(
    data: List<T>,
    columns: List<C>,
    getColumnKey: (C) -> String,
    getRowId: (T) -> Any,
    horizontalScrollState: ScrollState,
    lazyListState: LazyListState,
    columnConfigs: List<ColumnConfig>,
    layoutConfig: TableLayoutConfig,
    showLeftSlot: Boolean = false,
    showActionColumn: Boolean,
    getColumnLabel: @Composable (C) -> Unit,
    emptyContentSlot: @Composable () -> Unit,
    getCellContent: @Composable (item: T, column: C) -> Unit,
    rowLeftSlot: @Composable (item: T, index: Int) -> Unit,
    columnRightSlot: @Composable ((C) -> Unit),
) {
    Column(modifier = Modifier.fillMaxSize()) {
        RenderTableHeaderRow(
            columns = columns,
            getColumnKey = getColumnKey,
            getColumnLabel = getColumnLabel,
            horizontalScrollState = horizontalScrollState,
            columnConfigs = columnConfigs,
            layoutConfig = layoutConfig,
            showLeftSlot = showLeftSlot,
            showActionColumn = showActionColumn,
            columnRightSlot = columnRightSlot,
        )

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
        ) {
            if (data.isEmpty()) {
                item { emptyContentSlot() }
            } else {
                itemsIndexed(
                    items = data,
                    key = { _, item -> getRowId(item) },
                ) { index, item ->
                    RenderTableBodyRow(
                        item = item,
                        index = index,
                        columns = columns,
                        getColumnKey = getColumnKey,
                        columnConfigs = columnConfigs,
                        getCellContent = getCellContent,
                        horizontalScrollState = horizontalScrollState,
                        rowLeftSlot = rowLeftSlot,
                        layoutConfig = layoutConfig,
                        showLeftSlot = showLeftSlot,
                        showActionColumn = showActionColumn,
                    )
                }
            }
        }
    }
}
