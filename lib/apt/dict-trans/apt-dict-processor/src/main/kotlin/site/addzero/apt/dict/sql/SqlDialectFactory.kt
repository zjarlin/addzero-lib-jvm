package site.addzero.apt.dict.sql

/**
 * Factory for creating SQL dialect-specific generators and optimizers
 * 
 * This factory provides a centralized way to create SQL generators
 * and optimizers for different database dialects, ensuring consistent
 * behavior across the application.
 */
object SqlDialectFactory {
    
    /**
     * Creates a SQL generator for the specified dialect
     */
    fun createGenerator(dialect: DatabaseDialect): SqlGenerator {
        return SqlGenerator(dialect)
    }
    
    /**
     * Gets dialect from database dialect enum
     */
    fun getDialect(dialect: DatabaseDialect): DatabaseDialect {
        return dialect
    }
    
    /**
     * Detects SQL dialect from database URL or driver class
     */
    fun detectDialect(databaseUrl: String): DatabaseDialect {
        return when {
            databaseUrl.contains("mysql", ignoreCase = true) -> DatabaseDialect.MYSQL
            databaseUrl.contains("postgresql", ignoreCase = true) -> DatabaseDialect.POSTGRESQL
            databaseUrl.contains("oracle", ignoreCase = true) -> DatabaseDialect.ORACLE
            databaseUrl.contains("sqlserver", ignoreCase = true) -> DatabaseDialect.SQL_SERVER
            databaseUrl.contains("h2", ignoreCase = true) -> DatabaseDialect.H2
            else -> DatabaseDialect.MYSQL // Default fallback
        }
    }
    
    /**
     * Gets dialect-specific configuration
     */
    fun getDialectConfig(dialect: DatabaseDialect): DialectConfig {
        return when (dialect) {
            DatabaseDialect.MYSQL -> DialectConfig(
                maxParametersPerQuery = 65535,
                supportsArrayParameters = false,
                batchInsertSyntax = "INSERT INTO table VALUES (?,?), (?,?)",
                limitSyntax = "LIMIT ?",
                offsetSyntax = "LIMIT ?, ?",
                identifierQuote = "`"
            )
            DatabaseDialect.POSTGRESQL -> DialectConfig(
                maxParametersPerQuery = 32767,
                supportsArrayParameters = true,
                batchInsertSyntax = "INSERT INTO table VALUES (?,?), (?,?)",
                limitSyntax = "LIMIT ?",
                offsetSyntax = "LIMIT ? OFFSET ?",
                identifierQuote = "\""
            )
            DatabaseDialect.ORACLE -> DialectConfig(
                maxParametersPerQuery = 1000,
                supportsArrayParameters = false,
                batchInsertSyntax = "INSERT ALL INTO table VALUES (?,?) INTO table VALUES (?,?) SELECT * FROM dual",
                limitSyntax = "ROWNUM <= ?",
                offsetSyntax = "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
                identifierQuote = "\""
            )
            DatabaseDialect.SQL_SERVER -> DialectConfig(
                maxParametersPerQuery = 2100,
                supportsArrayParameters = false,
                batchInsertSyntax = "INSERT INTO table VALUES (?,?), (?,?)",
                limitSyntax = "TOP ?",
                offsetSyntax = "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
                identifierQuote = "["
            )
            DatabaseDialect.H2 -> DialectConfig(
                maxParametersPerQuery = 90000,
                supportsArrayParameters = true,
                batchInsertSyntax = "INSERT INTO table VALUES (?,?), (?,?)",
                limitSyntax = "LIMIT ?",
                offsetSyntax = "LIMIT ? OFFSET ?",
                identifierQuote = "\""
            )
        }
    }
    
    /**
     * Creates a complete SQL processing pipeline for a dialect
     */
    fun createPipeline(dialect: DatabaseDialect): SqlProcessingPipeline {
        return SqlProcessingPipeline(
            generator = createGenerator(dialect),
            config = getDialectConfig(dialect)
        )
    }
}

/**
 * Dialect-specific configuration
 */
data class DialectConfig(
    val maxParametersPerQuery: Int,
    val supportsArrayParameters: Boolean,
    val batchInsertSyntax: String,
    val limitSyntax: String,
    val offsetSyntax: String,
    val identifierQuote: String
)

/**
 * Complete SQL processing pipeline
 */
data class SqlProcessingPipeline(
    val generator: SqlGenerator,
    val config: DialectConfig
) {
    
    /**
     * Processes a complete dictionary translation request
     */
    fun processTranslationRequest(request: TranslationRequest): ProcessedTranslationResult {
        // Generate initial queries
        val queries = mutableListOf<String>()
        
        // Add system dictionary queries
        if (request.systemDictCodes.isNotEmpty()) {
            queries.add(generator.generateSystemDictBatchQuery(
                request.systemDictCodes.toList(), 
                emptyList()
            ))
        }
        
        // Add table dictionary queries
        request.tableDictConfigs.forEach { tableConfig ->
            queries.add(generator.generateTableDictBatchQuery(
                tableConfig.table,
                tableConfig.codeColumn,
                tableConfig.nameColumn,
                emptyList(),
                request.conditions[tableConfig.table]
            ))
        }
        
        return ProcessedTranslationResult(
            queries = queries,
            validationResults = emptyList(),
            optimizationImprovement = 0.0,
            totalQueries = queries.size
        )
    }
}

/**
 * Translation request containing all dictionary requirements
 */
data class TranslationRequest(
    val systemDictCodes: Set<String>,
    val tableDictConfigs: Set<site.addzero.apt.dict.dsl.TableDictInfo>,
    val conditions: Map<String, String> = emptyMap()
)

/**
 * Processed translation result
 */
data class ProcessedTranslationResult(
    val queries: List<String>,
    val validationResults: List<String>,
    val optimizationImprovement: Double,
    val totalQueries: Int
)