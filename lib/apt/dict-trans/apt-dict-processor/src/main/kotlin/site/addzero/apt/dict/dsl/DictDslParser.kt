package site.addzero.apt.dict.dsl

import site.addzero.apt.dict.processor.DictFieldInfo
import java.util.regex.Pattern

/**
 * DSL Parser for dictionary translation rules
 * 
 * This parser processes DSL template definitions and converts them into
 * intermediate representation for code generation. It supports RBAC and
 * complex nested scenarios with intuitive syntax.
 * 
 * DSL Syntax Example:
 * ```
 * entity UserEntity {
 *     field status -> systemDict("user_status")
 *     field departmentId -> tableDict("sys_department", "id", "dept_name")
 *     
 *     nested roles -> collection<RoleEntity> {
 *         field roleCode -> tableDict("sys_role", "role_code", "role_name")
 *         field status -> systemDict("role_status")
 *     }
 * }
 * ```
 */
class DictDslParser {
    
    companion object {
        // DSL syntax patterns
        private val ENTITY_PATTERN = Pattern.compile(
            "entity\\s+(\\w+)\\s*\\{([^}]*)\\}"
        )
        
        private val FIELD_PATTERN = Pattern.compile(
            "field\\s+(\\w+)\\s*->\\s*(.+)"
        )
        
        private val NESTED_PATTERN = Pattern.compile(
            "nested\\s+(\\w+)\\s*->\\s*(collection<(\\w+)>|(\\w+))\\s*\\{([^}]*)\\}"
        )
        
        private val RECURSIVE_PATTERN = Pattern.compile(
            "recursive\\s+(\\w+)\\s*->\\s*(collection<(\\w+)>|(\\w+))"
        )
        
        private val SYSTEM_DICT_PATTERN = Pattern.compile(
            "systemDict\\(\"([^\"]+)\"\\)"
        )
        
        private val TABLE_DICT_PATTERN = Pattern.compile(
            "tableDict\\(\"([^\"]+)\",\\s*\"([^\"]+)\",\\s*\"([^\"]+)\"\\)(?:\\s*\\{\\s*where\\s+\"([^\"]+)\"\\s*\\})?"
        )
        
        private val SPEL_PATTERN = Pattern.compile(
            "spel\\(\"([^\"]+)\"\\)"
        )
        
        private val MULTI_DICT_PATTERN = Pattern.compile(
            "\\[([^\\]]+)\\]"
        )
    }
    
    /**
     * Parses DSL template content and returns entity translation rules
     */
    fun parseDslTemplate(dslContent: String): List<EntityTranslationRule> {
        val rules = mutableListOf<EntityTranslationRule>()
        val entityMatcher = ENTITY_PATTERN.matcher(dslContent)
        
        while (entityMatcher.find()) {
            val entityName = entityMatcher.group(1)
            val entityBody = entityMatcher.group(2)
            
            val rule = parseEntityRule(entityName, entityBody)
            rules.add(rule)
        }
        
        return rules
    }
    
    /**
     * Parses a single entity rule
     */
    private fun parseEntityRule(entityName: String, entityBody: String): EntityTranslationRule {
        val fieldRules = mutableListOf<FieldTranslationRule>()
        val nestedRules = mutableListOf<NestedTranslationRule>()
        val recursiveRules = mutableListOf<RecursiveTranslationRule>()
        
        // Parse field rules
        val fieldMatcher = FIELD_PATTERN.matcher(entityBody)
        while (fieldMatcher.find()) {
            val fieldName = fieldMatcher.group(1)
            val translationExpr = fieldMatcher.group(2).trim()
            
            val fieldRule = parseFieldTranslationRule(fieldName, translationExpr)
            fieldRules.add(fieldRule)
        }
        
        // Parse nested rules
        val nestedMatcher = NESTED_PATTERN.matcher(entityBody)
        while (nestedMatcher.find()) {
            val fieldName = nestedMatcher.group(1)
            val typeExpr = nestedMatcher.group(2)
            val nestedBody = nestedMatcher.group(5)
            
            val isCollection = typeExpr.startsWith("collection<")
            val targetType = if (isCollection) {
                nestedMatcher.group(3)
            } else {
                nestedMatcher.group(4)
            }
            
            val nestedRule = NestedTranslationRule(
                fieldName = fieldName,
                targetType = targetType,
                isCollection = isCollection,
                nestedRules = parseEntityRule(targetType, nestedBody)
            )
            nestedRules.add(nestedRule)
        }
        
        // Parse recursive rules
        val recursiveMatcher = RECURSIVE_PATTERN.matcher(entityBody)
        while (recursiveMatcher.find()) {
            val fieldName = recursiveMatcher.group(1)
            val typeExpr = recursiveMatcher.group(2)
            
            val isCollection = typeExpr.startsWith("collection<")
            val targetType = if (isCollection) {
                recursiveMatcher.group(3)
            } else {
                recursiveMatcher.group(4)
            }
            
            val recursiveRule = RecursiveTranslationRule(
                fieldName = fieldName,
                targetType = targetType,
                isCollection = isCollection
            )
            recursiveRules.add(recursiveRule)
        }
        
        return EntityTranslationRule(
            entityName = entityName,
            fieldRules = fieldRules,
            nestedRules = nestedRules,
            recursiveRules = recursiveRules
        )
    }
    
