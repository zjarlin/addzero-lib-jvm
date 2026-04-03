package site.addzero.component.table.original.render

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
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
import site.addzero.component.table.original.entity.TableLayoutConfig
import site.addzero.component.table.original.rememberTableVisualStyle

/**
 * 渲染固定序号列。
 */
@Composable
fun <T> RenderFixedIndexColumn(
    verticalScrollState: LazyListState,
    data: List<T>,
    layoutConfig: TableLayoutConfig,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val layoutInfo = verticalScrollState.layoutInfo
    val tableStyle = rememberTableVisualStyle()

    Surface(
        modifier = modifier
            .width(layoutConfig.indexColumnWidthDp.dp)
            .fillMaxHeight()
            .clipToBounds(),
        color = tableStyle.fixedColumnContainer,
        border = BorderStroke(1.dp, tableStyle.fixedColumnBorder),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.height(layoutConfig.headerHeightDp.dp),
                color = tableStyle.headerContainer,
                border = BorderStroke(1.dp, tableStyle.rowBorder),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "#",
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        color = tableStyle.accentText,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds(),
                color = tableStyle.fixedColumnContainer,
            ) {
                if (data.isNotEmpty()) {
                    layoutInfo.visibleItemsInfo.forEach { itemInfo ->
                        val itemIndex = itemInfo.index
                        if (itemIndex < data.size) {
                            val itemOffset = with(density) { itemInfo.offset.toDp() }
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(layoutConfig.rowHeightDp.dp)
                                    .offset(y = itemOffset),
                                color = if (itemIndex % 2 == 0) {
                                    tableStyle.rowEvenContainer
                                } else {
                                    tableStyle.rowOddContainer
                                },
                                border = BorderStroke(1.dp, tableStyle.rowBorder),
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "${itemIndex + 1}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = tableStyle.accentText,
                                        textAlign = TextAlign.Center,
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
