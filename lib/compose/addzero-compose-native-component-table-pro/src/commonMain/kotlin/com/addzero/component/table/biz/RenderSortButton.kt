package com.addzero.component.table.biz

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.addzero.component.button.AddIconButton
import com.addzero.component.table.original.entity.ColumnConfig
import com.addzero.component.table.vm.koin.TableSortViewModel
import com.addzero.entity.low_table.EnumSortDirection
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@Composable
context(tableSortViewModel: TableSortViewModel, columnConfigs: List<ColumnConfig>)

fun <C> RenderSortButton(column: C, getColumnKey: (C) -> String) {

    val columnKey = getColumnKey(column)
    val columnConfig = columnKey.findConfig()
    val showSort = columnConfig?.showSort?:true
    if (!showSort) {
        return
    }

    val sortDirection = tableSortViewModel.getSortDirection(
        columnKey
    )
    val (text, icon) = when (sortDirection) {
        EnumSortDirection.ASC -> "升序" to Icons.Default.ArrowUpward
        EnumSortDirection.DESC -> "降序" to Icons.Default.ArrowDownward
        else -> "默认" to Icons.AutoMirrored.Filled.Sort
    }

    AddIconButton(
        text = text,
        imageVector = icon,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(16.dp)
    ) {
        tableSortViewModel.changeSorting(columnKey)
    }
}

context(columnConfigs: List<ColumnConfig>)
private fun String.findConfig(): ColumnConfig? {
    val find = columnConfigs.find { it.key == this }
    return find

}
