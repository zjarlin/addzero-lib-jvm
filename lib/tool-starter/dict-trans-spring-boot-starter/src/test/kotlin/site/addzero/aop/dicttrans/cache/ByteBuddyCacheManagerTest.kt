package site.addzero.aop.dicttrans.cache

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import site.addzero.aop.dicttrans.config.CacheProperties
import site.addzero.aop.dicttrans.config.MemoryManagementProperties
import site.addzero.aop.dicttrans.config.MonitoringProperties
import site.addzero.aop.dicttrans.config.ProcessingProperties
import site.addzero.aop.dicttrans.dictaop.entity.NeedAddInfo
import java.time.Duration

/**
 * Test for ByteBuddy cache manager
 */
class ByteBuddyCacheManagerTest {
    
    private lateinit var cacheManager: ByteBuddyCacheManager
    private lateinit var properties: MemoryManagementProperties
    
    @BeforeEach
    fun setUp() {
        properties = MemoryManagementProperties(
            byteBuddyCache = CacheProperties(
                maxSize = 10,
                expireAfterAccess = Duration.ofMinutes(5),
                expireAfterWrite = Duration.ofMinutes(10),
                enableMetrics = true
            ),
            reflectionCache = CacheProperties(),
            processing = ProcessingProperties(),
            monitoring = MonitoringProperties()
        )
        cacheManager = ByteBuddyCacheManagerImpl(properties)
    }
    
    @Test
    fun `should cache and reuse generated classes`() {
        // Given
        val baseClass = TestClass::class.java
        val fields = listOf(
            NeedAddInfo(
                rootObject = TestClass(),
                fieldName = "testField",
                recur = false,
                isT = false,
                isColl = false,
                type = String::class.java
            )
        )
        
        // When - first call
        val class1 = cacheManager.getOrCreateClass(baseClass, fields)
        val stats1 = cacheManager.getStatistics()
        
        // When - second call with same parameters
        val class2 = cacheManager.getOrCreateClass(baseClass, fields)
        val stats2 = cacheManager.getStatistics()
        
        // Then
        assertSame(class1, class2, "Should return the same cached class")
        assertEquals(1, stats2.hitCount, "Should have one cache hit")
        assertTrue(stats2.hitRate > 0, "Hit rate should be greater than 0")
    }
    
    @Test
    fun `should handle cache eviction`() {
        // Given - fill cache beyond capacity
        val baseClass = TestClass::class.java
        
        // When - add more items than cache capacity
        for (i in 1..15) {
            val fields = listOf(
                NeedAddInfo(
                    rootObject = TestClass(),
                    fieldName = "field$i",
                    recur = false,
                    isT = false,
                    isColl = false,
                    type = String::class.java
                )
            )
            cacheManager.getOrCreateClass(baseClass, fields)
        }
        
        val stats = cacheManager.getStatistics()
        
        // Then
        assertTrue(stats.size <= properties.byteBuddyCache.maxSize, 
            "Cache size should not exceed maximum")
        assertTrue(stats.evictionCount > 0, "Should have evictions")
    }
    
    @Test
    fun `should clear all cache entries`() {
        // Given
        val baseClass = TestClass::class.java
        val fields = listOf(
            NeedAddInfo(
                rootObject = TestClass(),
                fieldName = "testField",
                recur = false,
                isT = false,
                isColl = false,
                type = String::class.java
            )
        )
        
        cacheManager.getOrCreateClass(baseClass, fields)
        assertTrue(cacheManager.size() > 0, "Cache should have entries")
        
        // When
        cacheManager.evictAll()
        
        // Then
        assertEquals(0, cacheManager.size(), "Cache should be empty after evictAll")
    }
    
    @Test
    fun `should handle failed class generation gracefully`() {
        // Given - a class that cannot be subclassed (final class)
        val baseClass = String::class.java // String is final
        val fields = listOf(
            NeedAddInfo(
                rootObject = TestClass(),
                fieldName = "testField",
                recur = false,
                isT = false,
                isColl = false,
                type = String::class.java
            )
        )
        
        // When
        val result = cacheManager.getOrCreateClass(baseClass, fields)
        
        // Then - should return original class on failure
        assertSame(baseClass, result, "Should return original class on generation failure")
    }
    
    // Test class for ByteBuddy generation
    open class TestClass {
        var existingField: String = "test"
    }
}