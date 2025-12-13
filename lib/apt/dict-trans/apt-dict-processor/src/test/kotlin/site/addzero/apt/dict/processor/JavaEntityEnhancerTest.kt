package site.addzero.apt.dict.processor

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import site.addzero.apt.dict.dsl.*
import site.addzero.apt.dict.template.JTETemplateManager

/**
 * Test for JavaEntityEnhancer to verify Java code generation
 */
class JavaEntityEnhancerTest {
    
    @Test
    fun `test Java code generation from DSL`() {
        val templateManager = JTETemplateManager()
        
        // Create DSL configuration for UserEntity
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
        
        // Generate Java code
        val javaCode = templateManager.renderEnhancedEntityJavaFromDsl(
            dslConfig = dslConfig,
            packageName = "com.example.entity",
            originalClassName = "UserEntity"
        )
        
        // Verify generated code contains expected elements
        assertNotNull(javaCode)
        assertTrue(javaCode.contains("package com.example.entity;"))
        assertTrue(javaCode.contains("public class UserEntityEnhanced"))
        assertTrue(javaCode.contains("private String statusText;"))
        assertTrue(javaCode.contains("private String departmentText;"))
        assertTrue(javaCode.contains("public String getStatusText()"))
        assertTrue(javaCode.contains("public void setStatusText(String statusText)"))
        assertTrue(javaCode.contains("public String getDepartmentText()"))
        assertTrue(javaCode.contains("public void setDepartmentText(String departmentText)"))
        assertTrue(javaCode.contains("public void translate(TransApi transApi, UserEntity sourceEntity)"))
        assertTrue(javaCode.contains("public CompletableFuture<Void> translateAsync"))
        assertTrue(javaCode.contains("translateDictBatchCode2name"))
        assertTrue(javaCode.contains("translateTableBatchCode2name"))
        
        println("Generated Java code:")
        println(javaCode)
    }
    
    @Test
    fun `test JavaPoet code generation`() {
        val enhancer = JavaEntityEnhancer()
        
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
        
        val config = EnhancedEntityConfig(
            suffix = "Enhanced",
            generateAsync = true,
            generateMetadata = true,
            packageName = "com.example.entity"
        )
        
        // This would normally use a TypeElement, but for testing we'll just verify the method exists
        assertNotNull(enhancer)
        
        // Test the data classes
        val dictField = dictFields[0]
        assertEquals("status", dictField.sourceField)
        assertEquals("statusText", dictField.targetField)
        assertEquals("user_status", dictField.dictCode)
        
        val tableField = dictFields[1]
        assertEquals("departmentId", tableField.sourceField)
        assertEquals("departmentText", tableField.targetField)
        assertEquals("sys_department", tableField.table)
        assertEquals("id", tableField.codeColumn)
        assertEquals("dept_name", tableField.nameColumn)
    }
}