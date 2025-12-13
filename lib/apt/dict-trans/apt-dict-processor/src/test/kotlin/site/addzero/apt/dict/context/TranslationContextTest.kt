package site.addzero.apt.dict.context

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

/**
 * Unit tests for TranslationContext
 * 
 * Tests the translation context functionality for batch dictionary translation
 * and N+1 query problem elimination
 */
class TranslationContextTest {
    
    private lateinit var context: TranslationContext
    
    @BeforeEach
    fun setUp() {
        val systemDictData = mapOf(
            "user_status" to mapOf(
                "1" to "Active",
                "2" to "Inactive",
                "3" to "Pending"
            ),
            "user_type" to mapOf(
                "admin" to "Administrator",
                "user" to "Regular User",
                "guest" to "Guest User"
            )
        )
        
        val tableDictData = mapOf(
            "sys_dept:id:name" to mapOf(
                "100" to "Technology Department",
                "200" to "Sales Department",
                "300" to "HR Department"
            ),
            "sys_role:role_id:role_name" to mapOf(
                "1" to "System Admin",
                "2" to "Department Manager",
                "3" to "Regular Employee"
            )
        )
        
        context = TranslationContext(
            systemDictData = systemDictData,
            tableDictData = tableDictData
        )
    }
    
    @Test
    fun `test system dict translation success`() {
        val result = context.getSystemDictTranslation("user_status", "1")
        assertEquals("Active", result)
        
        val result2 = context.getSystemDictTranslation("user_type", "admin")
        assertEquals("Administrator", result2)
    }
    
    @Test
    fun `test system dict translation not found`() {
        val result = context.getSystemDictTranslation("user_status", "999")
        assertNull(result)
        
        val result2 = context.getSystemDictTranslation("nonexistent_dict", "1")
        assertNull(result2)
    }
    
    @Test
    fun `test system dict translation with null code`() {
        val result = context.getSystemDictTranslation("user_status", null)
        assertNull(result)
    }
    
    @Test
    fun `test table dict translation success`() {
        val result = context.getTableDictTranslation("sys_dept", "id", "name", "100")
        assertEquals("Technology Department", result)
        
        val result2 = context.getTableDictTranslation("sys_role", "role_id", "role_name", 2)
        assertEquals("Department Manager", result2)
    }
    
    @Test
    fun `test table dict translation not found`() {
        val result = context.getTableDictTranslation("sys_dept", "id", "name", "999")
        assertNull(result)
        
        val result2 = context.getTableDictTranslation("nonexistent_table", "id", "name", "100")
        assertNull(result2)
    }
    
    @Test
    fun `test table dict translation with null code`() {
        val result = context.getTableDictTranslation("sys_dept", "id", "name", null)
        assertNull(result)
    }
    
    @Test
    fun `test has system dict`() {
        assertTrue(context.hasSystemDict("user_status"))
        assertTrue(context.hasSystemDict("user_type"))
        assertFalse(context.hasSystemDict("nonexistent_dict"))
    }
    
    @Test
    fun `test has table dict`() {
        assertTrue(context.hasTableDict("sys_dept", "id", "name"))
        assertTrue(context.hasTableDict("sys_role", "role_id", "role_name"))
        assertFalse(context.hasTableDict("nonexistent_table", "id", "name"))
        assertFalse(context.hasTableDict("sys_dept", "wrong_column", "name"))
    }
    
    @Test
    fun `test get available system dicts`() {
        val availableDicts = context.getAvailableSystemDicts()
        assertEquals(2, availableDicts.size)
        assertTrue(availableDicts.contains("user_status"))
        assertTrue(availableDicts.contains("user_type"))
    }
    
    @Test
    fun `test get available table dicts`() {
        val availableTableDicts = context.getAvailableTableDicts()
        assertEquals(2, availableTableDicts.size)
        assertTrue(availableTableDicts.contains("sys_dept:id:name"))
        assertTrue(availableTableDicts.contains("sys_role:role_id:role_name"))
    }
    
    @Test
    fun `test with system dict`() {
        val newData = mapOf("new_key" to "New Value")
        val newContext = context.withSystemDict("new_dict", newData)
        
        // Original context should be unchanged
        assertFalse(context.hasSystemDict("new_dict"))
        
        // New context should have the additional data
        assertTrue(newContext.hasSystemDict("new_dict"))
        assertEquals("New Value", newContext.getSystemDictTranslation("new_dict", "new_key"))
        
        // New context should still have original data
        assertTrue(newContext.hasSystemDict("user_status"))
        assertEquals("Active", newContext.getSystemDictTranslation("user_status", "1"))
    }
    
