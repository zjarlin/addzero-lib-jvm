package com.addzero.component.table

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.addzero.component.card.AddCard
import com.addzero.component.card.MellumCardType

/**
 * 转置表格组件 - 香烟垂直排列模式，简化统一架构
 * 使用统一的CigaretteColumn组件，支持大数据量和多字段
 */
@Composable
fun <T, C> TableOriginal(
    columns: List<C>,
    data: List<T>,
    getColumnKey: (C) -> String,
    getColumnLabel: @Composable (C) -> Unit,
    getCellContent: @Composable (item: T, column: C) -> Unit,
    modifier: Modifier = Modifier,
    slots: TableSlots<T> = TableSlots(),
    getColumnWidth: (C) -> Dp? = { null },
    getColumnMinWidth: (C) -> Dp = { 80.dp },
    getColumnMaxWidth: (C) -> Dp = { 300.dp },
    // 每行操作按钮
    rowActions: (@Composable (item: T, index: Int) -> Unit)? = null,
    // AddCard 样式配置
    headerCardType: MellumCardType = MellumCardType.Light,
    headerCornerRadius: Dp = 12.dp,
    headerElevation: Dp = 2.dp
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // 计算列宽度
    val columnWidths = columns.associate { column ->
        getColumnKey(column) to run {
            getColumnWidth(column) ?: run {
                val headerWidth = with(density) {
                    textMeasurer.measure(
                        text = getColumnKey(column),
                        style = MaterialTheme.typography.titleSmall
                    ).size.width.toDp()
                }

                val sampleData = data.take(3)
                val maxContentWidth = if (sampleData.isNotEmpty()) {
                    sampleData.maxOfOrNull {
                        with(density) {
                            textMeasurer.measure(
                                text = it.toString().take(15),
                                style = MaterialTheme.typography.bodyMedium
                            ).size.width.toDp()
                        }
                    } ?: 0.dp
                } else 0.dp

                val calculatedWidth = maxOf(headerWidth.value + 32, maxContentWidth.value + 16, 80f).dp
                calculatedWidth.coerceIn(getColumnMinWidth(column), getColumnMaxWidth(column))
            }
        }
    }

    // 共享滚动状态
    val sharedScrollState = rememberLazyListState()

    Column(modifier = modifier) {
        // 固定头部栏
        slots.headerBar?.let { headerBar ->
            AddCard(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                backgroundType = headerCardType,
                cornerRadius = headerCornerRadius,
                elevation = headerElevation,
                padding = 16.dp
            ) {
                headerBar()
            }
        }

        // 固定选择区域
        slots.selectContent?.invoke()

        // 表格主体
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxSize()) {
                // 序号列 - 固定
                CigaretteColumn(
                    width = 80.dp,
                    headerContent = {
                        Text(
                            "#",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                    },
                    data = data,
                    scrollState = sharedScrollState,
                    cellContent = { _, index ->
                        Text(
                            "${index + 1}",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    },
                    headerCardType = headerCardType,
                    headerCornerRadius = headerCornerRadius,
                    headerElevation = headerElevation,
                    modifier = Modifier
                )

                // 数据列 - 当字段少时拉伸填满，字段多时可滚动
                if (columns.size <= 5) {
                    // 字段较少时：使用Row平均分配空间，填满边界
                    Row(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        columns.forEach { column ->
                            CigaretteColumn(
                                width = 0.dp, // 让Column内部使用weight分配空间
                                modifier = Modifier.weight(1f),
                                headerContent = { getColumnLabel(column) },
                                data = data,
                                scrollState = sharedScrollState,
                                cellContent = { item, _ ->
                                    getCellContent(item, column)
                                },
                                headerCardType = headerCardType,
                                headerCornerRadius = headerCornerRadius,
                                headerElevation = headerElevation
                            )
                        }
                    }
                } else {
                    // 字段较多时：使用LazyRow水平滚动
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        itemsIndexed(columns) { _, column ->
                            CigaretteColumn(
                                width = columnWidths[getColumnKey(column)] ?: 100.dp,
                                headerContent = { getColumnLabel(column) },
                                data = data,
                                scrollState = sharedScrollState,
                                cellContent = { item, _ ->
                                    getCellContent(item, column)
                                },
                                headerCardType = headerCardType,
                                headerCornerRadius = headerCornerRadius,
                                headerElevation = headerElevation,
                                modifier = Modifier
                            )
                        }
                    }
                }

                // 操作列 - 固定
                rowActions?.let { actions ->
                    CigaretteColumn(
                        width = 120.dp,
                        headerContent = {
                            Text(
                                "操作",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
                            )
                        },
                        data = data,
                        scrollState = sharedScrollState,
                        cellContent = { item, index ->
                            actions(item, index)
                        },
                        headerCardType = headerCardType,
                        headerCornerRadius = headerCornerRadius,
                        headerElevation = headerElevation,
                        modifier = Modifier
                    )
                }
            }
        }

        // 固定分页区域
        slots.pagination?.invoke()
    }
}

/**
 * 统一的香烟列组件 - 所有列都使用相同结构，支持拉伸填满
 */
@Composable
private fun <T> CigaretteColumn(
    width: Dp,
    headerContent: @Composable () -> Unit,
    data: List<T>,
    scrollState: LazyListState,
    cellContent: @Composable (item: T, index: Int) -> Unit,
    headerCardType: MellumCardType,
    headerCornerRadius: Dp,
    headerElevation: Dp,
    modifier: Modifier = Modifier // 支持外部modifier，用于weight拉伸
) {
    val sizeModifier = if (width > 0.dp) {
        modifier.width(width).fillMaxHeight()
    } else {
        modifier.fillMaxHeight() // width=0时使用外部的weight
    }
    
    Surface(
        modifier = sizeModifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 烟嘴 - 固定表头
            AddCard(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(56.dp),
                backgroundType = headerCardType,
                cornerRadius = headerCornerRadius,
                elevation = headerElevation,
                padding = 0.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    headerContent()
                }
            }

            // 烟身 - 同步滚动的数据区域
            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                if (data.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "无数据",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    itemsIndexed(data) { index, item ->
                        Box(
                            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            cellContent(item, index)
                        }
                    }
                }
            }
        }
    }
}
