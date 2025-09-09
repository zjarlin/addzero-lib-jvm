package com.addzero.component.table.original.render

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.addzero.component.table.original.entity.ColumnConfig
import com.addzero.component.table.original.entity.TableLayoutConfig

/**
 * 渲染完整数据行 - 使用细粒度参数
 */
@Composable
fun <T, C> RenderTableBodyRow(
    item: T,
    index: Int,
    columns: List<C>,
    getColumnKey: (C) -> String,
    columnConfigs: List<ColumnConfig>,
    getCellContent: @Composable ((item: T, column: C) -> Unit),
    horizontalScrollState: ScrollState,
    rowLeftSlot: @Composable ((item: T, index: Int) -> Unit),
    layoutConfig: TableLayoutConfig
) {
    val backgroundColor = if (index % 2 == 0) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    val dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
    val columnConfigDict = columnConfigs.associateBy { it.key }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(1.dp, dividerColor),
                shape = MaterialTheme.shapes.medium
            ),
        color = backgroundColor,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(layoutConfig.rowHeightDp.dp)
                .horizontalScroll(horizontalScrollState)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 行左侧插槽（如复选框）
            rowLeftSlot(item, index)
            // 数据列（按列配置宽度渲染）
            columns.forEach { column ->
                val columnKey = getColumnKey(column)
                val columnConfig = columnConfigDict[columnKey]
                Box(
                    modifier = Modifier
                        .width((columnConfig?.width ?: layoutConfig.defaultColumnWidthDp).dp)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    getCellContent(item, column)
                }
            }

//            if (showActionColumn) {
//                 右侧为固定操作列预留占位，避免数据列遮挡操作表头
//                Spacer(modifier = Modifier.width(layoutConfig.actionColumnWidthDp.dp).fillMaxHeight())
//            }
        }
    }
}


