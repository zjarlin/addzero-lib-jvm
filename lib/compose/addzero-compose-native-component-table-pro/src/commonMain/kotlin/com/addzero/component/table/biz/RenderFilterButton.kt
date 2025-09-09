package com.addzero.component.table.biz

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Filter1
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.addzero.component.button.AddIconButton
import com.addzero.component.table.original.entity.ColumnConfig
import com.addzero.component.table.vm.TableFilterViewModel

@Composable
context(tableSortViewModel: TableFilterViewModel<C>, columnConfigs:
List<ColumnConfig>)
fun <C> RenderFilterButton(column: C, getColumnKey: (C) -> String) {

    val columnKey = getColumnKey(column)
    val columnConfig = columnKey.findConfig()
    val showSort = columnConfig?.showFilter?:true
    if (!showSort) {
        return
    }

    AddIconButton(
        text = "高级搜索",
        imageVector = Icons.Default.Filter1,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(16.dp)
    ) {
        tableSortViewModel.showFieldAdvSearchDrawer = tableSortViewModel.showFieldAdvSearchDrawer.not()
    }
}

context(columnConfigs: List<ColumnConfig>)
private fun String.findConfig(): ColumnConfig? {
    val find = columnConfigs.find { it.key == this }
    return find

}
