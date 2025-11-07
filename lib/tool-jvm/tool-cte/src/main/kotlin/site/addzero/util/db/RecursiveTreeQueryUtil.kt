package site.addzero.util.db

import org.springframework.jdbc.core.JdbcTemplate

/**
 * 执行递归树查询
 */
fun recursiveTreeQuery(
    jdbcTemplate: JdbcTemplate,
    tableName: String,
    id: String,
    pid: String,
    customSqlSegment: String,
    finalCustomSqlSegment: String,
    databaseTypeName: String = "mysql"
): List<Map<String?, Any?>?> {
    val databaseType = DatabaseType.fromTypeName(databaseTypeName)
    
    // 使用策略模式获取对应数据库的CTE实现
    val cteStrategy = CteStrategyFactory.getStrategy(databaseType)
    val sql = cteStrategy.generateRecursiveTreeQuerySql(
        tableName, id, pid, customSqlSegment, finalCustomSqlSegment
    )
    
    return jdbcTemplate.queryForList(sql)
}