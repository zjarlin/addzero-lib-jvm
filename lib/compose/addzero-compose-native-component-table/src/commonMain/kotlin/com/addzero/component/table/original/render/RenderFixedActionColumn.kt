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
 * 渲染固定操作列 - 表头 + 行操作
 */
@Composable
fun <T> RenderFixedActionColumn(
    verticalScrollState: LazyListState,
    data: List<T>,
    layoutConfig: TableLayoutConfig,
    modifier: Modifier = Modifier.Companion,
    rowActionSlot: @Composable (item: T) -> Unit,
) {
    val density = LocalDensity.current
    val layoutInfo = verticalScrollState.layoutInfo

    Surface(
        modifier = modifier.width(layoutConfig.actionColumnWidthDp.dp).fillMaxHeight().clipToBounds(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 固定表头
            AddCard(modifier = Modifier.height(layoutConfig.headerHeightDp.dp), padding = 0.dp) {
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

            // 行操作内容（与主列表滚动同步）
            Box(modifier = Modifier.fillMaxSize().clipToBounds()) {
                if (data.isNotEmpty()) {
                    layoutInfo.visibleItemsInfo.forEach { itemInfo ->
                        val index = itemInfo.index
                        if (index < data.size) {
                            val item = data[index]
                            val y = with(density) { itemInfo.offset.toDp() }
                            Surface(
                                modifier = Modifier.fillMaxWidth().height(layoutConfig.rowHeightDp.dp).offset(y = y),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surface,
                                tonalElevation = 1.dp
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
