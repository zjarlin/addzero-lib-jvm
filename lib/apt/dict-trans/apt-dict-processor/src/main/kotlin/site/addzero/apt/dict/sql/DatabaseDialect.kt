package site.addzero.apt.dict.sql

/**
 * Database dialect enumeration for SQL generation
 * 
 * This enum defines the supported database dialects and their specific
 * SQL syntax variations for dictionary translation queries.
 */
enum class DatabaseDialect(
    val dialectName: String,
    val driverClassName: String,
    val urlPrefix: String
) {
    /**
     * MySQL database dialect
     */
    MYSQL(
        dialectName = "MySQL",
        driverClassName = "com.mysql.cj.jdbc.Driver",
        urlPrefix = "jdbc:mysql://"
    ) {
        override fun getLimitClause(limit: Int, offset: Int): String {
            return if (offset > 0) {
                "LIMIT $offset, $limit"
            } else {
                "LIMIT $limit"
            }
        }
        
        override fun getQuoteCharacter(): String = "`"
        
        override fun supportsUpsert(): Boolean = true
        
        override fun getUpsertClause(table: String, columns: List<String>, conflictColumns: List<String>): String {
            val columnList = columns.joinToString(", ") { getQuoteCharacter() + it + getQuoteCharacter() }
            val valueList = columns.joinToString(", ") { "?" }
            val updateList = columns.filter { it !in conflictColumns }
                .joinToString(", ") { "${getQuoteCharacter()}$it${getQuoteCharacter()} = VALUES(${getQuoteCharacter()}$it${getQuoteCharacter()})" }
            
            return "INSERT INTO ${getQuoteCharacter()}$table${getQuoteCharacter()} ($columnList) VALUES ($valueList) ON DUPLICATE KEY UPDATE $updateList"
        }
    },
    
    /**
     * PostgreSQL database dialect
     */
    POSTGRESQL(
        dialectName = "PostgreSQL",
        driverClassName = "org.postgresql.Driver",
        urlPrefix = "jdbc:postgresql://"
    ) {
        override fun getLimitClause(limit: Int, offset: Int): String {
            return if (offset > 0) {
                "LIMIT $limit OFFSET $offset"
            } else {
                "LIMIT $limit"
            }
        }
        
        override fun getQuoteCharacter(): String = "\""
        
        override fun supportsUpsert(): Boolean = true
        
        override fun getUpsertClause(table: String, columns: List<String>, conflictColumns: List<String>): String {
            val columnList = columns.joinToString(", ") { getQuoteCharacter() + it + getQuoteCharacter() }
            val valueList = columns.joinToString(", ") { "?" }
            val conflictList = conflictColumns.joinToString(", ") { getQuoteCharacter() + it + getQuoteCharacter() }
            val updateList = columns.filter { it !in conflictColumns }
                .joinToString(", ") { "${getQuoteCharacter()}$it${getQuoteCharacter()} = EXCLUDED.${getQuoteCharacter()}$it${getQuoteCharacter()}" }
            
            return "INSERT INTO ${getQuoteCharacter()}$table${getQuoteCharacter()} ($columnList) VALUES ($valueList) ON CONFLICT ($conflictList) DO UPDATE SET $updateList"
        }
    },
    
    /**
     * Oracle database dialect
     */
    ORACLE(
        dialectName = "Oracle",
        driverClassName = "oracle.jdbc.OracleDriver",
        urlPrefix = "jdbc:oracle:thin:@"
    ) {
        override fun getLimitClause(limit: Int, offset: Int): String {
            return if (offset > 0) {
                "OFFSET $offset ROWS FETCH NEXT $limit ROWS ONLY"
            } else {
                "FETCH FIRST $limit ROWS ONLY"
            }
        }
        
        override fun getQuoteCharacter(): String = "\""
        
        override fun supportsUpsert(): Boolean = true
        
        override fun getUpsertClause(table: String, columns: List<String>, conflictColumns: List<String>): String {
            // Oracle uses MERGE statement for upsert
            val columnList = columns.joinToString(", ") { getQuoteCharacter() + it + getQuoteCharacter() }
            val valueList = columns.joinToString(", ") { "?" }
            val matchCondition = conflictColumns.joinToString(" AND ") { 
                "target.${getQuoteCharacter()}$it${getQuoteCharacter()} = source.${getQuoteCharacter()}$it${getQuoteCharacter()}" 
            }
            val updateList = columns.filter { it !in conflictColumns }
                .joinToString(", ") { "${getQuoteCharacter()}$it${getQuoteCharacter()} = source.${getQuoteCharacter()}$it${getQuoteCharacter()}" }
            
            return """
                MERGE INTO ${getQuoteCharacter()}$table${getQuoteCharacter()} target
                USING (SELECT $valueList FROM dual) source ($columnList)
                ON ($matchCondition)
                WHEN MATCHED THEN UPDATE SET $updateList
                WHEN NOT MATCHED THEN INSERT ($columnList) VALUES ($valueList)
            """.trimIndent()
        }
    },
    
    /**
     * SQL Server database dialect
     */
    SQL_SERVER(
        dialectName = "SQL Server",
        driverClassName = "com.microsoft.sqlserver.jdbc.SQLServerDriver",
        urlPrefix = "jdbc:sqlserver://"
    ) {
        override fun getLimitClause(limit: Int, offset: Int): String {
            return if (offset > 0) {
                "OFFSET $offset ROWS FETCH NEXT $limit ROWS ONLY"
            } else {
                "OFFSET 0 ROWS FETCH NEXT $limit ROWS ONLY"
            }
        }
        
        override fun getQuoteCharacter(): String = "["
        
        override fun getCloseQuoteCharacter(): String = "]"
        
        override fun supportsUpsert(): Boolean = true
        
        override fun getUpsertClause(table: String, columns: List<String>, conflictColumns: List<String>): String {
            // SQL Server uses MERGE statement for upsert
            val columnList = columns.joinToString(", ") { getQuoteCharacter() + it + getCloseQuoteCharacter() }
            val valueList = columns.joinToString(", ") { "?" }
            val matchCondition = conflictColumns.joinToString(" AND ") { 
                "target.${getQuoteCharacter()}$it${getCloseQuoteCharacter()} = source.${getQuoteCharacter()}$it${getCloseQuoteCharacter()}" 
            }
            val updateList = columns.filter { it !in conflictColumns }
                .joinToString(", ") { "${getQuoteCharacter()}$it${getCloseQuoteCharacter()} = source.${getQuoteCharacter()}$it${getCloseQuoteCharacter()}" }
            
            return """
                MERGE ${getQuoteCharacter()}$table${getCloseQuoteCharacter()} AS target
                USING (VALUES ($valueList)) AS source ($columnList)
                ON $matchCondition
                WHEN MATCHED THEN UPDATE SET $updateList
                WHEN NOT MATCHED THEN INSERT ($columnList) VALUES ($valueList);
            """.trimIndent()
        }
    },
    
    /**
     * H2 database dialect (for testing)
     */
    H2(
        dialectName = "H2",
        driverClassName = "org.h2.Driver",
        urlPrefix = "jdbc:h2:"
    ) {
        override fun getLimitClause(limit: Int, offset: Int): String {
            return if (offset > 0) {
                "LIMIT $limit OFFSET $offset"
            } else {
                "LIMIT $limit"
            }
        }
        
        override fun getQuoteCharacter(): String = "\""
        
        override fun supportsUpsert(): Boolean = true
        
        override fun getUpsertClause(table: String, columns: List<String>, conflictColumns: List<String>): String {
            // H2 supports MERGE statement
            val columnList = columns.joinToString(", ") { getQuoteCharacter() + it + getQuoteCharacter() }
            val valueList = columns.joinToString(", ") { "?" }
            val matchCondition = conflictColumns.joinToString(" AND ") { 
                "target.${getQuoteCharacter()}$it${getQuoteCharacter()} = source.${getQuoteCharacter()}$it${getQuoteCharacter()}" 
            }
            val updateList = columns.filter { it !in conflictColumns }
                .joinToString(", ") { "${getQuoteCharacter()}$it${getQuoteCharacter()} = source.${getQuoteCharacter()}$it${getQuoteCharacter()}" }
            
            return """
                MERGE INTO ${getQuoteCharacter()}$table${getQuoteCharacter()} target
                USING (VALUES ($valueList)) source ($columnList)
                ON $matchCondition
                WHEN MATCHED THEN UPDATE SET $updateList
                WHEN NOT MATCHED THEN INSERT ($columnList) VALUES ($valueList)
            """.trimIndent()
        }
    };
    
    /**
     * Gets the LIMIT clause for this dialect
     */
    abstract fun getLimitClause(limit: Int, offset: Int = 0): String
    
    /**
     * Gets the quote character for identifiers
     */
    abstract fun getQuoteCharacter(): String
    
    /**
     * Gets the closing quote character for identifiers (defaults to same as opening)
     */
    open fun getCloseQuoteCharacter(): String = getQuoteCharacter()
    
    /**
     * Checks if this dialect supports upsert operations
     */
    abstract fun supportsUpsert(): Boolean
    
    /**
     * Gets the upsert clause for this dialect
     */
    abstract fun getUpsertClause(table: String, columns: List<String>, conflictColumns: List<String>): String
    
    /**
     * Quotes an identifier for this dialect
     */
    fun quoteIdentifier(identifier: String): String {
        return getQuoteCharacter() + identifier + getCloseQuoteCharacter()
    }
    
    /**
     * Gets the batch insert statement for this dialect
     */
    fun getBatchInsertStatement(table: String, columns: List<String>, batchSize: Int): String {
        val quotedTable = quoteIdentifier(table)
        val quotedColumns = columns.joinToString(", ") { quoteIdentifier(it) }
        val valueRows = (1..batchSize).joinToString(", ") { 
            "(" + columns.joinToString(", ") { "?" } + ")"
        }
        
        return "INSERT INTO $quotedTable ($quotedColumns) VALUES $valueRows"
    }
    
    /**
     * Gets the batch update statement for this dialect
     */
    fun getBatchUpdateStatement(table: String, setColumns: List<String>, whereColumns: List<String>): String {
        val quotedTable = quoteIdentifier(table)
        val setClause = setColumns.joinToString(", ") { "${quoteIdentifier(it)} = ?" }
        val whereClause = whereColumns.joinToString(" AND ") { "${quoteIdentifier(it)} = ?" }
        
        return "UPDATE $quotedTable SET $setClause WHERE $whereClause"
    }
    
    /**
     * Detects database dialect from JDBC URL
     */
    companion object {
        fun fromJdbcUrl(jdbcUrl: String): DatabaseDialect {
            return when {
                jdbcUrl.startsWith("jdbc:mysql://") -> MYSQL
                jdbcUrl.startsWith("jdbc:postgresql://") -> POSTGRESQL
                jdbcUrl.startsWith("jdbc:oracle:") -> ORACLE
                jdbcUrl.startsWith("jdbc:sqlserver://") -> SQL_SERVER
                jdbcUrl.startsWith("jdbc:h2:") -> H2
                else -> throw IllegalArgumentException("Unsupported JDBC URL: $jdbcUrl")
            }
        }
        
        /**
         * Detects database dialect from driver class name
         */
        fun fromDriverClassName(driverClassName: String): DatabaseDialect {
            return when (driverClassName) {
                "com.mysql.cj.jdbc.Driver", "com.mysql.jdbc.Driver" -> MYSQL
                "org.postgresql.Driver" -> POSTGRESQL
                "oracle.jdbc.OracleDriver", "oracle.jdbc.driver.OracleDriver" -> ORACLE
                "com.microsoft.sqlserver.jdbc.SQLServerDriver" -> SQL_SERVER
                "org.h2.Driver" -> H2
                else -> throw IllegalArgumentException("Unsupported driver class: $driverClassName")
            }
        }
    }
}