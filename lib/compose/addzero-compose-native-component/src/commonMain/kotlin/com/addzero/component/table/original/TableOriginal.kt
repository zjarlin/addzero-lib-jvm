package com.addzero.component.table.original

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.addzero.core.ext.toMap
import kotlinx.coroutines.delay

/**
 * 表格组件 - 单一LazyColumn行虚拟化架构
 * 解决多LazyColumn滚动不同步问题，支持大数据量和多字段
 */
@Composable
inline fun <reified T, C> TableOriginal(
    columns: List<C>,
    data: List<T>,
    noinline getColumnKey: (C) -> String,
    noinline getColumnLabel: @Composable (C) -> Unit,
    noinline getCellContent: @Composable ((item: T, column: C) -> Unit)?=null,
    noinline getRowId: (T) -> Any,
    modifier: Modifier = Modifier,
    config: TableConfig<C> = TableConfig(),
    slots: TableSlots<T> = TableSlots()
) {
    // 性能缓存 - 预计算Map转换
    val dataMapsCache by remember {
        derivedStateOf {
            data.associateWith { it!!.toMap() }
        }
    }
    val newCellConten by remember {
        derivedStateOf {
            getCellContent ?: @Composable { item, column ->
                val itemMap = dataMapsCache[item] ?: emptyMap()
                val columnKey = getColumnKey(column)
                val value = itemMap[columnKey]
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

            }
        }
    }


    val params = TableParams(
        columns = columns,
        data = data,
        getColumnKey = getColumnKey,
        getColumnLabel = getColumnLabel,
        getCellContent = newCellConten,
        getRowId = getRowId,
        config = config,
        slots = slots
    )


    val tableState = rememberTableState(params)

    TableLayout(
        tableState = tableState,
        params = params,
        modifier = modifier
    )
}

/**
 * 表格状态管理
 */
@Stable
data class TableState<T, C>(
    val columnWidths: State<Map<String, Dp>>,
    val visibleItemsInfo: State<List<LazyListItemInfo>>,
    val horizontalScrollState: ScrollState,
    val verticalScrollState: LazyListState
)

/**
 * 表格配置 - 统一管理所有配置项
 */
@Stable
data class TableConfig<C>(
    val getColumnWidth: (C) -> Dp? = { null },
    val getColumnMinWidth: (C) -> Dp = { 80.dp },
    val getColumnMaxWidth: (C) -> Dp = { 300.dp },
    val headerCardType: MellumCardType = MellumCardType.Light,
    val headerCornerRadius: Dp = 12.dp,
    val headerElevation: Dp = 2.dp,
    val enableVirtualization: Boolean = true,
    val sampleSizeForWidthCalculation: Int = 3
)

/**
 * 表格参数打包 - 解决组件拆分后参数传递冗余问题
 */
@Stable
data class TableParams<T, C>(
    val columns: List<C>,
    val data: List<T>,
    val getColumnKey: (C) -> String,
    val getColumnLabel: @Composable (C) -> Unit,
    val getCellContent: @Composable (item: T, column: C) -> Unit,
    val getRowId: (T) -> Any,
    val config: TableConfig<C>,
    val slots: TableSlots<T>
)

@Composable
fun <T, C> rememberTableState(
    params: TableParams<T, C>
): TableState<T, C> {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberLazyListState()

    // 异步列宽计算，避免阻塞主线程
    val columnWidths = rememberAsyncColumnWidths(params)

    // 共享可见项状态，避免重复计算
    val visibleItemsInfo = rememberVisibleItemsInfo(verticalScrollState)

    return remember(params.columns, params.data) {
        TableState(
            columnWidths = columnWidths,
            visibleItemsInfo = visibleItemsInfo,
            horizontalScrollState = horizontalScrollState,
            verticalScrollState = verticalScrollState
        )
    }
}

/**
 * 异步列宽计算，避免阻塞主线程
 */
@Composable
private fun <T, C> rememberAsyncColumnWidths(
    params: TableParams<T, C>
): State<Map<String, Dp>> {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val titleStyle = MaterialTheme.typography.titleSmall
    val bodyStyle = MaterialTheme.typography.bodyMedium

    return produceState(
        initialValue = params.columns.associate { params.getColumnKey(it) to params.config.getColumnMinWidth(it) },
        key1 = params.columns,
        key2 = params.data,
        key3 = params.config
    ) {
        // 异步计算列宽，不阻塞主线程
        delay(1) // 让出主线程

        val calculatedWidths = params.columns.associate { column ->
            params.getColumnKey(column) to run {
                params.config.getColumnWidth(column) ?: run {
                    val headerWidth = with(density) {
                        textMeasurer.measure(
                            text = params.getColumnKey(column),
                            style = titleStyle
                        ).size.width.toDp()
                    }

                    val sampleData = params.data.take(params.config.sampleSizeForWidthCalculation)
                    val maxContentWidth = if (sampleData.isNotEmpty()) {
                        sampleData.maxOfOrNull { item ->
                            with(density) {
                                textMeasurer.measure(
                                    text = item.toString().take(15),
                                    style = bodyStyle
                                ).size.width.toDp()
                            }
                        } ?: 0.dp
                    } else 0.dp

                    val calculatedWidth = maxOf(headerWidth.value + 32, maxContentWidth.value + 16, 80f).dp
                    calculatedWidth.coerceIn(params.config.getColumnMinWidth(column), params.config.getColumnMaxWidth(column))
                }
            }
        }

        value = calculatedWidths
    }
}

