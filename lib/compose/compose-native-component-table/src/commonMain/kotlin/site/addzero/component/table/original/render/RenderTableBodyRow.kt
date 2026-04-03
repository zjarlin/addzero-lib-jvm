package site.addzero.component.table.original.render

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.component.table.original.entity.ColumnConfig
import site.addzero.component.table.original.entity.TableLayoutConfig
import site.addzero.component.table.original.rememberTableVisualStyle
import site.addzero.component.table.original.resolvedWidth
import site.addzero.component.table.original.sortTableColumns

/**
 * 渲染完整数据行。
 */
@Composable
fun <T, C> RenderTableBodyRow(
    item: T,
    index: Int,
    columns: List<C>,
    getColumnKey: (C) -> String,
    columnConfigs: List<ColumnConfig>,
    getCellContent: @Composable ((item: T, column: C) -> Unit),
    horizontalScrollState: ScrollState,
    rowLeftSlot: @Composable ((item: T, index: Int) -> Unit),
    layoutConfig: TableLayoutConfig,
    showLeftSlot: Boolean = false,
    showActionColumn: Boolean = false,
) {
    val tableStyle = rememberTableVisualStyle()
    val columnConfigDict = columnConfigs.associateBy { config -> config.key }
    val backgroundColor = if (index % 2 == 0) {
        tableStyle.rowEvenContainer
    } else {
        tableStyle.rowOddContainer
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, tableStyle.rowBorder)),
        color = backgroundColor,
        tonalElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(layoutConfig.rowHeightDp.dp)
                .horizontalScroll(horizontalScrollState)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(
                modifier = Modifier
                    .width(layoutConfig.indexColumnWidthDp.dp)
                    .fillMaxHeight(),
            )

            if (showLeftSlot) {
                rowLeftSlot(item, index)
            }

            sortTableColumns(
                columns = columns,
                getColumnKey = getColumnKey,
                columnConfigs = columnConfigs,
            ).forEach { column ->
                val columnKey = getColumnKey(column)
                val columnConfig = columnConfigDict[columnKey]
                Box(
                    modifier = Modifier
                        .width(columnConfig.resolvedWidth(layoutConfig))
                        .fillMaxHeight()
                        .padding(horizontal = tableStyle.cellHorizontalPadding),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    getCellContent(item, column)
                }
            }

            if (showActionColumn) {
                Spacer(
                    modifier = Modifier
                        .width(layoutConfig.actionColumnWidthDp.dp)
                        .fillMaxHeight(),
                )
            }
        }
    }
}