    @Test
    fun `test with table dict`() {
        val newData = mapOf("1" to "New Department")
        val newContext = context.withTableDict("new_table", "id", "name", newData)
        
        // Original context should be unchanged
        assertFalse(context.hasTableDict("new_table", "id", "name"))
        
        // New context should have the additional data
        assertTrue(newContext.hasTableDict("new_table", "id", "name"))
        assertEquals("New Department", newContext.getTableDictTranslation("new_table", "id", "name", "1"))
        
        // New context should still have original data
        assertTrue(newContext.hasTableDict("sys_dept", "id", "name"))
        assertEquals("Technology Department", newContext.getTableDictTranslation("sys_dept", "id", "name", "100"))
    }
    
    @Test
    fun `test with spel context`() {
        val newContext = context.withSpelContext("currentUser", "admin")
        
        // This is a placeholder test since SPEL evaluation is not implemented yet
        // The context should be created successfully
        assertNotNull(newContext)
    }
    
    @Test
    fun `test processing statistics`() {
        val stats = context.processingStats
        
        // Initial state
        assertEquals(0, stats.getSystemDictLookups())
        assertEquals(0, stats.getTableDictLookups())
        assertEquals(0, stats.getSuccessfulTranslations())
        assertEquals(0, stats.getFailedTranslations())
        
        // Perform some translations
        context.getSystemDictTranslation("user_status", "1") // Success
        context.getSystemDictTranslation("user_status", "999") // Failure
        context.getTableDictTranslation("sys_dept", "id", "name", "100") // Success
        context.getTableDictTranslation("sys_dept", "id", "name", "999") // Failure
        
        // Check statistics
        assertEquals(2, stats.getSystemDictLookups())
        assertEquals(2, stats.getTableDictLookups())
        assertEquals(2, stats.getSuccessfulTranslations())
        assertEquals(2, stats.getFailedTranslations())
        assertEquals(4, stats.getTotalLookups())
        assertEquals(4, stats.getTotalTranslations())
        assertEquals(0.5, stats.getSuccessRate(), 0.001)
    }
    
    @Test
    fun `test processing statistics success rate calculation`() {
        val stats = ProcessingStatistics()
        
        // No translations yet
        assertEquals(0.0, stats.getSuccessRate(), 0.001)
        
        // Add some successful translations
        stats.incrementSuccessfulTranslations()
        stats.incrementSuccessfulTranslations()
        assertEquals(1.0, stats.getSuccessRate(), 0.001)
        
        // Add some failed translations
        stats.incrementFailedTranslations()
        assertEquals(0.667, stats.getSuccessRate(), 0.001)
    }
    
    @Test
    fun `test processing statistics cache hit rate calculation`() {
        val stats = ProcessingStatistics()
        
        // No cache operations yet
        assertEquals(0.0, stats.getCacheHitRate(), 0.001)
        
        // Add some cache hits
        stats.incrementCacheHits()
        stats.incrementCacheHits()
        assertEquals(1.0, stats.getCacheHitRate(), 0.001)
        
        // Add some cache misses
        stats.incrementCacheMisses()
        assertEquals(0.667, stats.getCacheHitRate(), 0.001)
    }
    
    @Test
    fun `test processing statistics elapsed time`() {
        val stats = ProcessingStatistics()
        
        // Elapsed time should be positive
        Thread.sleep(10) // Small delay to ensure elapsed time > 0
        assertTrue(stats.getElapsedTime() > 0)
    }
    
    @Test
    fun `test processing statistics toString`() {
        val stats = ProcessingStatistics()
        stats.incrementSystemDictLookups()
        stats.incrementSuccessfulTranslations()
        stats.incrementCacheHits()
        
        val statsString = stats.toString()
        assertTrue(statsString.contains("systemDictLookups=1"))
        assertTrue(statsString.contains("successfulTranslations=1"))
        assertTrue(statsString.contains("successRate=100.00%"))
        assertTrue(statsString.contains("cacheHitRate=100.00%"))
        assertTrue(statsString.contains("elapsedTime="))
    }
    
    @Test
    fun `test empty translation context`() {
        val emptyContext = TranslationContext()
        
        assertNull(emptyContext.getSystemDictTranslation("any_dict", "any_code"))
        assertNull(emptyContext.getTableDictTranslation("any_table", "any_column", "any_column", "any_code"))
        assertFalse(emptyContext.hasSystemDict("any_dict"))
        assertFalse(emptyContext.hasTableDict("any_table", "any_column", "any_column"))
        assertTrue(emptyContext.getAvailableSystemDicts().isEmpty())
        assertTrue(emptyContext.getAvailableTableDicts().isEmpty())
    }
}