/**
 * 共享可见项状态，避免重复计算
 */
@Composable
private fun rememberVisibleItemsInfo(
    scrollState: LazyListState
): State<List<LazyListItemInfo>> {
    return remember {
        derivedStateOf {
            scrollState.layoutInfo.visibleItemsInfo
        }
    }
}

/**
 * 表格布局管理器 - 负责整体布局结构
 */
@Composable
fun <T, C> TableLayout(
    tableState: TableState<T, C>,
    params: TableParams<T, C>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // 顶部插槽区域
        TableTopSlots(params)

        // 主表格内容区域
        TableMainContent(
            tableState = tableState,
            params = params,
            modifier = Modifier.weight(1f)
        )

        // 底部插槽区域
        params.slots.bottomPagination?.invoke()
        params.slots.bottomSummary?.invoke(params.data.size)
    }
}

/**
 * 顶部插槽渲染器
 */
@Composable
private fun <T, C> TableTopSlots(
    params: TableParams<T, C>
) {
    // 顶部标题栏
    params.slots.topHeaderBar?.let { headerBar ->
        AddCard(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            backgroundType = params.config.headerCardType,
            cornerRadius = params.config.headerCornerRadius,
            elevation = params.config.headerElevation,
            padding = 16.dp
        ) {
            headerBar()
        }
    }

    // 顶部选择面板
    params.slots.topSelectionPanel?.invoke()
}

/**
 * 主表格内容区域 - 包含表头、数据行和固定列
 */
@Composable
private fun <T, C> TableMainContent(
    tableState: TableState<T, C>,
    params: TableParams<T, C>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        // 主内容滚动区域
        TableScrollableContent(
            tableState = tableState,
            params = params
        )

        // 序号列固定遮罩
        FixedIndexColumn(
            verticalScrollState = tableState.verticalScrollState,
            data = params.data,
            headerCardType = params.config.headerCardType,
            headerCornerRadius = params.config.headerCornerRadius,
            headerElevation = params.config.headerElevation,
            modifier = Modifier.align(Alignment.CenterStart).zIndex(1f)
        )

        // 操作列固定遮罩
        params.slots.rowActions?.let { actions ->
            FixedActionColumn(
                verticalScrollState = tableState.verticalScrollState,
                data = params.data,
                rowActions = actions,
                headerCardType = params.config.headerCardType,
                headerCornerRadius = params.config.headerCornerRadius,
                headerElevation = params.config.headerElevation,
                modifier = Modifier.align(Alignment.CenterEnd).zIndex(1f)
            )
        }
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
    horizontalScrollState: ScrollState
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
    horizontalScrollState: ScrollState
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
    verticalScrollState: LazyListState,
    data: List<T>,
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
    verticalScrollState: LazyListState,
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

/**
 * 可滚动内容区域 - 包含表头和数据行
 */
@Composable
private fun <T, C> TableScrollableContent(
    tableState: TableState<T, C>,
    params: TableParams<T, C>
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 使用现有的CompleteHeaderRow
        CompleteHeaderRow(
            columns = params.columns,
            columnWidths = tableState.columnWidths.value,
            getColumnKey = params.getColumnKey,
            getColumnLabel = params.getColumnLabel,
            hasRowActions = params.slots.rowActions != null,
            headerCardType = params.config.headerCardType,
            headerCornerRadius = params.config.headerCornerRadius,
            headerElevation = params.config.headerElevation,
            horizontalScrollState = tableState.horizontalScrollState
        )

        // 使用现有的LazyColumn数据渲染
        LazyColumn(
            state = tableState.verticalScrollState,
            modifier = Modifier.fillMaxSize()
        ) {
            if (params.data.isEmpty()) {
                item {
                    (params.slots.emptyStateContent ?: DefaultTableSlots.DefaultEmptyContent())()
                }
            } else {
                itemsIndexed(
                    items = params.data,
                    key = { _, item -> params.getRowId(item) }
                ) { index, item ->
                    CompleteDataRow(
                        item = item,
                        index = index,
                        columns = params.columns,
                        columnWidths = tableState.columnWidths.value,
                        getColumnKey = params.getColumnKey,
                        getCellContent = params.getCellContent,
                        rowActions = params.slots.rowActions,
                        horizontalScrollState = tableState.horizontalScrollState
                    )
                }
            }
        }
    }
}
