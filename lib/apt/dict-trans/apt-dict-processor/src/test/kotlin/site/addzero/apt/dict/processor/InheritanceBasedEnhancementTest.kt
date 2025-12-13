package site.addzero.apt.dict.processor

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import site.addzero.apt.dict.dsl.*

/**
 * Test for inheritance-based enhanced entity generation (T->R mapping where R extends T)
 */
class InheritanceBasedEnhancementTest : FunSpec({
    
    test("should generate enhanced entity that extends original entity") {
        val enhancer = JavaEntityEnhancer()
        
        // Create DSL configuration for a simple entity
        val fieldRule = FieldTranslationRule(
            fieldName = "status",
            translationType = TranslationType.SYSTEM_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.SYSTEM_DICT,
                    dictCode = "user_status"
                )
            ),
            targetFieldName = "statusText"
        )
        
        val entityRule = EntityTranslationRule(
            entityName = "User",
            fieldRules = listOf(fieldRule)
        )
        
        val dslConfig = DslTemplateConfig(
            entityClass = "User",
            translationRules = listOf(entityRule)
        )
        
        // Generate Java code
        val javaCode = enhancer.generateEnhancedEntityJava(
            originalClass = null, // Mock would be needed for real test
            dslConfig = dslConfig,
            packageName = "com.example"
        )
        
        // Verify inheritance relationship
        javaCode shouldContain "public class UserEnhanced extends User"
        javaCode shouldContain "super();"
        javaCode shouldContain "this.get"
        javaCode shouldContain "T->R mapping"
        javaCode shouldContain "extends the original entity"
    }
    
    test("should generate translate method that uses this instead of sourceEntity") {
        val enhancer = JavaEntityEnhancer()
        
        val fieldRule = FieldTranslationRule(
            fieldName = "roleId",
            translationType = TranslationType.TABLE_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.TABLE_DICT,
                    table = "sys_role",
                    codeColumn = "id",
                    nameColumn = "name"
                )
            ),
            targetFieldName = "roleName"
        )
        
        val entityRule = EntityTranslationRule(
            entityName = "UserRole",
            fieldRules = listOf(fieldRule)
        )
        
        val dslConfig = DslTemplateConfig(
            entityClass = "UserRole",
            translationRules = listOf(entityRule)
        )
        
        val javaCode = enhancer.generateEnhancedEntityJava(
            originalClass = null,
            dslConfig = dslConfig,
            packageName = "com.example"
        )
        
        // Verify that translate method uses 'this' instead of 'sourceEntity'
        javaCode shouldContain "public void translate(TransApi transApi)"
        javaCode shouldContain "this.getRoleId()"
        javaCode shouldNotContain "sourceEntity"
        javaCode shouldContain "Since this class extends the original entity"
    }
    
    test("should generate metadata constants for enhanced entity") {
        val enhancer = JavaEntityEnhancer()
        
        val systemDictRule = FieldTranslationRule(
            fieldName = "status",
            translationType = TranslationType.SYSTEM_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.SYSTEM_DICT,
                    dictCode = "user_status"
                )
            ),
            targetFieldName = "statusText"
        )
        
        val tableDictRule = FieldTranslationRule(
            fieldName = "deptId",
            translationType = TranslationType.TABLE_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.TABLE_DICT,
                    table = "sys_dept",
                    codeColumn = "id",
                    nameColumn = "name"
                )
            ),
            targetFieldName = "deptName"
        )
        
        val entityRule = EntityTranslationRule(
            entityName = "Employee",
            fieldRules = listOf(systemDictRule, tableDictRule)
        )
        
        val dslConfig = DslTemplateConfig(
            entityClass = "Employee",
            translationRules = listOf(entityRule)
        )
        
        val javaCode = enhancer.generateEnhancedEntityJava(
            originalClass = null,
            dslConfig = dslConfig,
            packageName = "com.example"
        )
        
        // Verify metadata constants
        javaCode shouldContain "SYSTEM_DICT_CODES"
        javaCode shouldContain "TABLE_DICT_CONFIGS"
        javaCode shouldContain "user_status"
        javaCode shouldContain "sys_dept:id:name"
    }
})