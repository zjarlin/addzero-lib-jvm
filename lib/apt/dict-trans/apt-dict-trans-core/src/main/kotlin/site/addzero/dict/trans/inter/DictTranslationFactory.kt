package site.addzero.dict.trans.inter

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 字典翻译工厂（单例）
 * 使用咖啡因缓存优化性能，处理预编译SQL和批量查询
 */
object DictTranslationFactory {
    
    /**
     * 系统字典缓存
     * Key: "dictType:code" (例如: "sys_user_sex:0")
     * Value: 翻译后的名称
     */
    private val systemDictCache: Cache<String, String> = Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .recordStats()
        .build()
    
    /**
     * 表字典缓存
     * Key: "table:codeColumn:nameColumn:code" (例如: "equipment:id:name:49")
     * Value: 翻译后的名称
     */
    private val tableDictCache: Cache<String, String> = Caffeine.newBuilder()
        .maximumSize(50_000)
        .expireAfterWrite(15, TimeUnit.MINUTES)
        .recordStats()
        .build()
    
    /**
     * 预编译SQL缓存
     * Key: 字典配置键 (例如: "system:sys_user_sex")
     * Value: 预编译的SQL模板
     */
    private val precompiledSqlCache: Cache<String, PrecompiledSql> = Caffeine.newBuilder()
        .maximumSize(1_000)
        .expireAfterWrite(1, TimeUnit.HOURS)
        .build()
    
    /**
     * 批量查询防重复映射
     * 防止同一时间对相同配置的重复查询
     */
    private val batchQueryMap = ConcurrentHashMap<String, CompletableFuture<Map<String, String>>>()
    
    /**
     * SQL执行器
     */
    private var sqlExecutor: SqlExecutor? = null
    
    /**
     * 初始化工厂
     */
    fun initialize(sqlExecutor: SqlExecutor) {
        this.sqlExecutor = sqlExecutor
    }
    
    /**
     * 处理翻译任务列表
     * @param tasks 编译时生成的翻译任务
     * @param valueExtractor 值提取函数（运行时提供）
     * @return 翻译结果映射
     */
    fun processTranslationTasks(
        tasks: List<TransTask>,
        valueExtractor: (String) -> Any?
    ): CompletableFuture<Map<String, String>> {
        
        return CompletableFuture.supplyAsync {
            val results = mutableMapOf<String, String>()
            
            // 按缓存键分组任务
            val taskGroups = tasks.groupBy { it.getCacheKey() }
            
            taskGroups.forEach { (cacheKey, taskList) ->
                try {
                    val taskResults = processTaskGroup(cacheKey, taskList, valueExtractor)
                    results.putAll(taskResults)
                } catch (e: Exception) {
                    println("Failed to process task group $cacheKey: ${e.message}")
                }
            }
            
            results
        }
    }
    
    /**
     * 处理单个任务组
     */
    private fun processTaskGroup(
        cacheKey: String,
        tasks: List<TransTask>,
        valueExtractor: (String) -> Any?
    ): Map<String, String> {
        
        val firstTask = tasks.first()
        val results = mutableMapOf<String, String>()
        
        // 收集需要翻译的值
        val valuesToTranslate = tasks.mapNotNull { task ->
            val value = valueExtractor(task.valueExpression)
            if (value != null) {
                task.taskId to value.toString()
            } else {
                null
            }
        }.toMap()
        
        if (valuesToTranslate.isEmpty()) {
            return results
        }
        
        when {
            firstTask.isSystemDict() -> {
                results.putAll(processSystemDictTasks(firstTask.dictConfig, valuesToTranslate))
            }
            firstTask.isTableDict() -> {
                results.putAll(processTableDictTasks(firstTask.dictConfig, valuesToTranslate))
            }
        }
        
        return results
    }
    
