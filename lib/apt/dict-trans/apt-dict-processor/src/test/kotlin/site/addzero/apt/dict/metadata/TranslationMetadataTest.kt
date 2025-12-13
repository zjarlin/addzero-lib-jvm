package site.addzero.apt.dict.metadata

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import site.addzero.apt.dict.model.TranslationType

/**
 * Unit tests for TranslationMetadata and related classes
 * 
 * Tests the metadata generation functionality for translation context optimization
 */
class TranslationMetadataTest {
    
    @Test
    fun `test translation metadata creation`() {
        val dictFields = listOf(
            DictFieldMetadata(
                sourceField = "status",
                targetField = "statusName",
                dictCode = "user_status"
            ),
            DictFieldMetadata(
                sourceField = "deptId",
                targetField = "deptName",
                table = "sys_dept",
                codeColumn = "id",
                nameColumn = "name"
            )
        )
        
        val metadata = TranslationMetadata(
            entityClass = String::class,
            enhancedClass = String::class,
            dictFields = dictFields
        )
        
        assertEquals(String::class, metadata.entityClass)
        assertEquals(String::class, metadata.enhancedClass)
        assertEquals(2, metadata.dictFields.size)
    }
    
    @Test
    fun `test get system dict codes`() {
        val dictFields = listOf(
            DictFieldMetadata(
                sourceField = "status",
                targetField = "statusName",
                dictCode = "user_status"
            ),
            DictFieldMetadata(
                sourceField = "type",
                targetField = "typeName",
                dictCode = "user_type"
            ),
            DictFieldMetadata(
                sourceField = "deptId",
                targetField = "deptName",
                table = "sys_dept",
                codeColumn = "id",
                nameColumn = "name"
            )
        )
        
        val metadata = TranslationMetadata(
            entityClass = String::class,
            enhancedClass = String::class,
            dictFields = dictFields
        )
        
        val systemDictCodes = metadata.getSystemDictCodes()
        assertEquals(2, systemDictCodes.size)
        assertTrue(systemDictCodes.contains("user_status"))
        assertTrue(systemDictCodes.contains("user_type"))
    }
    
    @Test
    fun `test get table dict configs`() {
        val dictFields = listOf(
            DictFieldMetadata(
                sourceField = "status",
                targetField = "statusName",
                dictCode = "user_status"
            ),
            DictFieldMetadata(
                sourceField = "deptId",
                targetField = "deptName",
                table = "sys_dept",
                codeColumn = "id",
                nameColumn = "name"
            ),
            DictFieldMetadata(
                sourceField = "roleId",
                targetField = "roleName",
                table = "sys_role",
                codeColumn = "role_id",
                nameColumn = "role_name"
            )
        )
        
        val metadata = TranslationMetadata(
            entityClass = String::class,
            enhancedClass = String::class,
            dictFields = dictFields
        )
        
        val tableDictConfigs = metadata.getTableDictConfigs()
        assertEquals(2, tableDictConfigs.size)
        
        val deptConfig = tableDictConfigs.find { it.table == "sys_dept" }
        assertNotNull(deptConfig)
        assertEquals("id", deptConfig!!.codeColumn)
        assertEquals("name", deptConfig.nameColumn)
        
        val roleConfig = tableDictConfigs.find { it.table == "sys_role" }
        assertNotNull(roleConfig)
        assertEquals("role_id", roleConfig!!.codeColumn)
        assertEquals("role_name", roleConfig.nameColumn)
    }
    
    @Test
    fun `test get spel expressions`() {
        val dictFields = listOf(
            DictFieldMetadata(
                sourceField = "status",
                targetField = "statusName",
                dictCode = "user_status"
            ),
            DictFieldMetadata(
                sourceField = "complexField",
                targetField = "complexFieldName",
                spelExp = "#{someService.translate(#root.complexField)}"
            ),
            DictFieldMetadata(
                sourceField = "anotherField",
                targetField = "anotherFieldName",
                spelExp = "#{anotherService.process(#root.anotherField)}"
            )
        )
        
        val metadata = TranslationMetadata(
            entityClass = String::class,
            enhancedClass = String::class,
            dictFields = dictFields
        )
        
        val spelExpressions = metadata.getSpelExpressions()
        assertEquals(2, spelExpressions.size)
        assertTrue(spelExpressions.contains("#{someService.translate(#root.complexField)}"))
        assertTrue(spelExpressions.contains("#{anotherService.process(#root.anotherField)}"))
    }
    
