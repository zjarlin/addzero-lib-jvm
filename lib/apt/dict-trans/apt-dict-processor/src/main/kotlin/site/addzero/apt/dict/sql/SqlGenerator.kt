package site.addzero.apt.dict.sql

import site.addzero.apt.dict.dsl.*

/**
 * SQL generation and optimization engine for compile-time dictionary translation
 * 
 * This class generates optimized SQL statements for batch dictionary translation,
 * eliminating N+1 query problems through intelligent query optimization and
 * supporting multiple SQL dialects.
 */
class SqlGenerator(
    private val dialect: DatabaseDialect = DatabaseDialect.MYSQL
) {
    
    /**
     * Generates optimized SQL for system dictionary batch translation
     */
    fun generateSystemDictBatchQuery(dictCodes: List<String>, keys: List<String>): String {
        val dictCodePlaceholders = dictCodes.joinToString(",") { "?" }
        val keyPlaceholders = keys.joinToString(",") { "?" }
        
        return when (dialect) {
            DatabaseDialect.MYSQL -> """
                SELECT 
                    d.dict_code,
                    d.item_value as code,
                    d.item_text as name
                FROM sys_dict_item d
                WHERE d.dict_code IN ($dictCodePlaceholders)
                  AND d.item_value IN ($keyPlaceholders)
                  AND d.status = 1
                ORDER BY d.dict_code, d.sort_order
            """.trimIndent()
            
            DatabaseDialect.POSTGRESQL -> """
                SELECT 
                    d.dict_code,
                    d.item_value as code,
                    d.item_text as name
                FROM sys_dict_item d
                WHERE d.dict_code = ANY(?)
                  AND d.item_value = ANY(?)
                  AND d.status = 1
                ORDER BY d.dict_code, d.sort_order
            """.trimIndent()
            
            else -> """
                SELECT 
                    d.dict_code,
                    d.item_value as code,
                    d.item_text as name
                FROM sys_dict_item d
                WHERE d.dict_code IN ($dictCodePlaceholders)
                  AND d.item_value IN ($keyPlaceholders)
                  AND d.status = 1
                ORDER BY d.dict_code, d.sort_order
            """.trimIndent()
        }
    }
    
    /**
     * Generates optimized SQL for table dictionary batch translation
     */
    fun generateTableDictBatchQuery(
        table: String,
        codeColumn: String,
        nameColumn: String,
        keys: List<String>,
        condition: String? = null
    ): String {
        val quotedTable = dialect.quoteIdentifier(table)
        val quotedCodeColumn = dialect.quoteIdentifier(codeColumn)
        val quotedNameColumn = dialect.quoteIdentifier(nameColumn)
        val keyPlaceholders = keys.joinToString(",") { "?" }
        
        val baseQuery = when (dialect) {
            DatabaseDialect.POSTGRESQL -> """
                SELECT 
                    $quotedCodeColumn as code,
                    $quotedNameColumn as name
                FROM $quotedTable
                WHERE $quotedCodeColumn = ANY(?)
            """.trimIndent()
            
            else -> """
                SELECT 
                    $quotedCodeColumn as code,
                    $quotedNameColumn as name
                FROM $quotedTable
                WHERE $quotedCodeColumn IN ($keyPlaceholders)
            """.trimIndent()
        }
        
        return if (condition != null) {
            "$baseQuery AND ($condition)"
        } else {
            baseQuery
        }
    }
    
    /**
     * Generates parameters for SQL queries
     */
    fun generateParameters(values: List<String>, dialect: DatabaseDialect): List<String> {
        return when (dialect) {
            DatabaseDialect.POSTGRESQL -> {
                // PostgreSQL uses array parameters
                listOf("{${values.joinToString(",")}}")
            }
            else -> {
                // Other databases use individual parameters
                values
            }
        }
    }
    
