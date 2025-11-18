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
    fun actStrategy(databaseType: DatabaseType): CteStrategy {
        val actStrategy =
            ctes.firstOrNull { it.supports(databaseType) } ?: throw IllegalArgumentException("No CTE strategy found")
        return actStrategy
    }

    /**
     * 执行递归树查询
     */
    fun recursiveTreeQuerySqlUp(
        tableName: String,
        id: String = "id",
        pid: String = "parent_id",
        databaseType: DatabaseType = DatabaseType.MYSQL,
        cteWrapperContext: WrapperContext,
        combinedDataWrapperContext: WrapperContext,
        returnBreadcrumb: Boolean = true,
        breadcrumbColumn: String? = null,
    ): List<Map<String, Any?>> {
        val customSqlSegment =
            parseWrapperSqlToString(cteWrapperContext.customSqlSegment, cteWrapperContext.paramNameValuePairs)
        val finalCustomSqlSegment = parseWrapperSqlToString(
            combinedDataWrapperContext.customSqlSegment,
            combinedDataWrapperContext.paramNameValuePairs
        )
        val generateRecursiveTreeQuerySql = actStrategy(databaseType).generateRecursiveTreeQuerySqlUp(
            tableName, id, pid,
            customSqlSegment, finalCustomSqlSegment, returnBreadcrumb, breadcrumbColumn
        )
        val queryForList = jdbcTemplate.queryForList(generateRecursiveTreeQuerySql)
        return queryForList
     }


    /**
     * 执行递归树查询
     */
    fun recursiveTreeQuerySqlUpAndDown(
        tableName: String,
        id: String = "id",
        pid: String = "parent_id",
        databaseType: DatabaseType = DatabaseType.MYSQL,
        cteWrapperContext: WrapperContext,
        combinedDataWrapperContext: WrapperContext,
        returnBreadcrumb: Boolean = true,
        breadcrumbColumn: String? = null,
    ): List<Map<String, Any?>> {
        val customSqlSegment =
            parseWrapperSqlToString(cteWrapperContext.customSqlSegment, cteWrapperContext.paramNameValuePairs)
        val finalCustomSqlSegment = parseWrapperSqlToString(
            combinedDataWrapperContext.customSqlSegment,
            combinedDataWrapperContext.paramNameValuePairs
        )
        val generateRecursiveTreeQuerySql = actStrategy(databaseType) .generateRecursiveTreeQuerySqlUpAndDown(
            tableName,
            id,
            pid,
            customSqlSegment,
            finalCustomSqlSegment,
            returnBreadcrumb,
            breadcrumbColumn
        )
        val queryForList = jdbcTemplate.queryForList(generateRecursiveTreeQuerySql)
        return queryForList
    }




}
