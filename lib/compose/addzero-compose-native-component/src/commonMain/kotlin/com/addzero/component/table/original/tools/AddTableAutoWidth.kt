package com.addzero.component.table.original.tools

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import com.addzero.component.table.original.TableLayoutConfig
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * 按内容自适应列宽计算（异步、采样）
 */
@Composable
fun <T, C> rememberAddTableAutoWidth(
    data: List<T>,
    columns: List<C>,
    getColumnKey: (C) -> String,
    getCellText: (item: T, column: C) -> String,
    layoutConfig: TableLayoutConfig,
    headerTextStyle: TextStyle,
    cellTextStyle: TextStyle
): Map<String, Float> {
    if (!layoutConfig.enableAutoWidth || data.isEmpty() || columns.isEmpty()) return emptyMap()

    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val sampleSize = minOf(layoutConfig.autoWidthSampleRows, data.size)
    val sampled = remember(data, sampleSize) { data.take(sampleSize) }
    val fontWeightHeader = FontWeight.Medium

    var widths by remember(columns, layoutConfig, sampled) { mutableStateOf<Map<String, Float>>(emptyMap()) }

    LaunchedEffect(columns, sampled, layoutConfig) {
        // 在组合的协程作用域中并发计算，安全使用 awaitAll
        widths = coroutineScope {
            val jobs = columns.map { column ->
                async {
                    val key = getColumnKey(column)
                    val headerWidthPx = measurer.measure(
                        text = key,
                        style = headerTextStyle.copy(fontWeight = fontWeightHeader)
                    ).size.width
                    val cellWidthsPx = sampled.map { item ->
                        measurer.measure(
                            text = getCellText(item, column),
                            style = cellTextStyle
                        ).size.width
                    }
                    val maxPx = (cellWidthsPx + listOf(headerWidthPx)).maxOrNull() ?: 0
                    with(density) {
                        val dp = maxPx.toDp().value + 32f // padding余量
                        val clamped = dp.coerceIn(layoutConfig.autoWidthMinDp, layoutConfig.autoWidthMaxDp)
                        key to clamped
                    }
                }
            }
            jobs.awaitAll().toMap()
        }
    }

    return widths
} 