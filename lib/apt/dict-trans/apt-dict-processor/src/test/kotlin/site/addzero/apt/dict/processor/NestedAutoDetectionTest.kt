package site.addzero.apt.dict.processor

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import site.addzero.apt.dict.dsl.*

/**
 * Test for automatic nested structure and List detection functionality
 */
class NestedAutoDetectionTest : FunSpec({
    
    test("should generate Java code with nested structure handling") {
        val enhancer = JavaEntityEnhancer()
        
        // Create a DSL config with nested field rules
        val dslConfig = DslTemplateConfig(
            entityClass = "UserEntity",
            translationRules = listOf(
                EntityTranslationRule(
                    entityName = "UserEntity",
                    fieldRules = listOf(
                        FieldTranslationRule(
                            fieldName = "status",
                            translationType = TranslationType.SYSTEM_DICT,
                            dictConfigs = listOf(
                                DictConfig(
                                    type = TranslationType.SYSTEM_DICT,
                                    dictCode = "USER_STATUS"
                                )
                            ),
                            targetFieldName = "statusText"
                        ),
                        FieldTranslationRule(
                            fieldName = "department.type",
                            translationType = TranslationType.SYSTEM_DICT,
                            dictConfigs = listOf(
                                DictConfig(
                                    type = TranslationType.SYSTEM_DICT,
                                    dictCode = "DEPT_TYPE"
                                )
                            ),
                            targetFieldName = "department_typeText"
                        ),
                        FieldTranslationRule(
                            fieldName = "roles[].level",
                            translationType = TranslationType.TABLE_DICT,
                            dictConfigs = listOf(
                                DictConfig(
                                    type = TranslationType.TABLE_DICT,
                                    table = "sys_role_level",
                                    codeColumn = "level_code",
                                    nameColumn = "level_name"
                                )
                            ),
                            targetFieldName = "roles_list_levelText"
                        )
                    )
                )
            )
        )
        
        val javaCode = enhancer.generateJavaClassCode(
            packageName = "com.example.entity",
            originalClassName = "UserEntity",
            enhancedClassName = "UserEntityEnhanced",
            dslConfig = dslConfig
        )
        
        println("Generated Java code:")
        println(javaCode)
        
        // Verify the generated code contains expected elements
        javaCode shouldContain "public class UserEntityEnhanced extends UserEntity"
        javaCode shouldContain "private String statusText;"
        javaCode shouldContain "private String department_typeText;"
        javaCode shouldContain "private String roles_list_levelText;"
        
        // Verify nested structure handling methods are generated
        javaCode shouldContain "translateNestedStructures(TransApi transApi)"
        javaCode shouldContain "hasTranslateMethod(Class<?> clazz)"
        javaCode shouldContain "invokeTranslateMethod(Object obj, TransApi transApi)"
        
        // Verify reflection-based nested handling
        javaCode shouldContain "java.lang.reflect.Field[] fields = this.getClass().getSuperclass().getDeclaredFields();"
        javaCode shouldContain "if (fieldValue instanceof java.util.List)"
        
        // Verify system dict translation
        javaCode shouldContain "USER_STATUS"
        javaCode shouldContain "translateDictBatchCode2name"
        
        // Verify table dict translation
        javaCode shouldContain "sys_role_level"
        javaCode shouldContain "translateTableBatchCode2name"
    }
    
    test("should generate system dictionary translation code correctly") {
        val enhancer = JavaEntityEnhancer()
        
        val dslConfig = DslTemplateConfig(
            entityClass = "TestEntity",
            translationRules = listOf(
                EntityTranslationRule(
                    entityName = "TestEntity",
                    fieldRules = listOf(
                        FieldTranslationRule(
                            fieldName = "status",
                            translationType = TranslationType.SYSTEM_DICT,
                            dictConfigs = listOf(
                                DictConfig(
                                    type = TranslationType.SYSTEM_DICT,
                                    dictCode = "STATUS_CODE"
                                )
                            ),
                            targetFieldName = "statusText"
                        )
                    )
                )
            )
        )
        
        val systemDictCode = enhancer.generateSystemDictTranslation(dslConfig)
        
        println("Generated system dict code:")
        println(systemDictCode)
        
        systemDictCode shouldContain "String systemDictCodes = \"STATUS_CODE\";"
        systemDictCode shouldContain "this.getStatus()"
        systemDictCode shouldContain "transApi.translateDictBatchCode2name"
        systemDictCode shouldContain "this.setStatusText"
    }
    
    test("should generate table dictionary translation code correctly") {
        val enhancer = JavaEntityEnhancer()
        
        val dslConfig = DslTemplateConfig(
            entityClass = "TestEntity",
            translationRules = listOf(
                EntityTranslationRule(
                    entityName = "TestEntity",
                    fieldRules = listOf(
                        FieldTranslationRule(
                            fieldName = "roleId",
                            translationType = TranslationType.TABLE_DICT,
                            dictConfigs = listOf(
                                DictConfig(
                                    type = TranslationType.TABLE_DICT,
                                    table = "sys_role",
                                    codeColumn = "role_id",
                                    nameColumn = "role_name"
                                )
                            ),
                            targetFieldName = "roleText"
                        )
                    )
                )
            )
        )
        
        val tableDictCode = enhancer.generateTableDictTranslation(dslConfig)
        
        println("Generated table dict code:")
        println(tableDictCode)
        
        tableDictCode shouldContain "List<String> tableKeys_sys_role = new ArrayList<>();"
        tableDictCode shouldContain "this.getRoleId()"
        tableDictCode shouldContain "transApi.translateTableBatchCode2name(\"sys_role\", \"role_name\", \"role_id\""
        tableDictCode shouldContain "this.setRoleText"
    }
    
    test("should handle multiple nested levels correctly") {
        val enhancer = JavaEntityEnhancer()
        
        val dslConfig = DslTemplateConfig(
            entityClass = "ComplexEntity",
            translationRules = listOf(
                EntityTranslationRule(
                    entityName = "ComplexEntity",
                    fieldRules = listOf(
                        FieldTranslationRule(
                            fieldName = "user.department.type",
                            translationType = TranslationType.SYSTEM_DICT,
                            dictConfigs = listOf(
                                DictConfig(
                                    type = TranslationType.SYSTEM_DICT,
                                    dictCode = "DEPT_TYPE"
                                )
                            ),
                            targetFieldName = "user_department_typeText"
                        ),
                        FieldTranslationRule(
                            fieldName = "permissions[].module.category",
                            translationType = TranslationType.TABLE_DICT,
                            dictConfigs = listOf(
                                DictConfig(
                                    type = TranslationType.TABLE_DICT,
                                    table = "sys_module_category",
                                    codeColumn = "category_code",
                                    nameColumn = "category_name"
                                )
                            ),
                            targetFieldName = "permissions_list_module_categoryText"
                        )
                    )
                )
            )
        )
        
        val javaCode = enhancer.generateJavaClassCode(
            packageName = "com.example.complex",
            originalClassName = "ComplexEntity",
            enhancedClassName = "ComplexEntityEnhanced",
            dslConfig = dslConfig
        )
        
        // Verify complex nested field handling
        javaCode shouldContain "private String user_department_typeText;"
        javaCode shouldContain "private String permissions_list_module_categoryText;"
        
        // Verify getter/setter generation for complex names
        javaCode shouldContain "public String getUser_department_typeText()"
        javaCode shouldContain "public void setUser_department_typeText(String user_department_typeText)"
        javaCode shouldContain "public String getPermissions_list_module_categoryText()"
        javaCode shouldContain "public void setPermissions_list_module_categoryText(String permissions_list_module_categoryText)"
    }
})