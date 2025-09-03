package com.addzero.component.table.original.render

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
import androidx.compose.ui.unit.dp
import com.addzero.component.card.AddCard
import com.addzero.component.table.original.ColumnConfig

/**
 * 渲染表头行 - 使用细粒度参数
 */
@Composable
fun <C> RenderTableHeaderRow(
    columns: List<C>,
    getColumnKey: (C) -> String,
    getColumnLabel: @Composable (C) -> Unit,
    horizontalScrollState: ScrollState,
    columnConfigs: List<ColumnConfig>
) {
    val columnConfigDict = columnConfigs.associateBy { it.key }
    AddCard(
        modifier = Modifier.fillMaxWidth().height(56.dp), padding = 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize().horizontalScroll(state = horizontalScrollState)
                .padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            // 序号列
            Box(
                modifier = Modifier.width(80.dp).fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "#",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            }
            // 数据列
            columns.forEach { column ->
                val columnKey = getColumnKey(column)
                val columnConfig = columnConfigDict[columnKey]
                Box(
                    modifier = Modifier.width(columnConfig?.width?.dp ?: 100.dp).fillMaxHeight()
                        .padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart
                ) {
                    Row {
                        getColumnLabel(column)
                    }
                }
            }
            // 操作列
            Box(
                modifier = Modifier.width(120.dp).fillMaxHeight(),
                contentAlignment = Alignment.Center
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
