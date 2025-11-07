package site.addzero.util.db

/**
 * CTE策略工厂类，根据数据库类型创建对应的CTE策略实现
 */
object CteStrategyFactory {
    
    private val strategies = mutableListOf<CteStrategy>()
    
    init {
        // 注册默认的MySQL策略
        registerStrategy(MySQLCteStrategy())
        // 注册PostgreSQL策略
        registerStrategy(PostgreSQLCteStrategy())
        // 可以在这里注册其他数据库的策略
    }
    
    /**
     * 注册CTE策略
     */
    fun registerStrategy(strategy: CteStrategy) {
        strategies.add(strategy)
    }
    
    /**
     * 根据数据库类型获取对应的CTE策略
     */
    fun getStrategy(databaseType: DatabaseType): CteStrategy {
        // 查找支持指定数据库类型的策略
        for (strategy in strategies) {
            if (strategy.supports(databaseType)) {
                return strategy
            }
        }
        
        // 如果没有找到对应策略，默认返回MySQL策略
        val mysqlStrategy = strategies.find { it.supports(DatabaseType.MYSQL) }
        return mysqlStrategy ?: MySQLCteStrategy()
    }
    
    /**
     * 根据数据库类型名称获取对应的CTE策略
     */
    fun getStrategy(databaseTypeName: String): CteStrategy {
        val databaseType = DatabaseType.fromTypeName(databaseTypeName)
        return getStrategy(databaseType)
    }
    
    /**
     * 获取默认的CTE策略（MySQL）
     */
    fun getStrategy(): CteStrategy {
        return getStrategy(DatabaseType.MYSQL)
    }
}