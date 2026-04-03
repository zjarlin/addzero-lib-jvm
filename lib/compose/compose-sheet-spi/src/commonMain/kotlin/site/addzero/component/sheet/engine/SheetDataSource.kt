package site.addzero.component.sheet.engine

/**
 * 在线表格文档数据源。
 */
interface SheetDataSource {
    suspend fun load(documentId: String): SheetDocument

    suspend fun applyOperations(
        documentId: String,
        baseVersion: Long,
        operations: List<SheetOperation>,
    ): SheetDocument
}
