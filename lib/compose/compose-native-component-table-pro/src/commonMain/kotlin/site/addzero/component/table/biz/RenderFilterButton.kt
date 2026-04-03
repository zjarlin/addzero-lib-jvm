package site.addzero.component.table.biz

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Filter1
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.runtime.Composable
import site.addzero.component.table.original.entity.ColumnConfig

/**
 * 列头高级筛选按钮。
 */
@Composable
fun <C> RenderFilterButton(
    column: C,
    getColumnKey: (C) -> String,
    columnConfigs: List<ColumnConfig>,
    hasFilter: Boolean,
    onClick: () -> Unit,
) {
    val columnKey = getColumnKey(column)
    val columnConfig = columnConfigs.find { it.key == columnKey }
    val showFilter = columnConfig?.showFilter ?: true

    if (!showFilter) {
        return
    }

    TableHeaderActionIcon(
        label = "高级搜索",
        icon = if (hasFilter) Icons.Default.Filter1 else Icons.Default.FilterAlt,
        onClick = onClick,
        active = hasFilter,
    )
}
