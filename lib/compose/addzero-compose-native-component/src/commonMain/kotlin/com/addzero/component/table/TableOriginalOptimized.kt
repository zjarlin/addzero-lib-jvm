package com.addzero.component.table

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.addzero.component.card.AddCard
import com.addzero.component.card.MellumCardType

/**
 * 高性能表格组件 - 优化架构
 * 核心思路：单一LazyColumn虚拟化整行 + 固定列叠加
 */
@Composable
fun <T, C> TableOriginalOptimized(
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
    
    // 获取Typography样式
    val titleStyle = MaterialTheme.typography.titleSmall
    val bodyStyle = MaterialTheme.typography.bodyMedium
    
    // 计算列宽度
    val columnWidths by remember(columns, data, titleStyle, bodyStyle) {
        derivedStateOf {
            columns.associate { column ->
                getColumnKey(column) to run {
                    getColumnWidth(column) ?: run {
                        val headerWidth = with(density) {
                            textMeasurer.measure(
                                text = getColumnKey(column),
                                style = titleStyle
                            ).size.width.toDp()
                        }

                        val sampleData = data.take(3)
                        val maxContentWidth = if (sampleData.isNotEmpty()) {
                            sampleData.maxOfOrNull { 
                                with(density) {
                                    textMeasurer.measure(
                                        text = it.toString().take(15),
                                        style = bodyStyle
                                    ).size.width.toDp()
                                }
                            } ?: 0.dp
                        } else 0.dp

                        val calculatedWidth = maxOf(headerWidth.value + 32, maxContentWidth.value + 16, 80f).dp
                        calculatedWidth.coerceIn(getColumnMinWidth(column), getColumnMaxWidth(column))
                    }
                }
            }
        }
    }
    
    // 计算智能列宽 - 少字段时拉伸，多字段时固定
    val intelligentColumnWidths by remember {
        derivedStateOf {
            val totalRequiredWidth = columnWidths.values.sumOf { it.value.toDouble() }.dp
            val availableWidth = 800.dp // 假设可用宽度，实际应从父组件获取
            val hasSpace = totalRequiredWidth < availableWidth && columns.size <= 6
            
            if (hasSpace) {
                // 字段较少时平均分配剩余空间
                val extraWidth = (availableWidth - totalRequiredWidth) / columns.size
                columnWidths.mapValues { (_, width) -> width + extraWidth }
            } else {
                columnWidths
            }
        }
    }
    
    // 共享水平滚动状态 - 表头和数据行同步
    val horizontalScrollState = rememberScrollState()
    
    // 固定列宽度
    val indexColumnWidth = 80.dp
    val actionColumnWidth = if (rowActions != null) 120.dp else 0.dp

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

        // 表格主体 - 叠加架构
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            
            // 主表格区域 - 单一LazyColumn虚拟化整行
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(
                    start = indexColumnWidth + 8.dp, // 为序号列留空间
                    end = actionColumnWidth + 8.dp   // 为操作列留空间
                )
            ) {
                // 表头行
                item {
                    TableHeaderRow(
                        columns = columns,
                        columnWidths = intelligentColumnWidths,
                        getColumnKey = getColumnKey,
                        getColumnLabel = getColumnLabel,
                        horizontalScrollState = horizontalScrollState,
                        headerCardType = headerCardType,
                        headerCornerRadius = headerCornerRadius,
                        headerElevation = headerElevation
                    )
                }
                
                // 数据行 - 虚拟化
                if (data.isEmpty()) {
                    item {
                        (slots.emptyContent ?: DefaultTableSlots.DefaultEmptyContent())()
                    }
                } else {
                    itemsIndexed(data) { index, item ->
                        TableDataRow(
                            item = item,
                            index = index,
                            columns = columns,
                            columnWidths = intelligentColumnWidths,
                            getColumnKey = getColumnKey,
                            getCellContent = getCellContent,
                            horizontalScrollState = horizontalScrollState
                        )
                    }
                }
            }
            
            // 序号列 - 叠加在左侧
            IndexColumnOverlay(
                data = data,
                width = indexColumnWidth,
                headerCardType = headerCardType,
                headerCornerRadius = headerCornerRadius,
                headerElevation = headerElevation,
                modifier = Modifier.align(Alignment.CenterStart).zIndex(1f)
            )
            
            // 操作列 - 叠加在右侧
            rowActions?.let { actions ->
                ActionColumnOverlay(
                    data = data,
                    rowActions = actions,
                    width = actionColumnWidth,
                    headerCardType = headerCardType,
                    headerCornerRadius = headerCornerRadius,
                    headerElevation = headerElevation,
                    modifier = Modifier.align(Alignment.CenterEnd).zIndex(1f)
                )
            }
        }

        // 固定分页区域
        slots.pagination?.invoke()
    }
}

