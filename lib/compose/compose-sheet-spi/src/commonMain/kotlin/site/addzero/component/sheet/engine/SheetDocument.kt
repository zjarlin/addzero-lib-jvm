package site.addzero.component.sheet.engine

/**
 * 单个工作表。
 */
data class SheetPage(
    val sheetId: String,
    val title: String,
    val rowCount: Int = 1000,
    val columnCount: Int = 26,
    val frozenRowCount: Int = 1,
    val frozenColumnCount: Int = 1,
    val cells: Map<SheetCellAddress, SheetCellValue> = emptyMap(),
) {
    init {
        require(rowCount > 0) { "rowCount 必须大于 0" }
        require(columnCount > 0) { "columnCount 必须大于 0" }
        require(frozenRowCount >= 0) { "frozenRowCount 不能小于 0" }
        require(frozenColumnCount >= 0) { "frozenColumnCount 不能小于 0" }
    }

    fun cell(address: SheetCellAddress): SheetCellValue? {
        return cells[address]
    }
}

/**
 * 整个在线表格文档。
 */
data class SheetDocument(
    val documentId: String,
    val version: Long = 0L,
    val activeSheetId: String? = null,
    val sheets: List<SheetPage> = emptyList(),
    val presences: List<SheetPresence> = emptyList(),
) {
    val resolvedActiveSheetId
        get() = activeSheetId ?: sheets.firstOrNull()?.sheetId

    fun activeSheet(): SheetPage? {
        val resolvedSheetId = resolvedActiveSheetId ?: return null
        return sheetById(resolvedSheetId)
    }

    fun sheetById(sheetId: String): SheetPage? {
        return sheets.find { it.sheetId == sheetId }
    }
}
