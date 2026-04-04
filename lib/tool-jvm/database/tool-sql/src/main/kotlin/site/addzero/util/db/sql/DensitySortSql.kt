package site.addzero.util.db.sql

/**
 * Builds SQL that left-shifts non-null values across columns and orders by non-null count.
 */
object DensitySortSql {
    /**
     * Original 4-column script (kept for direct reuse).
     */
    val FOUR_COLUMN_SCRIPT: String = """
        SELECT id,
               COALESCE(col1, col2, col3, col4) AS col1,
               CASE WHEN col1 IS NOT NULL THEN COALESCE(col2, col3, col4)
                    WHEN col2 IS NOT NULL THEN COALESCE(col3, col4)
                    WHEN col3 IS NOT NULL THEN col4
                    ELSE NULL END AS col2,
               CASE WHEN col1 IS NOT NULL AND col2 IS NOT NULL THEN COALESCE(col3, col4)
                    WHEN (col1 IS NOT NULL OR col2 IS NOT NULL) AND col3 IS NOT NULL THEN col4
                    ELSE NULL END AS col3,
               CASE WHEN col1 IS NOT NULL AND col2 IS NOT NULL AND col3 IS NOT NULL THEN col4
                    ELSE NULL END AS col4
        FROM big_table
        ORDER BY (CASE WHEN col1 IS NOT NULL THEN 1 ELSE 0 END +
                  CASE WHEN col2 IS NOT NULL THEN 1 ELSE 0 END +
                  CASE WHEN col3 IS NOT NULL THEN 1 ELSE 0 END +
                  CASE WHEN col4 IS NOT NULL THEN 1 ELSE 0 END) DESC;
    """.trimIndent()

    /**
     * Build a SELECT statement that:
     * 1) Shifts non-null values to the left across the provided columns
     * 2) Orders rows by non-null column count (descending by default)
     */
    @JvmOverloads
    fun build(
        table: String,
        columns: List<String>,
        idColumn: String? = "id",
        orderByDesc: Boolean = true
    ): String {
        require(table.isNotBlank()) { "table must not be blank" }
        require(columns.isNotEmpty()) { "columns must not be empty" }
        require(columns.all { it.isNotBlank() }) { "columns must not contain blank names" }

        val selectParts = mutableListOf<String>()
        if (!idColumn.isNullOrBlank()) {
            selectParts += idColumn
        }

        columns.forEachIndexed { index, column ->
            val expr = shiftNonNullExpression(columns, index)
            selectParts += "$expr AS $column"
        }

        val orderExpr = nonNullCountExpression(columns)
        val orderClause = if (orderByDesc) " ORDER BY $orderExpr DESC" else " ORDER BY $orderExpr"

        return "SELECT ${selectParts.joinToString(", ")} FROM $table$orderClause"
    }

    /**
     * Convenience sample that matches the original 4-column script.
     */
    fun sample(): String = FOUR_COLUMN_SCRIPT

    private fun shiftNonNullExpression(columns: List<String>, index: Int): String {
        return if (index == 0) {
            "COALESCE(${columns.joinToString(", ")})"
        } else {
            val whenClauses = columns.indices
                .filter { it >= index }
                .joinToString(" ") { columnIndex ->
                    val countPrev = nonNullCountExpression(columns.subList(0, columnIndex))
                    "WHEN ($countPrev) = $index AND ${columns[columnIndex]} IS NOT NULL THEN ${columns[columnIndex]}"
                }
            "CASE $whenClauses ELSE NULL END"
        }
    }

    private fun nonNullCountExpression(columns: List<String>): String {
        if (columns.isEmpty()) {
            return "0"
        }
        return columns.joinToString(" + ") { "CASE WHEN $it IS NOT NULL THEN 1 ELSE 0 END" }
    }
}
