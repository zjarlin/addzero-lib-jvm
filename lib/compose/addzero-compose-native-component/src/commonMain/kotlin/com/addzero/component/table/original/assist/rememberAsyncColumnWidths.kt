package com.addzero.component.table.original.assist

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.addzero.component.table.original.TableParams
import kotlinx.coroutines.delay

/**
 * 异步列宽计算，避免阻塞主线程
 */
@Composable
 fun <T, C> rememberAsyncColumnWidths(
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
                    calculatedWidth.coerceIn(
                        params.config.getColumnMinWidth(column),
                        params.config.getColumnMaxWidth(column)
                    )
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
 fun rememberVisibleItemsInfo(
    scrollState: LazyListState
): State<List<LazyListItemInfo>> {
    return remember {
        derivedStateOf {
            scrollState.layoutInfo.visibleItemsInfo
        }
    }
}
