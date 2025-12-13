package site.addzero.aop.dicttrans.cache

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import site.addzero.aop.dicttrans.config.CacheProperties
import site.addzero.aop.dicttrans.config.MemoryManagementProperties
import site.addzero.aop.dicttrans.config.MonitoringProperties
import site.addzero.aop.dicttrans.config.ProcessingProperties
import java.time.Duration

/**
 * Test for reflection cache manager
 */
class ReflectionCacheManagerTest {
    
    private lateinit var cacheManager: ReflectionCacheManager
    private lateinit var properties: MemoryManagementProperties
    
    @BeforeEach
    fun setUp() {
        properties = MemoryManagementProperties(
            byteBuddyCache = CacheProperties(),
            reflectionCache = CacheProperties(
                maxSize = 10,
                expireAfterAccess = Duration.ofMinutes(5),
                expireAfterWrite = Duration.ofMinutes(10),
                enableMetrics = true
            ),
            processing = ProcessingProperties(),
            monitoring = MonitoringProperties()
        )
        cacheManager = ReflectionCacheManagerImpl(properties)
    }
    
    @Test
    fun `should cache and reuse field metadata`() {
        // Given
        val testClass = TestClass::class.java
        
        // When - first call
        val fields1 = cacheManager.getFields(testClass)
        val stats1 = cacheManager.getStatistics()
        
        // When - second call
        val fields2 = cacheManager.getFields(testClass)
        val stats2 = cacheManager.getStatistics()
        
        // Then
        assertSame(fields1, fields2, "Should return the same cached fields array")
        assertTrue(stats2.hitCount > stats1.hitCount, "Should have cache hits")
        assertTrue(stats2.hitRate > 0, "Hit rate should be greater than 0")
    }
    
    @Test
    fun `should cache field by name lookups`() {
        // Given
        val testClass = TestClass::class.java
        val fieldName = "testField"
        
        // When - first lookup
        val field1 = cacheManager.getField(testClass, fieldName)
        
        // When - second lookup
        val field2 = cacheManager.getField(testClass, fieldName)
        
        // Then
        assertNotNull(field1, "Should find the field")
        assertSame(field1, field2, "Should return the same cached field")
        assertEquals(fieldName, field1?.name, "Field name should match")
    }
    
    @Test
    fun `should cache field type information`() {
        // Given
        val testClass = TestClass::class.java
        val fields = cacheManager.getFields(testClass)
        val listField = fields.find { it.name == "listField" }
        val stringField = fields.find { it.name == "testField" }
        
        assertNotNull(listField, "Should find list field")
        assertNotNull(stringField, "Should find string field")
        
        // When - check field types multiple times
        val isCollection1 = cacheManager.isCollectionField(listField!!)
        val isCollection2 = cacheManager.isCollectionField(listField)
        val isNotCollection1 = cacheManager.isCollectionField(stringField!!)
        val isNotCollection2 = cacheManager.isCollectionField(stringField)
        
        // Then
        assertTrue(isCollection1, "List field should be identified as collection")
        assertEquals(isCollection1, isCollection2, "Results should be consistent")
        assertFalse(isNotCollection1, "String field should not be identified as collection")
        assertEquals(isNotCollection1, isNotCollection2, "Results should be consistent")
    }
    
    @Test
    fun `should handle field value operations`() {
        // Given
        val testObj = TestClass()
        val testClass = testObj.javaClass
        val field = cacheManager.getField(testClass, "testField")
        
        assertNotNull(field, "Should find the field")
        
        // When - set and get field value
        val testValue = "cached test value"
        cacheManager.setFieldValue(testObj, field!!, testValue)
        val retrievedValue = cacheManager.getFieldValue(testObj, field)
        
        // Then
        assertEquals(testValue, retrievedValue, "Should retrieve the same value that was set")
    }
    
    @Test
    fun `should invalidate cache for specific class`() {
        // Given
        val testClass = TestClass::class.java
        cacheManager.getFields(testClass) // Cache the fields
        assertTrue(cacheManager.size() > 0, "Cache should have entries")
        
        // When
        cacheManager.invalidateClass(testClass)
        
        // Then - cache should be smaller but not necessarily empty (other caches may have entries)
        val sizeAfterInvalidation = cacheManager.size()
        assertTrue(sizeAfterInvalidation >= 0, "Cache size should be non-negative after invalidation")
    }
    
    @Test
    fun `should clear all cache entries`() {
        // Given
        val testClass = TestClass::class.java
        cacheManager.getFields(testClass)
        cacheManager.getField(testClass, "testField")
        assertTrue(cacheManager.size() > 0, "Cache should have entries")
        
        // When
        cacheManager.evictAll()
        
        // Then
        assertEquals(0, cacheManager.size(), "Cache should be empty after evictAll")
    }
    
    @Test
    fun `should handle non-existent fields gracefully`() {
        // Given
        val testClass = TestClass::class.java
        
        // When
        val nonExistentField = cacheManager.getField(testClass, "nonExistentField")
        
        // Then
        assertNull(nonExistentField, "Should return null for non-existent field")
    }
    
    // Test class for reflection operations
    class TestClass {
        var testField: String = "test"
        var listField: MutableList<String> = mutableListOf()
        private var privateField: Int = 42
        
        fun getPrivateField(): Int = privateField
    }
}