    /**
     * 处理系统字典任务
     */
    private fun processSystemDictTasks(
        dictConfig: String,
        valuesToTranslate: Map<String, String>
    ): Map<String, String> {
        
        val results = mutableMapOf<String, String>()
        val uncachedValues = mutableListOf<String>()
        
        // 先从缓存中获取
        valuesToTranslate.forEach { (taskId, code) ->
            val cacheKey = "$dictConfig:$code"
            val cachedValue = systemDictCache.getIfPresent(cacheKey)
            if (cachedValue != null) {
                results[taskId] = cachedValue
            } else {
                uncachedValues.add(code)
            }
        }
        
        // 批量查询未缓存的值
        if (uncachedValues.isNotEmpty()) {
            val batchResults = executeBatchSystemDictQuery(dictConfig, uncachedValues)
            
            // 更新缓存和结果
            batchResults.forEach { (code, name) ->
                val cacheKey = "$dictConfig:$code"
                systemDictCache.put(cacheKey, name)
                
                // 找到对应的任务ID
                valuesToTranslate.forEach { (taskId, taskCode) ->
                    if (taskCode == code) {
                        results[taskId] = name
                    }
                }
            }
        }
        
        return results
    }
    
    /**
     * 处理表字典任务
     */
    private fun processTableDictTasks(
        dictConfig: String,
        valuesToTranslate: Map<String, String>
    ): Map<String, String> {
        
        val results = mutableMapOf<String, String>()
        val uncachedValues = mutableListOf<String>()
        
        val configParts = dictConfig.split("|")
        if (configParts.size < 3) return results
        
        val table = configParts[0]
        val codeColumn = configParts[1]
        val nameColumn = configParts[2]
        
        // 先从缓存中获取
        valuesToTranslate.forEach { (taskId, code) ->
            val cacheKey = "$table:$codeColumn:$nameColumn:$code"
            val cachedValue = tableDictCache.getIfPresent(cacheKey)
            if (cachedValue != null) {
                results[taskId] = cachedValue
            } else {
                uncachedValues.add(code)
            }
        }
        
        // 批量查询未缓存的值
        if (uncachedValues.isNotEmpty()) {
            val batchResults = executeBatchTableDictQuery(dictConfig, uncachedValues)
            
            // 更新缓存和结果
            batchResults.forEach { (code, name) ->
                val cacheKey = "$table:$codeColumn:$nameColumn:$code"
                tableDictCache.put(cacheKey, name)
                
                // 找到对应的任务ID
                valuesToTranslate.forEach { (taskId, taskCode) ->
                    if (taskCode == code) {
                        results[taskId] = name
                    }
                }
            }
        }
        
        return results
    }
    
    /**
     * 执行批量系统字典查询
     */
    private fun executeBatchSystemDictQuery(
        dictConfig: String,
        codes: List<String>
    ): Map<String, String> {
        
        val cacheKey = "system:$dictConfig"
        
        // 防止重复查询
        return batchQueryMap.computeIfAbsent(cacheKey) {
            CompletableFuture.supplyAsync {
                try {
                    sqlExecutor?.executeSystemDictQuery(dictConfig, codes) ?: emptyMap()
                } catch (e: Exception) {
                    println("Failed to execute system dict query for $dictConfig: ${e.message}")
                    emptyMap()
                } finally {
                    // 查询完成后移除，允许后续查询
                    batchQueryMap.remove(cacheKey)
                }
            }
        }.get()
    }
    
    /**
     * 执行批量表字典查询
     */
    private fun executeBatchTableDictQuery(
        dictConfig: String,
        codes: List<String>
    ): Map<String, String> {
        
        val cacheKey = "table:$dictConfig"
        
        return batchQueryMap.computeIfAbsent(cacheKey) {
            CompletableFuture.supplyAsync {
                try {
                    val configParts = dictConfig.split("|")
                    if (configParts.size < 3) return@supplyAsync emptyMap<String, String>()
                    
                    val context = DictQueryContext(
                        table = configParts[0],
                        codeColumn = configParts[1],
                        nameColumn = configParts[2],
                        codes = codes,
                        whereCondition = configParts.getOrNull(3) ?: ""
                    )
                    
                    val queryResults = sqlExecutor?.executeTableDictQuery(context) ?: emptyList()
                    
                    queryResults.associate { row ->
                        val code = row[context.codeColumn]?.toString() ?: ""
                        val name = row[context.nameColumn]?.toString() ?: ""
                        code to name
                    }
                } catch (e: Exception) {
                    println("Failed to execute table dict query for $dictConfig: ${e.message}")
                    emptyMap()
                } finally {
                    batchQueryMap.remove(cacheKey)
                }
            }
        }.get()
    }
    
