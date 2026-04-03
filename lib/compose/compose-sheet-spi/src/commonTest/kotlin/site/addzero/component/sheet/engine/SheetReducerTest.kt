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
