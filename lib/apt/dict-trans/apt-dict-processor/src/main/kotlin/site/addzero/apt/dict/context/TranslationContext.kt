package site.addzero.apt.dict.context

/**
 * Translation context for batch dictionary translation
 * 
 * This class eliminates N+1 query problems by pre-loading all required translation data
 * in a single optimized database query per dictionary type.
 * 
 * Features:
 * - Batch loading of system and table dictionaries
 * - Concurrent processing of different dictionary types
 * - Caching and reuse of translation data
 * - Performance monitoring and statistics
 */
class TranslationContext(
    /**
     * Pre-loaded system dictionary data
     * Map structure: dictCode -> (code -> name)
     */
    private val systemDictData: Map<String, Map<String, String>> = emptyMap(),
    
    /**
     * Pre-loaded table dictionary data
     * Map structure: tableKey -> (code -> name)
     */
    private val tableDictData: Map<String, Map<String, String>> = emptyMap(),
    
    /**
     * SPEL evaluation context
     */
    private val spelContext: Map<String, Any> = emptyMap(),
    
    /**
     * Processing statistics for performance monitoring
     */
    val processingStats: ProcessingStatistics = ProcessingStatistics()
) {
    
    /**
     * Gets system dictionary translation for a given code
     * 
     * @param dictCode The system dictionary code
     * @param code The value to translate
     * @return Translated value or null if not found
     */
    fun getSystemDictTranslation(dictCode: String, code: String?): String? {
        if (code == null) return null
        
        processingStats.incrementSystemDictLookups()
        
        return systemDictData[dictCode]?.get(code).also {
            if (it != null) {
                processingStats.incrementSuccessfulTranslations()
            } else {
                processingStats.incrementFailedTranslations()
            }
        }
    }
    
    /**
     * Gets table dictionary translation for a given code
     * 
     * @param table The table name
     * @param codeColumn The code column name
     * @param nameColumn The name column name
     * @param code The value to translate
     * @return Translated value or null if not found
     */
    fun getTableDictTranslation(table: String, codeColumn: String, nameColumn: String, code: Any?): String? {
        if (code == null) return null
        
        processingStats.incrementTableDictLookups()
        
        val tableKey = generateTableKey(table, codeColumn, nameColumn)
        return tableDictData[tableKey]?.get(code.toString()).also {
            if (it != null) {
                processingStats.incrementSuccessfulTranslations()
            } else {
                processingStats.incrementFailedTranslations()
            }
        }
    }
    
    /**
     * Gets SPEL expression evaluation result
     * 
     * @param expression The SPEL expression
     * @param context The evaluation context
     * @return Evaluation result or null if evaluation fails
     */
    fun getSpelTranslation(expression: String, context: Any): String? {
        processingStats.incrementSpelEvaluations()
        
        // SPEL evaluation would be implemented here
        // For now, return null as placeholder
        return null
    }
    
    /**
     * Checks if the context contains data for a specific system dictionary
     */
    fun hasSystemDict(dictCode: String): Boolean {
        return systemDictData.containsKey(dictCode)
    }
    
    /**
     * Checks if the context contains data for a specific table dictionary
     */
    fun hasTableDict(table: String, codeColumn: String, nameColumn: String): Boolean {
        val tableKey = generateTableKey(table, codeColumn, nameColumn)
        return tableDictData.containsKey(tableKey)
    }
    
    /**
     * Gets all available system dictionary codes
     */
    fun getAvailableSystemDicts(): Set<String> {
        return systemDictData.keys
    }
    
    /**
     * Gets all available table dictionary configurations
     */
    fun getAvailableTableDicts(): Set<String> {
        return tableDictData.keys
    }
    
    /**
     * Generates a unique key for table dictionary configuration
     */
    private fun generateTableKey(table: String, codeColumn: String, nameColumn: String): String {
        return "$table:$codeColumn:$nameColumn"
    }
    
    /**
     * Creates a new context with additional system dictionary data
     */
    fun withSystemDict(dictCode: String, data: Map<String, String>): TranslationContext {
        val newSystemDictData = systemDictData.toMutableMap()
        newSystemDictData[dictCode] = data
        
        return TranslationContext(
            systemDictData = newSystemDictData,
            tableDictData = tableDictData,
            spelContext = spelContext,
            processingStats = processingStats
        )
    }
    
    /**
     * Creates a new context with additional table dictionary data
     */
    fun withTableDict(table: String, codeColumn: String, nameColumn: String, data: Map<String, String>): TranslationContext {
        val tableKey = generateTableKey(table, codeColumn, nameColumn)
        val newTableDictData = tableDictData.toMutableMap()
        newTableDictData[tableKey] = data
        
        return TranslationContext(
            systemDictData = systemDictData,
            tableDictData = newTableDictData,
            spelContext = spelContext,
            processingStats = processingStats
        )
    }
    
    /**
     * Creates a new context with additional SPEL context data
     */
    fun withSpelContext(key: String, value: Any): TranslationContext {
        val newSpelContext = spelContext.toMutableMap()
        newSpelContext[key] = value
        
        return TranslationContext(
            systemDictData = systemDictData,
            tableDictData = tableDictData,
            spelContext = newSpelContext,
            processingStats = processingStats
        )
    }
}

