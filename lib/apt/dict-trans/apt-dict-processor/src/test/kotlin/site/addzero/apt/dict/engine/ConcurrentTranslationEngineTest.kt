package site.addzero.apt.dict.engine

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import site.addzero.apt.dict.context.TranslationContext
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Unit tests for ConcurrentTranslationEngine
 * 
 * Tests concurrent translation functionality, error isolation,
 * and performance characteristics
 */
class ConcurrentTranslationEngineTest {
    
    private lateinit var engine: ConcurrentTranslationEngine
    private lateinit var context: TranslationContext
    
    @BeforeEach
    fun setUp() {
        engine = ConcurrentTranslationEngine(maxConcurrency = 4, timeoutMillis = 5000)
        
        // Setup translation context with test data
        val systemDictData = mapOf(
            "user_status" to mapOf(
                "1" to "Active",
                "2" to "Inactive"
            ),
            "user_type" to mapOf(
                "admin" to "Administrator",
                "user" to "Regular User"
            )
        )
        
        val tableDictData = mapOf(
            "sys_dept:id:name" to mapOf(
                "100" to "Technology Department",
                "200" to "Sales Department"
            )
        )
        
        context = TranslationContext(
            systemDictData = systemDictData,
            tableDictData = tableDictData
        )
    }
    
    @AfterEach
    fun tearDown() {
        engine.shutdown()
    }
    
    @Test
    fun `test translate single entity with system dict`() {
        val user = TestUser(id = 1, status = "1", statusText = null)
        val metadata = EntityMetadata(
            entityClass = TestUser::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("status", "statusText", "user_status")
            )
        )
        
        val future = engine.translateEntity(user, metadata, context)
        val result = future.get(1, TimeUnit.SECONDS)
        
