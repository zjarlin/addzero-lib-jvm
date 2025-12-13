package site.addzero.apt.dict.sql

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import site.addzero.apt.dict.dsl.TableDictInfo

/**
 * Test cases for SqlGenerator
 * 
 * Tests SQL generation functionality including:
 * - System dictionary batch queries
 * - Table dictionary batch queries
 * - Query optimization and validation
 * - Multiple SQL dialect support
 */
class SqlGeneratorTest {
    
    private lateinit var sqlGenerator: SqlGenerator
    
    @BeforeEach
    fun setUp() {
        sqlGenerator = SqlGenerator(SqlDialect.MYSQL)
    }
    
    @Test
    fun `should generate system dictionary batch SQL`() {
        val dictCodes = setOf("user_status", "role_type", "permission_level")
        
        val batchQuery = sqlGenerator.generateSystemDictBatchSql(dictCodes)
        
        assertNotNull(batchQuery)
        assertEquals(QueryType.SYSTEM_DICT_BATCH, batchQuery.queryType)
        assertEquals(6, batchQuery.parameterCount) // 3 dict codes + 3 value placeholders
        assertTrue(batchQuery.sql.contains("sys_dict_item"))
        assertTrue(batchQuery.sql.contains("dict_code IN"))
        assertTrue(batchQuery.sql.contains("item_value IN"))
        assertTrue(batchQuery.sql.contains("ORDER BY"))
        assertTrue(batchQuery.optimizationHints.isNotEmpty())
    }
    
    @Test
    fun `should generate table dictionary batch SQL`() {
        val tableConfig = TableDictInfo(
            table = "sys_department",
            codeColumn = "id",
            nameColumn = "dept_name"
        )
        
        val batchQuery = sqlGenerator.generateTableDictBatchSql(tableConfig)
        
        assertNotNull(batchQuery)
        assertEquals(QueryType.TABLE_DICT_BATCH, batchQuery.queryType)
        assertEquals(1, batchQuery.parameterCount)
        assertTrue(batchQuery.sql.contains("sys_department"))
        assertTrue(batchQuery.sql.contains("id as code"))
        assertTrue(batchQuery.sql.contains("dept_name as name"))
        assertTrue(batchQuery.sql.contains("WHERE"))
        assertEquals(tableConfig, batchQuery.tableInfo)
    }
    
    @Test
    fun `should generate table dictionary SQL with custom condition`() {
        val tableConfig = TableDictInfo(
            table = "sys_region",
            codeColumn = "region_code",
            nameColumn = "region_name"
        )
        val condition = "status = 1 AND level <= 3"
        
        val batchQuery = sqlGenerator.generateTableDictBatchSql(tableConfig, condition)
        
        assertTrue(batchQuery.sql.contains("AND ($condition)"))
        assertTrue(batchQuery.optimizationHints.any { it.contains(condition) })
    }
    
    @Test
    fun `should generate consolidated table dictionary SQL`() {
        val tableConfigs = setOf(
            TableDictInfo("sys_department", "id", "name"),
            TableDictInfo("sys_organization", "id", "name"),
            TableDictInfo("sys_region", "code", "text") // Different structure
        )
        
        val consolidatedQueries = sqlGenerator.generateConsolidatedTableDictSql(tableConfigs)
        
        // Should group by column structure
        assertTrue(consolidatedQueries.size <= tableConfigs.size)
        
        // Check for UNION query for same structure tables
        val unionQuery = consolidatedQueries.find { it.queryType == QueryType.UNION_TABLE_DICT_BATCH }
        if (unionQuery != null) {
            assertTrue(unionQuery.sql.contains("UNION ALL"))
            assertTrue(unionQuery.sql.contains("source_table"))
        }
    }
    
    @Test
    fun `should optimize batch size correctly`() {
        val optimization = sqlGenerator.optimizeBatchSize(
            QueryType.SYSTEM_DICT_BATCH,
            totalItems = 1500,
            maxParameters = 1000
        )
        
        assertTrue(optimization.optimalBatchSize <= 500) // System dict limit
        assertTrue(optimization.optimalBatchSize <= 1000) // Max parameters
        assertEquals(3, optimization.totalBatches) // ceil(1500/500)
        assertTrue(optimization.estimatedExecutionTime > 0)
        assertTrue(optimization.recommendations.isNotEmpty())
    }
    
    @Test
    fun `should generate executable SQL with parameters`() {
        val batchQuery = BatchSqlQuery(
            sql = "SELECT * FROM test WHERE id IN (?)",
            parameterCount = 1,
            queryType = QueryType.TABLE_DICT_BATCH,
            estimatedRows = 10
        )
        val values = listOf("1", "2", "3")
        
        val executableQuery = sqlGenerator.generateExecutableSql(batchQuery, values)
        
        assertEquals(values, executableQuery.parameters)
        assertEquals(QueryType.TABLE_DICT_BATCH, executableQuery.queryType)
        assertNotNull(executableQuery.executionPlan)
        assertTrue(executableQuery.executionPlan.estimatedCost > 0)
    }
    
    @Test
    fun `should validate query for security and performance`() {
        val goodQuery = BatchSqlQuery(
            sql = "SELECT id, name FROM users WHERE status = ? ORDER BY id",
            parameterCount = 1,
            queryType = QueryType.TABLE_DICT_BATCH,
            estimatedRows = 100,
            optimizationHints = listOf("Use index on status")
        )
        
        val validation = sqlGenerator.validateQuery(goodQuery)
        
        assertTrue(validation.isValid)
        assertTrue(validation.issues.isEmpty())
        assertTrue(validation.performanceScore > 70)
    }
    
    @Test
    fun `should detect SQL injection risks`() {
        val riskyQuery = BatchSqlQuery(
            sql = "SELECT * FROM users WHERE name = 'test'",
            parameterCount = 0,
            queryType = QueryType.TABLE_DICT_BATCH,
            estimatedRows = 1
        )
        
        val validation = sqlGenerator.validateQuery(riskyQuery)
        
        assertFalse(validation.isValid)
        assertTrue(validation.issues.any { it.contains("SQL injection") })
    }
    
    @Test
    fun `should warn about performance issues`() {
        val slowQuery = BatchSqlQuery(
            sql = "SELECT * FROM large_table",
            parameterCount = 0,
            queryType = QueryType.TABLE_DICT_BATCH,
            estimatedRows = 50000
        )
        
        val validation = sqlGenerator.validateQuery(slowQuery)
        
        assertTrue(validation.warnings.any { it.contains("full table scan") })
        assertTrue(validation.warnings.any { it.contains("Large result set") })
        assertTrue(validation.performanceScore < 50)
    }
    
    @Test
    fun `should support different SQL dialects`() {
        val dictCodes = setOf("test_dict")
        
        // Test MySQL
        val mysqlGenerator = SqlGenerator(SqlDialect.MYSQL)
        val mysqlQuery = mysqlGenerator.generateSystemDictBatchSql(dictCodes)
        assertTrue(mysqlQuery.sql.contains("IN (?)"))
        
        // Test PostgreSQL
        val pgGenerator = SqlGenerator(SqlDialect.POSTGRESQL)
        val pgQuery = pgGenerator.generateSystemDictBatchSql(dictCodes)
        assertTrue(pgQuery.sql.contains("= ANY(?)"))
        
        // Test Oracle
        val oracleGenerator = SqlGenerator(SqlDialect.ORACLE)
        val oracleQuery = oracleGenerator.generateSystemDictBatchSql(dictCodes)
        assertTrue(oracleQuery.sql.contains("IN (?)"))
    }
}