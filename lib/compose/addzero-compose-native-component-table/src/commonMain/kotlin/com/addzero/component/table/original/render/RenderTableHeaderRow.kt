package com.addzero.component.table.original.render

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.addzero.component.card.AddCard
import com.addzero.component.table.original.entity.ColumnConfig
import com.addzero.component.table.original.entity.TableLayoutConfig

/**
 * 渲染表头行 - 使用细粒度参数
 */
@Composable
fun <C> RenderTableHeaderRow(
    columns: List<C>,
    getColumnKey: (C) -> String,
    getColumnLabel: @Composable (C) -> Unit,
    columnRightSlot: @Composable (C) -> Unit ,
    horizontalScrollState: ScrollState,
    columnConfigs: List<ColumnConfig>,
    layoutConfig: TableLayoutConfig,
    showActionColumn: Boolean
) {
    val columnConfigDict = columnConfigs.associateBy { it.key }
    AddCard(
        modifier = Modifier.fillMaxWidth().height(layoutConfig.headerHeightDp.dp), padding = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().horizontalScroll(state = horizontalScrollState)
                .padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧为固定序号列预留占位，避免重复渲染
            Spacer(modifier = Modifier.width(layoutConfig.indexColumnWidthDp.dp).fillMaxHeight())
            // 数据列
            columns
                .sortedBy {
                    val columnKey = getColumnKey(it)
                    val columnConfig = columnConfigDict[columnKey]
                    columnConfig?.order
                }
                .forEach { column ->
                val columnKey = getColumnKey(column)
                val columnConfig = columnConfigDict[columnKey]
                Box(
                    modifier = Modifier.width((columnConfig?.width ?: layoutConfig.defaultColumnWidthDp).dp).fillMaxHeight()
                        .padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart
                ) {
                    Row {
                        getColumnLabel(column)
                        columnRightSlot(column)
                    }
                }
            }
            if (showActionColumn) {
                // 右侧为固定操作列预留占位，避免重复渲染
                Spacer(modifier = Modifier.width(layoutConfig.actionColumnWidthDp.dp).fillMaxHeight())
            }
        }
    }
}
