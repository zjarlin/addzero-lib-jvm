package site.addzero.util.db.cte.strategy.impl

import org.springframework.stereotype.Component
import site.addzero.util.db.DatabaseType
import site.addzero.util.db.cte.strategy.CteStrategy

/**
 * SQL Server数据库的CTE策略实现
 */
@Component
class SqlServerCteStrategy : CteStrategy {

    override fun supports(databaseType: DatabaseType): Boolean {
        return databaseType == DatabaseType.SQLSERVER
    }



    override fun generateRecursiveTreeQuerySqlUp(
        tableName: String,
        id: String,
        pid: String,
        customSqlSegment: String,
        finalCustomSqlSegment: String,
        returnBreadcrumb: Boolean,
        breadcrumbColumn: String?
    ): String {
        val recursiveDataUp = generateRecursiveDataUp(tableName, id, pid, customSqlSegment, returnBreadcrumb, breadcrumbColumn)
        
        // 合并WHERE条件，避免出现两个WHERE子句
        val whereClause = if (finalCustomSqlSegment.isBlank()) {
            "WHERE tree_depth > 0"
        } else {
            // 移除finalCustomSqlSegment中的"WHERE"关键字，然后合并条件
            val condition = finalCustomSqlSegment.trim().removePrefix("WHERE").trim()
            if (condition.isEmpty()) {
                "WHERE tree_depth > 0"
            } else {
                "WHERE tree_depth > 0 AND $condition"
            }
        }
        
        return """
            WITH 
            ${recursiveDataUp}
            
            SELECT *
            FROM recursive_data_up
            ${whereClause}
            ORDER BY tree_depth;
        """.trimIndent()
    }

    override fun generateRecursiveTreeQuerySqlUpAndDown(
        tableName: String,
        id: String,
        pid: String,
        customSqlSegment: String,
        finalCustomSqlSegment: String,
        returnBreadcrumb: Boolean,
        breadcrumbColumn: String?
    ): String {
        val recursiveDataDown =
            generateRecursiveDataDown(tableName, id, pid, customSqlSegment, returnBreadcrumb, breadcrumbColumn)
        val recursiveDataUp =
            generateRecursiveDataUp(tableName, id, pid, customSqlSegment, returnBreadcrumb, breadcrumbColumn)
        
        return """
            WITH 
            ${recursiveDataDown},
            
            ${recursiveDataUp},
            
            combined_data AS (
                SELECT * FROM recursive_data_up WHERE tree_depth > 0
                UNION 
                SELECT * FROM recursive_data_down
            )
            
            SELECT DISTINCT *
            FROM combined_data
            ${finalCustomSqlSegment}  
            ORDER BY tree_direction, tree_depth;
        """.trimIndent()
    }

    /**
     * 生成向下递归的CTE片段
     */
    private fun generateRecursiveDataDown(
        tableName: String,
        id: String,
        pid: String,
        customSqlSegment: String,
        returnBreadcrumb: Boolean,
        breadcrumbColumn: String?
    ): String {
        val breadcrumbField = breadcrumbColumn ?: id
        val breadcrumbAnchor = if (returnBreadcrumb) {
            "CAST(t.${breadcrumbField} AS NVARCHAR(1000)) as tree_breadcrumb,"
        } else {
            "CAST(NULL AS NVARCHAR(1000)) as tree_breadcrumb,"
        }
        val breadcrumbRecursive = if (returnBreadcrumb) {
            "rd.tree_breadcrumb + ',' + CAST(t.${breadcrumbField} AS NVARCHAR(1000)) as tree_breadcrumb,"
        } else {
            "CAST(NULL AS NVARCHAR(1000)) as tree_breadcrumb,"
        }
        return """
            recursive_data_down AS (
                SELECT 
                    t.*,
                    0 as tree_depth,
                    'down' as tree_direction,
                    CAST(t.${id} AS NVARCHAR(1000)) as tree_path,
                    ${breadcrumbAnchor}
                FROM ${tableName} t
                ${customSqlSegment} 
                
                UNION ALL
                
                SELECT 
                    t.*,
                    rd.tree_depth + 1,
                    'down',
                    rd.tree_path + ',' + CAST(t.${id} AS NVARCHAR(1000)),
                    ${breadcrumbRecursive}
                FROM ${tableName} t
                INNER JOIN recursive_data_down rd ON t.${pid} = rd.${id}
            )
        """.trimIndent()
    }

    /**
     * 生成向上递归的CTE片段
     */
    private fun generateRecursiveDataUp(
        tableName: String,
        id: String,
        pid: String,
        customSqlSegment: String,
        returnBreadcrumb: Boolean,
        breadcrumbColumn: String?
    ): String {
        val breadcrumbField = breadcrumbColumn ?: id
        val breadcrumbAnchor = if (returnBreadcrumb) {
            "CAST(t.${breadcrumbField} AS NVARCHAR(1000)) as tree_breadcrumb,"
        } else {
            "CAST(NULL AS NVARCHAR(1000)) as tree_breadcrumb,"
        }
        return """
            recursive_data_up AS (
                SELECT 
                    t.*,
                    0 as tree_depth,
                    'up' as tree_direction,
                    CAST(t.${id} AS NVARCHAR(1000)) as tree_path,
                    ${breadcrumbAnchor}
                FROM ${tableName} t
                ${customSqlSegment}  
                
                UNION ALL
                
                SELECT 
                    t.*,
                    ru.tree_depth + 1,
                    'up',
                    CAST(t.${id} AS NVARCHAR(1000)) + ',' + ru.tree_path,
                    CASE 
                        WHEN ru.tree_breadcrumb IS NOT NULL AND ru.tree_breadcrumb != '' THEN
                            CAST(t.${breadcrumbField} AS NVARCHAR(1000)) + ',' + ru.tree_breadcrumb
                        ELSE
                            CAST(t.${breadcrumbField} AS NVARCHAR(1000))
                    END as tree_breadcrumb,
                FROM ${tableName} t
                INNER JOIN recursive_data_up ru ON t.${id} = ru.${pid} AND ru.${pid} IS NOT NULL
            )
        """.trimIndent()
    }
}