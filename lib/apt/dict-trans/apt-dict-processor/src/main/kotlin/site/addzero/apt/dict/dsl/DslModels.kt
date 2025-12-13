package site.addzero.apt.dict.dsl

/**
 * DSL data models for dictionary translation rules
 * 
 * These models represent the parsed DSL structure and provide
 * a type-safe way to work with translation rules.
 */

/**
 * Entity translation rule containing all translation configurations for an entity
 */
data class EntityTranslationRule(
    val entityName: String,
    val fieldRules: List<FieldTranslationRule> = emptyList(),
    val nestedRules: List<NestedTranslationRule> = emptyList(),
    val recursiveRules: List<RecursiveTranslationRule> = emptyList()
) {
    
    /**
     * Gets all field rules including nested ones
     */
    fun getAllFieldRules(): List<FieldTranslationRule> {
        val allRules = mutableListOf<FieldTranslationRule>()
        allRules.addAll(fieldRules)
        
        nestedRules.forEach { nestedRule ->
            allRules.addAll(nestedRule.nestedRules.getAllFieldRules())
        }
        
        return allRules
    }
    
    /**
     * Checks if this entity has any system dictionary fields
     */
    fun hasSystemDictFields(): Boolean {
        return getAllFieldRules().any { rule ->
            rule.dictConfigs.any { it.type == TranslationType.SYSTEM_DICT }
        }
    }
    
    /**
     * Checks if this entity has any table dictionary fields
     */
    fun hasTableDictFields(): Boolean {
        return getAllFieldRules().any { rule ->
            rule.dictConfigs.any { it.type == TranslationType.TABLE_DICT }
        }
    }
    
    /**
     * Gets all system dictionary codes used by this entity
     */
    fun getSystemDictCodes(): Set<String> {
        return getAllFieldRules().flatMap { rule ->
            rule.dictConfigs.filter { it.type == TranslationType.SYSTEM_DICT }
                .map { it.dictCode }
        }.toSet()
    }
    
    /**
     * Gets all table dictionary configurations used by this entity
     */
    fun getTableDictConfigs(): Set<TableDictInfo> {
        return getAllFieldRules().flatMap { rule ->
            rule.dictConfigs.filter { it.type == TranslationType.TABLE_DICT }
                .map { TableDictInfo(it.table, it.codeColumn, it.nameColumn, it.condition) }
        }.toSet()
    }
}

/**
 * Field translation rule for a single field
 */
data class FieldTranslationRule(
    val fieldName: String,
    val translationType: TranslationType,
    val dictConfigs: List<DictConfig> = emptyList(),
    val targetFieldName: String = "${fieldName}Text"
) {
    
    /**
     * Checks if this field has multiple dictionary configurations
     */
    fun isMultiDict(): Boolean {
        return dictConfigs.size > 1
    }
    
    /**
     * Gets the primary dictionary configuration (first one)
     */
    fun getPrimaryDictConfig(): DictConfig? {
        return dictConfigs.firstOrNull()
    }
}

/**
 * Nested translation rule for complex object hierarchies
 */
data class NestedTranslationRule(
    val fieldName: String,
    val targetType: String,
    val isCollection: Boolean,
    val nestedRules: EntityTranslationRule
) {
    
    /**
     * Gets the enhanced type name for the nested field
     */
    fun getEnhancedTypeName(): String {
        val baseType = "${targetType}Enhanced"
        return if (isCollection) "List<$baseType>" else baseType
    }
}

/**
 * Recursive translation rule for self-referencing entities
 */
data class RecursiveTranslationRule(
    val fieldName: String,
    val targetType: String,
    val isCollection: Boolean,
    val maxDepth: Int = 10 // Prevent infinite recursion
) {
    
    /**
     * Gets the enhanced type name for the recursive field
     */
    fun getEnhancedTypeName(): String {
        val baseType = "${targetType}Enhanced"
        return if (isCollection) "List<$baseType>" else baseType
    }
}

/**
 * Dictionary configuration for a specific translation
 */
