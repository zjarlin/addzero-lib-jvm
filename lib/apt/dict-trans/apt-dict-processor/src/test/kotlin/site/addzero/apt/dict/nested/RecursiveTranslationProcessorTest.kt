package site.addzero.apt.dict.nested

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import site.addzero.apt.dict.context.TranslationContext
import site.addzero.apt.dict.engine.*

/**
 * Unit tests for RecursiveTranslationProcessor
 * 
 * Tests recursive translation functionality, circular reference detection,
 * and nested structure processing
 */
class RecursiveTranslationProcessorTest {
    
    private lateinit var processor: RecursiveTranslationProcessor
    private lateinit var context: TranslationContext
    
    @BeforeEach
    fun setUp() {
        processor = RecursiveTranslationProcessor(maxDepth = 5, circularReferenceDetection = true)
        
        // Setup translation context with test data
        val systemDictData = mapOf(
            "user_status" to mapOf(
                "1" to "Active",
                "2" to "Inactive"
            ),
            "role_type" to mapOf(
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
    
    @Test
    fun `test simple nested translation`() {
        val role = TestRole(id = 1, type = "admin", typeName = null)
        val user = TestUserWithRole(
            id = 1,
            status = "1",
            statusText = null,
            role = role
        )
        
        val roleMetadata = EntityMetadata(
            entityClass = TestRole::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("type", "typeName", "role_type")
            )
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
        
        val result = processor.processNestedTranslation(user, userMetadata, context)
        
        assertEquals("Active", result.statusText)
        assertEquals("Administrator", result.role?.typeName)
    }
    
    @Test
    fun `test nested collection translation`() {
        val roles = listOf(
            TestRole(id = 1, type = "admin", typeName = null),
            TestRole(id = 2, type = "user", typeName = null)
        )
        val user = TestUserWithRoles(
            id = 1,
            status = "1",
            statusText = null,
            roles = roles
        )
        
        val roleMetadata = EntityMetadata(
            entityClass = TestRole::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("type", "typeName", "role_type")
            )
        )
        
        val userMetadata = EntityMetadata(
            entityClass = TestUserWithRoles::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("status", "statusText", "user_status")
            ),
            nestedFields = listOf(
                NestedFieldInfo("roles", roleMetadata)
            )
        )
        
        val result = processor.processNestedTranslation(user, userMetadata, context)
        
        assertEquals("Active", result.statusText)
        assertEquals("Administrator", result.roles[0].typeName)
        assertEquals("Regular User", result.roles[1].typeName)
    }
    
    @Test
    fun `test deep nested translation`() {
        val department = TestDepartment(id = 100, deptId = "100", deptName = null)
        val role = TestRoleWithDept(
            id = 1, 
            type = "admin", 
            typeName = null,
            department = department
        )
        val user = TestUserWithRole(
            id = 1,
            status = "1",
            statusText = null,
            role = role
        )
        
        val deptMetadata = EntityMetadata(
            entityClass = TestDepartment::class.java,
            tableDictFields = listOf(
                TableDictFieldInfo("deptId", "deptName", "sys_dept", "id", "name")
            )
        )
        
        val roleMetadata = EntityMetadata(
            entityClass = TestRoleWithDept::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("type", "typeName", "role_type")
            ),
            nestedFields = listOf(
                NestedFieldInfo("department", deptMetadata)
            )
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
        
        val result = processor.processNestedTranslation(user, userMetadata, context)
        
        assertEquals("Active", result.statusText)
        assertEquals("Administrator", (result.role as TestRoleWithDept).typeName)
        assertEquals("Technology Department", (result.role as TestRoleWithDept).department?.deptName)
    }
    
    @Test
    fun `test circular reference detection`() {
        val user = TestUserCircular(id = 1, status = "1", statusText = null)
        val role = TestRoleCircular(id = 1, type = "admin", typeName = null, user = user)
        user.role = role // Create circular reference
        
        val roleMetadata = EntityMetadata(
            entityClass = TestRoleCircular::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("type", "typeName", "role_type")
            )
        )
        
        val userMetadata = EntityMetadata(
            entityClass = TestUserCircular::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("status", "statusText", "user_status")
            ),
            nestedFields = listOf(
                NestedFieldInfo("role", roleMetadata)
            )
        )
        