        assertEquals("Active", result.statusText)
        assertEquals("1", result.status) // Original value unchanged
    }
    
    @Test
    fun `test translate single entity with table dict`() {
        val user = TestUser(id = 1, deptId = "100", deptName = null)
        val metadata = EntityMetadata(
            entityClass = TestUser::class.java,
            tableDictFields = listOf(
                TableDictFieldInfo("deptId", "deptName", "sys_dept", "id", "name")
            )
        )
        
        val future = engine.translateEntity(user, metadata, context)
        val result = future.get(1, TimeUnit.SECONDS)
        
        assertEquals("Technology Department", result.deptName)
        assertEquals("100", result.deptId) // Original value unchanged
    }
    
    @Test
    fun `test translate single entity with mixed fields`() {
        val user = TestUser(
            id = 1, 
            status = "1", 
            statusText = null,
            deptId = "200", 
            deptName = null
        )
        val metadata = EntityMetadata(
            entityClass = TestUser::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("status", "statusText", "user_status")
            ),
            tableDictFields = listOf(
                TableDictFieldInfo("deptId", "deptName", "sys_dept", "id", "name")
            )
        )
        
        val future = engine.translateEntity(user, metadata, context)
        val result = future.get(1, TimeUnit.SECONDS)
        
        assertEquals("Active", result.statusText)
        assertEquals("Sales Department", result.deptName)
    }
    
    @Test
    fun `test translate multiple entities concurrently`() {
        val users = listOf(
            TestUser(id = 1, status = "1", statusText = null),
            TestUser(id = 2, status = "2", statusText = null),
            TestUser(id = 3, status = "1", statusText = null)
        )
        
        val metadata = EntityMetadata(
            entityClass = TestUser::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("status", "statusText", "user_status")
            )
        )
        
        val future = engine.translateEntities(users, metadata, context)
        val results = future.get(2, TimeUnit.SECONDS)
        
        assertEquals(3, results.size)
        assertEquals("Active", results[0].statusText)
        assertEquals("Inactive", results[1].statusText)
        assertEquals("Active", results[2].statusText)
    }
    
    @Test
    fun `test translate empty collection`() {
        val users = emptyList<TestUser>()
        val metadata = EntityMetadata(entityClass = TestUser::class.java)
        
        val future = engine.translateEntities(users, metadata, context)
        val results = future.get(1, TimeUnit.SECONDS)
        
        assertTrue(results.isEmpty())
    }
    
    @Test
    fun `test concurrent execution performance`() {
        val userCount = 100
        val users = (1..userCount).map { i ->
            TestUser(id = i, status = if (i % 2 == 0) "1" else "2", statusText = null)
        }
        
        val metadata = EntityMetadata(
            entityClass = TestUser::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("status", "statusText", "user_status")
            )
        )
        
        val startTime = System.currentTimeMillis()
        val future = engine.translateEntities(users, metadata, context)
        val results = future.get(5, TimeUnit.SECONDS)
        val endTime = System.currentTimeMillis()
        
        assertEquals(userCount, results.size)
        assertTrue(endTime - startTime < 2000, "Should complete quickly with concurrent processing")
        
        // Verify all translations are correct
        results.forEachIndexed { index, user ->
            val expectedStatus = if ((index + 1) % 2 == 0) "Active" else "Inactive"
            assertEquals(expectedStatus, user.statusText)
        }
    }
    
    @Test
    fun `test error isolation`() {
        // Create a user with invalid status that won't be found in dictionary
        val users = listOf(
            TestUser(id = 1, status = "1", statusText = null), // Valid
            TestUser(id = 2, status = "999", statusText = null), // Invalid - should not fail entire batch
            TestUser(id = 3, status = "2", statusText = null)  // Valid
        )
        
        val metadata = EntityMetadata(
            entityClass = TestUser::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("status", "statusText", "user_status")
            )
        )
        
        val future = engine.translateEntities(users, metadata, context)
        val results = future.get(2, TimeUnit.SECONDS)
        
        assertEquals(3, results.size)
        assertEquals("Active", results[0].statusText)
        assertNull(results[1].statusText) // Translation failed but didn't break the batch
        assertEquals("Inactive", results[2].statusText)
    }
    
    @Test
    fun `test nested entity translation`() {
        val role = TestRole(id = 1, name = "Admin Role")
        val user = TestUserWithRole(
            id = 1, 
            status = "1", 
            statusText = null,
            role = role
        )
        
        val roleMetadata = EntityMetadata(
            entityClass = TestRole::class.java,
            // Role doesn't have translation fields in this test
        )
        
        val userMetadata = EntityMetadata(
            entityClass = TestUserWithRole::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("status", "statusText", "user_status")
            ),
            nestedFields = listOf(
                NestedFieldInfo("role", roleMetadata)
            )
        )
        
        val future = engine.translateNestedEntities(listOf(user), userMetadata, context)
        val results = future.get(2, TimeUnit.SECONDS)
        
        assertEquals(1, results.size)
        assertEquals("Active", results[0].statusText)
        assertNotNull(results[0].role)
    }
    
    @Test
    fun `test engine statistics`() {
        val user = TestUser(id = 1, status = "1", statusText = null)
        val metadata = EntityMetadata(
            entityClass = TestUser::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("status", "statusText", "user_status")
            )
        )
        
        // Initial statistics
        val initialStats = engine.getEngineStatistics()
        assertEquals(0, initialStats.totalTranslations)
        assertEquals(0, initialStats.failedTranslations)
        
        // Perform translation
        val future = engine.translateEntity(user, metadata, context)
        future.get(1, TimeUnit.SECONDS)
        
        // Check updated statistics
        val finalStats = engine.getEngineStatistics()
        assertEquals(1, finalStats.totalTranslations)
        assertEquals(1, finalStats.successfulTranslations)
        assertEquals(0, finalStats.failedTranslations)
        assertEquals(1.0, finalStats.getSuccessRate(), 0.001)
    }
    
    @Test
    fun `test engine health check`() {
        assertTrue(engine.isHealthy())
        
        engine.shutdown()
        
        // Give some time for shutdown to complete
        Thread.sleep(100)
        assertFalse(engine.isHealthy())
    }
    
    @Test
    fun `test timeout handling`() {
        // Create engine with very short timeout
        val shortTimeoutEngine = ConcurrentTranslationEngine(timeoutMillis = 1)
        
        try {
            val user = TestUser(id = 1, status = "1", statusText = null)
            val metadata = EntityMetadata(
                entityClass = TestUser::class.java,
                systemDictFields = listOf(
                    SystemDictFieldInfo("status", "statusText", "user_status")
                )
            )
            
            val future = shortTimeoutEngine.translateEntity(user, metadata, context)
            
            // This should timeout
            assertThrows(Exception::class.java) {
                future.get()
            }
        } finally {
            shortTimeoutEngine.shutdown()
        }
    }
    
    @Test
    fun `test system dict translator directly`() {
        val translator = SystemDictTranslator()
        val user = TestUser(id = 1, status = "1", statusText = null)
        val field = SystemDictFieldInfo("status", "statusText", "user_status")
        
        translator.translateField(user, field, context)
        
        assertEquals("Active", user.statusText)
    }
    
    @Test
    fun `test table dict translator directly`() {
        val translator = TableDictTranslator()
        val user = TestUser(id = 1, deptId = "100", deptName = null)
        val field = TableDictFieldInfo("deptId", "deptName", "sys_dept", "id", "name")
        
        translator.translateField(user, field, context)
        
        assertEquals("Technology Department", user.deptName)
    }
    
    @Test
    fun `test spel translator directly`() {
        val translator = SpelTranslator()
        val user = TestUser(id = 1, statusText = null)
        val field = SpelFieldInfo("statusText", "#{entity.id + '_computed'}")
        
        // SPEL translation is not fully implemented yet, so this should not crash
        assertDoesNotThrow {
            translator.translateField(user, field, context)
        }
    }
    
    @Test
    fun `test concurrent access safety`() {
        val userCount = 50
        val threadCount = 10
        val latch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)
        val errorCount = AtomicInteger(0)
        
        val users = (1..userCount).map { i ->
            TestUser(id = i, status = "1", statusText = null)
        }
        
        val metadata = EntityMetadata(
            entityClass = TestUser::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("status", "statusText", "user_status")
            )
        )
        
        // Launch multiple threads doing translations concurrently
        repeat(threadCount) {
            Thread {
                try {
                    val future = engine.translateEntities(users, metadata, context)
                    val results = future.get(5, TimeUnit.SECONDS)
                    
                    if (results.size == userCount && results.all { it.statusText == "Active" }) {
                        successCount.incrementAndGet()
                    }
                } catch (e: Exception) {
                    errorCount.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }.start()
        }
        
        // Wait for all threads to complete
        assertTrue(latch.await(10, TimeUnit.SECONDS))
        
        // All threads should succeed
        assertEquals(threadCount, successCount.get())
        assertEquals(0, errorCount.get())
    }
    
    @Test
    fun `test data classes`() {
        // Test EntityMetadata
        val metadata = EntityMetadata(
            entityClass = TestUser::class.java,
            systemDictFields = listOf(SystemDictFieldInfo("status", "statusText", "user_status"))
        )
        assertEquals(TestUser::class.java, metadata.entityClass)
        assertEquals(1, metadata.systemDictFields.size)
        
        // Test SystemDictFieldInfo
        val systemField = SystemDictFieldInfo("status", "statusText", "user_status")
        assertEquals("status", systemField.sourceField)
        assertEquals("statusText", systemField.targetField)
        assertEquals("user_status", systemField.dictCode)
        
        // Test TableDictFieldInfo
        val tableField = TableDictFieldInfo("deptId", "deptName", "sys_dept", "id", "name")
        assertEquals("deptId", tableField.sourceField)
        assertEquals("deptName", tableField.targetField)
        assertEquals("sys_dept", tableField.table)
        assertEquals("id", tableField.codeColumn)
        assertEquals("name", tableField.nameColumn)
        assertEquals("", tableField.condition)
        
        // Test SpelFieldInfo
        val spelField = SpelFieldInfo("computed", "#{entity.id}")
        assertEquals("computed", spelField.targetField)
        assertEquals("#{entity.id}", spelField.expression)
        
        // Test NestedFieldInfo
        val nestedField = NestedFieldInfo("role", metadata)
        assertEquals("role", nestedField.fieldName)
        assertEquals(metadata, nestedField.nestedMetadata)
    }
    
    @Test
    fun `test engine statistics calculations`() {
        val stats = EngineStatistics(
            activeTranslations = 2,
            totalTranslations = 10,
            failedTranslations = 3,
            successfulTranslations = 7,
            maxConcurrency = 4,
            timeoutMillis = 5000
        )
        
        assertEquals(0.7, stats.getSuccessRate(), 0.001)
        assertEquals(0.3, stats.getFailureRate(), 0.001)
        
        val statsString = stats.toString()
        assertTrue(statsString.contains("activeTranslations=2"))
        assertTrue(statsString.contains("totalTranslations=10"))
        assertTrue(statsString.contains("successRate=70.00%"))
        assertTrue(statsString.contains("maxConcurrency=4"))
    }
    
    @Test
    fun `test translation exception`() {
        val cause = RuntimeException("Database error")
        val exception = TranslationException("Translation failed", cause)
        
        assertEquals("Translation failed", exception.message)
        assertEquals(cause, exception.cause)
        
        val exceptionWithoutCause = TranslationException("Simple error")
        assertEquals("Simple error", exceptionWithoutCause.message)
        assertNull(exceptionWithoutCause.cause)
    }
}

/**
 * Test entity classes
 */
data class TestUser(
    val id: Int,
    val status: String? = null,
    var statusText: String? = null,
    val deptId: String? = null,
    var deptName: String? = null
)

data class TestRole(
    val id: Int,
    val name: String
)

data class TestUserWithRole(
    val id: Int,
    val status: String? = null,
    var statusText: String? = null,
    val role: TestRole? = null
)