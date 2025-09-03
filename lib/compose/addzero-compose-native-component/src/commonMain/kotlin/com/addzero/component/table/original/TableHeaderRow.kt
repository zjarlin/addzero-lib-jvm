package com.addzero.component.table.original

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
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
import com.addzero.component.card.MellumCardType

/**
 * 完整表头行 - 包含序号列、数据列、操作列
 */
@Composable
 fun <C> TableHeaderRow(
    columns: List<C>,
    columnWidths: Map<String, Dp>,
    getColumnKey: (C) -> String,
    getColumnLabel: @Composable (C) -> Unit,
    showActions: Boolean,
    headerCardType: MellumCardType,
    headerCornerRadius: Dp,
    headerElevation: Dp,
    horizontalScrollState: ScrollState
) {
    AddCard(
        modifier = Modifier.Companion.fillMaxWidth().height(56.dp),
        backgroundType = headerCardType,
        cornerRadius = headerCornerRadius,
        elevation = headerElevation,
        padding = 0.dp
    ) {
        Row(
            modifier = Modifier.Companion
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            // 序号列
            Box(
                modifier = Modifier.Companion
                    .width(80.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Companion.Center
            ) {
                Text(
                    "#",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Companion.Bold),
                    textAlign = TextAlign.Companion.Center
                )
            }

            // 数据列
            columns.forEach { column ->
                Box(
                    modifier = Modifier.Companion
                        .width(columnWidths[getColumnKey(column)] ?: 100.dp)
                        .clickable(onClick = {
                        })
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Companion.CenterStart
                ) {
                    Row {

                        getColumnLabel(column)

                    }
                }
            }

            // 操作列
            if (showActions) {
                Box(
                    modifier = Modifier.Companion
                        .width(120.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Companion.Center
                ) {
                    Text(
                        "操作",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Companion.Bold),
                        textAlign = TextAlign.Companion.Center
                    )
                }
            }
        }
    }
}
