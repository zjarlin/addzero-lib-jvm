package site.addzero.apt.dict.context

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.*
import org.mockito.kotlin.whenever
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import java.util.concurrent.CompletableFuture

/**
 * Unit tests for TranslationContextBuilder
 * 
 * Tests context building functionality, cache management,
 * and asynchronous context population
 */
class TranslationContextBuilderTest {
    
    private lateinit var mockTransApi: TransApi
    private lateinit var contextBuilder: TranslationContextBuilder
    
    @BeforeEach
    fun setUp() {
        mockTransApi = mock(TransApi::class.java)
        contextBuilder = TranslationContextBuilder(mockTransApi)
    }
    
    @Test
    fun `test build empty context`() {
        val context = contextBuilder.build()
        
        assertNotNull(context)
        context.getAvailableSystemDicts().shouldBeEmpty()
        context.getAvailableTableDicts().shouldBeEmpty()
    }
    
    @Test
    fun `test build context with system dictionaries`() {
        val systemDictCodes = setOf("user_status", "user_type")
        
        // Mock system dictionary data
        whenever(mockTransApi.translateDictBatchCode2name("user_status", "1,2"))
            .thenReturn(listOf(
                DictModel("1", "Active"),
                DictModel("2", "Inactive")
            ))
        
        whenever(mockTransApi.translateDictBatchCode2name("user_type", "admin,user"))
            .thenReturn(listOf(
                DictModel("admin", "Administrator"),
                DictModel("user", "Regular User")
            ))
        
        val context = contextBuilder
            .withSystemDictionaries(systemDictCodes)
            .build()
        
        context.getAvailableSystemDicts().shouldNotBeEmpty()
        context.getAvailableSystemDicts() shouldContain "user_status"
        context.getAvailableSystemDicts() shouldContain "user_type"
    }
    
    @Test
    fun `test build context with table dictionaries`() {
        val tableDictRequests = setOf(
            TableDictRequest("sys_dept", "code", "name", null),
            TableDictRequest("sys_role", "id", "name", "status = 1")
        )
        
        val context = contextBuilder
            .withTableDictionaries(tableDictRequests)
            .build()
        
        context.getAvailableTableDicts().shouldNotBeEmpty()
    }
    
    @Test
    fun `test build context asynchronously`() {
        val systemDictCodes = setOf("user_status")
        
        // Mock async system dictionary data
        whenever(mockTransApi.translateDictBatchCode2name("user_status", "1"))
            .thenReturn(listOf(DictModel("1", "Active")))
        
        val future = contextBuilder
            .withSystemDictionaries(systemDictCodes)
            .buildAsync()
        
        val context = future.get()
        
        assertNotNull(context)
        context.getAvailableSystemDicts() shouldContain "user_status"
    }
    
    @Test
    fun `test context builder with cache statistics`() {
        val stats = contextBuilder.getCacheStatistics()
        
        assertNotNull(stats)
        assertTrue(stats.totalEntries >= 0)
        assertTrue(stats.hitRate >= 0.0)
    }
    
    @Test
    fun `test clear cache`() {
        // Build a context to populate cache
        contextBuilder
            .withSystemDictionaries(setOf("user_status"))
            .build()
        
        // Clear cache
        contextBuilder.clearCache()
        
        val stats = contextBuilder.getCacheStatistics()
        assertEquals(0, stats.totalEntries)
    }
    
    @Test
    fun `test context builder copy`() {
        val originalBuilder = contextBuilder
            .withSystemDictionaries(setOf("user_status"))
        
        val copiedBuilder = originalBuilder.copy()
        
        assertNotSame(originalBuilder, copiedBuilder)
        
        val originalContext = originalBuilder.build()
        val copiedContext = copiedBuilder.build()
        
        assertEquals(
            originalContext.getAvailableSystemDicts(),
            copiedContext.getAvailableSystemDicts()
        )
    }
    
    @Test
    fun `test system dictionary translation`() {
        val dictCode = "user_status"
        val codes = "1,2,3"
        
        whenever(mockTransApi.translateDictBatchCode2name(dictCode, codes))
            .thenReturn(listOf(
                DictModel("1", "Active"),
                DictModel("2", "Inactive"),
                DictModel("3", "Pending")
            ))
        
        val result = contextBuilder.translateSystemDict(dictCode, codes)
        
        assertEquals(3, result.size)
        assertEquals("Active", result.find { it.code == "1" }?.name)
        assertEquals("Inactive", result.find { it.code == "2" }?.name)
        assertEquals("Pending", result.find { it.code == "3" }?.name)
    }
    
    @Test
    fun `test table dictionary translation`() {
        val request = TableDictRequest("sys_dept", "code", "name", null)
        val codes = "001,002"
        
        whenever(mockTransApi.translateTableDictBatchCode2name(
            request.table, request.codeColumn, request.nameColumn, codes, request.condition
        )).thenReturn(listOf(
            DictModel("001", "IT Department"),
            DictModel("002", "HR Department")
        ))
        
        val result = contextBuilder.translateTableDict(request, codes)
        
        assertEquals(2, result.size)
        assertEquals("IT Department", result.find { it.code == "001" }?.name)
        assertEquals("HR Department", result.find { it.code == "002" }?.name)
    }
    
    @Test
    fun `test error handling in system dictionary translation`() {
        val dictCode = "invalid_dict"
        val codes = "1,2"
        
        whenever(mockTransApi.translateDictBatchCode2name(dictCode, codes))
            .thenThrow(RuntimeException("Dictionary not found"))
        
        val result = contextBuilder.translateSystemDict(dictCode, codes)
        
        // Should return empty list on error
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `test error handling in table dictionary translation`() {
        val request = TableDictRequest("invalid_table", "code", "name", null)
        val codes = "1,2"
        
        whenever(mockTransApi.translateTableDictBatchCode2name(
            request.table, request.codeColumn, request.nameColumn, codes, request.condition
        )).thenThrow(RuntimeException("Table not found"))
        
        val result = contextBuilder.translateTableDict(request, codes)
        
        // Should return empty list on error
        assertTrue(result.isEmpty())
    }
    
    @Test
    fun `test context builder statistics`() {
        val systemDictCodes = setOf("user_status", "user_type")
        val tableDictRequests = setOf(
            TableDictRequest("sys_dept", "code", "name", null)
        )
        
        val context = contextBuilder
            .withSystemDictionaries(systemDictCodes)
            .withTableDictionaries(tableDictRequests)
            .build()
        
        val stats = contextBuilder.getProcessingStatistics()
        
        assertNotNull(stats)
        assertTrue(stats.totalProcessingTime >= 0)
        assertTrue(stats.systemDictCount >= 0)
        assertTrue(stats.tableDictCount >= 0)
    }
}

/**
 * Mock classes for testing
 */
data class DictModel(
    val code: String,
    val name: String
)

/**
 * Mock TransApi interface for testing
 */
interface TransApi {
    fun translateDictBatchCode2name(dictCode: String, codes: String): List<DictModel>
    fun translateTableDictBatchCode2name(
        table: String,
        codeColumn: String,
        nameColumn: String,
        codes: String,
        condition: String?
    ): List<DictModel>
}