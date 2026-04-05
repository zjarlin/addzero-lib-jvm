package site.addzero.component.sheet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import site.addzero.component.sheet.engine.SheetCellAddress
import site.addzero.component.sheet.engine.SheetController
import site.addzero.component.sheet.engine.SheetRange
import site.addzero.component.sheet.engine.SheetSelectionMode
import site.addzero.component.sheet.engine.SheetViewport
import site.addzero.component.sheet.engine.sheetColumnName

/**
 * 在线表格工作台。
 */
@Composable
fun SheetWorkbench(
    controller: SheetController,
    modifier: Modifier = Modifier,
    metrics: SheetGridMetrics = SheetGridMetrics(),
    toolbarSlot: @Composable RowScope.(SheetController) -> Unit = {},
) {
    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    val document = controller.document
    val activeSheet = controller.activeSheet
    val selectedCell = controller.state.selection.primaryCell
    var clipboardMessage by remember { mutableStateOf<String?>(null) }
    val selectionLabel = remember(
        controller.state.selection,
        activeSheet,
    ) {
        buildSelectionLabel(
            mode = controller.state.selection.mode,
            range = controller.state.selection.activeRange,
        )
    }
    val formulaEditable = remember(controller.state.selection) {
        val activeRange = controller.state.selection.activeRange
        activeRange != null &&
            activeRange.isSingleCell &&
            controller.state.selection.mode == SheetSelectionMode.CELL
    }
    val formulaValue = remember(
        controller.state.editingCell,
        controller.state.editingInput,
        selectedCell,
        activeSheet,
    ) {
        when {
            controller.state.editingCell != null -> controller.state.editingInput
            selectedCell != null -> activeSheet?.cell(selectedCell)?.raw.orEmpty()
            else -> ""
        }
    }

    fun ensureEditingForSelectedCell() {
        val currentCell = selectedCell ?: return
        if (controller.state.editingCell != currentCell) {
            controller.startEditing(currentCell)
        }
    }

    fun handleCellClick(address: SheetCellAddress) {
        val currentSelection = controller.state.selection.activeRange
        val currentPrimaryCell = controller.state.selection.primaryCell
        if (controller.state.editingCell != null && controller.state.editingCell != address) {
            controller.commitEditing()
            controller.selectRange(
                range = SheetRange.single(address),
                mode = SheetSelectionMode.CELL,
            )
            return
        }

        if (currentPrimaryCell == address && currentSelection?.isSingleCell == true) {
            controller.startEditing(address)
            return
        }

        controller.selectRange(
            range = SheetRange.single(address),
            mode = SheetSelectionMode.CELL,
        )
    }

    fun handleCellRangeSelect(range: SheetRange) {
        val editingCell = controller.state.editingCell
        if (editingCell != null && !range.contains(editingCell)) {
            controller.commitEditing()
        }
        controller.selectRange(
            range = range,
            mode = if (range.isSingleCell) {
                SheetSelectionMode.CELL
            } else {
                SheetSelectionMode.RANGE
            },
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SheetWorkbenchToolbar(
                controller = controller,
                documentTitle = document?.documentId ?: "未加载表格",
                toolbarSlot = toolbarSlot,
            )

            if (controller.loading || controller.saving) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            controller.errorMessage?.let { message ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            SheetSelectionActionBar(
                controller = controller,
                selectionLabel = selectionLabel,
                clipboardMessage = clipboardMessage,
                onCopySelection = {
                    val copiedText = controller.selectedRangeAsPlainText()
                    if (copiedText == null) {
                        clipboardMessage = "当前没有可复制的选区"
                        return@SheetSelectionActionBar
                    }
                    clipboardManager.setText(AnnotatedString(copiedText))
                    clipboardMessage = "已复制选区 $selectionLabel"
                },
                onPasteClipboard = {
                    val clipboardText = clipboardManager.getText()
                    if (clipboardText == null) {
                        clipboardMessage = "剪贴板里没有可粘贴的文本"
                        return@SheetSelectionActionBar
                    }
                    val pastedRange = controller.pastePlainText(clipboardText.text)
                    clipboardMessage = if (pastedRange == null) {
                        "剪贴板内容为空，未执行粘贴"
                    } else {
                        "已粘贴到 ${buildSelectionLabel(SheetSelectionMode.RANGE, pastedRange)}"
                    }
                },
            )

            SheetFormulaBar(
                selectedCellLabel = selectionLabel,
                value = formulaValue,
                enabled = formulaEditable,
                metrics = metrics,
                onValueChange = { nextValue ->
                    ensureEditingForSelectedCell()
                    controller.updateEditingInput(nextValue)
                },
                onCommit = {
                    controller.commitEditing()
                },
                onClear = {
                    if (controller.state.editingCell != null) {
                        controller.updateEditingInput("")
                        controller.commitEditing()
                    } else {
                        controller.clearSelectedCells()
                    }
                },
            )

            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                tonalElevation = 1.dp,
                shadowElevation = 0.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                if (document == null || activeSheet == null) {
                    SheetWorkbenchEmptyState(
                        loading = controller.loading,
                    )
                } else {
                    SheetGrid(
                        page = activeSheet,
                        selection = controller.state.selection,
                        editingCell = controller.state.editingCell,
                        editingInput = controller.state.editingInput,
                        viewport = controller.state.viewport,
                        metrics = metrics,
                        onViewportChange = controller::updateViewport,
                        onCellClick = ::handleCellClick,
                        onCellRangeSelect = ::handleCellRangeSelect,
                        onRowHeaderClick = controller::selectRow,
                        onColumnHeaderClick = controller::selectColumn,
                        onEditingInputChange = controller::updateEditingInput,
                    )
                }
            }

            if (document != null) {
                SheetTabBar(
                    sheets = document.sheets,
                    activeSheetId = controller.state.activeSheetId ?: document.resolvedActiveSheetId,
                    metrics = metrics,
                    onOpenSheet = controller::openSheet,
                )
            }
        }
    }
}

