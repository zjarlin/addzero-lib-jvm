package site.addzero.component.table.biz

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Filter1
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.component.button.AddIconButton
import site.addzero.component.table.original.entity.ColumnConfig
import site.addzero.entity.low_table.StateSearch

@Composable
fun <C> RenderFilterButton(
    column: C,
    getColumnKey: (C) -> String,
    columnConfigs: List<ColumnConfig>,
    hasFilter: Boolean,
    onClick: () -> Unit
) {
    val columnKey = getColumnKey(column)
    val columnConfig = columnConfigs.find { it.key == columnKey }
    val showFilter = columnConfig?.showFilter ?: true

    if (!showFilter) {
        return
    }

    AddIconButton(
        text = "高级搜索",
        imageVector = if (hasFilter) Icons.Default.Filter1 else Icons.Default.FilterAlt,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(16.dp)
    ) {
        onClick()
    }
}
