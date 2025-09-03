package com.addzero.component.table.original.render

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.addzero.component.card.AddCard

/**
 * 渲染固定操作列 - 使用细粒度参数
 */
@Composable
fun <T> RenderFixedActionColumn(
    verticalScrollState: LazyListState,
    data: List<T>,
    modifier: Modifier = Modifier.Companion,
    rowActionSlot: @Composable (item: T) -> Unit,
) {
    val density = LocalDensity.current
    val layoutInfo = verticalScrollState.layoutInfo

    Surface(
        modifier = modifier.width(120.dp).fillMaxHeight().clipToBounds(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 固定表头
            AddCard {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
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
                modifier = Modifier.fillMaxSize().clipToBounds()
            ) {
                if (data.isNotEmpty()) {
                    // 只渲染可见的操作项，与主表格完全同步
                    val visibleItemsInfo = layoutInfo.visibleItemsInfo
                    visibleItemsInfo.forEachIndexed { _, itemInfo ->
                        val itemIndex = itemInfo.index
                        if (itemIndex < data.size) {
                            val item = data[itemIndex]
                            val itemOffset = with(density) { itemInfo.offset.toDp() }
                            Surface(
                                modifier = Modifier.fillMaxWidth().height(60.dp) // 56dp + 4dp padding
                                    .offset(y = itemOffset),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize().padding(vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // 传递行数据和索引给操作插槽
                                    rowActionSlot(item)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
