package site.addzero.component.sheet.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 网格渲染尺寸规格。
 */
@Immutable
data class SheetGridMetrics(
    val rowHeaderWidth: Dp = 72.dp,
    val columnWidth: Dp = 136.dp,
    val rowHeight: Dp = 38.dp,
    val formulaBarHeight: Dp = 56.dp,
    val sheetTabHeight: Dp = 42.dp,
)
