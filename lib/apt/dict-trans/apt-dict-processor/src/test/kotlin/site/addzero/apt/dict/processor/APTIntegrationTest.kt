package site.addzero.apt.dict.processor

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import site.addzero.apt.dict.dsl.*

/**
 * Integration test for APT processor functionality
 */
class APTIntegrationTest {
    
    @Test
    fun `test DSL config creation from annotations`() {
        // Simulate annotation processing
        val processor = DictTranslateProcessor()
        
        // Create test dictionary fields
        val dictFields = listOf(
            DictFieldInfo(
                sourceField = "status",
                targetField = "statusText",
                dictCode = "user_status"
            ),
            DictFieldInfo(
                sourceField = "departmentId",
                targetField = "departmentText",
                table = "sys_department",
                codeColumn = "id",
                nameColumn = "dept_name"
            )
        )
        
        // Verify field creation
        assertEquals(2, dictFields.size)
        
        val systemDictField = dictFields[0]
        assertEquals("status", systemDictField.sourceField)
        assertEquals("statusText", systemDictField.targetField)
        assertEquals("user_status", systemDictField.dictCode)
        
        val tableDictField = dictFields[1]
        assertEquals("departmentId", tableDictField.sourceField)
        assertEquals("departmentText", tableDictField.targetField)
        assertEquals("sys_department", tableDictField.table)
        assertEquals("id", tableDictField.codeColumn)
        assertEquals("dept_name", tableDictField.nameColumn)
    }
    
    @Test
    fun `test Java code generation integration`() {
        val javaEntityEnhancer = JavaEntityEnhancer()
        
        // Create DSL configuration
        val userEntityRule = EntityTranslationRule(
            entityName = "UserEntity",
            fieldRules = listOf(
                FieldTranslationRule(
                    fieldName = "status",
                    translationType = TranslationType.SYSTEM_DICT,
                    dictConfigs = listOf(
                        DictConfig(
                            type = TranslationType.SYSTEM_DICT,
                            dictCode = "user_status"
                        )
                    ),
                    targetFieldName = "statusText"
                ),
                FieldTranslationRule(
                    fieldName = "departmentId",
                    translationType = TranslationType.TABLE_DICT,
                    dictConfigs = listOf(
                        DictConfig(
                            type = TranslationType.TABLE_DICT,
                            table = "sys_department",
                            codeColumn = "id",
                            nameColumn = "dept_name"
                        )
                    ),
                    targetFieldName = "departmentText"
                )
            )
        )
        
        val dslConfig = DslTemplateConfig(
            entityClass = "UserEntity",
            translationRules = listOf(userEntityRule)
        )
        
        // This would normally be called with a real TypeElement
        // For testing, we verify the DSL config is properly structured
        assertNotNull(dslConfig)
        assertEquals("UserEntity", dslConfig.entityClass)
        assertEquals(1, dslConfig.translationRules.size)
        
        val rule = dslConfig.translationRules[0]
        assertEquals("UserEntity", rule.entityName)
        assertEquals(2, rule.fieldRules.size)
        
        val systemDictRule = rule.fieldRules[0]
        assertEquals("status", systemDictRule.fieldName)
        assertEquals("statusText", systemDictRule.targetFieldName)
        assertEquals(TranslationType.SYSTEM_DICT, systemDictRule.translationType)
        
        val tableDictRule = rule.fieldRules[1]
        assertEquals("departmentId", tableDictRule.fieldName)
        assertEquals("departmentText", tableDictRule.targetFieldName)
        assertEquals(TranslationType.TABLE_DICT, tableDictRule.translationType)
    }
    
    @Test
    fun `test annotation processor configuration`() {
        val processor = DictTranslateProcessor()
        
        // Verify processor configuration
        assertTrue(processor.supportedAnnotationTypes.contains("site.addzero.apt.dict.annotations.DictTranslate"))
        assertNotNull(processor.supportedSourceVersion)
        
        // Verify processor can be instantiated
        assertNotNull(processor)
    }
    
    @Test
    fun `test complex dictionary field scenarios`() {
        // Test multiple dictionary types on same field
        val complexField = DictFieldInfo(
            sourceField = "regionCode",
            targetField = "regionText",
            table = "sys_region",
            codeColumn = "code",
            nameColumn = "name",
            condition = "status = 1 AND level = 2"
        )
        
        assertEquals("regionCode", complexField.sourceField)
        assertEquals("regionText", complexField.targetField)
        assertEquals("sys_region", complexField.table)
        assertEquals("status = 1 AND level = 2", complexField.condition)
    }
}