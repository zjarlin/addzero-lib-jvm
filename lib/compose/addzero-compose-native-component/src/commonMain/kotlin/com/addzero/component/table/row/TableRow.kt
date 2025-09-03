package com.addzero.component.table.row

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
context(tableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<T>, horizontalScrollState: ScrollState) fun <T> TableRow(
    item: T, onRowClick: ((T) -> Unit)?, rowCusTomRender: @Composable com.addzero.component.table.model.AddCleanColumn<T>.(T) -> Unit, index: Int, leftSloat: @Composable () -> Unit = {}, rightSloat: @Composable () -> Unit = {}
) {
    val getidFun = tableViewModel.getIdFun
    val isSelected = tableViewModel._selectedItemIds.contains(getidFun(item))
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(
        alpha = 0.3f
    )
    val rowNumber = (tableViewModel._pageState.currentPage - 1) * tableViewModel._pageState.pageSize + index + 1
    val rowHeight = tableViewModel.tableMetadata.rowheight.toInt().takeIf { it != 0 } ?: 36
//    val showActions = tableViewModel.tableMetadata.showactions.takeIf { !it }
    val showActions = true


    Row(
        modifier = Modifier.Companion.fillMaxWidth().height(rowHeight.dp).background(backgroundColor), verticalAlignment = Alignment.Companion.CenterVertically
    ) {
        // 固定列区域（复选框和行号）
        Row(
            modifier = Modifier.Companion.width(120.dp)  // 固定宽度
                .fillMaxHeight().background(backgroundColor).zIndex(1f), verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            // 多选框列
            leftSloat()

            // 行号列
            Box(
                modifier = Modifier.Companion.padding(horizontal = 4.dp).width(40.dp), contentAlignment = Alignment.Companion.Center
            ) {
                Text(
                    text = "$rowNumber", style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Companion.Bold
                    ), color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // 可滚动的数据列区域
        Row(
            // @RBAC_PERMISSION: table.row.click - 行点击权限
            modifier = Modifier.Companion.weight(1f).fillMaxHeight().horizontalScroll(horizontalScrollState).clickable(enabled = onRowClick != null) { onRowClick?.invoke(item) }.padding(start = 8.dp, end = 8.dp), verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            // 数据列
            val visibleColumns = tableViewModel.visibleColumns
            for (column in visibleColumns) {
                _root_ide_package_.com.addzero.component.table.header.column.RenderCell(
                    column, item
                )
            }
        }

        // 固定操作列区域
        if (showActions) {
            Box(
                modifier = Modifier.Companion.width(160.dp)  // 固定宽度
                    .fillMaxHeight().background(backgroundColor).zIndex(1f), contentAlignment = Alignment.Center
            ) {
                rightSloat()
            }
        }
    }
}
