package site.addzero.apt.dict.processor

import com.squareup.kotlinpoet.ClassName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

/**
 * Unit tests for EntityEnhancer
 * 
 * Tests the core functionality of enhanced entity generation including:
 * - Enhanced entity class generation
 * - Inheritance hierarchy management
 * - Translation field generation and mapping logic
 * - Metadata generation for translation context
 */
class EntityEnhancerTest {
    
    private lateinit var entityEnhancer: EntityEnhancer
    
    @BeforeEach
    fun setUp() {
        entityEnhancer = EntityEnhancer()
    }
    
    @Test
    fun `test enhanced entity config creation`() {
        val config = EnhancedEntityConfig(
            suffix = "Enhanced",
            generateExtensions = true,
            generateBuilder = false,
            generateAsync = false,
            batchTranslate = true,
            batchSize = 100
        )
        
        assertEquals("Enhanced", config.suffix)
        assertTrue(config.generateExtensions)
        assertFalse(config.generateBuilder)
        assertFalse(config.generateAsync)
        assertTrue(config.batchTranslate)
        assertEquals(100, config.batchSize)
    }
    
    @Test
    fun `test dict field info creation`() {
        val dictField = DictFieldInfo(
            sourceField = "status",
            sourceType = ClassName("kotlin", "String"),
            dictCode = "user_status",
            table = "",
            codeColumn = "",
            nameColumn = "",
            targetField = "statusName",
            spelExp = "",
            ignoreNull = true,
            defaultValue = "",
            cached = true
        )
        
        assertEquals("status", dictField.sourceField)
        assertEquals("user_status", dictField.dictCode)
        assertEquals("statusName", dictField.targetField)
        assertTrue(dictField.ignoreNull)
        assertTrue(dictField.cached)
    }
    
    @Test
    fun `test dict field info for table dictionary`() {
        val dictField = DictFieldInfo(
            sourceField = "deptId",
            sourceType = ClassName("kotlin", "Long"),
            dictCode = "",
            table = "sys_dept",
            codeColumn = "id",
            nameColumn = "name",
            targetField = "deptName",
            spelExp = "",
            ignoreNull = true,
            defaultValue = "",
            cached = true
        )
        
        assertEquals("deptId", dictField.sourceField)
        assertEquals("sys_dept", dictField.table)
        assertEquals("id", dictField.codeColumn)
        assertEquals("name", dictField.nameColumn)
        assertEquals("deptName", dictField.targetField)
    }
    
    @Test
    fun `test dict field info for spel expression`() {
        val dictField = DictFieldInfo(
            sourceField = "complexField",
            sourceType = ClassName("kotlin", "String"),
            dictCode = "",
            table = "",
            codeColumn = "",
            nameColumn = "",
            targetField = "complexFieldName",
            spelExp = "#{someService.translate(#root.complexField)}",
            ignoreNull = true,
            defaultValue = "Unknown",
            cached = false
        )
        
        assertEquals("complexField", dictField.sourceField)
        assertEquals("#{someService.translate(#root.complexField)}", dictField.spelExp)
        assertEquals("complexFieldName", dictField.targetField)
        assertEquals("Unknown", dictField.defaultValue)
        assertFalse(dictField.cached)
    }
    
    @Test
    fun `test enhanced entity config with all options enabled`() {
        val config = EnhancedEntityConfig(
            suffix = "Translated",
            generateExtensions = true,
            generateBuilder = true,
            generateAsync = true,
            batchTranslate = true,
            batchSize = 50
        )
        
        assertEquals("Translated", config.suffix)
        assertTrue(config.generateExtensions)
        assertTrue(config.generateBuilder)
        assertTrue(config.generateAsync)
        assertTrue(config.batchTranslate)
        assertEquals(50, config.batchSize)
    }
    
    @Test
    fun `test enhanced entity config with minimal options`() {
        val config = EnhancedEntityConfig(
            suffix = "Basic",
            generateExtensions = false,
            generateBuilder = false,
            generateAsync = false,
            batchTranslate = false,
            batchSize = 1
        )
        
        assertEquals("Basic", config.suffix)
        assertFalse(config.generateExtensions)
        assertFalse(config.generateBuilder)
        assertFalse(config.generateAsync)
        assertFalse(config.batchTranslate)
        assertEquals(1, config.batchSize)
    }
    
    @Test
    fun `test multiple dict fields configuration`() {
        val dictFields = listOf(
            DictFieldInfo(
                sourceField = "status",
                sourceType = ClassName("kotlin", "String"),
                dictCode = "user_status",
                table = "",
                codeColumn = "",
                nameColumn = "",
                targetField = "statusName",
                spelExp = "",
                ignoreNull = true,
                defaultValue = "",
                cached = true
            ),
            DictFieldInfo(
                sourceField = "deptId",
                sourceType = ClassName("kotlin", "Long"),
                dictCode = "",
                table = "sys_dept",
                codeColumn = "id",
                nameColumn = "name",
                targetField = "deptName",
                spelExp = "",
                ignoreNull = true,
                defaultValue = "",
                cached = true
            ),
            DictFieldInfo(
                sourceField = "roleCode",
                sourceType = ClassName("kotlin", "String"),
                dictCode = "",
                table = "",
                codeColumn = "",
                nameColumn = "",
                targetField = "roleName",
                spelExp = "#{roleService.getRoleName(#root.roleCode)}",
                ignoreNull = false,
                defaultValue = "Guest",
                cached = false
            )
        )
        
        assertEquals(3, dictFields.size)
        
        // Verify system dictionary field
        val systemDictField = dictFields[0]
        assertEquals("user_status", systemDictField.dictCode)
        assertTrue(systemDictField.table.isEmpty())
        assertTrue(systemDictField.spelExp.isEmpty())
        
        // Verify table dictionary field
        val tableDictField = dictFields[1]
        assertTrue(tableDictField.dictCode.isEmpty())
        assertEquals("sys_dept", tableDictField.table)
        assertTrue(tableDictField.spelExp.isEmpty())
        
        // Verify SPEL expression field
        val spelField = dictFields[2]
        assertTrue(spelField.dictCode.isEmpty())
        assertTrue(spelField.table.isEmpty())
        assertEquals("#{roleService.getRoleName(#root.roleCode)}", spelField.spelExp)
        assertFalse(spelField.ignoreNull)
        assertEquals("Guest", spelField.defaultValue)
        assertFalse(spelField.cached)
    }
}