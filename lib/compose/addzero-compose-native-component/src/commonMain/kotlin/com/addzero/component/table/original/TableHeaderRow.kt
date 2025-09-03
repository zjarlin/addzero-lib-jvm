package com.addzero.component.table.original

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.addzero.component.card.AddCard

/**
 * 完整表头行 - 包含序号列、数据列、操作列
 */
@Composable
fun <T, C> TableHeaderRow(
    params: TableParams<T, C>,
    columnWidths: Map<String, Dp>,
    horizontalScrollState: ScrollState
) {
    val getColumnKey = params.getColumnKey
    val getColumnLabel = params.getColumnLabel
    val showActions = params.config.showActions
    AddCard(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        padding = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            // 序号列
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight(), contentAlignment = Alignment.Center
            ) {
                Text(
                    "#",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            }

            // 数据列
            params.columns.forEach { column ->
                Box(
                    modifier = Modifier.Companion
                        .width(columnWidths[getColumnKey(column)] ?: 100.dp)
//                        .clickable(onClick = {
//                        })
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart
                ) {
                    Row {
//                        params.slots.columnLeftSlot()
                        getColumnLabel(column)
//                        params.slots.columnRightSlot()
                    }
                }
            }

            // 操作列
            if (showActions) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .fillMaxHeight(), contentAlignment = Alignment.Center
                ) {
                    Text(
                        "操作",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