/**
 * Statistics for translation context performance monitoring
 */
data class ProcessingStatistics(
    private var systemDictLookups: Long = 0,
    private var tableDictLookups: Long = 0,
    private var spelEvaluations: Long = 0,
    private var successfulTranslations: Long = 0,
    private var failedTranslations: Long = 0,
    private var cacheHits: Long = 0,
    private var cacheMisses: Long = 0,
    val startTime: Long = System.currentTimeMillis()
) {
    
    fun incrementSystemDictLookups() {
        systemDictLookups++
    }
    
    fun incrementTableDictLookups() {
        tableDictLookups++
    }
    
    fun incrementSpelEvaluations() {
        spelEvaluations++
    }
    
    fun incrementSuccessfulTranslations() {
        successfulTranslations++
    }
    
    fun incrementFailedTranslations() {
        failedTranslations++
    }
    
    fun incrementCacheHits() {
        cacheHits++
    }
    
    fun incrementCacheMisses() {
        cacheMisses++
    }
    
    fun getSystemDictLookups(): Long = systemDictLookups
    fun getTableDictLookups(): Long = tableDictLookups
    fun getSpelEvaluations(): Long = spelEvaluations
    fun getSuccessfulTranslations(): Long = successfulTranslations
    fun getFailedTranslations(): Long = failedTranslations
    fun getCacheHits(): Long = cacheHits
    fun getCacheMisses(): Long = cacheMisses
    
    fun getTotalLookups(): Long = systemDictLookups + tableDictLookups + spelEvaluations
    fun getTotalTranslations(): Long = successfulTranslations + failedTranslations
    fun getSuccessRate(): Double = if (getTotalTranslations() > 0) successfulTranslations.toDouble() / getTotalTranslations() else 0.0
    fun getCacheHitRate(): Double = if (cacheHits + cacheMisses > 0) cacheHits.toDouble() / (cacheHits + cacheMisses) else 0.0
    fun getElapsedTime(): Long = System.currentTimeMillis() - startTime
    
    override fun toString(): String {
        return "ProcessingStatistics(" +
                "systemDictLookups=$systemDictLookups, " +
                "tableDictLookups=$tableDictLookups, " +
                "spelEvaluations=$spelEvaluations, " +
                "successfulTranslations=$successfulTranslations, " +
                "failedTranslations=$failedTranslations, " +
                "successRate=${String.format("%.2f", getSuccessRate() * 100)}%, " +
                "cacheHitRate=${String.format("%.2f", getCacheHitRate() * 100)}%, " +
                "elapsedTime=${getElapsedTime()}ms" +
                ")"
    }
}