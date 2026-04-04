package site.addzero.component.sheet.engine

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * 在线表格交互状态。
 */
@Stable
class SheetState internal constructor(
    activeSheetId: String? = null,
    viewport: SheetViewport = SheetViewport(),
    selection: SheetSelection = SheetSelection.empty(),
    editingCell: SheetCellAddress? = null,
    editingInput: String = "",
) {
    var activeSheetId by mutableStateOf(activeSheetId)
        private set

    var viewport by mutableStateOf(viewport)
        private set

    var selection by mutableStateOf(selection)
        private set

    var editingCell by mutableStateOf(editingCell)
        private set

    var editingInput by mutableStateOf(editingInput)
        private set

    fun openSheet(sheetId: String?) {
        activeSheetId = sheetId
        clearSelection()
        stopEditing()
    }

    fun syncActiveSheet(sheetId: String?) {
        activeSheetId = sheetId
    }

    fun updateViewport(nextViewport: SheetViewport) {
        viewport = nextViewport
    }

    fun replaceSelection(nextSelection: SheetSelection) {
        selection = nextSelection
    }

    fun selectRange(
        range: SheetRange,
        mode: SheetSelectionMode = SheetSelectionMode.RANGE,
    ) {
        selection = SheetSelection.fromRange(
            range = range,
            mode = mode,
        )
    }

    fun clearSelection() {
        selection = SheetSelection.empty()
    }

    fun startEditing(
        cell: SheetCellAddress,
        initialInput: String,
    ) {
        editingCell = cell
        editingInput = initialInput
    }

    fun updateEditingInput(nextInput: String) {
        editingInput = nextInput
    }

    fun stopEditing() {
        editingCell = null
        editingInput = ""
    }
}

fun createSheetState(
    activeSheetId: String? = null,
    viewport: SheetViewport = SheetViewport(),
): SheetState {
    return SheetState(
        activeSheetId = activeSheetId,
        viewport = viewport,
    )
}

@Composable
fun rememberSheetState(
    activeSheetId: String? = null,
    viewport: SheetViewport = SheetViewport(),
): SheetState {
    return remember(activeSheetId, viewport) {
        createSheetState(
            activeSheetId = activeSheetId,
            viewport = viewport,
        )
    }
}
