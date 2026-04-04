package site.addzero.component.sheet.preview

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import site.addzero.component.sheet.engine.SheetCellAddress
import site.addzero.component.sheet.engine.SheetCellValue
import site.addzero.component.sheet.engine.SheetController
import site.addzero.component.sheet.engine.SheetDataSource
import site.addzero.component.sheet.engine.SheetDocument
import site.addzero.component.sheet.engine.SheetOperation
import site.addzero.component.sheet.engine.SheetPage
import site.addzero.component.sheet.engine.SheetRange
import site.addzero.component.sheet.engine.SheetReducer
import site.addzero.component.sheet.engine.SheetSelectionMode
import site.addzero.component.sheet.engine.SheetState

/**
 * 在线表格引擎独立场景入口。
 *
 * 该入口不依赖桌面 UI，可直接用于本地和 CI 验证。
 */
fun main() = runBlocking {
  println("== Sheet Engine Scenario ==")
  val dataSource = PreviewSheetDataSource(
    initialDocument = previewDocument(),
  )
  val controller = SheetController()
  controller.bind(
    scope = this,
    state = SheetState(),
    dataSource = dataSource,
    resolveLoadErrorMessage = { it.message ?: "加载失败" },
    resolveSaveErrorMessage = { it.message ?: "保存失败" },
  )

  controller.load("preview-sheet")
  settle(controller)
  printCheckpoint("加载完成", controller)

  controller.selectRange(
    range = SheetRange(
      start = SheetCellAddress(0, 0),
      end = SheetCellAddress(2, 1),
    ),
    mode = SheetSelectionMode.RANGE,
  )
  controller.fillDownSelection()
  settle(controller)
  printCheckpoint("向下填充后", controller)

  controller.selectColumn(1)
  controller.insertColumnsRight()
  settle(controller)
  printCheckpoint("右插列后", controller)

  controller.startEditing(SheetCellAddress(1, 2))
  controller.updateEditingInput("new-col")
  controller.commitEditing()
  settle(controller)
  printCheckpoint("编辑新列后", controller)

  controller.undo()
  printCheckpoint("撤销后", controller)
  controller.redo()
  printCheckpoint("重做后", controller)

  require(controller.activeSheet?.columnCount == 5) {
    "列数不符合预期: ${controller.activeSheet?.columnCount}"
  }
  require(controller.activeSheet?.cell(SheetCellAddress(1, 2))?.raw == "new-col") {
    "目标单元格内容不符合预期"
  }
  println("Scenario OK")
}

private suspend fun settle(controller: SheetController) {
  while (controller.loading || controller.saving) {
    yield()
  }
}

private fun printCheckpoint(
  title: String,
  controller: SheetController,
) {
  val sheet = controller.activeSheet ?: error("当前没有激活工作表")
  println("-- $title --")
  println("sheet=${sheet.title}, rows=${sheet.rowCount}, columns=${sheet.columnCount}")
  println(
    listOf(
      "A1=${sheet.cell(SheetCellAddress(0, 0))?.raw.orEmpty()}",
      "B1=${sheet.cell(SheetCellAddress(0, 1))?.raw.orEmpty()}",
      "A2=${sheet.cell(SheetCellAddress(1, 0))?.raw.orEmpty()}",
      "B3=${sheet.cell(SheetCellAddress(2, 1))?.raw.orEmpty()}",
      "C2=${sheet.cell(SheetCellAddress(1, 2))?.raw.orEmpty()}",
    ).joinToString(" | "),
  )
}

private class PreviewSheetDataSource(
  initialDocument: SheetDocument,
) : SheetDataSource {
  private var currentDocument = initialDocument

  override suspend fun load(documentId: String): SheetDocument {
    check(currentDocument.documentId == documentId) {
      "未找到文档: $documentId"
    }
    return currentDocument
  }

  override suspend fun applyOperations(
    documentId: String,
    baseVersion: Long,
    operations: List<SheetOperation>,
  ): SheetDocument {
    check(currentDocument.documentId == documentId) {
      "未找到文档: $documentId"
    }
    check(currentDocument.version == baseVersion) {
      "版本冲突: current=${currentDocument.version}, base=$baseVersion"
    }
    currentDocument = SheetReducer.apply(currentDocument, operations)
    return currentDocument
  }
}

private fun previewDocument(): SheetDocument {
  return SheetDocument(
    documentId = "preview-sheet",
    activeSheetId = "sheet-ops",
    sheets = listOf(
      SheetPage(
        sheetId = "sheet-ops",
        title = "引擎场景",
        rowCount = 12,
        columnCount = 4,
        cells = mapOf(
          SheetCellAddress(0, 0) to SheetCellValue.infer("region"),
          SheetCellAddress(0, 1) to SheetCellValue.infer("status"),
          SheetCellAddress(1, 0) to SheetCellValue.infer("华东"),
          SheetCellAddress(1, 1) to SheetCellValue.infer("运行"),
          SheetCellAddress(2, 0) to SheetCellValue.infer("华南"),
          SheetCellAddress(2, 1) to SheetCellValue.infer("运行"),
        ),
      ),
    ),
  )
}
