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
import com.addzero.component.table.original.entity.TableLayoutConfig

/**
 * 渲染固定序号列 - 使用细粒度参数
 */
@Composable
fun <T> RenderFixedIndexColumn(
    verticalScrollState: LazyListState,
    data: List<T>,
    layoutConfig: TableLayoutConfig,
    modifier: Modifier = Modifier.Companion
) {
    val density = LocalDensity.current

    // 监听滚动状态获取可见项信息
    val layoutInfo = verticalScrollState.layoutInfo

    Surface(
        modifier = modifier
            .width(layoutConfig.indexColumnWidthDp.dp)
            .fillMaxHeight()
            .clipToBounds(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.Companion.fillMaxSize()) {
            // 固定表头
            AddCard(modifier = Modifier.height(layoutConfig.headerHeightDp.dp), padding = 0.dp) {
                Box(
                    modifier = Modifier.Companion.fillMaxSize(),
                    contentAlignment = Alignment.Companion.Center
                ) {
                    Text(
                        "#",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Companion.Bold),
                        textAlign = TextAlign.Companion.Center
                    )
                }
            }

            // 序号内容区域 - 根据主表格滚动位置动态渲染可见项
            Box(
                modifier = Modifier.Companion
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
                                modifier = Modifier.Companion
                                    .fillMaxWidth()
                                    .height(layoutConfig.rowHeightDp.dp)
                                    .offset(y = itemOffset),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp
                            ) {
                                Box(
                                    modifier = Modifier.Companion
                                        .fillMaxSize(),
                                    contentAlignment = Alignment.Companion.Center
                                ) {
                                    Text(
                                        "${itemIndex + 1}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Companion.Center
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