data class DictConfig(
    val type: TranslationType,
    val dictCode: String = "",
    val table: String = "",
    val codeColumn: String = "",
    val nameColumn: String = "",
    val condition: String = "",
    val spelExpression: String = "",
    val defaultValue: String = "",
    val cached: Boolean = true
) {
    
    /**
     * Validates this dictionary configuration
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        when (type) {
            TranslationType.SYSTEM_DICT -> {
                if (dictCode.isBlank()) {
                    errors.add("System dictionary code cannot be blank")
                }
            }
            TranslationType.TABLE_DICT -> {
                if (table.isBlank()) errors.add("Table name cannot be blank")
                if (codeColumn.isBlank()) errors.add("Code column cannot be blank")
                if (nameColumn.isBlank()) errors.add("Name column cannot be blank")
            }
            TranslationType.SPEL -> {
                if (spelExpression.isBlank()) {
                    errors.add("SPEL expression cannot be blank")
                }
            }
            else -> {
                // Multi-dict validation is handled at field level
            }
        }
        
        return errors
    }
    
    /**
     * Gets a unique key for this dictionary configuration
     */
    fun getKey(): String {
        return when (type) {
            TranslationType.SYSTEM_DICT -> "system:$dictCode"
            TranslationType.TABLE_DICT -> "table:$table:$codeColumn:$nameColumn"
            TranslationType.SPEL -> "spel:${spelExpression.hashCode()}"
            else -> "unknown:${hashCode()}"
        }
    }
}

/**
 * Types of dictionary translation
 */
enum class TranslationType {
    SYSTEM_DICT,    // Built-in system dictionary
    TABLE_DICT,     // Custom table dictionary
    SPEL,           // SPEL expression
    MULTI_DICT      // Multiple dictionary configurations
}

/**
 * Table dictionary information
 */
data class TableDictInfo(
    val table: String,
    val codeColumn: String,
    val nameColumn: String,
    val condition: String = ""
) {
    
    /**
     * Gets a unique key for this table dictionary
     */
    fun getKey(): String {
        return "$table:$codeColumn:$nameColumn"
    }
    
    /**
     * Checks if this table dictionary has a WHERE condition
     */
    fun hasCondition(): Boolean {
        return condition.isNotBlank()
    }
}

/**
 * DSL template configuration
 */
data class DslTemplateConfig(
    val entityClass: String,
    val translationRules: List<EntityTranslationRule>,
    val concurrencyConfig: ConcurrencyConfig = ConcurrencyConfig(),
    val optimizationHints: OptimizationHints = OptimizationHints()
) {
    
    /**
     * Gets all entities referenced in the translation rules
     */
    fun getReferencedEntities(): Set<String> {
        val entities = mutableSetOf<String>()
        entities.add(entityClass)
        
        translationRules.forEach { rule ->
            entities.add(rule.entityName)
            
            rule.nestedRules.forEach { nestedRule ->
                entities.add(nestedRule.targetType)
            }
            
            rule.recursiveRules.forEach { recursiveRule ->
                entities.add(recursiveRule.targetType)
            }
        }
        
        return entities
    }
    
    /**
     * Validates the entire DSL template configuration
     */
    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        
        if (entityClass.isBlank()) {
            errors.add("Entity class name cannot be blank")
        }
        
        if (translationRules.isEmpty()) {
            errors.add("At least one translation rule must be defined")
        }
        
        // Validate each translation rule
        translationRules.forEach { rule ->
            rule.fieldRules.forEach { fieldRule ->
                fieldRule.dictConfigs.forEach { config ->
                    errors.addAll(config.validate())
                }
            }
        }
        
        return errors
    }
}

/**
 * Concurrency configuration for translation processing
 */
data class ConcurrencyConfig(
    val enableConcurrentTranslation: Boolean = true,
    val maxConcurrentThreads: Int = Runtime.getRuntime().availableProcessors(),
    val batchSize: Int = 100,
    val timeoutMillis: Long = 5000
)

/**
 * Optimization hints for code generation
 */
data class OptimizationHints(
    val enableBatchOptimization: Boolean = true,
    val enableCaching: Boolean = true,
    val enableLazyLoading: Boolean = false,
    val generateAsyncMethods: Boolean = false,
    val optimizationLevel: OptimizationLevel = OptimizationLevel.STANDARD
) {
    
    enum class OptimizationLevel {
        MINIMAL,    // Basic functionality only
        STANDARD,   // Standard optimizations
        AGGRESSIVE  // All optimizations enabled
    }
}

/**
 * DSL compilation result
 */
data class DslCompilationResult(
    val success: Boolean,
    val entityRules: List<EntityTranslationRule> = emptyList(),
    val errors: List<DslValidationError> = emptyList(),
    val warnings: List<String> = emptyList(),
    val compilationTimeMs: Long = 0
) {
    
    /**
     * Checks if compilation was successful without errors
     */
    fun isSuccessful(): Boolean {
        return success && errors.isEmpty()
    }
    
    /**
     * Gets a summary of the compilation result
     */
    fun getSummary(): String {
        return if (isSuccessful()) {
            "Compilation successful: ${entityRules.size} entities processed in ${compilationTimeMs}ms"
        } else {
            "Compilation failed: ${errors.size} errors, ${warnings.size} warnings"
        }
    }
}