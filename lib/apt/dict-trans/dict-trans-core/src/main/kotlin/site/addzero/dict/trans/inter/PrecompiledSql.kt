package site.addzero.dict.trans.inter

/**
 * Precompiled SQL template for table dictionary translation
 * Generated at compile time, executed at runtime with actual parameters
 */
data class PrecompiledSql(
    val sqlTemplate: String, // e.g., "SELECT id, name FROM role WHERE id IN (?) AND status = ?"
    val table: String,
    val textColumn: String,
    val codeColumn: String,
    val whereCondition: String
) {
    /**
     * Generate actual SQL by replacing placeholders with runtime values
     */
    fun generateSql(keys: String): String {
        return sqlTemplate.replace("?", "'$keys'") // Simple replacement, in practice use prepared statements
    }
}