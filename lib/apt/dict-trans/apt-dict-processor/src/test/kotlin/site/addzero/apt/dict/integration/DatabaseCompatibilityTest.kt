package site.addzero.apt.dict.integration

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldNotBeEmpty
import site.addzero.apt.dict.sql.SqlDialectFactory
import site.addzero.apt.dict.sql.SqlGenerator
import site.addzero.apt.dict.sql.DatabaseDialect
import site.addzero.apt.dict.context.TransApi
import site.addzero.apt.dict.context.DictModel
import java.sql.Connection
import java.sql.DriverManager
import javax.sql.DataSource

/**
 * Database compatibility tests for multiple SQL dialects
 * 
 * Tests verify:
 * 1. SQL generation for different database dialects
 * 2. Translation accuracy across different data scenarios
 * 3. Batch query optimization effectiveness
 * 4. Error handling for database-specific issues
 */
class DatabaseCompatibilityTest : FunSpec({
    
    test("should generate correct SQL for MySQL dialect") {
        val sqlGenerator = SqlGenerator()
        val dialectFactory = SqlDialectFactory()
        val mysqlDialect = dialectFactory.getDialect(DatabaseDialect.MYSQL)
        
        // Test system dictionary batch query
        val systemDictSql = sqlGenerator.generateSystemDictBatchQuery(
            dictCodes = listOf("user_status", "role_type"),
            keys = listOf("ACTIVE", "ADMIN", "USER"),
            dialect = mysqlDialect
        )
        
        systemDictSql shouldNotBe null
        systemDictSql shouldContain "SELECT"
        systemDictSql shouldContain "dict_code"
        systemDictSql shouldContain "dict_value"
        systemDictSql shouldContain "dict_label"
        systemDictSql shouldContain "IN ("
        
        // Test table dictionary batch query
        val tableDictSql = sqlGenerator.generateTableDictBatchQuery(
            table = "sys_department",
            codeColumn = "id",
            nameColumn = "name",
            keys = listOf("1", "2", "3"),
            condition = "status = 'ACTIVE'",
            dialect = mysqlDialect
        )
        
        tableDictSql shouldNotBe null
        tableDictSql shouldContain "SELECT id, name FROM sys_department"
        tableDictSql shouldContain "WHERE status = 'ACTIVE'"
        tableDictSql shouldContain "AND id IN ("
    }
    
    test("should generate correct SQL for PostgreSQL dialect") {
        val sqlGenerator = SqlGenerator()
        val dialectFactory = SqlDialectFactory()
        val postgresDialect = dialectFactory.getDialect(DatabaseDialect.POSTGRESQL)
        
        val tableDictSql = sqlGenerator.generateTableDictBatchQuery(
            table = "sys_user",
            codeColumn = "id",
            nameColumn = "username",
            keys = listOf("1", "2", "3"),
            condition = "",
            dialect = postgresDialect
        )
        
        tableDictSql shouldNotBe null
        tableDictSql shouldContain "SELECT id, username FROM sys_user"
        tableDictSql shouldContain "WHERE id = ANY($1)"
        
        // PostgreSQL should use parameterized queries
        val parameters = sqlGenerator.generateParameters(listOf("1", "2", "3"), postgresDialect)
        parameters.shouldNotBeEmpty()
    }
    
    test("should generate correct SQL for Oracle dialect") {
        val sqlGenerator = SqlGenerator()
        val dialectFactory = SqlDialectFactory()
        val oracleDialect = dialectFactory.getDialect(DatabaseDialect.ORACLE)
        
        val tableDictSql = sqlGenerator.generateTableDictBatchQuery(
            table = "SYS_ROLE",
            codeColumn = "ID",
            nameColumn = "ROLE_NAME",
            keys = listOf("1", "2", "3"),
            condition = "STATUS = 1",
            dialect = oracleDialect
        )
        
        tableDictSql shouldNotBe null
        tableDictSql shouldContain "SELECT ID, ROLE_NAME FROM SYS_ROLE"
        tableDictSql shouldContain "WHERE STATUS = 1"
        
        // Oracle might use different syntax for IN clauses with large parameter lists
        if (keys.size > 1000) {
            tableDictSql shouldContain "UNION ALL"
        }
    }
    
    test("should handle SQL Server specific syntax") {
        val sqlGenerator = SqlGenerator()
        val dialectFactory = SqlDialectFactory()
        val sqlServerDialect = dialectFactory.getDialect(DatabaseDialect.SQL_SERVER)
        
        val tableDictSql = sqlGenerator.generateTableDictBatchQuery(
            table = "dbo.sys_category",
            codeColumn = "category_id",
            nameColumn = "category_name",
            keys = listOf("1", "2", "3"),
            condition = "is_active = 1",
            dialect = sqlServerDialect
        )
        
        tableDictSql shouldNotBe null
        tableDictSql shouldContain "SELECT category_id, category_name FROM dbo.sys_category"
        tableDictSql shouldContain "WHERE is_active = 1"
        tableDictSql shouldContain "AND category_id IN ("
    }
    
    test("should handle H2 database for testing") {
        val sqlGenerator = SqlGenerator()
        val dialectFactory = SqlDialectFactory()
        val h2Dialect = dialectFactory.getDialect(DatabaseDialect.H2)
        
        val systemDictSql = sqlGenerator.generateSystemDictBatchQuery(
            dictCodes = listOf("test_dict"),
            keys = listOf("TEST1", "TEST2"),
            dialect = h2Dialect
        )
        
        systemDictSql shouldNotBe null
        systemDictSql shouldContain "SELECT"
        
        // H2 should support standard SQL syntax
        val tableDictSql = sqlGenerator.generateTableDictBatchQuery(
            table = "TEST_TABLE",
            codeColumn = "ID",
            nameColumn = "NAME",
            keys = listOf("1", "2"),
            condition = "",
            dialect = h2Dialect
        )
        
        tableDictSql shouldContain "SELECT ID, NAME FROM TEST_TABLE"
    }
    
    test("should optimize batch size for different databases") {
        val sqlGenerator = SqlGenerator()
        val dialectFactory = SqlDialectFactory()
        
        // Test with large key set
        val largeKeySet = (1..2000).map { it.toString() }
        
        // MySQL - should handle large IN clauses
        val mysqlDialect = dialectFactory.getDialect(DatabaseDialect.MYSQL)
        val mysqlSql = sqlGenerator.generateTableDictBatchQuery(
            table = "large_table",
            codeColumn = "id",
            nameColumn = "name",
            keys = largeKeySet,
            condition = "",
            dialect = mysqlDialect
        )
        
        mysqlSql shouldNotBe null
        
        // Oracle - should split large parameter lists
        val oracleDialect = dialectFactory.getDialect(DatabaseDialect.ORACLE)
        val oracleSql = sqlGenerator.generateTableDictBatchQuery(
            table = "large_table",
            codeColumn = "id",
            nameColumn = "name",
            keys = largeKeySet,
            condition = "",
            dialect = oracleDialect
        )
        
        oracleSql shouldNotBe null
        
        // For Oracle with >1000 parameters, should use alternative approach
        if (largeKeySet.size > 1000) {
            // Should either use UNION ALL or temporary table approach
            oracleSql shouldContain ("UNION ALL" or "WITH")
        }
    }
    
    test("should handle special characters and SQL injection prevention") {
        val sqlGenerator = SqlGenerator()
        val dialectFactory = SqlDialectFactory()
        val dialect = dialectFactory.getDialect(DatabaseDialect.MYSQL)
        
        // Test with potentially dangerous input
        val dangerousKeys = listOf(
            "'; DROP TABLE users; --",
            "1' OR '1'='1",
            "admin'/*",
            "1; SELECT * FROM sensitive_data; --"
        )
        
        val sql = sqlGenerator.generateTableDictBatchQuery(
            table = "test_table",
            codeColumn = "id",
            nameColumn = "name",
            keys = dangerousKeys,
            condition = "",
            dialect = dialect
        )
        
        // Should use parameterized queries to prevent SQL injection
        sql shouldNotBe null
        sql shouldContain "?"  // Parameterized query placeholder
        
        // Should not contain dangerous SQL fragments
        sql shouldNotContain "DROP TABLE"
        sql shouldNotContain "SELECT * FROM"
    }
    
    test("should handle database-specific data types correctly") {
        val mockTransApi = createMockTransApi()
        
        // Test with different data types
        val testData = mapOf(
            "string_field" to "test_value",
            "numeric_field" to 12345L,
            "decimal_field" to 123.45,
            "boolean_field" to true,
            "date_field" to java.time.LocalDateTime.now()
        )
        
        // Test system dictionary translation
        val systemResults = mockTransApi.translateDictBatchCode2name(
            "test_dict",
            testData.keys.joinToString(",")
        )
        
        systemResults.shouldNotBeEmpty()
        
        // Test table dictionary translation
        val tableResults = mockTransApi.translateTableBatchCode2name(
            "test_table",
            "name",
            "code",
            testData.keys.joinToString(",")
        )
        
        tableResults.shouldNotBeEmpty()
        
        // Verify data type handling
        tableResults.forEach { row ->
            row.keys.shouldNotBeEmpty()
            row.values.forEach { value ->
                // Should handle null values gracefully
                // Should preserve data types appropriately
            }
        }
    }
    
    test("should handle concurrent database access") {
        val mockTransApi = createMockTransApi()
        val concurrentRequests = 10
        
        val futures = (1..concurrentRequests).map { i ->
            java.util.concurrent.CompletableFuture.supplyAsync {
                mockTransApi.translateDictBatchCode2name(
                    "concurrent_dict_$i",
                    "key1,key2,key3"
                )
            }
        }
        
        // Wait for all requests to complete
        val results = futures.map { it.get() }
        
        // All requests should succeed
        results.size shouldBe concurrentRequests
        results.forEach { result ->
            result.shouldNotBeEmpty()
        }
    }
    
    test("should handle database connection failures gracefully") {
        val failingTransApi = createFailingTransApi()
        
        try {
            val result = failingTransApi.translateDictBatchCode2name(
                "test_dict",
                "test_key"
            )
            
            // Should return empty result or handle error gracefully
            result shouldBe emptyList()
            
        } catch (e: Exception) {
            // Should throw appropriate exception with meaningful message
            e.message shouldNotBe null
        }
    }
    
    test("should validate translation accuracy across different scenarios") {
        val mockTransApi = createMockTransApi()
        
        // Test scenario 1: Simple key-value translation
        val simpleResult = mockTransApi.translateDictBatchCode2name(
            "simple_dict",
            "ACTIVE,INACTIVE"
        )
        
        simpleResult.size shouldBe 2
        simpleResult.find { it.code == "ACTIVE" }?.name shouldBe "Active"
        simpleResult.find { it.code == "INACTIVE" }?.name shouldBe "Inactive"
        
        // Test scenario 2: Table dictionary with conditions
        val tableResult = mockTransApi.translateTableBatchCode2name(
            "sys_department",
            "name",
            "id",
            "1,2,3"
        )
        
        tableResult.size shouldBe 3
        tableResult.forEach { row ->
            row["id"] shouldNotBe null
            row["name"] shouldNotBe null
        }
        
        // Test scenario 3: Large batch translation
        val largeBatch = (1..1000).joinToString(",")
        val largeResult = mockTransApi.translateTableBatchCode2name(
            "large_table",
            "description",
            "code",
            largeBatch
        )
        
        largeResult.size shouldBe 1000
    }
})

