package site.addzero.component.sheet.engine

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SheetControllerTest {
    @Test
    fun latestLoadRequestWins() = runTest {
        val dataSource = FakeSheetDataSource(
            loadHandler = { documentId ->
                if (documentId == "first") {
                    delay(100)
                }
                sampleDocument(documentId)
            },
        )
        val controller = SheetController()

        controller.bind(
            scope = this,
            state = SheetState(),
            dataSource = dataSource,
            resolveLoadErrorMessage = { it.message ?: "加载失败" },
            resolveSaveErrorMessage = { it.message ?: "保存失败" },
        )

        controller.load("first")
        controller.load("second")
        advanceUntilIdle()

        assertEquals("second", controller.document?.documentId)
        assertFalse(controller.loading)
    }

    @Test
    fun commitEditCreatesUndoableSnapshot() = runTest {
        val dataSource = FakeSheetDataSource(
            loadHandler = ::sampleDocument,
        )
        val controller = SheetController()

        controller.bind(
            scope = this,
            state = SheetState(),
            dataSource = dataSource,
            resolveLoadErrorMessage = { it.message ?: "加载失败" },
            resolveSaveErrorMessage = { it.message ?: "保存失败" },
        )

        controller.load("doc-1")
        advanceUntilIdle()
        controller.startEditing(SheetCellAddress(0, 0))
        controller.updateEditingInput("updated")
        controller.commitEditing()
        advanceUntilIdle()

        assertEquals(
            "updated",
            controller.activeSheet?.cell(SheetCellAddress(0, 0))?.raw,
        )
        assertTrue(controller.canUndo)

        controller.undo()
        assertEquals(
            "seed",
            controller.activeSheet?.cell(SheetCellAddress(0, 0))?.raw,
        )
        assertTrue(controller.canRedo)
    }

    @Test
    fun clearSelectedCellsRemovesRangeValues() = runTest {
        val dataSource = FakeSheetDataSource(
            loadHandler = ::sampleDocument,
        )
        val controller = SheetController()

        controller.bind(
            scope = this,
            state = SheetState(),
            dataSource = dataSource,
            resolveLoadErrorMessage = { it.message ?: "加载失败" },
            resolveSaveErrorMessage = { it.message ?: "保存失败" },
        )

        controller.load("doc-1")
        advanceUntilIdle()
        controller.selectRange(
            SheetRange(
                start = SheetCellAddress(0, 0),
                end = SheetCellAddress(0, 0),
            ),
        )
        controller.clearSelectedCells()
        advanceUntilIdle()

        assertEquals(
            null,
            controller.activeSheet?.cell(SheetCellAddress(0, 0)),
        )
        assertFalse(controller.saving)
    }

    @Test
    fun selectRowAndInsertRowsBelowSelectionWorkTogether() = runTest {
        val dataSource = FakeSheetDataSource(
            loadHandler = ::sampleDocument,
        )
        val controller = SheetController()

        controller.bind(
            scope = this,
            state = SheetState(),
            dataSource = dataSource,
            resolveLoadErrorMessage = { it.message ?: "加载失败" },
            resolveSaveErrorMessage = { it.message ?: "保存失败" },
        )

        controller.load("doc-1")
        advanceUntilIdle()
        controller.selectRow(0)
        controller.insertRowsBelowSelection()
        advanceUntilIdle()

        assertEquals(SheetSelectionMode.ROW, controller.state.selection.mode)
        assertEquals(1001, controller.activeSheet?.rowCount)
        assertEquals(1, controller.state.selection.primaryRowIndex)
    }

    @Test
    fun selectColumnAndDeleteSelectedColumnsShiftCellsLeft() = runTest {
        val dataSource = FakeSheetDataSource(
            loadHandler = ::sampleDocument,
        )
        val controller = SheetController()

        controller.bind(
            scope = this,
            state = SheetState(),
            dataSource = dataSource,
            resolveLoadErrorMessage = { it.message ?: "加载失败" },
            resolveSaveErrorMessage = { it.message ?: "保存失败" },
        )

        controller.load("doc-1")
        advanceUntilIdle()
        controller.selectColumn(0)
        controller.deleteSelectedColumns()
        advanceUntilIdle()

        assertEquals(SheetSelectionMode.COLUMN, controller.state.selection.mode)
        assertEquals(25, controller.activeSheet?.columnCount)
        assertEquals(
            "b2",
            controller.activeSheet?.cell(SheetCellAddress(1, 0))?.raw,
        )
    }

    @Test
    fun fillDownSelectionCopiesTopRowAcrossSelectedRows() = runTest {
        val dataSource = FakeSheetDataSource(
            loadHandler = { documentId ->
                SheetDocument(
                    documentId = documentId,
                    activeSheetId = "sheet-1",
                    sheets = listOf(
                        SheetPage(
                            sheetId = "sheet-1",
                            title = "主表",
                            cells = mapOf(
                                SheetCellAddress(0, 0) to SheetCellValue.infer("A1"),
                                SheetCellAddress(0, 1) to SheetCellValue.infer("B1"),
                                SheetCellAddress(1, 0) to SheetCellValue.infer("old-a2"),
                                SheetCellAddress(1, 1) to SheetCellValue.infer("old-b2"),
                            ),
                        ),
                    ),
                )
            },
        )
        val controller = SheetController()

        controller.bind(
            scope = this,
            state = SheetState(),
            dataSource = dataSource,
            resolveLoadErrorMessage = { it.message ?: "加载失败" },
            resolveSaveErrorMessage = { it.message ?: "保存失败" },
        )

        controller.load("doc-1")
        advanceUntilIdle()
        controller.selectRange(
            SheetRange(
                start = SheetCellAddress(0, 0),
                end = SheetCellAddress(2, 1),
            ),
            mode = SheetSelectionMode.RANGE,
        )
        controller.fillDownSelection()
        advanceUntilIdle()

        assertEquals("A1", controller.activeSheet?.cell(SheetCellAddress(1, 0))?.raw)
        assertEquals("B1", controller.activeSheet?.cell(SheetCellAddress(2, 1))?.raw)
        assertTrue(controller.canUndo)
    }

    @Test
    fun fillRightSelectionCopiesLeftColumnAcrossSelectedColumns() = runTest {
        val dataSource = FakeSheetDataSource(
            loadHandler = { documentId ->
                SheetDocument(
                    documentId = documentId,
                    activeSheetId = "sheet-1",
                    sheets = listOf(
                        SheetPage(
                            sheetId = "sheet-1",
                            title = "主表",
                            cells = mapOf(
                                SheetCellAddress(0, 0) to SheetCellValue.infer("A1"),
                                SheetCellAddress(1, 0) to SheetCellValue.infer("A2"),
                                SheetCellAddress(0, 1) to SheetCellValue.infer("old-b1"),
                            ),
                        ),
                    ),
                )
            },
        )
        val controller = SheetController()

        controller.bind(
            scope = this,
            state = SheetState(),
            dataSource = dataSource,
            resolveLoadErrorMessage = { it.message ?: "加载失败" },
            resolveSaveErrorMessage = { it.message ?: "保存失败" },
        )

        controller.load("doc-2")
        advanceUntilIdle()
        controller.selectRange(
            SheetRange(
                start = SheetCellAddress(0, 0),
                end = SheetCellAddress(1, 2),
            ),
            mode = SheetSelectionMode.RANGE,
        )
        controller.fillRightSelection()
        advanceUntilIdle()

        assertEquals("A1", controller.activeSheet?.cell(SheetCellAddress(0, 2))?.raw)
        assertEquals("A2", controller.activeSheet?.cell(SheetCellAddress(1, 1))?.raw)
        assertTrue(controller.canUndo)
    }

    @Test
    fun selectedRangeAsPlainTextExportsRectangularTsv() = runTest {
        val dataSource = FakeSheetDataSource(
            loadHandler = { documentId ->
                SheetDocument(
                    documentId = documentId,
                    activeSheetId = "sheet-1",
                    sheets = listOf(
                        SheetPage(
                            sheetId = "sheet-1",
                            title = "主表",
                            cells = mapOf(
                                SheetCellAddress(0, 0) to SheetCellValue.infer("A1"),
                                SheetCellAddress(0, 1) to SheetCellValue.infer("B1"),
                                SheetCellAddress(1, 0) to SheetCellValue.infer("A2"),
                            ),
                        ),
                    ),
                )
            },
        )
        val controller = SheetController()

        controller.bind(
            scope = this,
            state = SheetState(),
            dataSource = dataSource,
            resolveLoadErrorMessage = { it.message ?: "加载失败" },
            resolveSaveErrorMessage = { it.message ?: "保存失败" },
        )

        controller.load("doc-export")
        advanceUntilIdle()
        controller.selectRange(
            SheetRange(
                start = SheetCellAddress(0, 0),
                end = SheetCellAddress(1, 1),
            ),
            mode = SheetSelectionMode.RANGE,
        )

        assertEquals("A1\tB1\nA2\t", controller.selectedRangeAsPlainText())
    }

    @Test
    fun pastePlainTextWritesMatrixAndExpandsSelection() = runTest {
        val dataSource = FakeSheetDataSource(
            loadHandler = ::sampleDocument,
        )
        val controller = SheetController()

        controller.bind(
            scope = this,
            state = SheetState(),
            dataSource = dataSource,
            resolveLoadErrorMessage = { it.message ?: "加载失败" },
            resolveSaveErrorMessage = { it.message ?: "保存失败" },
        )

        controller.load("doc-paste")
        advanceUntilIdle()
        controller.selectRange(
            SheetRange.single(SheetCellAddress(1, 1)),
            mode = SheetSelectionMode.CELL,
        )

        val pastedRange = controller.pastePlainText("left\t\n3\t=true")
        advanceUntilIdle()

        assertEquals(
            SheetRange(
                start = SheetCellAddress(1, 1),
                end = SheetCellAddress(2, 2),
            ),
            pastedRange,
        )
        assertEquals("left", controller.activeSheet?.cell(SheetCellAddress(1, 1))?.raw)
        assertNull(controller.activeSheet?.cell(SheetCellAddress(1, 2)))
        assertEquals("3", controller.activeSheet?.cell(SheetCellAddress(2, 1))?.raw)
        assertEquals("=true", controller.activeSheet?.cell(SheetCellAddress(2, 2))?.raw)
        assertEquals(
            SheetRange(
                start = SheetCellAddress(1, 1),
                end = SheetCellAddress(2, 2),
            ),
            controller.state.selection.activeRange,
        )
        assertTrue(controller.canUndo)
    }
}

private class FakeSheetDataSource(
    private val loadHandler: suspend (String) -> SheetDocument,
) : SheetDataSource {
    private val documents = mutableMapOf<String, SheetDocument>()

    override suspend fun load(documentId: String): SheetDocument {
        val document = documents[documentId] ?: loadHandler(documentId)
        documents[documentId] = document
        return document
    }

    override suspend fun applyOperations(
        documentId: String,
        baseVersion: Long,
        operations: List<SheetOperation>,
    ): SheetDocument {
        val current = documents[documentId] ?: load(documentId)
        check(current.version == baseVersion) {
            "版本冲突: current=${current.version}, base=$baseVersion"
        }
        val updated = SheetReducer.apply(current, operations)
        documents[documentId] = updated
        return updated
    }
}

private fun sampleDocument(documentId: String): SheetDocument {
    return SheetDocument(
        documentId = documentId,
        activeSheetId = "sheet-1",
        sheets = listOf(
            SheetPage(
                sheetId = "sheet-1",
                title = "主表",
                cells = mapOf(
                    SheetCellAddress(0, 0) to SheetCellValue.infer("seed"),
                    SheetCellAddress(1, 1) to SheetCellValue.infer("b2"),
                ),
            ),
        ),
    )
}
