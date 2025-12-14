package site.addzero.dict.trans.inter

/**
 * Context for table dictionary translation
 * Used to collect all table translation requirements at compile time
 */
data class TableTranslateContext(
    val table: String,
    val textColumn: String,
    val codeColumn: String,
    val keys: String, // Will be replaced at runtime with actual values
    val whereCondition: String = ""
)