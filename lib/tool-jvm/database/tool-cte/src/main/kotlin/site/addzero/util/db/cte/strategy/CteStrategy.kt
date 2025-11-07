package site.addzero.util.db.cte.strategy

import site.addzero.util.db.DatabaseType

/**
 * CTE策略接口，用于不同数据库的CTE查询实现
 */
interface CteStrategy {

    /**
     * 判断该策略是否支持指定的数据库类型
     */
    fun supports(databaseType: DatabaseType): Boolean

    /**
     * 生成递归树查询的SQL语句
     */
    fun generateRecursiveTreeQuerySql(
        tableName: String,
        id: String,
        pid: String,
        customSqlSegment: String,
        finalCustomSqlSegment: String
    ): String
}