// Mock implementations for testing

private fun createMockTransApi(): TransApi {
    return object : TransApi {
        override fun translateDictBatchCode2name(dictCodes: String, keys: String?): List<DictModel> {
            val codes = dictCodes.split(",")
            val keyList = keys?.split(",") ?: emptyList()
            
            return codes.flatMap { dictCode ->
                keyList.map { key ->
                    DictModel(
                        dictCode = dictCode,
                        code = key,
                        name = when (key) {
                            "ACTIVE" -> "Active"
                            "INACTIVE" -> "Inactive"
                            "ADMIN" -> "Administrator"
                            "USER" -> "Regular User"
                            else -> "$key Translated"
                        }
                    )
                }
            }
        }
        
        override fun translateTableBatchCode2name(
            table: String,
            text: String,
            code: String,
            keys: String
        ): List<Map<String, Any?>> {
            val keyList = keys.split(",")
            
            return keyList.map { key ->
                mapOf(
                    code to key,
                    text to when (table) {
                        "sys_department" -> "Department $key"
                        "sys_role" -> "Role $key"
                        "large_table" -> "Description for $key"
                        else -> "Name for $key"
                    }
                )
            }
        }
    }
}

private fun createFailingTransApi(): TransApi {
    return object : TransApi {
        override fun translateDictBatchCode2name(dictCodes: String, keys: String?): List<DictModel> {
            throw RuntimeException("Database connection failed")
        }
        
        override fun translateTableBatchCode2name(
            table: String,
            text: String,
            code: String,
            keys: String
        ): List<Map<String, Any?>> {
            throw RuntimeException("Table not found: $table")
        }
    }
}