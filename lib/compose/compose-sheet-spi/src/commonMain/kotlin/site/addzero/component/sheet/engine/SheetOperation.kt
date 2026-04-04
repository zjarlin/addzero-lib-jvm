package site.addzero.component.sheet.engine

/**
 * 文档操作日志。
 *
 * 这一层应该天然适配后端操作流、审计日志和协同同步。
 */
sealed interface SheetOperation {
    val sheetId: String
}

data class SheetPasteCellPatch(
    val rowOffset: Int,
    val columnOffset: Int,
    val value: SheetCellValue?,
)

data class PutCellValue(
    override val sheetId: String,
    val address: SheetCellAddress,
    val value: SheetCellValue,
) : SheetOperation

data class ClearRange(
    override val sheetId: String,
    val range: SheetRange,
) : SheetOperation

data class InsertRows(
    override val sheetId: String,
    val startRowIndex: Int,
    val count: Int,
) : SheetOperation

data class DeleteRows(
    override val sheetId: String,
    val startRowIndex: Int,
    val count: Int,
) : SheetOperation

data class InsertColumns(
    override val sheetId: String,
    val startColumnIndex: Int,
    val count: Int,
) : SheetOperation

data class DeleteColumns(
    override val sheetId: String,
    val startColumnIndex: Int,
    val count: Int,
) : SheetOperation

data class FillDownRange(
    override val sheetId: String,
    val range: SheetRange,
) : SheetOperation

data class FillRightRange(
    override val sheetId: String,
    val range: SheetRange,
) : SheetOperation

data class PasteCells(
    override val sheetId: String,
    val startAddress: SheetCellAddress,
    val patches: List<SheetPasteCellPatch>,
) : SheetOperation

data class RenameSheet(
    override val sheetId: String,
    val title: String,
) : SheetOperation
