package site.addzero.component.sheet.engine

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 在线表格控制器。
 *
 * 它面向在线单元格编辑场景，而不是服务端分页列表。
 */
@Stable
class SheetController internal constructor() {
    private lateinit var scope: CoroutineScope
    private lateinit var stateHolder: SheetState
    private lateinit var dataSource: SheetDataSource
    private var resolveLoadErrorMessage: (Throwable) -> String = { it.message ?: "加载表格失败" }
    private var resolveSaveErrorMessage: (Throwable) -> String = { it.message ?: "保存表格失败" }

    private var loadRequestToken by mutableStateOf(0)
    private var historyPast: List<SheetDocument> = emptyList()
    private var historyFuture: List<SheetDocument> = emptyList()

    var documentId by mutableStateOf<String?>(null)
        private set

    var document by mutableStateOf<SheetDocument?>(null)
        private set

    var loading by mutableStateOf(false)
        private set

    var saving by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    val canUndo
        get() = historyPast.isNotEmpty()

    val canRedo
        get() = historyFuture.isNotEmpty()

    val state
        get() = stateHolder

    val activeSheet
        get() = document?.sheetById(
            stateHolder.activeSheetId ?: document?.resolvedActiveSheetId.orEmpty(),
        )

    internal fun bind(
        scope: CoroutineScope,
        state: SheetState,
        dataSource: SheetDataSource,
        resolveLoadErrorMessage: (Throwable) -> String,
        resolveSaveErrorMessage: (Throwable) -> String,
    ) {
        this.scope = scope
        stateHolder = state
        this.dataSource = dataSource
        this.resolveLoadErrorMessage = resolveLoadErrorMessage
        this.resolveSaveErrorMessage = resolveSaveErrorMessage
    }

    fun load(documentId: String) {
        this.documentId = documentId
        val requestToken = ++loadRequestToken
        loading = true
        errorMessage = null

        scope.launch {
            runCatching {
                dataSource.load(documentId)
            }.onSuccess { loadedDocument ->
                if (requestToken != loadRequestToken) {
                    return@launch
                }

                loading = false
                historyPast = emptyList()
                historyFuture = emptyList()
                document = loadedDocument
                stateHolder.openSheet(
                    loadedDocument.resolvedActiveSheetId,
                )
            }.onFailure { error ->
                if (requestToken != loadRequestToken) {
                    return@launch
                }

                loading = false
                errorMessage = resolveLoadErrorMessage(error)
            }
        }
    }

    fun reload() {
        val currentDocumentId = documentId ?: return
        load(currentDocumentId)
    }

    fun openSheet(sheetId: String) {
        if (document?.sheetById(sheetId) == null) {
            return
        }
        stateHolder.openSheet(sheetId)
    }

    fun updateViewport(viewport: SheetViewport) {
        stateHolder.updateViewport(viewport)
    }

    fun select(selection: SheetSelection) {
        stateHolder.replaceSelection(selection)
    }

    fun selectRange(
        range: SheetRange,
        mode: SheetSelectionMode = SheetSelectionMode.RANGE,
    ) {
        stateHolder.selectRange(range, mode)
    }

    fun selectRow(rowIndex: Int) {
        val sheet = activeSheet ?: return
        selectRange(
            range = SheetRange(
                start = SheetCellAddress(rowIndex = rowIndex, columnIndex = 0),
                end = SheetCellAddress(
                    rowIndex = rowIndex,
                    columnIndex = maxOf(0, sheet.columnCount - 1),
                ),
            ),
            mode = SheetSelectionMode.ROW,
        )
    }

    fun selectColumn(columnIndex: Int) {
        val sheet = activeSheet ?: return
        selectRange(
            range = SheetRange(
                start = SheetCellAddress(rowIndex = 0, columnIndex = columnIndex),
                end = SheetCellAddress(
                    rowIndex = maxOf(0, sheet.rowCount - 1),
                    columnIndex = columnIndex,
                ),
            ),
            mode = SheetSelectionMode.COLUMN,
        )
    }

    fun startEditing(cell: SheetCellAddress) {
        val initialInput = activeSheet?.cell(cell)?.raw.orEmpty()
        stateHolder.startEditing(cell, initialInput)
        stateHolder.selectRange(SheetRange.single(cell), SheetSelectionMode.CELL)
    }

    fun updateEditingInput(nextInput: String) {
        stateHolder.updateEditingInput(nextInput)
    }

