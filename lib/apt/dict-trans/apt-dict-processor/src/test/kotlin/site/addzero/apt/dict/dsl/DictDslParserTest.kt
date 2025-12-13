package site.addzero.apt.dict.dsl

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

/**
 * Test cases for DictDslParser
 * 
 * Tests the DSL parsing functionality including:
 * - Basic entity parsing
 * - Field translation rules
 * - Nested and recursive structures
 * - Validation and error handling
 */
class DictDslParserTest {
    
    private lateinit var parser: DictDslParser
    
    @BeforeEach
    fun setUp() {
        parser = DictDslParser()
    }
    
    @Test
    fun `should parse simple entity with system dictionary`() {
        val dsl = """
            entity UserEntity {
                field status -> systemDict("user_status")
            }
        """.trimIndent()
        
        val rules = parser.parseDslTemplate(dsl)
        
        assertEquals(1, rules.size)
        val entityRule = rules[0]
        assertEquals("UserEntity", entityRule.entityName)
        assertEquals(1, entityRule.fieldRules.size)
        
        val fieldRule = entityRule.fieldRules[0]
        assertEquals("status", fieldRule.fieldName)
        assertEquals(TranslationType.SYSTEM_DICT, fieldRule.translationType)
        assertEquals(1, fieldRule.dictConfigs.size)
        assertEquals("user_status", fieldRule.dictConfigs[0].dictCode)
    }
    
    @Test
    fun `should parse entity with table dictionary`() {
        val dsl = """
            entity UserEntity {
                field departmentId -> tableDict("sys_department", "id", "dept_name")
            }
        """.trimIndent()
        
        val rules = parser.parseDslTemplate(dsl)
        
        assertEquals(1, rules.size)
        val entityRule = rules[0]
        val fieldRule = entityRule.fieldRules[0]
        
        assertEquals("departmentId", fieldRule.fieldName)
        assertEquals(TranslationType.TABLE_DICT, fieldRule.translationType)
        
        val dictConfig = fieldRule.dictConfigs[0]
        assertEquals("sys_department", dictConfig.table)
        assertEquals("id", dictConfig.codeColumn)
        assertEquals("dept_name", dictConfig.nameColumn)
    }
    
    @Test
    fun `should parse entity with table dictionary and WHERE condition`() {
        val dsl = """
            entity UserEntity {
                field resourceType -> tableDict("sys_resource_type", "type_code", "type_name") {
                    where "status = 1 AND is_active = true"
                }
            }
        """.trimIndent()
        
        val rules = parser.parseDslTemplate(dsl)
        
        val fieldRule = rules[0].fieldRules[0]
        val dictConfig = fieldRule.dictConfigs[0]
        
        assertEquals("sys_resource_type", dictConfig.table)
        assertEquals("type_code", dictConfig.codeColumn)
        assertEquals("type_name", dictConfig.nameColumn)
        assertEquals("status = 1 AND is_active = true", dictConfig.condition)
    }
    
    @Test
    fun `should parse entity with SPEL expression`() {
        val dsl = """
            entity BusinessEntity {
                field status -> spel("status == 1 ? '正常' : '禁用'")
            }
        """.trimIndent()
        
        val rules = parser.parseDslTemplate(dsl)
        
        val fieldRule = rules[0].fieldRules[0]
        assertEquals(TranslationType.SPEL, fieldRule.translationType)
        assertEquals("status == 1 ? '正常' : '禁用'", fieldRule.dictConfigs[0].spelExpression)
    }
    
    @Test
    fun `should parse entity with multiple dictionary configurations`() {
        val dsl = """
            entity RoleEntity {
                field roleType -> [
                    systemDict("role_type"),
                    tableDict("sys_role_type", "type_code", "type_desc")
                ]
            }
        """.trimIndent()
        
        val rules = parser.parseDslTemplate(dsl)
        
        val fieldRule = rules[0].fieldRules[0]
        assertEquals(TranslationType.MULTI_DICT, fieldRule.translationType)
        assertEquals(2, fieldRule.dictConfigs.size)
        
        val systemDict = fieldRule.dictConfigs[0]
        assertEquals(TranslationType.SYSTEM_DICT, systemDict.type)
        assertEquals("role_type", systemDict.dictCode)
        
        val tableDict = fieldRule.dictConfigs[1]
        assertEquals(TranslationType.TABLE_DICT, tableDict.type)
        assertEquals("sys_role_type", tableDict.table)
    }
    
    @Test
    fun `should parse entity with nested collection`() {
        val dsl = """
            entity UserEntity {
                nested roles -> collection<RoleEntity> {
                    field roleCode -> tableDict("sys_role", "role_code", "role_name")
                    field status -> systemDict("role_status")
                }
            }
        """.trimIndent()
        
        val rules = parser.parseDslTemplate(dsl)
        
        val entityRule = rules[0]
        assertEquals(1, entityRule.nestedRules.size)
        
        val nestedRule = entityRule.nestedRules[0]
        assertEquals("roles", nestedRule.fieldName)
        assertEquals("RoleEntity", nestedRule.targetType)
        assertTrue(nestedRule.isCollection)
        
        val nestedEntityRule = nestedRule.nestedRules
        assertEquals(2, nestedEntityRule.fieldRules.size)
    }
    