    /**
     * 获取缓存统计信息
     */
    fun getCacheStats(): CacheStats {
        return CacheStats(
            systemDictCacheStats = systemDictCache.stats(),
            tableDictCacheStats = tableDictCache.stats(),
            precompiledSqlCacheStats = precompiledSqlCache.stats(),
            systemDictCacheSize = systemDictCache.estimatedSize(),
            tableDictCacheSize = tableDictCache.estimatedSize(),
            precompiledSqlCacheSize = precompiledSqlCache.estimatedSize()
        )
    }
    
    /**
     * 单个系统字典翻译（带缓存）
     */
    fun translateSystemDict(dictConfig: String, code: String): String? {
        val cacheKey = "$dictConfig:$code"
        
        // 先从缓存获取
        val cachedValue = systemDictCache.getIfPresent(cacheKey)
        if (cachedValue != null) {
            return cachedValue
        }
        
        // 缓存未命中，执行查询
        try {
            val results = sqlExecutor?.executeSystemDictQuery(dictConfig, listOf(code)) ?: emptyMap()
            val translatedValue = results[code]
            
            if (translatedValue != null) {
                systemDictCache.put(cacheKey, translatedValue)
                return translatedValue
            }
        } catch (e: Exception) {
            println("Failed to translate system dict $dictConfig:$code - ${e.message}")
        }
        
        return null
    }
    
    /**
     * 单个表字典翻译（带缓存）
     */
    fun translateTableDict(dictConfig: String, code: String): String? {
        val configParts = dictConfig.split("|")
        if (configParts.size < 3) return null
        
        val table = configParts[0]
        val codeColumn = configParts[1]
        val nameColumn = configParts[2]
        val cacheKey = "$table:$codeColumn:$nameColumn:$code"
        
        // 先从缓存获取
        val cachedValue = tableDictCache.getIfPresent(cacheKey)
        if (cachedValue != null) {
            return cachedValue
        }
        
        // 缓存未命中，执行查询
        try {
            val context = DictQueryContext(
                table = table,
                codeColumn = codeColumn,
                nameColumn = nameColumn,
                codes = listOf(code),
                whereCondition = configParts.getOrNull(3) ?: ""
            )
            
            val queryResults = sqlExecutor?.executeTableDictQuery(context) ?: emptyList()
            val result = queryResults.firstOrNull()
            
            if (result != null) {
                val translatedValue = result[nameColumn]?.toString()
                if (translatedValue != null) {
                    tableDictCache.put(cacheKey, translatedValue)
                    return translatedValue
                }
            }
        } catch (e: Exception) {
            println("Failed to translate table dict $dictConfig:$code - ${e.message}")
        }
        
        return null
    }
    
    /**
     * 清理缓存
     */
    fun clearCaches() {
        systemDictCache.invalidateAll()
        tableDictCache.invalidateAll()
        precompiledSqlCache.invalidateAll()
        batchQueryMap.clear()
    }
}

/**
 * 缓存统计信息
 */
data class CacheStats(
    val systemDictCacheStats: com.github.benmanes.caffeine.cache.stats.CacheStats,
    val tableDictCacheStats: com.github.benmanes.caffeine.cache.stats.CacheStats,
    val precompiledSqlCacheStats: com.github.benmanes.caffeine.cache.stats.CacheStats,
    val systemDictCacheSize: Long,
    val tableDictCacheSize: Long,
    val precompiledSqlCacheSize: Long
)