    fun commitEditing() {
        val editingCell = stateHolder.editingCell ?: return
        val currentSheetId = activeSheet?.sheetId ?: return
        val rawInput = stateHolder.editingInput
        val operation = if (rawInput.isBlank()) {
            ClearRange(
                sheetId = currentSheetId,
                range = SheetRange.single(editingCell),
            )
        } else {
            PutCellValue(
                sheetId = currentSheetId,
                address = editingCell,
                value = SheetCellValue.infer(rawInput),
            )
        }
        applyOperations(listOf(operation))
        stateHolder.stopEditing()
    }

    fun clearSelectedCells() {
        val currentSheetId = activeSheet?.sheetId ?: return
        val currentRange = stateHolder.selection.activeRange ?: return
        applyOperations(
            listOf(
                ClearRange(
                    sheetId = currentSheetId,
                    range = currentRange,
                ),
            ),
        )
    }

    fun fillDownSelection() {
        val sheet = activeSheet ?: return
        val range = stateHolder.selection.activeRange ?: return
        val normalizedStart = range.normalizedStart
        val normalizedEnd = range.normalizedEnd
        if (normalizedEnd.rowIndex <= normalizedStart.rowIndex) {
            return
        }
        applyOperations(
            listOf(
                FillDownRange(
                    sheetId = sheet.sheetId,
                    range = range,
                ),
            ),
        )
    }

    fun fillRightSelection() {
        val sheet = activeSheet ?: return
        val range = stateHolder.selection.activeRange ?: return
        val normalizedStart = range.normalizedStart
        val normalizedEnd = range.normalizedEnd
        if (normalizedEnd.columnIndex <= normalizedStart.columnIndex) {
            return
        }
        applyOperations(
            listOf(
                FillRightRange(
                    sheetId = sheet.sheetId,
                    range = range,
                ),
            ),
        )
    }

    fun selectedRangeAsPlainText(): String? {
        val sheet = activeSheet ?: return null
        val range = stateHolder.selection.activeRange
            ?: stateHolder.editingCell?.let(SheetRange::single)
            ?: return null
        val normalizedStart = range.normalizedStart
        val normalizedEnd = range.normalizedEnd

        return buildString {
            for (rowIndex in normalizedStart.rowIndex..normalizedEnd.rowIndex) {
                if (rowIndex > normalizedStart.rowIndex) {
                    append('\n')
                }
                for (columnIndex in normalizedStart.columnIndex..normalizedEnd.columnIndex) {
                    if (columnIndex > normalizedStart.columnIndex) {
                        append('\t')
                    }
                    append(
                        sheet.cell(
                            SheetCellAddress(
                                rowIndex = rowIndex,
                                columnIndex = columnIndex,
                            ),
                        )?.raw.orEmpty(),
                    )
                }
            }
        }
    }

    fun pastePlainText(rawText: String): SheetRange? {
        val sheet = activeSheet ?: return null
        val parsedClipboard = parsePlainTextClipboard(rawText) ?: return null
        val startAddress = stateHolder.editingCell
            ?: stateHolder.selection.primaryCell
            ?: SheetCellAddress(rowIndex = 0, columnIndex = 0)
        val targetRange = SheetRange(
            start = startAddress,
            end = SheetCellAddress(
                rowIndex = startAddress.rowIndex + parsedClipboard.rowCount - 1,
                columnIndex = startAddress.columnIndex + parsedClipboard.columnCount - 1,
            ),
        )

        stateHolder.stopEditing()
        applyOperations(
            operations = listOf(
                PasteCells(
                    sheetId = sheet.sheetId,
                    startAddress = startAddress,
                    patches = parsedClipboard.patches,
                ),
            ),
            afterSuccess = {
                stateHolder.selectRange(
                    range = targetRange,
                    mode = if (targetRange.isSingleCell) {
                        SheetSelectionMode.CELL
                    } else {
                        SheetSelectionMode.RANGE
                    },
                )
            },
        )
        return targetRange
    }

    fun insertRowsAboveSelection(count: Int = 1) {
        if (count <= 0) {
            return
        }
        val sheet = activeSheet ?: return
        val startRowIndex = stateHolder.selection.activeRange?.normalizedStart?.rowIndex
            ?: stateHolder.selection.primaryRowIndex
            ?: return
        applyOperations(
            operations = listOf(
                InsertRows(
                    sheetId = sheet.sheetId,
                    startRowIndex = startRowIndex,
                    count = count,
                ),
            ),
            afterSuccess = {
                selectRow(startRowIndex)
            },
        )
    }

