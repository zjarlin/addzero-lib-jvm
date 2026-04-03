package site.addzero.component.table.biz

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.runtime.Composable
import site.addzero.component.table.original.entity.ColumnConfig
import site.addzero.entity.low_table.EnumSortDirection

/**
 * 列头排序按钮。
 */
@Composable
fun <C> RenderSortButton(
    column: C,
    getColumnKey: (C) -> String,
    columnConfigs: List<ColumnConfig>,
    sortDirection: EnumSortDirection,
    onClick: () -> Unit,
) {
    val columnKey = getColumnKey(column)
    val columnConfig = columnConfigs.find { it.key == columnKey }
    val showSort = columnConfig?.showSort ?: true

    if (!showSort) {
        return
    }

    val (text, icon) = when (sortDirection) {
        EnumSortDirection.ASC -> "升序" to Icons.Default.ArrowUpward
        EnumSortDirection.DESC -> "降序" to Icons.Default.ArrowDownward
        else -> "默认" to Icons.AutoMirrored.Filled.Sort
    }

    TableHeaderActionIcon(
        label = text,
        icon = icon,
        onClick = onClick,
        active = sortDirection != EnumSortDirection.NONE,
    )
}
