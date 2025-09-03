package com.addzero.component.table.original.render

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 渲染完整数据行 - 使用细粒度参数
 */
@Composable
fun <T, C> RenderTableBodyRow(
    item: T,
    index: Int,
    columns: List<C>,
    getCellContent: @Composable ((item: T, column: C) -> Unit),
    horizontalScrollState: ScrollState,
    rowLeftSlot: @Composable ((item: T, index: Int) -> Unit),
    rowActionSlot: @Composable ((item: T) -> Unit)
) {
    val backgroundColor = if (index % 2 == 0) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    val dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)

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
                .height(56.dp)
                .horizontalScroll(horizontalScrollState)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 行左侧插槽（如复选框）
            rowLeftSlot(item, index)

            // 序号列
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${index + 1}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 数据列
            columns.forEachIndexed { columnIndex, column ->
                Box(
                    modifier = Modifier
                        .width(150.dp) // 使用默认列宽
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    getCellContent(item, column)
                }
            }
            // 操作列
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                rowActionSlot(item)
            }
        }
    }
}


