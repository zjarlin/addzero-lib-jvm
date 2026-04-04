package site.addzero.component.sheet.engine

/**
 * 选择模式。
 */
enum class SheetSelectionMode {
    CELL,
    RANGE,
    ROW,
    COLUMN,
}

/**
 * 单一连续选择区。
 */
data class SheetRange(
    val start: SheetCellAddress,
    val end: SheetCellAddress,
) {
    val normalizedStart
        get() = SheetCellAddress(
            rowIndex = minOf(start.rowIndex, end.rowIndex),
            columnIndex = minOf(start.columnIndex, end.columnIndex),
        )

    val normalizedEnd
        get() = SheetCellAddress(
            rowIndex = maxOf(start.rowIndex, end.rowIndex),
            columnIndex = maxOf(start.columnIndex, end.columnIndex),
        )

    fun contains(address: SheetCellAddress): Boolean {
        val normalizedStart = normalizedStart
        val normalizedEnd = normalizedEnd
        return address.rowIndex in normalizedStart.rowIndex..normalizedEnd.rowIndex &&
            address.columnIndex in normalizedStart.columnIndex..normalizedEnd.columnIndex
    }

    val isSingleCell
        get() = normalizedStart == normalizedEnd

    companion object {
        fun single(address: SheetCellAddress): SheetRange {
            return SheetRange(
                start = address,
                end = address,
            )
        }
    }
}

/**
 * 当前选择态。
 */
data class SheetSelection(
    val mode: SheetSelectionMode = SheetSelectionMode.CELL,
    val anchor: SheetCellAddress? = null,
    val focus: SheetCellAddress? = null,
) {
    val activeRange
        get() = if (anchor != null && focus != null) {
            SheetRange(anchor, focus)
        } else {
            null
        }

    val primaryCell
        get() = activeRange?.normalizedStart

    val primaryRowIndex
        get() = primaryCell?.rowIndex

    val primaryColumnIndex
        get() = primaryCell?.columnIndex

    companion object {
        fun empty(): SheetSelection {
            return SheetSelection()
        }

        fun fromRange(
            range: SheetRange,
            mode: SheetSelectionMode = SheetSelectionMode.RANGE,
        ): SheetSelection {
            return SheetSelection(
                mode = mode,
                anchor = range.start,
                focus = range.end,
            )
        }
    }
}

/**
 * 协作光标。
 */
data class SheetPresence(
    val userId: String,
    val displayName: String,
    val colorHex: String = "#2563EB",
    val selection: SheetSelection = SheetSelection.empty(),
)