    fun insertRowsBelowSelection(count: Int = 1) {
        if (count <= 0) {
            return
        }
        val sheet = activeSheet ?: return
        val endRowIndex = stateHolder.selection.activeRange?.normalizedEnd?.rowIndex
            ?: stateHolder.selection.primaryRowIndex
            ?: return
        val insertAt = endRowIndex + 1
        applyOperations(
            operations = listOf(
                InsertRows(
                    sheetId = sheet.sheetId,
                    startRowIndex = insertAt,
                    count = count,
                ),
            ),
            afterSuccess = {
                selectRow(insertAt.coerceAtMost(maxOf(0, (document?.activeSheet()?.rowCount ?: insertAt) - 1)))
            },
        )
    }

    fun deleteSelectedRows() {
        val sheet = activeSheet ?: return
        val range = stateHolder.selection.activeRange ?: return
        val normalizedStart = range.normalizedStart.rowIndex
        val normalizedEnd = range.normalizedEnd.rowIndex
        val count = normalizedEnd - normalizedStart + 1
        applyOperations(
            operations = listOf(
                DeleteRows(
                    sheetId = sheet.sheetId,
                    startRowIndex = normalizedStart,
                    count = count,
                ),
            ),
            afterSuccess = { nextDocument ->
                val nextRowCount = nextDocument.activeSheet()?.rowCount ?: 0
                if (nextRowCount > 0) {
                    selectRow(normalizedStart.coerceAtMost(nextRowCount - 1))
                } else {
                    stateHolder.clearSelection()
                }
            },
        )
    }

    fun insertColumnsLeft(count: Int = 1) {
        if (count <= 0) {
            return
        }
        val sheet = activeSheet ?: return
        val startColumnIndex = stateHolder.selection.activeRange?.normalizedStart?.columnIndex
            ?: stateHolder.selection.primaryColumnIndex
            ?: return
        applyOperations(
            operations = listOf(
                InsertColumns(
                    sheetId = sheet.sheetId,
                    startColumnIndex = startColumnIndex,
                    count = count,
                ),
            ),
            afterSuccess = {
                selectColumn(startColumnIndex)
            },
        )
    }

    fun insertColumnsRight(count: Int = 1) {
        if (count <= 0) {
            return
        }
        val sheet = activeSheet ?: return
        val endColumnIndex = stateHolder.selection.activeRange?.normalizedEnd?.columnIndex
            ?: stateHolder.selection.primaryColumnIndex
            ?: return
        val insertAt = endColumnIndex + 1
        applyOperations(
            operations = listOf(
                InsertColumns(
                    sheetId = sheet.sheetId,
                    startColumnIndex = insertAt,
                    count = count,
                ),
            ),
            afterSuccess = {
                selectColumn(insertAt.coerceAtMost(maxOf(0, (document?.activeSheet()?.columnCount ?: insertAt) - 1)))
            },
        )
    }

    fun deleteSelectedColumns() {
        val sheet = activeSheet ?: return
        val range = stateHolder.selection.activeRange ?: return
        val normalizedStart = range.normalizedStart.columnIndex
        val normalizedEnd = range.normalizedEnd.columnIndex
        val count = normalizedEnd - normalizedStart + 1
        applyOperations(
            operations = listOf(
                DeleteColumns(
                    sheetId = sheet.sheetId,
                    startColumnIndex = normalizedStart,
                    count = count,
                ),
            ),
            afterSuccess = { nextDocument ->
                val nextColumnCount = nextDocument.activeSheet()?.columnCount ?: 0
                if (nextColumnCount > 0) {
                    selectColumn(normalizedStart.coerceAtMost(nextColumnCount - 1))
                } else {
                    stateHolder.clearSelection()
                }
            },
        )
    }

    fun applyOperations(
        operations: List<SheetOperation>,
        afterSuccess: (SheetDocument) -> Unit = {},
    ) {
        if (operations.isEmpty()) {
            return
        }

        val currentDocumentId = documentId ?: return
        val currentDocument = document ?: return
        saving = true
        errorMessage = null

        scope.launch {
            runCatching {
                dataSource.applyOperations(
                    documentId = currentDocumentId,
                    baseVersion = currentDocument.version,
                    operations = operations,
                )
            }.onSuccess { nextDocument ->
                historyPast = historyPast + currentDocument
                historyFuture = emptyList()
                document = nextDocument
                saving = false
                val currentSheetId = stateHolder.activeSheetId
                val nextSheetId = nextDocument.sheetById(currentSheetId.orEmpty())?.sheetId
                    ?: nextDocument.resolvedActiveSheetId
                stateHolder.syncActiveSheet(nextSheetId)
                afterSuccess(nextDocument)
            }.onFailure { error ->
                saving = false
                errorMessage = resolveSaveErrorMessage(error)
            }
        }
    }