    /**
     * Parses field translation rule expression
     */
    private fun parseFieldTranslationRule(fieldName: String, translationExpr: String): FieldTranslationRule {
        // Handle multi-dictionary expressions [dict1, dict2, ...]
        val multiDictMatcher = MULTI_DICT_PATTERN.matcher(translationExpr)
        if (multiDictMatcher.find()) {
            val dictExprs = multiDictMatcher.group(1).split(",").map { it.trim() }
            val dictConfigs = dictExprs.map { parseSingleDictExpression(it) }
            
            return FieldTranslationRule(
                fieldName = fieldName,
                translationType = TranslationType.MULTI_DICT,
                dictConfigs = dictConfigs
            )
        }
        
        // Handle single dictionary expression
        val dictConfig = parseSingleDictExpression(translationExpr)
        return FieldTranslationRule(
            fieldName = fieldName,
            translationType = dictConfig.type,
            dictConfigs = listOf(dictConfig)
        )
    }
    
    /**
     * Parses a single dictionary expression
     */
    private fun parseSingleDictExpression(expr: String): DictConfig {
        // System dictionary
        val systemDictMatcher = SYSTEM_DICT_PATTERN.matcher(expr)
        if (systemDictMatcher.find()) {
            return DictConfig(
                type = TranslationType.SYSTEM_DICT,
                dictCode = systemDictMatcher.group(1)
            )
        }
        
        // Table dictionary
        val tableDictMatcher = TABLE_DICT_PATTERN.matcher(expr)
        if (tableDictMatcher.find()) {
            return DictConfig(
                type = TranslationType.TABLE_DICT,
                table = tableDictMatcher.group(1),
                codeColumn = tableDictMatcher.group(2),
                nameColumn = tableDictMatcher.group(3),
                condition = tableDictMatcher.group(4) ?: ""
            )
        }
        
        // SPEL expression
        val spelMatcher = SPEL_PATTERN.matcher(expr)
        if (spelMatcher.find()) {
            return DictConfig(
                type = TranslationType.SPEL,
                spelExpression = spelMatcher.group(1)
            )
        }
        
        throw DslParseException("Invalid dictionary expression: $expr")
    }
    
    /**
     * Validates DSL syntax and returns validation errors
     */
    fun validateDslSyntax(dslContent: String): List<DslValidationError> {
        val errors = mutableListOf<DslValidationError>()
        
        try {
            val rules = parseDslTemplate(dslContent)
            
            // Validate entity rules
            rules.forEach { rule ->
                validateEntityRule(rule, errors)
            }
            
        } catch (e: DslParseException) {
            errors.add(DslValidationError(
                type = ValidationErrorType.SYNTAX_ERROR,
                message = e.message ?: "Unknown syntax error",
                line = -1,
                column = -1
            ))
        }
        
        return errors
    }
    
    /**
     * Validates a single entity rule
     */
    private fun validateEntityRule(rule: EntityTranslationRule, errors: MutableList<DslValidationError>) {
        // Validate entity name
        if (rule.entityName.isBlank()) {
            errors.add(DslValidationError(
                type = ValidationErrorType.INVALID_ENTITY_NAME,
                message = "Entity name cannot be blank",
                line = -1,
                column = -1
            ))
        }
        
        // Validate field rules
        rule.fieldRules.forEach { fieldRule ->
            validateFieldRule(fieldRule, errors)
        }
        
        // Validate nested rules
        rule.nestedRules.forEach { nestedRule ->
            validateNestedRule(nestedRule, errors)
        }
        
        // Validate recursive rules
        rule.recursiveRules.forEach { recursiveRule ->
            validateRecursiveRule(recursiveRule, errors)
        }
    }
    
    /**
     * Validates a field translation rule
     */
    private fun validateFieldRule(rule: FieldTranslationRule, errors: MutableList<DslValidationError>) {
        if (rule.fieldName.isBlank()) {
            errors.add(DslValidationError(
                type = ValidationErrorType.INVALID_FIELD_NAME,
                message = "Field name cannot be blank",
                line = -1,
                column = -1
            ))
        }
        
        if (rule.dictConfigs.isEmpty()) {
            errors.add(DslValidationError(
                type = ValidationErrorType.MISSING_DICT_CONFIG,
                message = "Field ${rule.fieldName} must have at least one dictionary configuration",
                line = -1,
                column = -1
            ))
        }
        
        rule.dictConfigs.forEach { config ->
            validateDictConfig(config, errors)
        }
    }
    