    @Test
    fun `should parse entity with recursive reference`() {
        val dsl = """
            entity OrganizationEntity {
                recursive parentOrg -> OrganizationEntity
                recursive childOrgs -> collection<OrganizationEntity>
            }
        """.trimIndent()
        
        val rules = parser.parseDslTemplate(dsl)
        
        val entityRule = rules[0]
        assertEquals(2, entityRule.recursiveRules.size)
        
        val parentRule = entityRule.recursiveRules[0]
        assertEquals("parentOrg", parentRule.fieldName)
        assertEquals("OrganizationEntity", parentRule.targetType)
        assertFalse(parentRule.isCollection)
        
        val childrenRule = entityRule.recursiveRules[1]
        assertEquals("childOrgs", childrenRule.fieldName)
        assertEquals("OrganizationEntity", childrenRule.targetType)
        assertTrue(childrenRule.isCollection)
    }
    
    @Test
    fun `should validate DSL syntax and return errors for invalid input`() {
        val invalidDsl = """
            entity {
                field -> systemDict("")
            }
        """.trimIndent()
        
        val errors = parser.validateDslSyntax(invalidDsl)
        
        assertTrue(errors.isNotEmpty())
        assertTrue(errors.any { it.type == ValidationErrorType.SYNTAX_ERROR })
    }
    
    @Test
    fun `should convert DSL rules to DictFieldInfo`() {
        val dsl = """
            entity UserEntity {
                field status -> systemDict("user_status")
                field departmentId -> tableDict("sys_department", "id", "dept_name")
            }
        """.trimIndent()
        
        val rules = parser.parseDslTemplate(dsl)
        val dictFields = parser.convertToDictFieldInfo(rules)
        
        assertEquals(2, dictFields.size)
        
        val systemDictField = dictFields.find { it.dictCode == "user_status" }
        assertNotNull(systemDictField)
        assertEquals("status", systemDictField!!.sourceField)
        assertEquals("statusText", systemDictField.targetField)
        
        val tableDictField = dictFields.find { it.table == "sys_department" }
        assertNotNull(tableDictField)
        assertEquals("departmentId", tableDictField!!.sourceField)
        assertEquals("departmentIdText", tableDictField.targetField)
        assertEquals("id", tableDictField.codeColumn)
        assertEquals("dept_name", tableDictField.nameColumn)
    }
    
    @Test
    fun `should handle complex RBAC scenario`() {
        val dsl = """
            entity UserEntity {
                field status -> systemDict("user_status")
                field departmentId -> tableDict("sys_department", "id", "dept_name")
                
                nested roles -> collection<RoleEntity> {
                    field roleCode -> tableDict("sys_role", "role_code", "role_name")
                    field status -> systemDict("role_status")
                }
                
                nested permissions -> collection<PermissionEntity> {
                    field permissionCode -> tableDict("sys_permission", "permission_code", "permission_name")
                    field resourceType -> tableDict("sys_resource_type", "type_code", "type_name") {
                        where "status = 1"
                    }
                }
            }
        """.trimIndent()
        
        val rules = parser.parseDslTemplate(dsl)
        
        assertEquals(1, rules.size)
        val entityRule = rules[0]
        
        // Check main entity fields
        assertEquals(2, entityRule.fieldRules.size)
        
        // Check nested rules
        assertEquals(2, entityRule.nestedRules.size)
        
        // Check roles nested rule
        val rolesRule = entityRule.nestedRules.find { it.fieldName == "roles" }
        assertNotNull(rolesRule)
        assertEquals("RoleEntity", rolesRule!!.targetType)
        assertTrue(rolesRule.isCollection)
        assertEquals(2, rolesRule.nestedRules.fieldRules.size)
        
        // Check permissions nested rule
        val permissionsRule = entityRule.nestedRules.find { it.fieldName == "permissions" }
        assertNotNull(permissionsRule)
        assertEquals("PermissionEntity", permissionsRule!!.targetType)
        assertTrue(permissionsRule.isCollection)
        assertEquals(2, permissionsRule.nestedRules.fieldRules.size)
        
        // Check WHERE condition in permissions
        val resourceTypeField = permissionsRule.nestedRules.fieldRules.find { it.fieldName == "resourceType" }
        assertNotNull(resourceTypeField)
        assertEquals("status = 1", resourceTypeField!!.dictConfigs[0].condition)
    }
    
    @Test
    fun `should validate entity rule completeness`() {
        val dsl = """
            entity UserEntity {
                field status -> systemDict("user_status")
                field departmentId -> tableDict("sys_department", "id", "dept_name")
            }
        """.trimIndent()
        
        val rules = parser.parseDslTemplate(dsl)
        val entityRule = rules[0]
        
        assertTrue(entityRule.hasSystemDictFields())
        assertTrue(entityRule.hasTableDictFields())
        
        val systemDictCodes = entityRule.getSystemDictCodes()
        assertEquals(1, systemDictCodes.size)
        assertTrue(systemDictCodes.contains("user_status"))
        
        val tableDictConfigs = entityRule.getTableDictConfigs()
        assertEquals(1, tableDictConfigs.size)
        val tableConfig = tableDictConfigs.first()
        assertEquals("sys_department", tableConfig.table)
        assertEquals("id", tableConfig.codeColumn)
        assertEquals("dept_name", tableConfig.nameColumn)
    }
}