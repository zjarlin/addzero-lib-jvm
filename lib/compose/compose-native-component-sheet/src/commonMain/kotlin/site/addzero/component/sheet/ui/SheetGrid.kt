package site.addzero.component.sheet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.ceil
import kotlin.math.floor
import site.addzero.component.sheet.engine.SheetCellAddress
import site.addzero.component.sheet.engine.SheetPage
import site.addzero.component.sheet.engine.SheetRange
import site.addzero.component.sheet.engine.SheetSelection
import site.addzero.component.sheet.engine.SheetSelectionMode
import site.addzero.component.sheet.engine.SheetViewport
import site.addzero.component.sheet.engine.sheetColumnName

/**
 * 单元格网格。
 */
@Composable
internal fun SheetGrid(
    page: SheetPage,
    selection: SheetSelection,
    editingCell: SheetCellAddress?,
    editingInput: String,
    viewport: SheetViewport,
    metrics: SheetGridMetrics,
    onViewportChange: (SheetViewport) -> Unit,
    onCellClick: (SheetCellAddress) -> Unit,
    onCellRangeSelect: (SheetRange) -> Unit,
    onRowHeaderClick: (Int) -> Unit,
    onColumnHeaderClick: (Int) -> Unit,
    onEditingInputChange: (String) -> Unit,
) {
    val density = LocalDensity.current
    val horizontalScrollState = rememberScrollState()
    val verticalListState = rememberLazyListState()

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
    ) {
        val visibleRowCount by remember(maxHeight, metrics.rowHeight) {
            derivedStateOf {
                maxOf(1, (maxHeight / metrics.rowHeight).toInt())
            }
        }
        val visibleColumnCount by remember(maxWidth, metrics.rowHeaderWidth, metrics.columnWidth) {
            derivedStateOf {
                maxOf(1, ((maxWidth - metrics.rowHeaderWidth) / metrics.columnWidth).toInt())
            }
        }

        LaunchedEffect(visibleRowCount, visibleColumnCount, viewport.zoomPercent) {
            val nextViewport = viewport.copy(
                visibleRowCount = visibleRowCount,
                visibleColumnCount = visibleColumnCount,
            )
            if (nextViewport != viewport) {
                onViewportChange(nextViewport)
            }
        }

        val columnWidthPx = with(density) { metrics.columnWidth.toPx() }.coerceAtLeast(1f)
        LaunchedEffect(horizontalScrollState, columnWidthPx) {
            snapshotFlow { horizontalScrollState.value }
                .distinctUntilChanged()
                .collect { scrollValue ->
                    val nextColumnOffset = (scrollValue / columnWidthPx).toInt().coerceAtLeast(0)
                    if (nextColumnOffset != viewport.columnOffset) {
                        onViewportChange(
                            viewport.copy(columnOffset = nextColumnOffset),
                        )
                    }
                }
        }

        LaunchedEffect(verticalListState) {
            snapshotFlow { verticalListState.firstVisibleItemIndex }
                .distinctUntilChanged()
                .collect { firstVisibleRow ->
                    if (firstVisibleRow != viewport.rowOffset) {
                        onViewportChange(
                            viewport.copy(rowOffset = firstVisibleRow),
                        )
                    }
                }
        }
        val rowHeightPx = with(density) { metrics.rowHeight.toPx() }.coerceAtLeast(1f)

        val visibleColumns by remember(page.columnCount, viewport.columnOffset, viewport.visibleColumnCount) {
            derivedStateOf {
                val start = viewport.columnOffset.coerceIn(0, maxOf(0, page.columnCount - 1))
                val endExclusive = (start + viewport.visibleColumnCount + 1)
                    .coerceAtMost(page.columnCount)
                (start until endExclusive).toList()
            }
        }
        val leadingSpacerWidth = metrics.columnWidth * viewport.columnOffset.toFloat()
        val trailingColumnCount = (page.columnCount - visibleColumns.lastOrNull().orMinusOne() - 1)
            .coerceAtLeast(0)
        val trailingSpacerWidth = metrics.columnWidth * trailingColumnCount.toFloat()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                SheetCornerCell(metrics = metrics)

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(horizontalScrollState),
                ) {
                    Spacer(modifier = Modifier.width(leadingSpacerWidth))
                    visibleColumns.forEach { columnIndex ->
                        SheetColumnHeaderCell(
                            label = sheetColumnName(columnIndex),
                            selected = selection.mode == SheetSelectionMode.COLUMN &&
                                selection.activeRange?.contains(
                                    SheetCellAddress(rowIndex = 0, columnIndex = columnIndex),
                                ) == true,
                            metrics = metrics,
                            onClick = {
                                onColumnHeaderClick(columnIndex)
                            },
                        )
                    }
                    Spacer(modifier = Modifier.width(trailingSpacerWidth))
                }
            }

            LazyColumn(
                state = verticalListState,
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    items = (0 until page.rowCount).toList(),
                    key = { rowIndex -> rowIndex },
                ) { rowIndex ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        SheetRowHeaderCell(
                            label = (rowIndex + 1).toString(),
                            selected = selection.mode == SheetSelectionMode.ROW &&
                                selection.activeRange?.contains(
                                    SheetCellAddress(rowIndex = rowIndex, columnIndex = 0),
                                ) == true,
                            metrics = metrics,
                            onClick = {
                                onRowHeaderClick(rowIndex)
                            },
                        )

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(horizontalScrollState),
                        ) {
                            Spacer(modifier = Modifier.width(leadingSpacerWidth))
                            visibleColumns.forEach { columnIndex ->
                                val address = SheetCellAddress(
                                    rowIndex = rowIndex,
                                    columnIndex = columnIndex,
                                )
                                val cellValue = page.cell(address)
                                val activeRange = selection.activeRange
                                val selected = activeRange?.contains(address) == true
                                val primarySelected = selection.primaryCell == address
                                val editing = editingCell == address

                                SheetGridCell(
                                    address = address,
                                    text = if (editing) {
                                        editingInput
                                    } else {
                                        cellValue?.display.orEmpty()
                                    },
                                    selected = selected,
                                    primarySelected = primarySelected,
                                    editing = editing,
                                    rowHeightPx = rowHeightPx,
                                    columnWidthPx = columnWidthPx,
                                    maxRowIndex = page.rowCount - 1,
                                    maxColumnIndex = page.columnCount - 1,
                                    metrics = metrics,
                                    onClick = {
                                        onCellClick(address)
                                    },
                                    onDragSelect = onCellRangeSelect,
                                    onEditingInputChange = onEditingInputChange,
                                )
                            }
                            Spacer(modifier = Modifier.width(trailingSpacerWidth))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetCornerCell(
    metrics: SheetGridMetrics,
) {
    Box(
        modifier = Modifier
            .width(metrics.rowHeaderWidth)
            .height(metrics.rowHeight)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "#",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SheetColumnHeaderCell(
    label: String,
    selected: Boolean,
    metrics: SheetGridMetrics,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(metrics.columnWidth)
            .height(metrics.rowHeight)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                },
            )
            .border(
                1.dp,
                if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SheetRowHeaderCell(
    label: String,
    selected: Boolean,
    metrics: SheetGridMetrics,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(metrics.rowHeaderWidth)
            .height(metrics.rowHeight)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLowest
                },
            )
            .border(
                1.dp,
                if (selected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SheetGridCell(
    address: SheetCellAddress,
    text: String,
    selected: Boolean,
    primarySelected: Boolean,
    editing: Boolean,
    rowHeightPx: Float,
    columnWidthPx: Float,
    maxRowIndex: Int,
    maxColumnIndex: Int,
    metrics: SheetGridMetrics,
    onClick: () -> Unit,
    onDragSelect: (SheetRange) -> Unit,
    onEditingInputChange: (String) -> Unit,
) {
    val containerColor = when {
        editing -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
        else -> MaterialTheme.colorScheme.surface
    }
    val borderColor = when {
        primarySelected || editing -> MaterialTheme.colorScheme.primary
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val pointerModifier = if (editing) {
        Modifier
    } else {
        Modifier.pointerInput(address, rowHeightPx, columnWidthPx, maxRowIndex, maxColumnIndex) {
            var totalDragX = 0f
            var totalDragY = 0f
            detectDragGestures(
                onDragStart = {
                    onDragSelect(SheetRange.single(address))
                },
                onDragEnd = {
                    totalDragX = 0f
                    totalDragY = 0f
                },
                onDragCancel = {
                    totalDragX = 0f
                    totalDragY = 0f
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    totalDragX += dragAmount.x
                    totalDragY += dragAmount.y
                    val target = SheetCellAddress(
                        rowIndex = (address.rowIndex + dragOffsetToIndexDelta(totalDragY, rowHeightPx))
                            .coerceIn(0, maxRowIndex),
                        columnIndex = (address.columnIndex + dragOffsetToIndexDelta(totalDragX, columnWidthPx))
                            .coerceIn(0, maxColumnIndex),
                    )
                    onDragSelect(
                        SheetRange(
                            start = address,
                            end = target,
                        ),
                    )
                },
            )
        }
    }

    Box(
        modifier = Modifier
            .width(metrics.columnWidth)
            .height(metrics.rowHeight)
            .background(containerColor)
            .border(
                width = if (primarySelected || editing) 1.5.dp else 1.dp,
                color = borderColor,
            )
            .then(pointerModifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (editing) {
            OutlinedTextField(
                value = text,
                onValueChange = onEditingInputChange,
                singleLine = true,
                modifier = Modifier.fillMaxSize(),
                textStyle = MaterialTheme.typography.bodySmall,
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
            )
        }
    }
}

private fun dragOffsetToIndexDelta(offsetPx: Float, cellSizePx: Float): Int {
    if (cellSizePx <= 0f) {
        return 0
    }
    val ratio = offsetPx / cellSizePx
    return if (ratio >= 0f) {
        floor(ratio).toInt()
    } else {
        ceil(ratio).toInt()
    }
}

private fun Int?.orMinusOne(): Int {
    return this ?: -1
}
