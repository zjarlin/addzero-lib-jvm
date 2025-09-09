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
import com.addzero.component.table.vm.koin.TableSortViewModel
import com.addzero.entity.low_table.EnumSortDirection

@Composable
context(addCleanTableViewModel: TableSortViewModel)
fun <C> RenderSort(column: C, getColumnKey: (C) -> String) {
    val columnKey = getColumnKey(column)
    val sortDirection = addCleanTableViewModel.getSortDirection(
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
        addCleanTableViewModel.changeSorting(columnKey)
    }
}
