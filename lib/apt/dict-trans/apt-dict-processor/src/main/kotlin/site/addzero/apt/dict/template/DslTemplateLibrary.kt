package site.addzero.apt.dict.template

import site.addzero.apt.dict.dsl.*

/**
 * Template library for common dictionary translation patterns
 * 
 * This library provides pre-built DSL configurations for common use cases:
 * 1. RBAC (Role-Based Access Control) patterns
 * 2. User management patterns
 * 3. Audit logging patterns
 * 4. System configuration patterns
 * 5. Multi-tenant patterns
 */
object DslTemplateLibrary {
    
    /**
     * RBAC User entity template with common dictionary translations
     */
    fun createRBACUserTemplate(): DslTemplateConfig {
        val userStatusRule = FieldTranslationRule(
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
        
        val departmentRule = FieldTranslationRule(
            fieldName = "departmentId",
            translationType = TranslationType.TABLE_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.TABLE_DICT,
                    table = "sys_department",
                    codeColumn = "id",
                    nameColumn = "name",
                    condition = "status = 'ACTIVE'"
                )
            ),
            targetFieldName = "departmentName"
        )
        
        val organizationRule = FieldTranslationRule(
            fieldName = "organizationId",
            translationType = TranslationType.TABLE_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.TABLE_DICT,
                    table = "sys_organization",
                    codeColumn = "id",
                    nameColumn = "full_name",
                    condition = "status = 'ACTIVE'"
                )
            ),
            targetFieldName = "organizationName"
        )
        
        val entityRule = EntityTranslationRule(
            entityName = "User",
            fieldRules = listOf(userStatusRule, departmentRule, organizationRule)
        )
        
        return DslTemplateConfig(
            entityClass = "User",
            translationRules = listOf(entityRule)
        )
    }
    
    /**
     * RBAC Role entity template
     */
    fun createRBACRoleTemplate(): DslTemplateConfig {
        val roleTypeRule = FieldTranslationRule(
            fieldName = "type",
            translationType = TranslationType.SYSTEM_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.SYSTEM_DICT,
                    dictCode = "role_type"
                )
            ),
            targetFieldName = "typeText"
        )
        
        val roleLevelRule = FieldTranslationRule(
            fieldName = "level",
            translationType = TranslationType.SYSTEM_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.SYSTEM_DICT,
                    dictCode = "role_level"
                )
            ),
            targetFieldName = "levelText"
        )
        
        val moduleRule = FieldTranslationRule(
            fieldName = "moduleId",
            translationType = TranslationType.TABLE_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.TABLE_DICT,
                    table = "sys_module",
                    codeColumn = "id",
                    nameColumn = "display_name",
                    condition = "enabled = true"
                )
            ),
            targetFieldName = "moduleName"
        )
        
        val entityRule = EntityTranslationRule(
            entityName = "Role",
            fieldRules = listOf(roleTypeRule, roleLevelRule, moduleRule)
        )
        
        return DslTemplateConfig(
            entityClass = "Role",
            translationRules = listOf(entityRule)
        )
    }
    
    /**
     * Permission entity template with SPEL expressions
     */
    fun createPermissionTemplate(): DslTemplateConfig {
        val permissionTypeRule = FieldTranslationRule(
            fieldName = "type",
            translationType = TranslationType.SYSTEM_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.SYSTEM_DICT,
                    dictCode = "permission_type"
                )
            ),
            targetFieldName = "typeText"
        )
        
        val resourceCategoryRule = FieldTranslationRule(
            fieldName = "resource",
            translationType = TranslationType.SPEL,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.SPEL,
                    spelExpression = "#{dict('resource_category', resource)}"
                )
            ),
            targetFieldName = "resourceCategoryText"
        )
        
        val actionRule = FieldTranslationRule(
            fieldName = "action",
            translationType = TranslationType.SPEL,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.SPEL,
                    spelExpression = "#{table('sys_action', 'code', 'display_name', action)}"
                )
            ),
            targetFieldName = "actionText"
        )
        
        val entityRule = EntityTranslationRule(
            entityName = "Permission",
            fieldRules = listOf(permissionTypeRule, resourceCategoryRule, actionRule)
        )
        
        return DslTemplateConfig(
            entityClass = "Permission",
            translationRules = listOf(entityRule)
        )
    }
    
    /**
     * Audit log template with comprehensive translations
     */
    fun createAuditLogTemplate(): DslTemplateConfig {
        val operatorRule = FieldTranslationRule(
            fieldName = "operatorId",
            translationType = TranslationType.TABLE_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.TABLE_DICT,
                    table = "rbac_user",
                    codeColumn = "id",
                    nameColumn = "username",
                    condition = "status != 'DELETED'"
                )
            ),
            targetFieldName = "operatorName"
        )
        
        val actionRule = FieldTranslationRule(
            fieldName = "action",
            translationType = TranslationType.SYSTEM_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.SYSTEM_DICT,
                    dictCode = "audit_action"
                )
            ),
            targetFieldName = "actionText"
        )
        
        val resultRule = FieldTranslationRule(
            fieldName = "result",
            translationType = TranslationType.SYSTEM_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.SYSTEM_DICT,
                    dictCode = "audit_result"
                )
            ),
            targetFieldName = "resultText"
        )
        
        val resourceRule = FieldTranslationRule(
            fieldName = "resourceId",
            translationType = TranslationType.TABLE_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.TABLE_DICT,
                    table = "sys_resource",
                    codeColumn = "id",
                    nameColumn = "display_name",
                    condition = "status = 'ACTIVE'"
                )
            ),
            targetFieldName = "resourceName"
        )
        
        val severityRule = FieldTranslationRule(
            fieldName = "result",
            translationType = TranslationType.SPEL,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.SPEL,
                    spelExpression = "#{dict('severity_level', result)}"
                )
            ),
            targetFieldName = "severityText"
        )
        
        val entityRule = EntityTranslationRule(
            entityName = "AuditLog",
            fieldRules = listOf(operatorRule, actionRule, resultRule, resourceRule, severityRule)
        )
        
        return DslTemplateConfig(
            entityClass = "AuditLog",
            translationRules = listOf(entityRule)
        )
    }
    
    /**
     * Multi-dictionary field template (multiple translation sources for one field)
     */
    fun createMultiDictTemplate(): DslTemplateConfig {
        val multiDictRule = FieldTranslationRule(
            fieldName = "categoryCode",
            translationType = TranslationType.MULTI_DICT,
            dictConfigs = listOf(
                // Try system dictionary first
                DictConfig(
                    type = TranslationType.SYSTEM_DICT,
                    dictCode = "category_type"
                ),
                // Fallback to table dictionary
                DictConfig(
                    type = TranslationType.TABLE_DICT,
                    table = "sys_category",
                    codeColumn = "code",
                    nameColumn = "name"
                ),
                // Final fallback with SPEL
                DictConfig(
                    type = TranslationType.SPEL,
                    spelExpression = "#{categoryCode + ' (Unknown)'}"
                )
            ),
            targetFieldName = "categoryText"
        )
        
        val entityRule = EntityTranslationRule(
            entityName = "Category",
            fieldRules = listOf(multiDictRule)
        )
        
        return DslTemplateConfig(
            entityClass = "Category",
            translationRules = listOf(entityRule)
        )
    }
    
    /**
     * System configuration template
     */
    fun createSystemConfigTemplate(): DslTemplateConfig {
        val configTypeRule = FieldTranslationRule(
            fieldName = "type",
            translationType = TranslationType.SYSTEM_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.SYSTEM_DICT,
                    dictCode = "config_type"
                )
            ),
            targetFieldName = "typeText"
        )
        
        val moduleRule = FieldTranslationRule(
            fieldName = "moduleId",
            translationType = TranslationType.TABLE_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.TABLE_DICT,
                    table = "sys_module",
                    codeColumn = "id",
                    nameColumn = "display_name"
                )
            ),
            targetFieldName = "moduleName"
        )
        
        val environmentRule = FieldTranslationRule(
            fieldName = "environment",
            translationType = TranslationType.SYSTEM_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.SYSTEM_DICT,
                    dictCode = "environment_type"
                )
            ),
            targetFieldName = "environmentText"
        )
        
        val entityRule = EntityTranslationRule(
            entityName = "SystemConfig",
            fieldRules = listOf(configTypeRule, moduleRule, environmentRule)
        )
        
        return DslTemplateConfig(
            entityClass = "SystemConfig",
            translationRules = listOf(entityRule)
        )
    }
    
    /**
     * Multi-tenant entity template
     */
    fun createMultiTenantTemplate(): DslTemplateConfig {
        val tenantRule = FieldTranslationRule(
            fieldName = "tenantId",
            translationType = TranslationType.TABLE_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.TABLE_DICT,
                    table = "sys_tenant",
                    codeColumn = "id",
                    nameColumn = "name",
                    condition = "status = 'ACTIVE'"
                )
            ),
            targetFieldName = "tenantName"
        )
        
        val statusRule = FieldTranslationRule(
            fieldName = "status",
            translationType = TranslationType.SYSTEM_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.SYSTEM_DICT,
                    dictCode = "tenant_status"
                )
            ),
            targetFieldName = "statusText"
        )
        
        val regionRule = FieldTranslationRule(
            fieldName = "regionCode",
            translationType = TranslationType.TABLE_DICT,
            dictConfigs = listOf(
                DictConfig(
                    type = TranslationType.TABLE_DICT,
                    table = "sys_region",
                    codeColumn = "code",
                    nameColumn = "display_name"
                )
            ),
            targetFieldName = "regionName"
        )
        
        val entityRule = EntityTranslationRule(
            entityName = "TenantEntity",
            fieldRules = listOf(tenantRule, statusRule, regionRule)
        )
        
        return DslTemplateConfig(
            entityClass = "TenantEntity",
            translationRules = listOf(entityRule)
        )
    }
    
    /**
     * Gets all available templates
     */
    fun getAllTemplates(): Map<String, DslTemplateConfig> {
        return mapOf(
            "rbac_user" to createRBACUserTemplate(),
            "rbac_role" to createRBACRoleTemplate(),
            "permission" to createPermissionTemplate(),
            "audit_log" to createAuditLogTemplate(),
            "multi_dict" to createMultiDictTemplate(),
            "system_config" to createSystemConfigTemplate(),
            "multi_tenant" to createMultiTenantTemplate()
        )
    }
    
    /**
     * Gets template by name
     */
    fun getTemplate(name: String): DslTemplateConfig? {
        return getAllTemplates()[name]
    }
    
    /**
     * Creates a custom template based on common patterns
     */
    fun createCustomTemplate(
        entityName: String,
        systemDictFields: Map<String, String> = emptyMap(),
        tableDictFields: Map<String, TableDictInfo> = emptyMap(),
        spelFields: Map<String, String> = emptyMap()
    ): DslTemplateConfig {
        val fieldRules = mutableListOf<FieldTranslationRule>()
        
        // Add system dictionary fields
        systemDictFields.forEach { (fieldName, dictCode) ->
            fieldRules.add(
                FieldTranslationRule(
                    fieldName = fieldName,
                    translationType = TranslationType.SYSTEM_DICT,
                    dictConfigs = listOf(
                        DictConfig(
                            type = TranslationType.SYSTEM_DICT,
                            dictCode = dictCode
                        )
                    ),
                    targetFieldName = "${fieldName}Text"
                )
            )
        }
        
        // Add table dictionary fields
        tableDictFields.forEach { (fieldName, tableInfo) ->
            fieldRules.add(
                FieldTranslationRule(
                    fieldName = fieldName,
                    translationType = TranslationType.TABLE_DICT,
                    dictConfigs = listOf(
                        DictConfig(
                            type = TranslationType.TABLE_DICT,
                            table = tableInfo.table,
                            codeColumn = tableInfo.codeColumn,
                            nameColumn = tableInfo.nameColumn,
                            condition = tableInfo.condition
                        )
                    ),
                    targetFieldName = "${fieldName}Name"
                )
            )
        }
        
        // Add SPEL fields
        spelFields.forEach { (fieldName, expression) ->
            fieldRules.add(
                FieldTranslationRule(
                    fieldName = fieldName,
                    translationType = TranslationType.SPEL,
                    dictConfigs = listOf(
                        DictConfig(
                            type = TranslationType.SPEL,
                            spelExpression = expression
                        )
                    ),
                    targetFieldName = "${fieldName}Text"
                )
            )
        }
        
        val entityRule = EntityTranslationRule(
            entityName = entityName,
            fieldRules = fieldRules
        )
        
        return DslTemplateConfig(
            entityClass = entityName,
            translationRules = listOf(entityRule)
        )
    }
}