        // Should not throw StackOverflowError due to circular reference detection
        assertDoesNotThrow {
            val result = processor.processNestedTranslation(user, userMetadata, context)
            assertEquals("Active", result.statusText)
        }
    }
    
    @Test
    fun `test max depth limit`() {
        val processor = RecursiveTranslationProcessor(maxDepth = 2)
        
        // Create a deep nested structure
        val level3 = TestNestedLevel(id = 3, status = "1", statusText = null, child = null)
        val level2 = TestNestedLevel(id = 2, status = "1", statusText = null, child = level3)
        val level1 = TestNestedLevel(id = 1, status = "1", statusText = null, child = level2)
        
        val metadata = EntityMetadata(
            entityClass = TestNestedLevel::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("status", "statusText", "user_status")
            ),
            nestedFields = listOf(
                NestedFieldInfo("child", EntityMetadata(TestNestedLevel::class.java))
            )
        )
        
        // Should stop at max depth and not process level 3
        assertDoesNotThrow {
            processor.processNestedTranslation(level1, metadata, context)
        }
    }
    
    @Test
    fun `test collection processing`() {
        val users = listOf(
            TestUser(id = 1, status = "1", statusText = null),
            TestUser(id = 2, status = "2", statusText = null)
        )
        
        val metadata = EntityMetadata(
            entityClass = TestUser::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("status", "statusText", "user_status")
            )
        )
        
        val results = processor.processNestedCollection(users, metadata, context)
        
        assertEquals(2, results.size)
        assertEquals("Active", results[0].statusText)
        assertEquals("Inactive", results[1].statusText)
    }
    
    @Test
    fun `test null nested field handling`() {
        val user = TestUserWithRole(
            id = 1,
            status = "1",
            statusText = null,
            role = null // Null nested field
        )
        
        val roleMetadata = EntityMetadata(
            entityClass = TestRole::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("type", "typeName", "role_type")
            )
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
        
        // Should not throw exception for null nested field
        assertDoesNotThrow {
            val result = processor.processNestedTranslation(user, userMetadata, context)
            assertEquals("Active", result.statusText)
            assertNull(result.role)
        }
    }
    
    @Test
    fun `test processing statistics`() {
        val user = TestUserWithRole(
            id = 1,
            status = "1",
            statusText = null,
            role = TestRole(id = 1, type = "admin", typeName = null)
        )
        
        val roleMetadata = EntityMetadata(
            entityClass = TestRole::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("type", "typeName", "role_type")
            )
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
        
        processor.processNestedTranslation(user, userMetadata, context)
        
        val stats = processor.getProcessingStatistics()
        assertTrue(stats.processedObjectCount >= 0)
        assertTrue(stats.maxDepthReached >= 0)
        assertEquals(5, stats.maxDepthLimit)
        assertTrue(stats.circularReferenceDetectionEnabled)
    }
    
    @Test
    fun `test processor reset`() {
        val user = TestUser(id = 1, status = "1", statusText = null)
        val metadata = EntityMetadata(
            entityClass = TestUser::class.java,
            systemDictFields = listOf(
                SystemDictFieldInfo("status", "statusText", "user_status")
            )
        )
        
        processor.processNestedTranslation(user, metadata, context)
        
        // Reset should clear internal state
        processor.reset()
        
        val stats = processor.getProcessingStatistics()
        assertEquals(0, stats.processedObjectCount)
    }
    
    @Test
    fun `test circular reference detector`() {
        val detector = CircularReferenceDetector()
        
        val obj1 = TestUser(id = 1, status = "1", statusText = null)
        val obj2 = TestUser(id = 2, status = "2", statusText = null)
        val obj3 = TestUser(id = 3, status = "1", statusText = null)
        
        // Add references: obj1 -> obj2 -> obj3
        detector.addReference(obj1, obj2)
        detector.addReference(obj2, obj3)
        
        // Adding obj3 -> obj1 would create a circle
        assertTrue(detector.wouldCreateCircle(obj3, obj1))
        
        // Adding obj3 -> obj2 would also create a circle
        assertTrue(detector.wouldCreateCircle(obj3, obj2))
        
        // Adding obj1 -> obj3 should be fine (no circle)
        assertFalse(detector.wouldCreateCircle(obj1, obj3))
        
        assertEquals(2, detector.getGraphSize())
        
        detector.clear()
        assertEquals(0, detector.getGraphSize())
    }
    
    @Test
    fun `test dynamic lookup generator`() {
        val generator = DynamicLookupGenerator()
        
        val trueRule = TranslationRule(
            type = TranslationRuleType.SYSTEM_DICT,
            sourceField = "entity.status",
            dictCode = "user_status"
        )
        
        val falseRule = TranslationRule(
            type = TranslationRuleType.LITERAL,
            sourceField = "",
            literalValue = "Unknown"
        )
        
        val lookup = generator.generateConditionalLookup(
            condition = "entity.status == '1'",
            trueTranslation = trueRule,
            falseTranslation = falseRule
        )
        
        val user1 = TestUser(id = 1, status = "1", statusText = null)
        val user2 = TestUser(id = 2, status = "2", statusText = null)
        
        val result1 = lookup(user1, context)
        val result2 = lookup(user2, context)
        
        assertEquals("Active", result1)
        assertEquals("Unknown", result2)
    }
    
    @Test
    fun `test multi condition lookup`() {
        val generator = DynamicLookupGenerator()
        
        val conditions = listOf(
            ConditionalTranslation(
                condition = "entity.status == '1'",
                translation = TranslationRule(
                    type = TranslationRuleType.LITERAL,
                    sourceField = "",
                    literalValue = "Active User"
                )
            ),
            ConditionalTranslation(
                condition = "entity.status == '2'",
                translation = TranslationRule(
                    type = TranslationRuleType.LITERAL,
                    sourceField = "",
                    literalValue = "Inactive User"
                )
            )
        )
        
        val lookup = generator.generateMultiConditionLookup(conditions)
        
        val user1 = TestUser(id = 1, status = "1", statusText = null)
        val user2 = TestUser(id = 2, status = "2", statusText = null)
        val user3 = TestUser(id = 3, status = "3", statusText = null)
        
        assertEquals("Active User", lookup(user1, context))
        assertEquals("Inactive User", lookup(user2, context))
        assertNull(lookup(user3, context)) // No matching condition
    }
    
    @Test
    fun `test collection iteration processor`() {
        val processor = CollectionIterationProcessor()
        
        val list = listOf("a", "b", "c")
        val array = arrayOf("x", "y", "z")
        val map = mapOf("key1" to "value1", "key2" to "value2")
        
        assertTrue(processor.isCollection(list))
        assertTrue(processor.isCollection(array))
        assertTrue(processor.isCollection(map))
        assertFalse(processor.isCollection("string"))
        
        assertEquals(3, processor.getCollectionSize(list))
        assertEquals(3, processor.getCollectionSize(array))
        assertEquals(2, processor.getCollectionSize(map))
        assertEquals(0, processor.getCollectionSize("string"))
        
        val processedItems = mutableListOf<Any>()
        processor.processCollection(list) { processedItems.add(it) }
        assertEquals(3, processedItems.size)
    }
    
    @Test
    fun `test translation rule data classes`() {
        val systemRule = TranslationRule(
            type = TranslationRuleType.SYSTEM_DICT,
            sourceField = "status",
            dictCode = "user_status"
        )
        
        assertEquals(TranslationRuleType.SYSTEM_DICT, systemRule.type)
        assertEquals("status", systemRule.sourceField)
        assertEquals("user_status", systemRule.dictCode)
        
        val tableRule = TranslationRule(
            type = TranslationRuleType.TABLE_DICT,
            sourceField = "deptId",
            table = "sys_dept",
            codeColumn = "id",
            nameColumn = "name"
        )
        
        assertEquals(TranslationRuleType.TABLE_DICT, tableRule.type)
        assertEquals("sys_dept", tableRule.table)
        
        val literalRule = TranslationRule(
            type = TranslationRuleType.LITERAL,
            sourceField = "",
            literalValue = "Fixed Value"
        )
        
        assertEquals("Fixed Value", literalRule.literalValue)
        
        val spelRule = TranslationRule(
            type = TranslationRuleType.SPEL,
            sourceField = "",
            spelExpression = "#{entity.id + '_computed'}"
        )
        
        assertEquals("#{entity.id + '_computed'}", spelRule.spelExpression)
    }
    
    @Test
    fun `test conditional translation`() {
        val conditional = ConditionalTranslation(
            condition = "entity.status == '1'",
            translation = TranslationRule(
                type = TranslationRuleType.LITERAL,
                sourceField = "",
                literalValue = "Active"
            )
        )
        
        assertEquals("entity.status == '1'", conditional.condition)
        assertEquals(TranslationRuleType.LITERAL, conditional.translation.type)
    }
    
    @Test
    fun `test nested processing statistics`() {
        val stats = NestedProcessingStatistics(
            processedObjectCount = 5,
            maxDepthReached = 3,
            maxDepthLimit = 10,
            circularReferenceDetectionEnabled = true
        )
        
        assertEquals(5, stats.processedObjectCount)
        assertEquals(3, stats.maxDepthReached)
        assertEquals(10, stats.maxDepthLimit)
        assertTrue(stats.circularReferenceDetectionEnabled)
        
        val statsString = stats.toString()
        assertTrue(statsString.contains("processedObjectCount=5"))
        assertTrue(statsString.contains("maxDepthReached=3"))
    }
}

/**
 * Test entity classes for nested translation testing
 */
data class TestUser(
    val id: Int,
    val status: String? = null,
    var statusText: String? = null
)

data class TestRole(
    val id: Int,
    val type: String? = null,
    var typeName: String? = null
)

data class TestUserWithRole(
    val id: Int,
    val status: String? = null,
    var statusText: String? = null,
    val role: TestRole? = null
)

data class TestUserWithRoles(
    val id: Int,
    val status: String? = null,
    var statusText: String? = null,
    val roles: List<TestRole> = emptyList()
)

data class TestDepartment(
    val id: Int,
    val deptId: String? = null,
    var deptName: String? = null
)

data class TestRoleWithDept(
    val id: Int,
    val type: String? = null,
    var typeName: String? = null,
    val department: TestDepartment? = null
)

data class TestUserCircular(
    val id: Int,
    val status: String? = null,
    var statusText: String? = null,
    var role: TestRoleCircular? = null
)

data class TestRoleCircular(
    val id: Int,
    val type: String? = null,
    var typeName: String? = null,
    val user: TestUserCircular? = null
)

data class TestNestedLevel(
    val id: Int,
    val status: String? = null,
    var statusText: String? = null,
    val child: TestNestedLevel? = null
)