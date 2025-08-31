package com.addzero.kmp.component.table.clean

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

interface TableHeaderScope<C> {
    val columns: List<C>
    val getLabel: (C) -> String
    val renderCheckbox: @Composable () -> Unit
    val renderSort: @Composable (C) -> Unit
    val renderAction: @Composable () -> Unit
    val columnCustomRender: (@Composable (C) -> Unit)?
}

/**
 * 表格头部组件
 */
@Composable
context(tableHeaderScope: TableHeaderScope<C>)
fun <C> AddCleanTableHeader() {
    val horizontalScrollState = rememberScrollState()
    
    // 默认的列渲染器
    val columnRender = tableHeaderScope.columnCustomRender ?: { column ->
        Text(
            text = tableHeaderScope.getLabel(column),
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium
            )
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FixedColumn(tableHeaderScope.renderCheckbox)
        
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .horizontalScroll(horizontalScrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tableHeaderScope.columns.forEach { column ->
                HeaderColumn(
                    column = column,
                    columnRender = columnRender,
                    renderSort = tableHeaderScope.renderSort
                )
            }
        }
        
        tableHeaderScope.renderAction()
    }
}

@Composable
private fun <C> HeaderColumn(
    column: C,
    columnRender: @Composable (C) -> Unit,
    renderSort: @Composable (C) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(160.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            columnRender(column)
            renderSort(column)
        }
    }
}



@Composable
private fun FixedColumn(
    renderCheckbox: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .width(120.dp)
            .fillMaxHeight()
            .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        renderCheckbox()
        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .width(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}



