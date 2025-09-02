package com.addzero.component.table

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
 * 表格组件 - 单一LazyColumn行虚拟化架构
 * 解决多LazyColumn滚动不同步问题，支持大数据量和多字段
 */
@Composable
fun <T, C> TableOriginal(
    columns: List<C>,
    data: List<T>,
    getColumnKey: (C) -> String,
    getColumnLabel: @Composable (C) -> Unit,
    getCellContent: @Composable (item: T, column: C) -> Unit,
    getRowId: (T) -> Any,
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

    // 水平滚动状态 - 表头和数据行共享
    val horizontalScrollState = rememberScrollState()

    // 垂直滚动状态 - 单一LazyColumn
    val verticalScrollState = rememberLazyListState()

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

        // 表格主体 - 单一LazyColumn + 固定列叠加架构
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // 主表格区域 - 单一LazyColumn确保滚动同步
            Column(modifier = Modifier.fillMaxSize()) {
                // 完整表头行
                CompleteHeaderRow(
                    columns = columns,
                    columnWidths = columnWidths,
                    getColumnKey = getColumnKey,
                    getColumnLabel = getColumnLabel,
                    hasRowActions = rowActions != null,
                    headerCardType = headerCardType,
                    headerCornerRadius = headerCornerRadius,
                    headerElevation = headerElevation,
                    horizontalScrollState = horizontalScrollState
                )

                // 完整数据行 - 单一LazyColumn确保垂直滚动同步
                LazyColumn(
                    state = verticalScrollState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (data.isEmpty()) {
                        item {
                            (slots.emptyContent ?: DefaultTableSlots.DefaultEmptyContent())()
                        }
                    } else {
                        itemsIndexed(
                            items = data,
                            key = { _, item -> getRowId(item) }
                        ) { index, item ->
                            CompleteDataRow(
                                item = item,
                                index = index,
                                columns = columns,
                                columnWidths = columnWidths,
                                getColumnKey = getColumnKey,
                                getCellContent = getCellContent,
                                rowActions = rowActions,
                                horizontalScrollState = horizontalScrollState
                            )
                        }
                    }
                }
            }

            // 序号列固定遮罩 - 使用偏移同步
            FixedIndexColumn(
                verticalScrollState = verticalScrollState,
                data = data,
                headerCardType = headerCardType,
                headerCornerRadius = headerCornerRadius,
                headerElevation = headerElevation,
                modifier = Modifier.align(Alignment.CenterStart).zIndex(1f)
            )

            // 操作列固定遮罩 - 使用偏移同步
            rowActions?.let { actions ->
                FixedActionColumn(
                    verticalScrollState = verticalScrollState,
                    data = data,
                    rowActions = actions,
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
 * 完整表头行 - 包含序号列、数据列、操作列
 */
@Composable
private fun <C> CompleteHeaderRow(
    columns: List<C>,
    columnWidths: Map<String, Dp>,
    getColumnKey: (C) -> String,
    getColumnLabel: @Composable (C) -> Unit,
    hasRowActions: Boolean,
    headerCardType: MellumCardType,
    headerCornerRadius: Dp,
    headerElevation: Dp,
    horizontalScrollState: androidx.compose.foundation.ScrollState
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
            // 序号列
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "#",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
            }

            // 数据列
            columns.forEach { column ->
                Box(
                    modifier = Modifier
                        .width(columnWidths[getColumnKey(column)] ?: 100.dp)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    getColumnLabel(column)
                }
            }

            // 操作列
            if (hasRowActions) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "操作",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * 完整数据行 - 包含序号列、数据列、操作列
 */
@Composable
private fun <T, C> CompleteDataRow(
    item: T,
    index: Int,
    columns: List<C>,
    columnWidths: Map<String, Dp>,
    getColumnKey: (C) -> String,
    getCellContent: @Composable (item: T, column: C) -> Unit,
    rowActions: (@Composable (item: T, index: Int) -> Unit)?,
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
            // 序号列
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${index + 1}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            // 数据列
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

            // 操作列
            rowActions?.let { actions ->
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    actions(item, index)
                }
            }
        }
    }
}

/**
 * 固定序号列 - 通过监听主表格滚动偏移来同步显示
 */
@Composable
private fun <T> FixedIndexColumn(
    verticalScrollState: androidx.compose.foundation.lazy.LazyListState,
    data: List<T>,
    headerCardType: MellumCardType,
    headerCornerRadius: Dp,
    headerElevation: Dp,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    // 监听滚动状态获取第一个可见项
    val layoutInfo = verticalScrollState.layoutInfo
    val firstVisibleIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
    val firstVisibleOffset = layoutInfo.visibleItemsInfo.firstOrNull()?.offset ?: 0
    
    Surface(
        modifier = modifier
            .width(80.dp)
            .fillMaxHeight()
            .clipToBounds(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 固定表头
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
            
            // 序号内容区域 - 根据主表格滚动位置动态渲染可见项
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
            ) {
                if (data.isNotEmpty()) {
                    // 只渲染可见的序号项，与主表格完全同步
                    layoutInfo.visibleItemsInfo.forEachIndexed { _, itemInfo ->
                        val itemIndex = itemInfo.index
                        if (itemIndex < data.size) {
                            val itemOffset = with(density) { itemInfo.offset.toDp() }
                            
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp) // 56dp + 4dp padding
                                    .offset(y = itemOffset),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${itemIndex + 1}",
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
    }
}

/**
 * 固定操作列 - 通过监听主表格滚动偏移来同步显示
 */
@Composable
private fun <T> FixedActionColumn(
    verticalScrollState: androidx.compose.foundation.lazy.LazyListState,
    data: List<T>,
    rowActions: @Composable (item: T, index: Int) -> Unit,
    headerCardType: MellumCardType,
    headerCornerRadius: Dp,
    headerElevation: Dp,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    
    // 监听滚动状态获取可见项信息
    val layoutInfo = verticalScrollState.layoutInfo
    
    Surface(
        modifier = modifier
            .width(120.dp)
            .fillMaxHeight()
            .clipToBounds(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 固定表头
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
            
            // 操作内容区域 - 根据主表格滚动位置动态渲染可见项
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
            ) {
                if (data.isNotEmpty()) {
                    // 只渲染可见的操作项，与主表格完全同步
                    layoutInfo.visibleItemsInfo.forEachIndexed { _, itemInfo ->
                        val itemIndex = itemInfo.index
                        if (itemIndex < data.size) {
                            val itemOffset = with(density) { itemInfo.offset.toDp() }
                            
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp) // 56dp + 4dp padding
                                    .offset(y = itemOffset),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    rowActions(data[itemIndex], itemIndex)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
