package site.addzero.component.table.original.render

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
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
 * 渲染表头行。
 */
@Composable
fun <C> RenderTableHeaderRow(
    columns: List<C>,
    getColumnKey: (C) -> String,
    getColumnLabel: @Composable (C) -> Unit,
    columnRightSlot: @Composable (C) -> Unit,
    horizontalScrollState: ScrollState,
    columnConfigs: List<ColumnConfig>,
    layoutConfig: TableLayoutConfig,
    showLeftSlot: Boolean = false,
    showActionColumn: Boolean,
) {
    val tableStyle = rememberTableVisualStyle()
    val columnConfigDict = columnConfigs.associateBy { config -> config.key }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(layoutConfig.headerHeightDp.dp),
        color = tableStyle.headerContainer,
        border = BorderStroke(1.dp, tableStyle.rowBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(state = horizontalScrollState)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(
                modifier = Modifier
                    .width(layoutConfig.indexColumnWidthDp.dp)
                    .fillMaxHeight(),
            )

            if (showLeftSlot) {
                Spacer(
                    modifier = Modifier
                        .width(layoutConfig.leftSlotWidthDp.dp)
                        .fillMaxHeight(),
                )
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            getColumnLabel(column)
                        }
                        columnRightSlot(column)
                    }
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
