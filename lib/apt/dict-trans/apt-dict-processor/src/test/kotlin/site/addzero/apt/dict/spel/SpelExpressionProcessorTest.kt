package site.addzero.apt.dict.spel

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import site.addzero.apt.dict.context.TranslationContext

/**
 * Unit tests for SpelExpressionProcessor
 * 
 * Tests SPEL expression parsing, validation, evaluation,
 * and code generation functionality
 */
class SpelExpressionProcessorTest {
    
    private lateinit var processor: SpelExpressionProcessor
    private lateinit var context: TranslationContext
    
    @BeforeEach
    fun setUp() {
        processor = SpelExpressionProcessor()
        
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
    
    @Test
    fun `test parse simple expression`() {
        val expression = "entity.name"
        val compiled = processor.parseExpression(expression)
        
        assertNotNull(compiled)
        assertEquals(expression, compiled.originalExpression)
        assertEquals(1, compiled.components.size)
        assertEquals(SpelComponentType.PROPERTY_ACCESS, compiled.components[0].type)
    }
    
    @Test
    fun `test parse dict lookup expression`() {
        val expression = "#dict('user_status', entity.status)"
        val compiled = processor.parseExpression(expression)
        
        assertNotNull(compiled)
        assertEquals(expression, compiled.originalExpression)
        assertEquals(1, compiled.components.size)
        assertEquals(SpelComponentType.DICT_LOOKUP, compiled.components[0].type)
    }
    
    @Test
    fun `test parse table lookup expression`() {
        val expression = "#table('sys_dept', 'id', 'name', entity.deptId)"
        val compiled = processor.parseExpression(expression)
        
        assertNotNull(compiled)
        assertEquals(expression, compiled.originalExpression)
        assertEquals(1, compiled.components.size)
        assertEquals(SpelComponentType.TABLE_LOOKUP, compiled.components[0].type)
    }
    
    @Test
    fun `test parse literal expression`() {
        val expression = "'Fixed Value'"
        val compiled = processor.parseExpression(expression)
        
        assertNotNull(compiled)
        assertEquals(expression, compiled.originalExpression)
        assertEquals(1, compiled.components.size)
        assertEquals(SpelComponentType.LITERAL, compiled.components[0].type)
    }
    
    @Test
    fun `test parse empty expression throws exception`() {
        assertThrows(SpelParseException::class.java) {
            processor.parseExpression("")
        }
        
        assertThrows(SpelParseException::class.java) {
            processor.parseExpression("   ")
        }
    }
    
    @Test
    fun `test parse expression with unbalanced parentheses throws exception`() {
        assertThrows(SpelParseException::class.java) {
            processor.parseExpression("#dict('user_status', entity.status")
        }
        
        assertThrows(SpelParseException::class.java) {
            processor.parseExpression("#dict('user_status', entity.status))")
        }
    }
    
    @Test
    fun `test evaluate dict lookup expression`() {
        val user = TestUser(id = 1, status = "1", name = "John")
        val expression = "#dict('user_status', entity.status)"
        
        val result = processor.evaluateExpression(expression, user, context)
        
        assertEquals("Active", result)
    }
    
    @Test
    fun `test evaluate table lookup expression`() {
        val user = TestUser(id = 1, deptId = "100", name = "John")
        val expression = "#table('sys_dept', 'id', 'name', entity.deptId)"
        
        val result = processor.evaluateExpression(expression, user, context)
        
        assertEquals("Technology Department", result)
    }
    
    @Test
    fun `test evaluate property access expression`() {
        val user = TestUser(id = 1, name = "John")
        val expression = "entity.name"
        
        val result = processor.evaluateExpression(expression, user, context)
        
        assertEquals("John", result)
    }
    
    @Test
    fun `test evaluate literal expression`() {
        val user = TestUser(id = 1, name = "John")
        val expression = "'Fixed Value'"
        
        val result = processor.evaluateExpression(expression, user, context)
        
        assertEquals("Fixed Value", result)
    }
    
    @Test
    fun `test evaluate expression with invalid dict code returns null`() {
        val user = TestUser(id = 1, status = "1", name = "John")
        val expression = "#dict('nonexistent_dict', entity.status)"
        
        val result = processor.evaluateExpression(expression, user, context)
        
        assertNull(result)
    }
    
    @Test
    fun `test evaluate expression with invalid property returns null`() {
        val user = TestUser(id = 1, name = "John")
        val expression = "entity.nonexistentProperty"
        
        val result = processor.evaluateExpression(expression, user, context)
        
        assertNull(result)
    }
    
    @Test
    fun `test validate valid expression`() {
        val expression = "#dict('user_status', entity.status)"
        val validation = processor.validateExpression(expression, TestUser::class.java)
        
        assertTrue(validation.isValid)
        assertTrue(validation.errors.isEmpty())
    }
    
    @Test
    fun `test validate expression with invalid property`() {
        val expression = "#dict('user_status', entity.nonexistentProperty)"
        val validation = processor.validateExpression(expression, TestUser::class.java)
        
        assertFalse(validation.isValid)
        assertTrue(validation.errors.any { it.contains("nonexistentProperty") })
    }
    
    @Test
    fun `test validate expression with unsafe operations`() {
        val expression = "System.exit(0)"
        val validation = processor.validateExpression(expression, TestUser::class.java)
        
        assertTrue(validation.warnings.any { it.contains("unsafe") })
    }
    
    @Test
    fun `test validate very long expression`() {
        val longExpression = "entity.name".repeat(100)
        val validation = processor.validateExpression(longExpression, TestUser::class.java)
        
        assertTrue(validation.warnings.any { it.contains("very long") })
    }
    
    @Test
    fun `test validate expression with syntax error`() {
        val expression = "#dict('user_status', entity.status"  // Missing closing parenthesis
        val validation = processor.validateExpression(expression, TestUser::class.java)
        
        assertFalse(validation.isValid)
        assertTrue(validation.errors.any { it.contains("Syntax error") })
    }
    
    @Test
    fun `test generate evaluation code`() {
        val expression = "#dict('user_status', entity.status)"
        val code = processor.generateEvaluationCode(expression, TestUser::class.java, "statusText")
        
        assertTrue(code.contains("SpelExpressionProcessor"))
        assertTrue(code.contains("evaluateExpression"))
        assertTrue(code.contains("setStatusText"))
        assertTrue(code.contains(expression))
    }
    
    @Test
    fun `test generate code for invalid expression throws exception`() {
        val expression = "#dict('user_status', entity.nonexistentProperty)"
        
        assertThrows(SpelCodeGenerationException::class.java) {
            processor.generateEvaluationCode(expression, TestUser::class.java, "statusText")
        }
    }
    
    @Test
    fun `test expression caching`() {
        val expression = "#dict('user_status', entity.status)"
        
        // Parse expression twice
        val compiled1 = processor.parseExpression(expression)
        val compiled2 = processor.parseExpression(expression)
        
        // Should return the same cached instance
        assertSame(compiled1, compiled2)
        
        val stats = processor.getCacheStatistics()
        assertEquals(1, stats.expressionCacheSize)
        assertTrue(stats.cachedExpressions.contains(expression))
    }
    
    @Test
    fun `test clear caches`() {
        val expression = "#dict('user_status', entity.status)"
        val user = TestUser(id = 1, status = "1", name = "John")
        
        // Use processor to populate caches
        processor.parseExpression(expression)
        processor.evaluateExpression(expression, user, context)
        
        val statsBefore = processor.getCacheStatistics()
        assertTrue(statsBefore.expressionCacheSize > 0)
        
        // Clear caches
        processor.clearCaches()
        
        val statsAfter = processor.getCacheStatistics()
        assertEquals(0, statsAfter.expressionCacheSize)
        assertEquals(0, statsAfter.contextCacheSize)
    }
    
    @Test
    fun `test spel expression parser`() {
        val parser = SpelExpressionParser()
        
        // Test valid expressions
        assertDoesNotThrow {
            parser.parseExpression("entity.name")
            parser.parseExpression("#dict('user_status', entity.status)")
            parser.parseExpression("'literal'")
        }
        
        // Test invalid expressions
        assertThrows(SpelParseException::class.java) {
            parser.parseExpression("")
        }
        
        assertThrows(SpelParseException::class.java) {
            parser.parseExpression("unbalanced(")
        }
    }
    
    @Test
    fun `test compiled spel expression evaluation`() {
        val expression = "#dict('user_status', entity.status)"
        val compiled = processor.parseExpression(expression)
        val user = TestUser(id = 1, status = "1", name = "John")
        val evaluationContext = SpelEvaluationContext(user, context)
        
        val result = compiled.evaluate(evaluationContext)
        
        assertEquals("Active", result)
    }
    
    @Test
    fun `test spel evaluation context`() {
        val user = TestUser(id = 1, status = "1", name = "John")
        val evaluationContext = SpelEvaluationContext(user, context)
        
        // Test property access
        assertEquals("1", evaluationContext.getEntityProperty("status"))
        assertEquals("John", evaluationContext.getEntityProperty("name"))
        assertEquals(1, evaluationContext.getEntityProperty("id"))
        
        // Test non-existent property
        assertNull(evaluationContext.getEntityProperty("nonexistent"))
        
        // Test property setting
        evaluationContext.setEntityProperty("name", "Jane")
        assertEquals("Jane", evaluationContext.getEntityProperty("name"))
        assertEquals("Jane", user.name) // Should update the actual entity
    }
    
    @Test
    fun `test spel evaluation exception handling`() {
        val expression = "#dict('user_status', entity.status)"
        val compiled = processor.parseExpression(expression)
        
        // Create context that will cause evaluation to fail
        val invalidUser = object {
            // No status property
        }
        val evaluationContext = SpelEvaluationContext(invalidUser, context)
        
        // Should handle evaluation errors gracefully
        assertDoesNotThrow {
            val result = compiled.evaluate(evaluationContext)
            // Result might be null due to missing property
        }
    }
    
    @Test
    fun `test spel component types`() {
        val dictComponent = SpelExpressionComponent(SpelComponentType.DICT_LOOKUP, "#dict('test', entity.field)")
        val tableComponent = SpelExpressionComponent(SpelComponentType.TABLE_LOOKUP, "#table('test', 'id', 'name', entity.field)")
        val propertyComponent = SpelExpressionComponent(SpelComponentType.PROPERTY_ACCESS, "entity.field")
        val literalComponent = SpelExpressionComponent(SpelComponentType.LITERAL, "'literal'")
        val complexComponent = SpelExpressionComponent(SpelComponentType.COMPLEX, "complex expression")
        
        assertEquals(SpelComponentType.DICT_LOOKUP, dictComponent.type)
        assertEquals(SpelComponentType.TABLE_LOOKUP, tableComponent.type)
        assertEquals(SpelComponentType.PROPERTY_ACCESS, propertyComponent.type)
        assertEquals(SpelComponentType.LITERAL, literalComponent.type)
        assertEquals(SpelComponentType.COMPLEX, complexComponent.type)
    }
    
    @Test
    fun `test spel validation result`() {
        val validResult = SpelValidationResult(
            isValid = true,
            errors = emptyList(),
            warnings = listOf("Minor warning"),
            expression = "entity.name"
        )
        
        assertTrue(validResult.isValid)
        assertTrue(validResult.errors.isEmpty())
        assertEquals(1, validResult.warnings.size)
        
        val invalidResult = SpelValidationResult(
            isValid = false,
            errors = listOf("Syntax error"),
            warnings = emptyList(),
            expression = "invalid expression"
        )
        
        assertFalse(invalidResult.isValid)
        assertEquals(1, invalidResult.errors.size)
        
        val resultString = validResult.toString()
        assertTrue(resultString.contains("isValid=true"))
        assertTrue(resultString.contains("errors=0"))
        assertTrue(resultString.contains("warnings=1"))
    }
    
    @Test
    fun `test spel cache statistics`() {
        val stats = SpelCacheStatistics(
            expressionCacheSize = 5,
            contextCacheSize = 3,
            cachedExpressions = setOf("expr1", "expr2", "expr3")
        )
        
        assertEquals(5, stats.expressionCacheSize)
        assertEquals(3, stats.contextCacheSize)
        assertEquals(3, stats.cachedExpressions.size)
        
        val statsString = stats.toString()
        assertTrue(statsString.contains("expressionCacheSize=5"))
        assertTrue(statsString.contains("contextCacheSize=3"))
        assertTrue(statsString.contains("totalCachedExpressions=3"))
    }
    
    @Test
    fun `test spel exceptions`() {
        val parseException = SpelParseException("Parse error")
        assertEquals("Parse error", parseException.message)
        
        val parseExceptionWithCause = SpelParseException("Parse error", RuntimeException("Cause"))
        assertEquals("Parse error", parseExceptionWithCause.message)
        assertNotNull(parseExceptionWithCause.cause)
        
        val evaluationException = SpelEvaluationException("Evaluation error")
        assertEquals("Evaluation error", evaluationException.message)
        
        val codeGenException = SpelCodeGenerationException("Code generation error")
        assertEquals("Code generation error", codeGenException.message)
    }
    
    @Test
    fun `test complex expression evaluation`() {
        // Test more complex expressions that combine multiple elements
        val user = TestUser(id = 1, status = "1", name = "John", deptId = "100")
        
        // This would be a complex expression in a real implementation
        val complexExpression = "entity.name + ' - ' + #dict('user_status', entity.status)"
        
        // For now, our simplified implementation will just return the expression
        val result = processor.evaluateExpression(complexExpression, user, context)
        
        // In the simplified implementation, complex expressions return the original expression
        assertEquals(complexExpression, result)
    }
    
    @Test
    fun `test property extraction from expressions`() {
        val processor = SpelExpressionProcessor()
        
        // Use reflection to test the private method (for testing purposes)
        val method = processor.javaClass.getDeclaredMethod("extractPropertyReferences", String::class.java)
        method.isAccessible = true
        
        @Suppress("UNCHECKED_CAST")
        val properties1 = method.invoke(processor, "entity.status") as Set<String>
        assertTrue(properties1.contains("status"))
        
        @Suppress("UNCHECKED_CAST")
        val properties2 = method.invoke(processor, "#dict('user_status', entity.status)") as Set<String>
        assertTrue(properties2.contains("status"))
        
        @Suppress("UNCHECKED_CAST")
        val properties3 = method.invoke(processor, "entity.name + entity.status") as Set<String>
        assertTrue(properties3.contains("name"))
        assertTrue(properties3.contains("status"))
    }
}

/**
 * Test entity class for SPEL testing
 */
data class TestUser(
    val id: Int,
    val status: String? = null,
    var name: String? = null,
    val deptId: String? = null
)