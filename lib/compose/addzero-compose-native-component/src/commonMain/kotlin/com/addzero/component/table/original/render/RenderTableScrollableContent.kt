package com.addzero.component.table.original.render

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.addzero.component.table.original.ColumnConfig
import com.addzero.component.table.original.TableLayoutConfig

/**
 * 渲染可滚动内容区域 - 使用细粒度context
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
    showActionColumn: Boolean,
    getColumnLabel: @Composable (C) -> Unit,
    emptyContentSlot: @Composable () -> Unit,
    getCellContent: @Composable (item: T, column: C) -> Unit,
    rowLeftSlot: @Composable (item: T, index: Int) -> Unit,
    rowActionSlot: (@Composable (item: T) -> Unit)?
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 表头行 - 只需要列配置和表头配置
        RenderTableHeaderRow(
            columns = columns,
            getColumnKey = getColumnKey,
            getColumnLabel = getColumnLabel,
            horizontalScrollState = horizontalScrollState!!,
            columnConfigs = columnConfigs,
            layoutConfig = layoutConfig,
            showActionColumn = showActionColumn
        )

        // 使用现有的LazyColumn数据渲染
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize()
        ) {
            if (data.isEmpty()) {
                item { emptyContentSlot() }
            } else {
                itemsIndexed(
                    items = data,
                    key = { _, item -> getRowId(item) }
                ) { index, item ->
                    // 数据行 - 只需要行相关的数据
                    RenderTableBodyRow(
                        item = item,
                        index = index,
                        columns = columns,
                        getColumnKey = getColumnKey,
                        columnConfigs = columnConfigs,
                        getCellContent = getCellContent,
                        horizontalScrollState = horizontalScrollState,
                        rowLeftSlot = rowLeftSlot,
                        rowActionSlot = rowActionSlot,
                        layoutConfig = layoutConfig,
                        showActionColumn = showActionColumn
                    )
                }
            }
        }
    }
}
