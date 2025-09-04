package com.addzero.component.table.original

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.addzero.component.table.original.render.RenderFixedActionColumn
import com.addzero.component.table.original.render.RenderFixedIndexColumn
import com.addzero.component.table.original.render.RenderTableScrollableContent
import com.addzero.core.ext.toMap

@Composable
inline fun <reified T, C> TableOriginal(
    data: List<T>,
    columns: List<C>,
    noinline getColumnKey: (C) -> String,
    noinline getRowId: (T) -> Any,
    columnConfigs: List<ColumnConfig>,
    noinline getColumnLabel: @Composable (C) -> Unit,
    topSlot: @Composable () -> Unit = {},
    bottomSlot: @Composable () -> Unit = {},
    noinline emptyContentSlot: @Composable () -> Unit = {
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "没有数据",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    },

    noinline getCellContent: @Composable (item: T, column: C) -> Unit = { item, column ->
        val toMap = item?.toMap()
        val any = toMap?.get(getColumnKey(column))
        val toString = any.toString()
        Text(text = toString)
    },
    // 行左侧插槽（如复选框）
    noinline rowLeftSlot: @Composable (item: T, index: Int) -> Unit = { _, _ -> },
    noinline rowActionSlot: @Composable (item: T) -> Unit={Text(text = "操作测试")},
    modifier: Modifier = Modifier
) {
    val rememberScrollState = rememberScrollState()
    val verticalScrollState = rememberLazyListState()


    // 使用ViewModel渲染表格
    Column(modifier = modifier) {
        // 顶部插槽区域
        topSlot()

        // 主表格内容区域
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // 主内容滚动区域
            RenderTableScrollableContent(
                data = data,
                columns = columns,
                getColumnKey = getColumnKey,
                getRowId = getRowId,
                horizontalScrollState = rememberScrollState,
                lazyListState = verticalScrollState,
                columnConfigs = columnConfigs,
                getColumnLabel = getColumnLabel,
                emptyContentSlot = emptyContentSlot,
                getCellContent = getCellContent,
                rowLeftSlot = rowLeftSlot,
                rowActionSlot = rowActionSlot
            )

            // 序号列固定遮罩 - 只需要滚动状态和数据
            RenderFixedIndexColumn(
                verticalScrollState = verticalScrollState,
                data = data,
                modifier = Modifier.align(Alignment.CenterStart).zIndex(1f)
            )

            // 操作列固定遮罩 - 只需要滚动状态、数据和插槽
            RenderFixedActionColumn(
                modifier = Modifier.align(Alignment.CenterEnd).zIndex(1f),
                verticalScrollState = verticalScrollState,
                data = data,
                rowActionSlot = rowActionSlot
            )
        }

        // 底部插槽区域
        bottomSlot()
    }
}


