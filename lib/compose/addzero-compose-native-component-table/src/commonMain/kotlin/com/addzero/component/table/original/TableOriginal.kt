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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.addzero.component.table.original.entity.ColumnConfig
import com.addzero.component.table.original.entity.TableLayoutConfig
import com.addzero.component.table.original.render.RenderFixedActionColumn
import com.addzero.component.table.original.render.RenderFixedIndexColumn
import com.addzero.component.table.original.render.RenderTableScrollableContent
import com.addzero.component.table.original.tools.rememberAddTableAutoWidth
import com.addzero.core.ext.bean2map

@Composable
//@ComposeAssist
inline fun <reified T, reified C> TableOriginal(
    data: List<T>,
    columns: List<C>,
    noinline getColumnKey: (C) -> String,
    noinline getRowId: ((T) -> Any)? = null,
    columnConfigs: List<ColumnConfig>,
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
    noinline columnRightSlot: @Composable ((C) -> Unit)? = null
) {
    // 设置默认值
    val actualGetRowId = getRowId ?: {
        val toMap = it?.bean2map()
        toMap?.get("id") ?: it.hashCode()
    }
    val actualGetColumnLabel = getColumnLabel ?: { column ->
        val columnKey = getColumnKey(column)
        val comment = columnConfigs.find { it.key == columnKey }?.comment
        val text = comment?.ifBlank { columnKey }?:columnKey
        Text(
            text = text, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )
    }
    val actualTopSlot = topSlot ?: {}
    val actualBottomSlot = bottomSlot ?: {}
    val actualEmptyContentSlot = emptyContentSlot ?: {
        Box(
            modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "没有数据",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    val actualGetCellContent = getCellContent ?: { item, column ->
        val toMap = item?.bean2map()
        val toString = toMap?.get(getColumnKey(column)).toString()
        Text(text = toString)
    }
    val actualRowLeftSlot = rowLeftSlot ?: { _, _ -> }

    val rememberScrollState = rememberScrollState()
    val verticalScrollState = rememberLazyListState()

    // 计算自适应列宽（可选）
    val textStyleHeader = MaterialTheme.typography.titleSmall
    val textStyleCell = MaterialTheme.typography.bodyMedium
    val autoWidths = rememberAddTableAutoWidth(
        data = data, columns = columns, getColumnKey = getColumnKey, getCellText = { item, column ->
            val m = item?.bean2map()
            (m?.get(getColumnKey(column)) ?: "").toString()
        }, layoutConfig = layoutConfig, headerTextStyle = textStyleHeader, cellTextStyle = textStyleCell
    )

    val mergedColumnConfigs = remember(columnConfigs, autoWidths) {
        if (autoWidths.isEmpty()) columnConfigs else columnConfigs.map { cfg ->
            val w = autoWidths[cfg.key]
            if (w != null) cfg.copy(width = w) else cfg
        }
    }

    val showFixedActionColumn = rowActionSlot != null

    // 使用ViewModel渲染表格
    Column(modifier = modifier) {
        // 顶部插槽区域
        actualTopSlot()

        // 主表格内容区域
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // 主内容滚动区域
            RenderTableScrollableContent(
                data = data,
                columns = columns,
                getColumnKey = getColumnKey,
                getRowId = actualGetRowId,
                horizontalScrollState = rememberScrollState,
                lazyListState = verticalScrollState,
                columnConfigs = mergedColumnConfigs,
                layoutConfig = layoutConfig,
                showActionColumn = showFixedActionColumn,
                getColumnLabel = actualGetColumnLabel,
                emptyContentSlot = actualEmptyContentSlot,
                getCellContent = actualGetCellContent,
                rowLeftSlot = actualRowLeftSlot,
                rowActionSlot = if (showFixedActionColumn) null else rowActionSlot,
                columnRightSlot = columnRightSlot ?: {},
            )

            // 序号列固定遮罩 - 只需要滚动状态和数据
            RenderFixedIndexColumn(
                verticalScrollState = verticalScrollState,
                data = data,
                layoutConfig = layoutConfig,
                modifier = Modifier.align(Alignment.CenterStart).zIndex(1f)
            )

            if (showFixedActionColumn) {
                RenderFixedActionColumn(
                    modifier = Modifier.align(Alignment.TopEnd).zIndex(1f),
                    verticalScrollState = verticalScrollState,
                    data = data,
                    layoutConfig = layoutConfig,
                    rowActionSlot = rowActionSlot!!
                )
            }
        }

        // 底部插槽区域
        actualBottomSlot()
    }
}
