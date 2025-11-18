package site.addzero.util.db.cte.strategy.impl

import org.springframework.stereotype.Component
import site.addzero.util.db.DatabaseType
import site.addzero.util.db.cte.strategy.CteStrategy

/**
 * Oracle数据库的CTE策略实现
 */
@Component
class OracleCteStrategy : CteStrategy {

    override fun supports(databaseType: DatabaseType): Boolean {
        return databaseType == DatabaseType.ORACLE
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
        
        return """
            WITH 
            ${recursiveDataUp}
            
            SELECT *
            FROM recursive_data_up
            ${finalCustomSqlSegment}
            ORDER BY tree_depth
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
        val recursiveDataDown = generateRecursiveDataDown(tableName, id, pid, customSqlSegment, returnBreadcrumb, breadcrumbColumn)
        val recursiveDataUp = generateRecursiveDataUp(tableName, id, pid, customSqlSegment, returnBreadcrumb, breadcrumbColumn)
        
        return """
            WITH 
            ${recursiveDataDown},
            
            ${recursiveDataUp},
            
            combined_data AS (
                SELECT * FROM recursive_data_up
                UNION ALL
                SELECT * FROM recursive_data_down
            )
            
            SELECT *
            FROM combined_data
            ${finalCustomSqlSegment}
            ORDER BY tree_direction, tree_depth
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
            "TO_CHAR(t.${breadcrumbField}) as tree_breadcrumb"
        } else {
            "CAST(NULL AS VARCHAR2(4000)) as tree_breadcrumb"
        }
        val breadcrumbRecursive = if (returnBreadcrumb) {
            "rd.tree_breadcrumb || ',' || TO_CHAR(t.${breadcrumbField})"
        } else {
            "CAST(NULL AS VARCHAR2(4000))"
        }
        return """
            recursive_data_down (${id}, ${pid}, tree_depth, tree_direction, tree_path, tree_breadcrumb) AS (
                SELECT 
                    t.${id},
                    t.${pid},
                    0 as tree_depth,
                    'down' as tree_direction,
                    TO_CHAR(t.${id}) as tree_path,
                    ${breadcrumbAnchor}
                FROM ${tableName} t
                ${customSqlSegment}
                
                UNION ALL
                
                SELECT 
                    t.${id},
                    t.${pid},
                    rd.tree_depth + 1,
                    'down',
                    rd.tree_path || ',' || TO_CHAR(t.${id}),
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
            "TO_CHAR(t.${breadcrumbField}) as tree_breadcrumb"
        } else {
            "CAST(NULL AS VARCHAR2(4000)) as tree_breadcrumb"
        }
        val breadcrumbRecursive = if (returnBreadcrumb) {
            "TO_CHAR(t.${breadcrumbField}) || ',' || ru.tree_breadcrumb"
        } else {
            "CAST(NULL AS VARCHAR2(4000))"
        }
        return """
            recursive_data_up (${id}, ${pid}, tree_depth, tree_direction, tree_path, tree_breadcrumb) AS (
                SELECT 
                    t.${id},
                    t.${pid},
                    0 as tree_depth,
                    'up' as tree_direction,
                    TO_CHAR(t.${id}) as tree_path,
                    ${breadcrumbAnchor}
                FROM ${tableName} t
                ${customSqlSegment}
                
                UNION ALL
                
                SELECT 
                    t.${id},
                    t.${pid},
                    ru.tree_depth + 1,
                    'up',
                    TO_CHAR(t.${id}) || ',' || ru.tree_path,
                    ${breadcrumbRecursive}
                FROM ${tableName} t
                INNER JOIN recursive_data_up ru ON t.${id} = ru.${pid}
            )
        """.trimIndent()
    }
}
