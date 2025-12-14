package site.addzero.dict.trans.inter

/**
 * SQL执行策略接口
 * 支持不同数据库系统的特定优化
 */
interface SqlExecutionStrategy {
    
    /**
     * 获取数据库方言名称
     */
    fun getDialectName(): String
    
    /**
     * 生成系统字典查询SQL
     * 
     * @param dictCode 字典编码
     * @param codeCount 代码数量，用于优化IN子句
     * @return 优化的SQL语句
     */
    fun generateSystemDictSql(dictCode: String, codeCount: Int): String
    
    /**
     * 生成表字典查询SQL
     * 
     * @param context 查询上下文
     * @return 优化的SQL语句
     */
    fun generateTableDictSql(context: DictQueryContext): String
    
    /**
     * 获取批量查询的最优批次大小
     * 不同数据库对IN子句的支持不同
     */
    fun getOptimalBatchSize(): Int
    
    /**
     * 是否支持并行查询
     */
    fun supportsParallelExecution(): Boolean
    
    /**
     * 获取连接池配置建议
     */
    fun getConnectionPoolConfig(): ConnectionPoolConfig
}

/**
 * 连接池配置
 */
data class ConnectionPoolConfig(
    val maxPoolSize: Int = 10,
    val minPoolSize: Int = 2,
    val connectionTimeout: Long = 30000,
    val idleTimeout: Long = 600000
)