/**
 * Template validation utilities
 */
object DslTemplateValidator {
    
    /**
     * Validates a DSL template configuration
     */
    fun validateTemplate(config: DslTemplateConfig): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        
        // Validate entity class name
        if (config.entityClass.isBlank()) {
            errors.add(ValidationError("Entity class name cannot be blank"))
        }
        
        // Validate translation rules
        config.translationRules.forEach { rule ->
            errors.addAll(validateEntityRule(rule))
        }
        
        return errors
    }
    
    private fun validateEntityRule(rule: EntityTranslationRule): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        
        // Check for duplicate target fields
        val targetFields = rule.fieldRules.map { it.targetFieldName }
        val duplicates = targetFields.groupBy { it }.filter { it.value.size > 1 }
        duplicates.forEach { (targetField, _) ->
            errors.add(ValidationError("Duplicate target field: $targetField"))
        }
        
        // Validate each field rule
        rule.fieldRules.forEach { fieldRule ->
            errors.addAll(validateFieldRule(fieldRule))
        }
        
        return errors
    }
    
    private fun validateFieldRule(rule: FieldTranslationRule): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        
        // Validate field names
        if (rule.fieldName.isBlank()) {
            errors.add(ValidationError("Field name cannot be blank"))
        }
        
        if (rule.targetFieldName.isBlank()) {
            errors.add(ValidationError("Target field name cannot be blank"))
        }
        
        // Validate dictionary configurations
        if (rule.dictConfigs.isEmpty()) {
            errors.add(ValidationError("Field ${rule.fieldName} must have at least one dictionary configuration"))
        }
        
        rule.dictConfigs.forEach { dictConfig ->
            errors.addAll(validateDictConfig(dictConfig, rule.fieldName))
        }
        
        return errors
    }
    
    private fun validateDictConfig(config: DictConfig, fieldName: String): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        
        when (config.type) {
            TranslationType.SYSTEM_DICT -> {
                if (config.dictCode.isBlank()) {
                    errors.add(ValidationError("System dictionary for field $fieldName must specify dictCode"))
                }
            }
            TranslationType.TABLE_DICT -> {
                if (config.table.isBlank()) {
                    errors.add(ValidationError("Table dictionary for field $fieldName must specify table"))
                }
                if (config.codeColumn.isBlank()) {
                    errors.add(ValidationError("Table dictionary for field $fieldName must specify codeColumn"))
                }
                if (config.nameColumn.isBlank()) {
                    errors.add(ValidationError("Table dictionary for field $fieldName must specify nameColumn"))
                }
            }
            TranslationType.SPEL -> {
                if (config.spelExpression.isBlank()) {
                    errors.add(ValidationError("SPEL dictionary for field $fieldName must specify spelExpression"))
                }
            }
            else -> {
                // Multi-dict validation would be more complex
            }
        }
        
        return errors
    }
}

/**
 * Validation error for DSL templates
 */
data class ValidationError(val message: String)