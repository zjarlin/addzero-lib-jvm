package site.addzero.dict.trans.inter

/**
 * SQL执行器接口
 * 提供字典查询的数据库执行抽象
 */
interface SqlExecutor {
    
    /**
     * 执行系统字典查询
     * 
     * @param dictCode 字典编码
     * @param codes 需要翻译的代码列表
     * @return 字典翻译结果映射 (code -> name)
     */
    fun executeSystemDictQuery(dictCode: String, codes: List<String>): Map<String, String>
    
    /**
     * 执行表字典查询
     * 
     * @param context 查询上下文
     * @return 字典翻译结果列表
     */
    fun executeTableDictQuery(context: DictQueryContext): List<Map<String, Any?>>
    
    /**
     * 批量执行多个字典查询
     * 支持并行执行以提高性能
     * 
     * @param systemQueries 系统字典查询列表
     * @param tableQueries 表字典查询列表
     * @return 合并的查询结果
     */
    fun executeBatchQueries(
        systemQueries: List<Pair<String, List<String>>>,
        tableQueries: List<DictQueryContext>
    ): BatchQueryResult
}

/**
 * 字典查询上下文
 * 包含执行表字典查询所需的所有参数
 */
data class DictQueryContext(
    val table: String,
    val codeColumn: String,
    val nameColumn: String,
    val codes: List<String>,
    val whereCondition: String = "",
    val parameters: Map<String, Any> = emptyMap()
)

/**
 * 批量查询结果
 * 包含系统字典和表字典的查询结果
 */
data class BatchQueryResult(
    val systemDictResults: Map<String, Map<String, String>>,
    val tableDictResults: Map<String, List<Map<String, Any?>>>
)