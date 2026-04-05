package site.addzero.component.table.original

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import site.addzero.component.table.original.entity.ColumnConfig
import site.addzero.component.table.original.entity.TableLayoutConfig
import site.addzero.component.table.original.render.RenderFixedActionColumn
import site.addzero.component.table.original.render.RenderFixedIndexColumn
import site.addzero.component.table.original.render.RenderTableScrollableContent
import site.addzero.component.table.original.tools.rememberAddTableAutoWidth
import site.addzero.core.ext.bean2map

/**
 * 底层自由拼装表格。
 *
 * 负责固定索引列、固定操作列、横向滚动内容区和上下插槽布局，
 * 上层业务表格可以在这一层之上继续叠加搜索、分页和筛选能力。
 */
@Composable
fun <T, C> TableOriginal(
    data: List<T>,
    columns: List<C>,
    getColumnKey: (C) -> String,
    getRowId: ((T) -> Any)? = null,
    columnConfigs: List<ColumnConfig>,
    layoutConfig: TableLayoutConfig = TableLayoutConfig(),
    getColumnLabel: (@Composable (C) -> Unit)? = null,
    topSlot: (@Composable () -> Unit)? = null,
    bottomSlot: (@Composable () -> Unit)? = null,
    emptyContentSlot: (@Composable () -> Unit)? = null,
    getCellContent: (@Composable (item: T, column: C) -> Unit)? = null,
    rowLeftSlot: (@Composable (item: T, index: Int) -> Unit)? = null,
    rowActionSlot: (@Composable (item: T) -> Unit)? = null,
    modifier: Modifier = Modifier,
    columnRightSlot: @Composable ((C) -> Unit)? = null,
) {
    val actualGetRowId = getRowId ?: { item ->
        val mapped = item?.bean2map()
        mapped?.get("id") ?: item.hashCode()
    }
    val actualGetColumnLabel = getColumnLabel ?: { column ->
        val columnKey = getColumnKey(column)
        val labelText = columnConfigs
            .find { config -> config.key == columnKey }
            ?.comment
            ?.ifBlank { columnKey }
            ?: columnKey
        Text(
            text = labelText,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleSmall,
        )
    }
    val actualTopSlot = topSlot ?: {}
    val actualBottomSlot = bottomSlot ?: {}
    val actualEmptyContentSlot = emptyContentSlot ?: {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "没有数据",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    val actualGetCellContent = getCellContent ?: { item, column ->
        val mapped = item?.bean2map()
        val valueText = (mapped?.get(getColumnKey(column)) ?: "").toString()
        Text(
            text = valueText,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    val actualRowLeftSlot = rowLeftSlot ?: { _, _ -> }

    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberLazyListState()
    val tableStyle = rememberTableVisualStyle()

    val autoWidths = rememberAddTableAutoWidth(
        data = data,
        columns = columns,
        getColumnKey = getColumnKey,
        getCellText = { item, column ->
            val mapped = item?.bean2map()
            (mapped?.get(getColumnKey(column)) ?: "").toString()
        },
        layoutConfig = layoutConfig,
        headerTextStyle = MaterialTheme.typography.titleSmall,
        cellTextStyle = MaterialTheme.typography.bodyMedium,
    )

    val mergedColumnConfigs = remember(columnConfigs, autoWidths) {
        if (autoWidths.isEmpty()) {
            columnConfigs
        } else {
            columnConfigs.map { config ->
                val autoWidth = autoWidths[config.key]
                if (autoWidth != null) {
                    config.copy(width = autoWidth)
                } else {
                    config
                }
            }
        }
    }

    val showLeftSlot = rowLeftSlot != null
    val showFixedActionColumn = rowActionSlot != null
    val actualRowActionSlot = rowActionSlot

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        actualTopSlot()

        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = tableStyle.shellContainer,
            border = BorderStroke(
                width = 1.dp,
                color = tableStyle.shellBorder,
            ),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                RenderTableScrollableContent(
                    data = data,
                    columns = columns,
                    getColumnKey = getColumnKey,
                    getRowId = actualGetRowId,
                    horizontalScrollState = horizontalScrollState,
                    lazyListState = verticalScrollState,
                    columnConfigs = mergedColumnConfigs,
                    layoutConfig = layoutConfig,
                    showLeftSlot = showLeftSlot,
                    showActionColumn = showFixedActionColumn,
                    getColumnLabel = actualGetColumnLabel,
                    emptyContentSlot = actualEmptyContentSlot,
                    getCellContent = actualGetCellContent,
                    rowLeftSlot = actualRowLeftSlot,
                    columnRightSlot = columnRightSlot ?: {},
                )

                RenderFixedIndexColumn(
                    verticalScrollState = verticalScrollState,
                    data = data,
                    layoutConfig = layoutConfig,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .zIndex(2f),
                )

                if (actualRowActionSlot != null) {
                    RenderFixedActionColumn(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .zIndex(3f),
                        verticalScrollState = verticalScrollState,
                        data = data,
                        layoutConfig = layoutConfig,
                        rowActionSlot = actualRowActionSlot,
                    )
                }
            }
        }

        actualBottomSlot()
    }
}