    @Test
    fun `test requires batch translation`() {
        // Single field - should not require batch translation by default
        val singleFieldMetadata = TranslationMetadata(
            entityClass = String::class,
            enhancedClass = String::class,
            dictFields = listOf(
                DictFieldMetadata(
                    sourceField = "status",
                    targetField = "statusName",
                    dictCode = "user_status"
                )
            )
        )
        assertFalse(singleFieldMetadata.requiresBatchTranslation())
        
        // Multiple fields - should require batch translation
        val multipleFieldsMetadata = TranslationMetadata(
            entityClass = String::class,
            enhancedClass = String::class,
            dictFields = listOf(
                DictFieldMetadata(
                    sourceField = "status",
                    targetField = "statusName",
                    dictCode = "user_status"
                ),
                DictFieldMetadata(
                    sourceField = "type",
                    targetField = "typeName",
                    dictCode = "user_type"
                )
            )
        )
        assertTrue(multipleFieldsMetadata.requiresBatchTranslation())
        
        // Force batch translation
        val forcedBatchMetadata = TranslationMetadata(
            entityClass = String::class,
            enhancedClass = String::class,
            dictFields = listOf(
                DictFieldMetadata(
                    sourceField = "status",
                    targetField = "statusName",
                    dictCode = "user_status"
                )
            ),
            config = TranslationConfig(forceBatchTranslation = true)
        )
        assertTrue(forcedBatchMetadata.requiresBatchTranslation())
    }
    
    @Test
    fun `test complexity score calculation`() {
        val dictFields = listOf(
            // System dict (score: 1)
            DictFieldMetadata(
                sourceField = "status",
                targetField = "statusName",
                dictCode = "user_status"
            ),
            // Table dict (score: 2)
            DictFieldMetadata(
                sourceField = "deptId",
                targetField = "deptName",
                table = "sys_dept",
                codeColumn = "id",
                nameColumn = "name"
            ),
            // SPEL expression (score: 3)
            DictFieldMetadata(
                sourceField = "complexField",
                targetField = "complexFieldName",
                spelExp = "#{someService.translate(#root.complexField)}"
            )
        )
        
        val metadata = TranslationMetadata(
            entityClass = String::class,
            enhancedClass = String::class,
            dictFields = dictFields
        )
        
        // Expected score: 1 (system dict) + 2 (table dict) + 3 (spel) = 6
        assertEquals(6, metadata.getComplexityScore())
    }
    
    @Test
    fun `test dict field metadata validation`() {
        // Valid system dict field
        val systemDictField = DictFieldMetadata(
            sourceField = "status",
            targetField = "statusName",
            dictCode = "user_status"
        )
        assertTrue(systemDictField.validate().isEmpty())
        assertEquals(TranslationType.SYSTEM_DICT, systemDictField.getTranslationType())
        
        // Valid table dict field
        val tableDictField = DictFieldMetadata(
            sourceField = "deptId",
            targetField = "deptName",
            table = "sys_dept",
            codeColumn = "id",
            nameColumn = "name"
        )
        assertTrue(tableDictField.validate().isEmpty())
        assertEquals(TranslationType.TABLE_DICT, tableDictField.getTranslationType())
        
        // Valid SPEL field
        val spelField = DictFieldMetadata(
            sourceField = "complexField",
            targetField = "complexFieldName",
            spelExp = "#{someService.translate(#root.complexField)}"
        )
        assertTrue(spelField.validate().isEmpty())
        assertEquals(TranslationType.SPEL_EXPRESSION, spelField.getTranslationType())
        
        // Invalid field - no translation configuration
        val invalidField = DictFieldMetadata(
            sourceField = "invalidField",
            targetField = "invalidFieldName"
        )
        val errors = invalidField.validate()
        assertFalse(errors.isEmpty())
        assertEquals(TranslationType.UNKNOWN, invalidField.getTranslationType())
        
        // Invalid table dict field - missing required fields
        val invalidTableField = DictFieldMetadata(
            sourceField = "deptId",
            targetField = "deptName",
            table = "sys_dept"
            // Missing codeColumn and nameColumn
        )
        val tableErrors = invalidTableField.validate()
        assertFalse(tableErrors.isEmpty())
        assertTrue(tableErrors.any { it.contains("codeColumn") })
        assertTrue(tableErrors.any { it.contains("nameColumn") })
    }
    
    @Test
    fun `test table dict config batch query generation`() {
        val config = TableDictConfig(
            table = "sys_dept",
            codeColumn = "id",
            nameColumn = "name"
        )
        
        val codes = setOf("1", "2", "3")
        val query = config.generateBatchQuery(codes)
        
        assertTrue(query.contains("SELECT id, name FROM sys_dept"))
        assertTrue(query.contains("WHERE id IN ('1','2','3')"))
    }
    
    @Test
    fun `test translation config defaults`() {
        val config = TranslationConfig()
        
        assertFalse(config.forceBatchTranslation)
        assertEquals(100, config.batchSize)
        assertTrue(config.enableCaching)
        assertEquals(300, config.cacheExpirationSeconds)
        assertTrue(config.enableConcurrentTranslation)
        assertEquals(4, config.maxConcurrentThreads)
    }
}