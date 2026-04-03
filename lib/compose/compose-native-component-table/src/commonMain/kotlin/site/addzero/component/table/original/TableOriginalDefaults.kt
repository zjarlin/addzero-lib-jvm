package site.addzero.component.table.original

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import site.addzero.component.table.original.entity.ColumnConfig
import site.addzero.component.table.original.entity.TableLayoutConfig

/**
 * 表格底层渲染使用的统一视觉语义。
 */
@Immutable
internal data class TableVisualStyle(
    val shellContainer: Color,
    val shellBorder: Color,
    val headerContainer: Color,
    val headerText: Color,
    val rowEvenContainer: Color,
    val rowOddContainer: Color,
    val rowBorder: Color,
    val fixedColumnContainer: Color,
    val fixedColumnBorder: Color,
    val accentText: Color,
    val cellHorizontalPadding: Dp = 12.dp,
)

/**
 * 根据当前主题生成表格默认配色。
 */
@Composable
internal fun rememberTableVisualStyle(): TableVisualStyle {
    val scheme = MaterialTheme.colorScheme
    return TableVisualStyle(
        shellContainer = scheme.surface,
        shellBorder = scheme.outlineVariant.copy(alpha = 0.72f),
        headerContainer = scheme.primary.copy(alpha = 0.05f),
        headerText = scheme.onSurface,
        rowEvenContainer = scheme.surface,
        rowOddContainer = scheme.surfaceVariant.copy(alpha = 0.22f),
        rowBorder = scheme.outlineVariant.copy(alpha = 0.42f),
        fixedColumnContainer = scheme.surfaceContainerLow,
        fixedColumnBorder = scheme.outlineVariant.copy(alpha = 0.62f),
        accentText = scheme.primary,
    )
}

/**
 * 按列配置顺序生成稳定列顺序。
 */
internal fun <C> sortTableColumns(
    columns: List<C>,
    getColumnKey: (C) -> String,
    columnConfigs: List<ColumnConfig>,
): List<C> {
    val orderByKey = columnConfigs.associate { config ->
        config.key to config.order
    }
    return columns.withIndex()
        .sortedWith(
            compareBy<IndexedValue<C>> { indexedColumn ->
                orderByKey[getColumnKey(indexedColumn.value)] ?: Int.MAX_VALUE
            }.thenBy { indexedColumn ->
                indexedColumn.index
            },
        )
        .map { indexedColumn ->
            indexedColumn.value
        }
}

/**
 * 解析列宽，优先使用列配置里的显式宽度。
 */
internal fun ColumnConfig?.resolvedWidth(
    layoutConfig: TableLayoutConfig,
): Dp {
    return ((this?.width ?: layoutConfig.defaultColumnWidthDp).dp)
}
