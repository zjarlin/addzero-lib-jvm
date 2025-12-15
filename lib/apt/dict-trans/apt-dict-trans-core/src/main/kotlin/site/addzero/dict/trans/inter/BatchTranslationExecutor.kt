package site.addzero.dict.trans.inter

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

/**
 * 批量翻译执行器
 * 基于编译时配置执行批量翻译，完全避免反射
 */
class BatchTranslationExecutor(
    private val sqlExecutor: SqlExecutor,
    private val executor: Executor? = null
) {
    
    /**
     * 执行批量翻译查询
     * @param tasks 编译时生成的翻译任务
     * @param codeValues 运行时收集的需要翻译的值
     * @return 翻译结果
     */
    fun executeBatchTranslation(
        tasks: List<TransTask>,
        codeValues: Map<String, Set<String>>
    ): CompletableFuture<Map<String, Map<String, String>>> {
        val future = CompletableFuture.supplyAsync({
            try {
                collectTranslationResults(tasks, codeValues)
            } catch (e: Exception) {
                throw RuntimeException("Batch translation execution failed", e)
            }
        }, executor)
        
        return future
    }
    
    /**
     * 收集翻译结果
     */
    private fun collectTranslationResults(
        tasks: List<TransTask>,
        codeValues: Map<String, Set<String>>
    ): Map<String, Map<String, String>> {
        val results = mutableMapOf<String, Map<String, String>>()
        
        // 按配置键分组
        val taskGroups = tasks.groupBy { it.getCacheKey() }
        
        taskGroups.forEach { (cacheKey, taskList) ->
            val task = taskList.first()
            val codes = codeValues[cacheKey] ?: emptySet()
            
            if (codes.isNotEmpty()) {
                try {
                    val dictMap = when {
                        task.isSystemDict() -> executeSystemDictQuery(task.dictConfig, codes.toList())
                        task.isTableDict() -> executeTableDictQuery(task, codes.toList())
                        else -> emptyMap()
                    }
                    results[cacheKey] = dictMap
                } catch (e: Exception) {
                    println("Failed to execute query for config $cacheKey: ${e.message}")
                    results[cacheKey] = emptyMap()
                }
            }
        }
        
        return results
    }
    
    /**
     * 执行系统字典查询
     */
    private fun executeSystemDictQuery(dicCode: String, codes: List<String>): Map<String, String> {
        return sqlExecutor.executeSystemDictQuery(dicCode, codes)
    }
    
    /**
     * 执行表字典查询
     */
    private fun executeTableDictQuery(task: TransTask, codes: List<String>): Map<String, String> {
        val parts = task.dictConfig.split("|")
        if (parts.size < 3) return emptyMap()
        
        val table = parts[0]
        val codeColumn = parts[1]
        val nameColumn = parts[2]
        val whereCondition = parts.getOrNull(3) ?: ""
        
        val context = DictQueryContext(
            table = table,
            codeColumn = codeColumn,
            nameColumn = nameColumn,
            codes = codes,
            whereCondition = whereCondition
        )
        
        val queryResult = sqlExecutor.executeTableDictQuery(context)
        
        return queryResult.associate { row ->
            val code = row[codeColumn]?.toString() ?: ""
            val name = row[nameColumn]?.toString() ?: ""
            code to name
        }
    }
}