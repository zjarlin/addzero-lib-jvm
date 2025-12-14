package site.addzero.dict.trans.inter

/**
 * Cache interface for dictionary translations
 * Implementations should provide caching for both system and table dictionaries
 */
interface DictCache {

    /**
     * Get cached system dictionary translations
     */
    fun getSystemDict(dictCode: String, key: String): String?

    /**
     * Put system dictionary translation in cache
     */
    fun putSystemDict(dictCode: String, key: String, value: String)

    /**
     * Get cached table dictionary translations
     */
    fun getTableDict(table: String, codeColumn: String, textColumn: String, key: String): String?

    /**
     * Put table dictionary translation in cache
     */
    fun putTableDict(table: String, codeColumn: String, textColumn: String, key: String, value: String)

    /**
     * Clear all caches
     */
    fun clear()
}