    /**
     * Validates a dictionary configuration
     */
    private fun validateDictConfig(config: DictConfig, errors: MutableList<DslValidationError>) {
        when (config.type) {
            TranslationType.SYSTEM_DICT -> {
                if (config.dictCode.isBlank()) {
                    errors.add(DslValidationError(
                        type = ValidationErrorType.INVALID_DICT_CODE,
                        message = "System dictionary code cannot be blank",
                        line = -1,
                        column = -1
                    ))
                }
            }
            TranslationType.TABLE_DICT -> {
                if (config.table.isBlank()) {
                    errors.add(DslValidationError(
                        type = ValidationErrorType.INVALID_TABLE_NAME,
                        message = "Table name cannot be blank",
                        line = -1,
                        column = -1
                    ))
                }
                if (config.codeColumn.isBlank()) {
                    errors.add(DslValidationError(
                        type = ValidationErrorType.INVALID_COLUMN_NAME,
                        message = "Code column name cannot be blank",
                        line = -1,
                        column = -1
                    ))
                }
                if (config.nameColumn.isBlank()) {
                    errors.add(DslValidationError(
                        type = ValidationErrorType.INVALID_COLUMN_NAME,
                        message = "Name column name cannot be blank",
                        line = -1,
                        column = -1
                    ))
                }
            }
            TranslationType.SPEL -> {
                if (config.spelExpression.isBlank()) {
                    errors.add(DslValidationError(
                        type = ValidationErrorType.INVALID_SPEL_EXPRESSION,
                        message = "SPEL expression cannot be blank",
                        line = -1,
                        column = -1
                    ))
                }
            }
            else -> {
                // Multi-dict validation is handled at field level
            }
        }
    }
    
    /**
     * Validates a nested translation rule
     */
    private fun validateNestedRule(rule: NestedTranslationRule, errors: MutableList<DslValidationError>) {
        if (rule.fieldName.isBlank()) {
            errors.add(DslValidationError(
                type = ValidationErrorType.INVALID_FIELD_NAME,
                message = "Nested field name cannot be blank",
                line = -1,
                column = -1
            ))
        }
        
        if (rule.targetType.isBlank()) {
            errors.add(DslValidationError(
                type = ValidationErrorType.INVALID_TYPE_NAME,
                message = "Nested target type cannot be blank",
                line = -1,
                column = -1
            ))
        }
        
        // Recursively validate nested rules
        validateEntityRule(rule.nestedRules, errors)
    }
    
    /**
     * Validates a recursive translation rule
     */
    private fun validateRecursiveRule(rule: RecursiveTranslationRule, errors: MutableList<DslValidationError>) {
        if (rule.fieldName.isBlank()) {
            errors.add(DslValidationError(
                type = ValidationErrorType.INVALID_FIELD_NAME,
                message = "Recursive field name cannot be blank",
                line = -1,
                column = -1
            ))
        }
        
        if (rule.targetType.isBlank()) {
            errors.add(DslValidationError(
                type = ValidationErrorType.INVALID_TYPE_NAME,
                message = "Recursive target type cannot be blank",
                line = -1,
                column = -1
            ))
        }
    }
    
    /**
     * Converts DSL rules to DictFieldInfo for code generation
     */
    fun convertToDictFieldInfo(rules: List<EntityTranslationRule>): List<DictFieldInfo> {
        val dictFields = mutableListOf<DictFieldInfo>()
        
        rules.forEach { entityRule ->
            entityRule.fieldRules.forEach { fieldRule ->
                fieldRule.dictConfigs.forEach { config ->
                    val dictField = DictFieldInfo(
                        sourceField = fieldRule.fieldName,
                        targetField = "${fieldRule.fieldName}Text",
                        dictCode = config.dictCode,
                        table = config.table,
                        codeColumn = config.codeColumn,
                        nameColumn = config.nameColumn,
                        spelExp = config.spelExpression,
                        condition = config.condition
                    )
                    dictFields.add(dictField)
                }
            }
        }
        
        return dictFields
    }
}

/**
 * Exception thrown when DSL parsing fails
 */
class DslParseException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * DSL validation error
 */
data class DslValidationError(
    val type: ValidationErrorType,
    val message: String,
    val line: Int,
    val column: Int
)

/**
 * Types of validation errors
 */
enum class ValidationErrorType {
    SYNTAX_ERROR,
    INVALID_ENTITY_NAME,
    INVALID_FIELD_NAME,
    INVALID_TYPE_NAME,
    INVALID_DICT_CODE,
    INVALID_TABLE_NAME,
    INVALID_COLUMN_NAME,
    INVALID_SPEL_EXPRESSION,
    MISSING_DICT_CONFIG,
    CIRCULAR_REFERENCE,
    UNKNOWN_TYPE
}