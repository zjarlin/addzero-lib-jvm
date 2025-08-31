package com.addzero.component.table

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.addzero.component.card.AddCard
import com.addzero.component.card.MellumCardType
import kotlin.math.max

/**
 * 表格列定义
 */
data class TableColumn<T>(
    val key: String,
    val label: String,
    val width: Dp? = null, // null表示自动计算宽度
    val minWidth: Dp = 80.dp,
    val maxWidth: Dp = 300.dp,
    val sortable: Boolean = false,
    val headerContent: (@Composable () -> Unit)? = null,
    val cellContent: @Composable (item: T) -> Unit
)

/**
 * 计算列宽度
 */
@Composable
private fun <T> calculateColumnWidths(
    columns: List<com.addzero.component.table.TableColumn<T>>,
    data: List<T>,
    textMeasurer: TextMeasurer
): List<Dp> {
    val density = LocalDensity.current

    return columns.map { column ->
        if (column.width != null) {
            column.width
        } else {
            // 计算表头文字宽度
            val headerWidth = with(density) {
                textMeasurer.measure(
                    text = column.label,
                    style = MaterialTheme.typography.titleSmall
                ).size.width.toDp()
            }

            // 估算数据内容宽度（取前几行样本）
            val sampleData = data.take(5)
            val maxContentWidth = if (sampleData.isNotEmpty()) {
                sampleData.maxOfOrNull { item ->
                    // 这里简化处理，实际应该根据cellContent渲染结果计算
                    with(density) {
                        textMeasurer.measure(
                            text = item.toString().take(20), // 简化处理
                            style = MaterialTheme.typography.bodyMedium
                        ).size.width.toDp()
                    }
                } ?: 0.dp
            } else 0.dp

            // 取较大值，但限制在最小最大宽度范围内
            val calculatedWidth = max(headerWidth.value + 32, maxContentWidth.value + 16).dp
            calculatedWidth.coerceIn(column.minWidth, column.maxWidth)
        }
    }
}

/**
 * 原始表格组件 - 确保表头和数据列完美对齐
 */
@Composable
fun <T> TableOriginal(
    columns: List<com.addzero.component.table.TableColumn<T>>,
    data: List<T>,
    modifier: Modifier = Modifier,
    // 可选插槽
    headerBar: @Composable () -> Unit = {},
    showCheckbox: Boolean = false,
    onItemChecked: ((T, Boolean) -> Unit)? = null,
    checkedItems: Set<T> = emptySet(),
    onHeaderSort: ((String) -> Unit)? = null,
    headerActions: @Composable () -> Unit = {},
    selectContent: @Composable () -> Unit = {},
    pagination: @Composable () -> Unit = {},
    emptyContent: @Composable () -> Unit = { _root_ide_package_.com.addzero.component.table.DefaultEmptyContent() },
    // AddCard 样式配置
    headerCardType: com.addzero.component.card.MellumCardType = _root_ide_package_.com.addzero.component.card.MellumCardType.Companion.Light,
    headerCornerRadius: Dp = 12.dp,
    headerElevation: Dp = 2.dp
) {
    val horizontalScrollState = rememberScrollState()
    val textMeasurer = rememberTextMeasurer()

    // 计算列宽度
    val columnWidths = _root_ide_package_.com.addzero.component.table.calculateColumnWidths(columns, data, textMeasurer)

    Column(modifier = modifier) {
        // 表格头部使用 AddCard - 固定不滚动
        _root_ide_package_.com.addzero.component.card.AddCard(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            backgroundType = headerCardType,
            cornerRadius = headerCornerRadius,
            elevation = headerElevation,
            padding = 0.dp
        ) {
            Column {
                // 头部栏
                if (headerBar != {}) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        headerBar()
                    }
                }

                // 表头
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 固定列（复选框和序号）- 不滚动
                    _root_ide_package_.com.addzero.component.table.FixedHeaderColumn(
                        showCheckbox = showCheckbox,
                        allChecked = data.isNotEmpty() && checkedItems.containsAll(data),
                        onAllCheckedChange = { isChecked ->
                            data.forEach { item ->
                                onItemChecked?.invoke(item, isChecked)
                            }
                        }
                    )

                    // 可滚动的列
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .horizontalScroll(horizontalScrollState),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        columns.forEachIndexed { index, column ->
                            _root_ide_package_.com.addzero.component.table.TableHeaderCell(
                                column = column,
                                width = columnWidths[index],
                                onSort = if (column.sortable) {
                                    { onHeaderSort?.invoke(column.key) }
                                } else null
                            )
                        }
                    }

                    // 操作列 - 固定不滚动
                    _root_ide_package_.com.addzero.component.table.ActionHeaderColumn(headerActions)
                }
            }
        }

        // 选择内容区域
        selectContent()

        // 表格内容 - 可垂直滚动
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (data.isEmpty()) {
                    item {
                        emptyContent()
                    }
                } else {
                    itemsIndexed(items = data) { index, item ->
                        _root_ide_package_.com.addzero.component.table.TableRow(
                            item = item,
                            index = index,
                            columns = columns,
                            columnWidths = columnWidths,
                            showCheckbox = showCheckbox,
                            isChecked = checkedItems.contains(item),
                            onCheckedChange = { isChecked ->
                                onItemChecked?.invoke(item, isChecked)
                            },
                            horizontalScrollState = horizontalScrollState
                        )

                        if (index < data.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier,
                                thickness = DividerDefaults.Thickness,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        // 分页控件
        pagination()
    }
}

@Composable
private fun <T> TableHeaderCell(
    column: com.addzero.component.table.TableColumn<T>,
    width: Dp,
    onSort: (() -> Unit)? = null
) {
    // 表头单元格与数据行卡片内部布局保持一致
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .padding(horizontal = 8.dp), // 与数据行卡片内部间距一致
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // 自定义头部内容或默认文本
            if (column.headerContent != null) {
                column.headerContent.invoke()
            } else {
                Text(
                    text = column.label,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }

            // 排序按钮
            if (onSort != null) {
                IconButton(
                    onClick = onSort,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "排序",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> TableRow(
    item: T,
    index: Int,
    columns: List<com.addzero.component.table.TableColumn<T>>,
    columnWidths: List<Dp>,
    showCheckbox: Boolean,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    horizontalScrollState: androidx.compose.foundation.ScrollState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 固定列（复选框和序号）- 不滚动
        _root_ide_package_.com.addzero.component.table.FixedDataColumn(
            index = index,
            showCheckbox = showCheckbox,
            isChecked = isChecked,
            onCheckedChange = onCheckedChange
        )

        // 整行数据作为一个"烟头"卡片
        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 1.dp
        ) {
            // 可滚动的数据列 - 在卡片内部水平排列
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScrollState)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                columns.forEachIndexed { columnIndex, column ->
                    Box(
                        modifier = Modifier
                            .width(columnWidths[columnIndex])
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        column.cellContent(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun FixedHeaderColumn(
    showCheckbox: Boolean,
    allChecked: Boolean,
    onAllCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .width(120.dp)
            .fillMaxHeight()
            .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showCheckbox) {
            Checkbox(
                checked = allChecked,
                onCheckedChange = onAllCheckedChange
            )
        }
        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .width(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun FixedDataColumn(
    index: Int,
    showCheckbox: Boolean,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .width(120.dp)
            .padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showCheckbox) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        }
        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .width(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${index + 1}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ActionHeaderColumn(
    headerActions: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        headerActions()
    }
}

@Composable
private fun DefaultEmptyContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "没有数据",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
