package site.addzero.component.sheet.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SheetReducerTest {
    @Test
    fun insertRowsShiftsFollowingCells() {
        val before = sampleDocument()

        val after = SheetReducer.apply(
            document = before,
            operation = InsertRows(
                sheetId = "sheet-1",
                startRowIndex = 1,
                count = 2,
            ),
        )

        val sheet = after.activeSheet()!!
        assertEquals(
            SheetCellValue.infer("B2"),
            sheet.cell(SheetCellAddress(3, 1)),
        )
    }

    @Test
    fun deleteColumnsRemovesCoveredCellsAndCompactsFollowingCells() {
        val before = sampleDocument()

        val after = SheetReducer.apply(
            document = before,
            operation = DeleteColumns(
                sheetId = "sheet-1",
                startColumnIndex = 0,
                count = 1,
            ),
        )

        val sheet = after.activeSheet()!!
        assertEquals(
            SheetCellValue.infer("B2"),
            sheet.cell(SheetCellAddress(1, 0)),
        )
        assertNull(sheet.cell(SheetCellAddress(1, 1)))
    }

    @Test
    fun fillDownCopiesTopRowIntoFollowingRows() {
        val before = sampleDocument()

        val after = SheetReducer.apply(
            document = before,
            operation = FillDownRange(
                sheetId = "sheet-1",
                range = SheetRange(
                    start = SheetCellAddress(0, 0),
                    end = SheetCellAddress(2, 1),
                ),
            ),
        )

        val sheet = after.activeSheet()!!
        assertEquals(
            SheetCellValue.infer("A1"),
            sheet.cell(SheetCellAddress(1, 0)),
        )
        assertEquals(
            SheetCellValue.infer("A1"),
            sheet.cell(SheetCellAddress(2, 0)),
        )
        assertNull(sheet.cell(SheetCellAddress(1, 1)))
    }

    @Test
    fun fillRightCopiesLeftColumnIntoFollowingColumns() {
        val before = sampleDocument()

        val after = SheetReducer.apply(
            document = before,
            operation = FillRightRange(
                sheetId = "sheet-1",
                range = SheetRange(
                    start = SheetCellAddress(0, 0),
                    end = SheetCellAddress(1, 2),
                ),
            ),
        )

        val sheet = after.activeSheet()!!
        assertEquals(
            SheetCellValue.infer("A1"),
            sheet.cell(SheetCellAddress(0, 1)),
        )
        assertEquals(
            SheetCellValue.infer("A1"),
            sheet.cell(SheetCellAddress(0, 2)),
        )
        assertNull(sheet.cell(SheetCellAddress(1, 2)))
    }

    @Test
    fun pasteCellsClearsEmptyTargetsAndExpandsSheetBounds() {
        val before = SheetDocument(
            documentId = "paste-demo",
            activeSheetId = "sheet-1",
            sheets = listOf(
                SheetPage(
                    sheetId = "sheet-1",
                    title = "Sheet1",
                    rowCount = 2,
                    columnCount = 2,
                    cells = mapOf(
                        SheetCellAddress(0, 0) to SheetCellValue.infer("A1"),
                    ),
                ),
            ),
        )

        val after = SheetReducer.apply(
            document = before,
            operation = PasteCells(
                sheetId = "sheet-1",
                startAddress = SheetCellAddress(2, 2),
                patches = listOf(
                    SheetPasteCellPatch(
                        rowOffset = 0,
                        columnOffset = 0,
                        value = SheetCellValue.infer("C3"),
                    ),
                    SheetPasteCellPatch(
                        rowOffset = 0,
                        columnOffset = 1,
                        value = null,
                    ),
                    SheetPasteCellPatch(
                        rowOffset = 1,
                        columnOffset = 1,
                        value = SheetCellValue.infer("D4"),
                    ),
                ),
            ),
        )

        val sheet = after.activeSheet()!!
        assertEquals(4, sheet.rowCount)
        assertEquals(4, sheet.columnCount)
        assertEquals(
            SheetCellValue.infer("C3"),
            sheet.cell(SheetCellAddress(2, 2)),
        )
        assertNull(sheet.cell(SheetCellAddress(2, 3)))
        assertEquals(
            SheetCellValue.infer("D4"),
            sheet.cell(SheetCellAddress(3, 3)),
        )
    }
}

private fun sampleDocument(): SheetDocument {
    return SheetDocument(
        documentId = "demo",
        activeSheetId = "sheet-1",
        sheets = listOf(
            SheetPage(
                sheetId = "sheet-1",
                title = "Sheet1",
                cells = mapOf(
                    SheetCellAddress(0, 0) to SheetCellValue.infer("A1"),
                    SheetCellAddress(1, 1) to SheetCellValue.infer("B2"),
                ),
            ),
        ),
    )
}