@Composable
private fun SheetSelectionActionBar(
    controller: SheetController,
    selectionLabel: String,
    clipboardMessage: String?,
    onCopySelection: () -> Unit,
    onPasteClipboard: () -> Unit,
) {
    val hasSelection = controller.state.selection.activeRange != null
    if (!hasSelection) {
        return
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "当前选择：$selectionLabel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                clipboardMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onCopySelection,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("复制")
                }
                OutlinedButton(
                    onClick = onPasteClipboard,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("粘贴")
                }
                OutlinedButton(
                    onClick = {
                        controller.fillDownSelection()
                    },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("向下填充")
                }
                OutlinedButton(
                    onClick = {
                        controller.fillRightSelection()
                    },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("向右填充")
                }
                OutlinedButton(
                    onClick = {
                        controller.insertRowsAboveSelection()
                    },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("上插行")
                }
                OutlinedButton(
                    onClick = {
                        controller.insertRowsBelowSelection()
                    },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("下插行")
                }
                OutlinedButton(
                    onClick = {
                        controller.insertColumnsLeft()
                    },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("左插列")
                }
                OutlinedButton(
                    onClick = {
                        controller.insertColumnsRight()
                    },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("右插列")
                }
                FilledTonalButton(
                    onClick = {
                        controller.deleteSelectedRows()
                    },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("删行")
                }
                FilledTonalButton(
                    onClick = {
                        controller.deleteSelectedColumns()
                    },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text("删列")
                }
            }
        }
    }
}

@Composable
private fun SheetWorkbenchToolbar(
    controller: SheetController,
    documentTitle: String,
    toolbarSlot: @Composable RowScope.(SheetController) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "在线配置表格工作台",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = documentTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            toolbarSlot(controller)

            OutlinedButton(
                onClick = controller::reload,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("刷新")
            }

            OutlinedButton(
                onClick = controller::undo,
                enabled = controller.canUndo,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("撤销")
            }

            FilledTonalButton(
                onClick = controller::redo,
                enabled = controller.canRedo,
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("重做")
            }
        }
    }
}

@Composable
private fun SheetWorkbenchEmptyState(
    loading: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (loading) {
                "正在加载在线表格..."
            } else {
                "当前没有可展示的工作表"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun buildSelectionLabel(
    mode: SheetSelectionMode,
    range: SheetRange?,
): String {
    val safeRange = range ?: return "—"
    val start = safeRange.normalizedStart
    val end = safeRange.normalizedEnd
    return when (mode) {
        SheetSelectionMode.ROW -> {
            if (start.rowIndex == end.rowIndex) {
                "第 ${start.rowIndex + 1} 行"
            } else {
                "第 ${start.rowIndex + 1}-${end.rowIndex + 1} 行"
            }
        }

        SheetSelectionMode.COLUMN -> {
            if (start.columnIndex == end.columnIndex) {
                "第 ${sheetColumnName(start.columnIndex)} 列"
            } else {
                "第 ${sheetColumnName(start.columnIndex)}-${sheetColumnName(end.columnIndex)} 列"
            }
        }

        SheetSelectionMode.CELL -> {
            start.a1Notation
        }

        SheetSelectionMode.RANGE -> {
            if (safeRange.isSingleCell) {
                start.a1Notation
            } else {
                "${start.a1Notation}:${end.a1Notation}"
            }
        }
    }
}
