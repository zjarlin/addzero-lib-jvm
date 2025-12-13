package site.addzero.apt.dict.template

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.collections.shouldContain
import site.addzero.apt.dict.dsl.*

/**
 * Test for DSL template library and validation
 */
class DslTemplateLibraryTest : FunSpec({
    
    test("should create RBAC user template with correct configuration") {
        val template = DslTemplateLibrary.createRBACUserTemplate()
        
        template.entityClass shouldBe "User"
        template.translationRules.size shouldBe 1
        
        val entityRule = template.translationRules.first()
        entityRule.entityName shouldBe "User"
        entityRule.fieldRules.size shouldBe 3
        
        // Check system dictionary field
        val statusRule = entityRule.fieldRules.find { it.fieldName == "status" }
        statusRule shouldNotBe null
        statusRule!!.translationType shouldBe TranslationType.SYSTEM_DICT
        statusRule.targetFieldName shouldBe "statusText"
        statusRule.dictConfigs.first().dictCode shouldBe "user_status"
        
        // Check table dictionary fields
        val deptRule = entityRule.fieldRules.find { it.fieldName == "departmentId" }
        deptRule shouldNotBe null
        deptRule!!.translationType shouldBe TranslationType.TABLE_DICT
        deptRule.targetFieldName shouldBe "departmentName"
        deptRule.dictConfigs.first().table shouldBe "sys_department"
    }
    
    test("should create RBAC role template with multiple system dictionaries") {
        val template = DslTemplateLibrary.createRBACRoleTemplate()
        
        template.entityClass shouldBe "Role"
        
        val entityRule = template.translationRules.first()
        val systemDictFields = entityRule.fieldRules.filter { 
            it.translationType == TranslationType.SYSTEM_DICT 
        }
        
        systemDictFields.size shouldBe 2
        systemDictFields.map { it.fieldName } shouldContain "type"
        systemDictFields.map { it.fieldName } shouldContain "level"
    }
    
    test("should create permission template with SPEL expressions") {
        val template = DslTemplateLibrary.createPermissionTemplate()
        
        val entityRule = template.translationRules.first()
        val spelFields = entityRule.fieldRules.filter { 
            it.translationType == TranslationType.SPEL 
        }
        
        spelFields.size shouldBe 2
        
        val resourceCategoryRule = spelFields.find { it.fieldName == "resource" }
        resourceCategoryRule shouldNotBe null
        resourceCategoryRule!!.dictConfigs.first().spelExpression shouldBe "#{dict('resource_category', resource)}"
        
        val actionRule = spelFields.find { it.fieldName == "action" }
        actionRule shouldNotBe null
        actionRule!!.dictConfigs.first().spelExpression shouldBe "#{table('sys_action', 'code', 'display_name', action)}"
    }
    
    test("should create multi-dictionary template with fallback configurations") {
        val template = DslTemplateLibrary.createMultiDictTemplate()
        
        val entityRule = template.translationRules.first()
        val multiDictRule = entityRule.fieldRules.first()
        
        multiDictRule.translationType shouldBe TranslationType.MULTI_DICT
        multiDictRule.dictConfigs.size shouldBe 3
        
        // Check fallback order
        multiDictRule.dictConfigs[0].type shouldBe TranslationType.SYSTEM_DICT
        multiDictRule.dictConfigs[1].type shouldBe TranslationType.TABLE_DICT
        multiDictRule.dictConfigs[2].type shouldBe TranslationType.SPEL
    }
    
    test("should create custom template with provided configurations") {
        val systemDictFields = mapOf(
            "status" to "custom_status",
            "type" to "custom_type"
        )
        
        val tableDictFields = mapOf(
            "categoryId" to TableDictInfo(
                table = "custom_category",
                codeColumn = "id",
                nameColumn = "name",
                condition = "active = true"
            )
        )
        
        val spelFields = mapOf(
            "complexField" to "#{dict('complex_dict', complexField)}"
        )
        
        val template = DslTemplateLibrary.createCustomTemplate(
            entityName = "CustomEntity",
            systemDictFields = systemDictFields,
            tableDictFields = tableDictFields,
            spelFields = spelFields
        )
        
        template.entityClass shouldBe "CustomEntity"
        
        val entityRule = template.translationRules.first()
        entityRule.fieldRules.size shouldBe 4
        
        // Verify system dict fields
        val systemRules = entityRule.fieldRules.filter { it.translationType == TranslationType.SYSTEM_DICT }
        systemRules.size shouldBe 2
        
        // Verify table dict fields
        val tableRules = entityRule.fieldRules.filter { it.translationType == TranslationType.TABLE_DICT }
        tableRules.size shouldBe 1
        tableRules.first().targetFieldName shouldBe "categoryIdName"
        
        // Verify SPEL fields
        val spelRules = entityRule.fieldRules.filter { it.translationType == TranslationType.SPEL }
        spelRules.size shouldBe 1
        spelRules.first().targetFieldName shouldBe "complexFieldText"
    }
    
    test("should get all available templates") {
        val templates = DslTemplateLibrary.getAllTemplates()
        
        templates.shouldNotBeEmpty()
        templates.keys shouldContain "rbac_user"
        templates.keys shouldContain "rbac_role"
        templates.keys shouldContain "permission"
        templates.keys shouldContain "audit_log"
        templates.keys shouldContain "multi_dict"
        templates.keys shouldContain "system_config"
        templates.keys shouldContain "multi_tenant"
        
        // Verify each template is valid
        templates.values.forEach { template ->
            template.entityClass.shouldNotBeEmpty()
            template.translationRules.shouldNotBeEmpty()
        }
    }
    
    test("should get template by name") {
        val userTemplate = DslTemplateLibrary.getTemplate("rbac_user")
        userTemplate shouldNotBe null
        userTemplate!!.entityClass shouldBe "User"
        
        val nonExistentTemplate = DslTemplateLibrary.getTemplate("non_existent")
        nonExistentTemplate shouldBe null
    }
    
    test("should validate valid template configuration") {
        val template = DslTemplateLibrary.createRBACUserTemplate()
        val errors = DslTemplateValidator.validateTemplate(template)
        
        errors.shouldBeEmpty()
    }
    
    test("should detect validation errors in invalid template") {
        val invalidFieldRule = FieldTranslationRule(
            fieldName = "", // Invalid: blank field name
            translationType = TranslationType.SYSTEM_DICT,
            dictConfigs = emptyList(), // Invalid: no dict configs
            targetFieldName = "validTarget"
        )
        
        val invalidEntityRule = EntityTranslationRule(
            entityName = "TestEntity",
            fieldRules = listOf(invalidFieldRule)
        )
        
        val invalidTemplate = DslTemplateConfig(
            entityClass = "", // Invalid: blank entity class
            translationRules = listOf(invalidEntityRule)
        )
        
        val errors = DslTemplateValidator.validateTemplate(invalidTemplate)
        
        errors.shouldNotBeEmpty()
        errors.any { it.message.contains("Entity class name cannot be blank") } shouldBe true
        errors.any { it.message.contains("Field name cannot be blank") } shouldBe true
        errors.any { it.message.contains("must have at least one dictionary configuration") } shouldBe true
    }
    
    test("should detect duplicate target fields") {
        val duplicateRule1 = FieldTranslationRule(
            fieldName = "field1",
            translationType = TranslationType.SYSTEM_DICT,
            dictConfigs = listOf(
                DictConfig(type = TranslationType.SYSTEM_DICT, dictCode = "dict1")
            ),
            targetFieldName = "duplicateTarget"
        )
        
        val duplicateRule2 = FieldTranslationRule(
            fieldName = "field2",
            translationType = TranslationType.SYSTEM_DICT,
            dictConfigs = listOf(
                DictConfig(type = TranslationType.SYSTEM_DICT, dictCode = "dict2")
            ),
            targetFieldName = "duplicateTarget" // Same target field
        )
        
        val entityRule = EntityTranslationRule(
            entityName = "TestEntity",
            fieldRules = listOf(duplicateRule1, duplicateRule2)
        )
        
        val template = DslTemplateConfig(
            entityClass = "TestEntity",
            translationRules = listOf(entityRule)
        )
        
        val errors = DslTemplateValidator.validateTemplate(template)
        
        errors.shouldNotBeEmpty()
        errors.any { it.message.contains("Duplicate target field: duplicateTarget") } shouldBe true
    }
    
    test("should validate system dictionary configuration") {
        val invalidSystemDictRule = FieldTranslationRule(
            fieldName = "status",
            translationType = TranslationType.SYSTEM_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.SYSTEM_DICT,
                    dictCode = "" // Invalid: blank dict code
                )
            ),
            targetFieldName = "statusText"
        )
        
        val entityRule = EntityTranslationRule(
            entityName = "TestEntity",
            fieldRules = listOf(invalidSystemDictRule)
        )
        
        val template = DslTemplateConfig(
            entityClass = "TestEntity",
            translationRules = listOf(entityRule)
        )
        
        val errors = DslTemplateValidator.validateTemplate(template)
        
        errors.shouldNotBeEmpty()
        errors.any { it.message.contains("must specify dictCode") } shouldBe true
    }
    
    test("should validate table dictionary configuration") {
        val invalidTableDictRule = FieldTranslationRule(
            fieldName = "deptId",
            translationType = TranslationType.TABLE_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.TABLE_DICT,
                    table = "sys_dept",
                    codeColumn = "", // Invalid: blank code column
                    nameColumn = "name"
                )
            ),
            targetFieldName = "deptName"
        )
        
        val entityRule = EntityTranslationRule(
            entityName = "TestEntity",
            fieldRules = listOf(invalidTableDictRule)
        )
        
        val template = DslTemplateConfig(
            entityClass = "TestEntity",
            translationRules = listOf(entityRule)
        )
        
        val errors = DslTemplateValidator.validateTemplate(template)
        
        errors.shouldNotBeEmpty()
        errors.any { it.message.contains("must specify codeColumn") } shouldBe true
    }
    
    test("should validate SPEL expression configuration") {
        val invalidSpelRule = FieldTranslationRule(
            fieldName = "complexField",
            translationType = TranslationType.SPEL,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.SPEL,
                    spelExpression = "" // Invalid: blank SPEL expression
                )
            ),
            targetFieldName = "complexFieldText"
        )
        
        val entityRule = EntityTranslationRule(
            entityName = "TestEntity",
            fieldRules = listOf(invalidSpelRule)
        )
        
        val template = DslTemplateConfig(
            entityClass = "TestEntity",
            translationRules = listOf(entityRule)
        )
        
        val errors = DslTemplateValidator.validateTemplate(template)
        
        errors.shouldNotBeEmpty()
        errors.any { it.message.contains("must specify spelExpression") } shouldBe true
    }
})