    /**
     * 当前 undo/redo 只恢复本地快照，不直接回放远端操作流。
     *
     * 多人协同下如果需要强一致 undo，应该在服务端 op-log 上层实现。
     */
    fun undo() {
        val previousDocument = historyPast.lastOrNull() ?: return
        val currentDocument = document ?: return
        historyPast = historyPast.dropLast(1)
        historyFuture = listOf(currentDocument) + historyFuture
        document = previousDocument
        stateHolder.syncActiveSheet(previousDocument.resolvedActiveSheetId)
        stateHolder.stopEditing()
    }

    fun redo() {
        val nextDocument = historyFuture.firstOrNull() ?: return
        val currentDocument = document ?: return
        historyFuture = historyFuture.drop(1)
        historyPast = historyPast + currentDocument
        document = nextDocument
        stateHolder.syncActiveSheet(nextDocument.resolvedActiveSheetId)
        stateHolder.stopEditing()
    }

    fun clearError() {
        errorMessage = null
    }
}

fun createSheetController(
    scope: CoroutineScope,
    dataSource: SheetDataSource,
    state: SheetState = createSheetState(),
    resolveLoadErrorMessage: (Throwable) -> String = { it.message ?: "加载表格失败" },
    resolveSaveErrorMessage: (Throwable) -> String = { it.message ?: "保存表格失败" },
): SheetController {
    return SheetController().also { controller ->
        controller.bind(
            scope = scope,
            state = state,
            dataSource = dataSource,
            resolveLoadErrorMessage = resolveLoadErrorMessage,
            resolveSaveErrorMessage = resolveSaveErrorMessage,
        )
    }
}

@Composable
fun rememberSheetController(
    dataSource: SheetDataSource,
    documentId: String? = null,
    state: SheetState = rememberSheetState(),
    autoLoad: Boolean = true,
    reloadKey: Any? = documentId,
    resolveLoadErrorMessage: (Throwable) -> String = { it.message ?: "加载表格失败" },
    resolveSaveErrorMessage: (Throwable) -> String = { it.message ?: "保存表格失败" },
): SheetController {
    val scope = rememberCoroutineScope()
    val controller = remember {
        createSheetController(
            scope = scope,
            dataSource = dataSource,
            state = state,
            resolveLoadErrorMessage = resolveLoadErrorMessage,
            resolveSaveErrorMessage = resolveSaveErrorMessage,
        )
    }

    controller.bind(
        scope = scope,
        state = state,
        dataSource = dataSource,
        resolveLoadErrorMessage = resolveLoadErrorMessage,
        resolveSaveErrorMessage = resolveSaveErrorMessage,
    )

    LaunchedEffect(
        controller,
        documentId,
        autoLoad,
        reloadKey,
    ) {
        if (autoLoad && documentId != null) {
            controller.load(documentId)
        }
    }

    return controller
}

private data class ParsedPlainTextClipboard(
    val rowCount: Int,
    val columnCount: Int,
    val patches: List<SheetPasteCellPatch>,
)

private fun parsePlainTextClipboard(rawText: String): ParsedPlainTextClipboard? {
    val normalized = rawText
        .replace("\r\n", "\n")
        .replace('\r', '\n')
        .trimEnd('\n')
    val lines = splitPreservingDelimiters(
        input = if (rawText.isEmpty()) "" else normalized,
        delimiter = '\n',
    )
    if (lines.isEmpty()) {
        return null
    }

    var maxColumnCount = 0
    val patches = buildList {
        lines.forEachIndexed { rowOffset, line ->
            val cells = splitPreservingDelimiters(
                input = line,
                delimiter = '\t',
            )
            maxColumnCount = maxOf(maxColumnCount, cells.size)
            cells.forEachIndexed { columnOffset, cellRaw ->
                add(
                    SheetPasteCellPatch(
                        rowOffset = rowOffset,
                        columnOffset = columnOffset,
                        value = cellRaw.takeUnless(String::isEmpty)?.let(SheetCellValue::infer),
                    ),
                )
            }
        }
    }

    if (maxColumnCount == 0) {
        return null
    }

    return ParsedPlainTextClipboard(
        rowCount = lines.size,
        columnCount = maxColumnCount,
        patches = patches,
    )
}

private fun splitPreservingDelimiters(
    input: String,
    delimiter: Char,
): List<String> {
    if (input.isEmpty()) {
        return listOf("")
    }

    val result = mutableListOf<String>()
    val current = StringBuilder()
    input.forEach { char ->
        if (char == delimiter) {
            result += current.toString()
            current.clear()
        } else {
            current.append(char)
        }
    }
    result += current.toString()
    return result
}
