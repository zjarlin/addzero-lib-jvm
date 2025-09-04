package com.addzero.component.table.original

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.addzero.component.table.original.render.RenderFixedActionColumn
import com.addzero.component.table.original.render.RenderFixedIndexColumn
import com.addzero.component.table.original.render.RenderTableScrollableContent
import com.addzero.component.table.original.tools.rememberAddTableAutoWidth
import com.addzero.core.ext.toMap

@Composable
inline fun <reified T, C> TableOriginal(
    data: List<T>,
    columns: List<C>,
    noinline getColumnKey: (C) -> String,
    noinline getRowId: (T) -> Any,
    columnConfigs: List<ColumnConfig> = emptyList(),
    layoutConfig: TableLayoutConfig = TableLayoutConfig(),
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
    noinline rowActionSlot: @Composable (item: T) -> Unit = { AddRowActionDefaults(it) },
    modifier: Modifier = Modifier
) {
    val rememberScrollState = rememberScrollState()
    val verticalScrollState = rememberLazyListState()

    // 计算自适应列宽（可选）
    val textStyleHeader = MaterialTheme.typography.titleSmall
    val textStyleCell = MaterialTheme.typography.bodyMedium
    val autoWidths = rememberAddTableAutoWidth(
        data = data,
        columns = columns,
        getColumnKey = getColumnKey,
        getCellText = { item, column ->
            val m = item?.toMap()
            (m?.get(getColumnKey(column)) ?: "").toString()
        },
        layoutConfig = layoutConfig,
        headerTextStyle = textStyleHeader,
        cellTextStyle = textStyleCell
    )

    val mergedColumnConfigs = remember(columnConfigs, autoWidths) {
        if (autoWidths.isEmpty()) columnConfigs else columnConfigs.map { cfg ->
            val w = autoWidths[cfg.key]
            if (w != null) cfg.copy(width = w) else cfg
        }
    }

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
                columnConfigs = mergedColumnConfigs,
                layoutConfig = layoutConfig,
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
                layoutConfig = layoutConfig,
                modifier = Modifier.align(Alignment.CenterStart).zIndex(1f)
            )

            // 固定操作列（表头+每行按钮）
            RenderFixedActionColumn(
                modifier = Modifier.align(Alignment.TopEnd).zIndex(1f),
                verticalScrollState = verticalScrollState,
                data = data,
                layoutConfig = layoutConfig,
                rowActionSlot = rowActionSlot
            )
        }

        // 底部插槽区域
        bottomSlot()
    }
}