/**
 * 表头行 - 支持水平滚动
 */
@Composable
private fun <C> TableHeaderRow(
    columns: List<C>,
    columnWidths: Map<String, Dp>,
    getColumnKey: (C) -> String,
    getColumnLabel: @Composable (C) -> Unit,
    horizontalScrollState: androidx.compose.foundation.ScrollState,
    headerCardType: MellumCardType,
    headerCornerRadius: Dp,
    headerElevation: Dp
) {
    AddCard(
        onClick = {},
        modifier = Modifier.fillMaxWidth().height(56.dp),
        backgroundType = headerCardType,
        cornerRadius = headerCornerRadius,
        elevation = headerElevation,
        padding = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(horizontalScrollState)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            columns.forEach { column ->
                Box(
                    modifier = Modifier
                        .width(columnWidths[getColumnKey(column)] ?: 100.dp)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    getColumnLabel(column)
                }
            }
        }
    }
}

/**
 * 数据行 - 支持水平滚动，与表头同步
 */
@Composable
private fun <T, C> TableDataRow(
    item: T,
    @Suppress("UNUSED_PARAMETER") index: Int,
    columns: List<C>,
    columnWidths: Map<String, Dp>,
    getColumnKey: (C) -> String,
    getCellContent: @Composable (item: T, column: C) -> Unit,
    horizontalScrollState: androidx.compose.foundation.ScrollState
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .horizontalScroll(horizontalScrollState)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            columns.forEach { column ->
                Box(
                    modifier = Modifier
                        .width(columnWidths[getColumnKey(column)] ?: 100.dp)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    getCellContent(item, column)
                }
            }
        }
    }
}

/**
 * 序号列叠加层 - 固定在左侧
 */
@Composable
private fun <T> IndexColumnOverlay(
    data: List<T>,
    width: Dp,
    headerCardType: MellumCardType,
    headerCornerRadius: Dp,
    headerElevation: Dp,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.width(width).fillMaxHeight().clipToBounds(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 表头
            AddCard(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(56.dp),
                backgroundType = headerCardType,
                cornerRadius = headerCornerRadius,
                elevation = headerElevation,
                padding = 0.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "#",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 数据行
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                if (data.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "无数据",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    itemsIndexed(data) { index, _ ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp), // 包含padding的总高度
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${index + 1}",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 操作列叠加层 - 固定在右侧
 */
@Composable
private fun <T> ActionColumnOverlay(
    data: List<T>,
    rowActions: @Composable (item: T, index: Int) -> Unit,
    width: Dp,
    headerCardType: MellumCardType,
    headerCornerRadius: Dp,
    headerElevation: Dp,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.width(width).fillMaxHeight().clipToBounds(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 表头
            AddCard(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(56.dp),
                backgroundType = headerCardType,
                cornerRadius = headerCornerRadius,
                elevation = headerElevation,
                padding = 0.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "操作",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 数据行操作
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) {
                if (data.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "无操作",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    itemsIndexed(data) { index, item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp), // 与数据行高度一致
                            contentAlignment = Alignment.Center
                        ) {
                            rowActions(item, index)
                        }
                    }
                }
            }
        }
    }
}