package site.addzero.util.db.cte

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import site.addzero.util.db.DatabaseType
import site.addzero.util.db.cte.strategy.CteStrategy
import site.addzero.util.db.wrapper.entity.WrapperContext
import site.addzero.util.db.wrapper.util.SqlTemplateParser.parseWrapperSqlToString

@Component
class CteUtil {
    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var ctes: List<CteStrategy>

    /**
     * 执行递归树查询
     */
    fun recursiveTreeQuery(
        tableName: String,
        id: String = "id",
        pid: String = "parent_id",
        databaseType: DatabaseType = DatabaseType.MYSQL,
        cteWrapperContext: WrapperContext,
        combinedDataWrapperContext: WrapperContext,
    ): List<Map<String, Any?>> {
        val actStrategy =
            ctes.firstOrNull { it.supports(databaseType) } ?: throw IllegalArgumentException("No CTE strategy found")
        val customSqlSegment =
            parseWrapperSqlToString(cteWrapperContext.customSqlSegment, cteWrapperContext.paramNameValuePairs)
        val finalCustomSqlSegment = parseWrapperSqlToString(
            combinedDataWrapperContext.customSqlSegment, combinedDataWrapperContext.paramNameValuePairs
        )
        val generateRecursiveTreeQuerySql =
            actStrategy.generateRecursiveTreeQuerySql(tableName, id, pid, customSqlSegment, finalCustomSqlSegment)
        val queryForList = jdbcTemplate.queryForList(generateRecursiveTreeQuerySql)
        return queryForList
     }


}
