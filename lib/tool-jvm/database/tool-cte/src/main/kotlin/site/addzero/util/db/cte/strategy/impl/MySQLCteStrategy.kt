package site.addzero.util.db.cte.strategy.impl

import org.springframework.stereotype.Component
import site.addzero.util.db.DatabaseType
import site.addzero.util.db.cte.strategy.CteStrategy

/**
 * MySQL数据库的CTE策略实现
 */
@Component
class MySQLCteStrategy : CteStrategy {

    override fun supports(databaseType: DatabaseType): Boolean {
        return databaseType == DatabaseType.MYSQL
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
        
        // 合并WHERE条件
        val whereClause = if (finalCustomSqlSegment.isBlank()) {
            ""
        } else {
            // 移除finalCustomSqlSegment中的"WHERE"关键字，然后合并条件
            val condition = finalCustomSqlSegment.trim().removePrefix("WHERE").trim()
            if (condition.isEmpty()) {
                ""
            } else {
                "WHERE $condition"
            }
        }
        
        return """
            WITH RECURSIVE 
            ${recursiveDataUp},
            
            deduplicated_data AS (
                SELECT rd.*,
                       ROW_NUMBER() OVER (PARTITION BY rd.${id} ORDER BY rd.tree_depth ASC) as rn
                FROM recursive_data_up rd
            ),
            
            final_data AS (
                SELECT t.*, dd.tree_depth, dd.tree_direction, dd.tree_path, dd.cycle_detection_path, dd.rn,
                       CASE 
                           WHEN dd.tree_depth = 0 AND dd.${pid} IS NOT NULL THEN
                               -- 对于有父节点的锚点节点，面包屑应该是父节点+当前节点
                               (SELECT CONCAT(parent.${breadcrumbColumn ?: id}, ',', dd.${breadcrumbColumn ?: id})
                                FROM deduplicated_data parent 
                                WHERE parent.${id} = dd.${pid} AND parent.rn = 1)
                           WHEN dd.tree_depth > 0 AND dd.${pid} IS NULL THEN
                               -- 对于根节点，面包屑应该只是自己的ID
                               dd.${breadcrumbColumn ?: id}
                           ELSE dd.tree_breadcrumb
                       END as tree_breadcrumb
                FROM deduplicated_data dd
                JOIN ${tableName} t ON t.${id} = dd.${id}
                WHERE dd.rn = 1
            )
            
            SELECT *
            FROM final_data
            ${whereClause}
            ORDER BY tree_depth, ${id};
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
        val recursiveDataUp = generateRecursiveDataUp(tableName, id, pid, customSqlSegment, returnBreadcrumb, breadcrumbColumn)

        // 处理最终的过滤条件
        val finalCondition = if (finalCustomSqlSegment.isNotBlank()) {
            val condition = finalCustomSqlSegment.trim().removePrefix("WHERE").trim()
            if (condition.isNotEmpty()) {
                "AND $condition"
            } else ""
        } else ""

        return """
            WITH RECURSIVE 
            ${recursiveDataDown},
            
            ${recursiveDataUp},
            
            combined_data AS (
                SELECT * FROM recursive_data_up WHERE tree_depth = 0
                UNION 
                SELECT * FROM recursive_data_up WHERE tree_depth > 0
                UNION
                SELECT * FROM recursive_data_down WHERE tree_depth > 0
            ),
            
            deduplicated_data AS (
                SELECT cd.*,
                       ROW_NUMBER() OVER (PARTITION BY cd.${id} ORDER BY 
                           CASE cd.tree_direction 
                               WHEN 'up' THEN 1 
                               WHEN 'down' THEN 2 
                               ELSE 3 
                           END,
                           cd.tree_depth ASC
                       ) as rn
                FROM combined_data cd
            ),
            
            final_data AS (
                SELECT t.*, dd.tree_depth, dd.tree_direction, dd.tree_path, dd.cycle_detection_path, dd.rn,
                       CASE 
                           WHEN dd.tree_depth = 0 AND dd.${pid} IS NOT NULL THEN
                               -- 对于有父节点的锚点节点，面包屑应该是父节点+当前节点
                               (SELECT CONCAT(parent.${breadcrumbColumn ?: id}, ',', dd.${breadcrumbColumn ?: id})
                                FROM deduplicated_data parent 
                                WHERE parent.${id} = dd.${pid} AND parent.rn = 1)
                           WHEN dd.tree_depth > 0 AND dd.${pid} IS NULL THEN
                               -- 对于根节点，面包屑应该只是自己的ID
                               dd.${breadcrumbColumn ?: id}
                           ELSE dd.tree_breadcrumb
                       END as tree_breadcrumb
                FROM deduplicated_data dd
                JOIN ${tableName} t ON t.${id} = dd.${id}
                WHERE dd.rn = 1
            )
            
            SELECT *
            FROM final_data
            ${finalCondition}
            ORDER BY tree_direction, tree_depth, ${id};
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
            "CAST(t.${breadcrumbField} AS CHAR(1000)) as tree_breadcrumb,"
        } else {
            "CAST(NULL AS CHAR(1000)) as tree_breadcrumb,"
        }
        val breadcrumbRecursive = if (returnBreadcrumb) {
            "CONCAT(rd.tree_breadcrumb, ',', t.${breadcrumbField}) as tree_breadcrumb,"
        } else {
            "CAST(NULL AS CHAR(1000)) as tree_breadcrumb,"
        }
        return """
            recursive_data_down AS (
                SELECT 
                    t.*,
                    0 as tree_depth,
                    'down' as tree_direction,
                    CAST(t.${id} AS CHAR(1000)) as tree_path,
                    ${breadcrumbAnchor}
                    CAST(t.${id} AS CHAR(2000)) as cycle_detection_path
                FROM ${tableName} t
                ${customSqlSegment} 
                
                UNION ALL
                
                SELECT 
                    t.*,
                    rd.tree_depth + 1 as tree_depth,
                    'down' as tree_direction,
                    CONCAT(rd.tree_path, ',', t.${id}) as tree_path,
                    ${breadcrumbRecursive}
                    CONCAT(rd.cycle_detection_path, ',', t.${id}) as cycle_detection_path
                FROM ${tableName} t
                INNER JOIN recursive_data_down rd ON t.${pid} = rd.${id}
                WHERE POSITION(CONCAT(',', t.${id}, ',') IN CONCAT(',', rd.cycle_detection_path, ',')) = 0
                  AND rd.tree_depth < 100
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
            "CAST(t.${breadcrumbField} AS CHAR(1000)) as tree_breadcrumb,"
        } else {
            "CAST(NULL AS CHAR(1000)) as tree_breadcrumb,"
        }
        return """
            recursive_data_up AS (
                SELECT 
                    t.*,
                    0 as tree_depth,
                    'up' as tree_direction,
                    CAST(t.${id} AS CHAR(1000)) as tree_path,
                    ${breadcrumbAnchor}
                    CAST(t.${id} AS CHAR(2000)) as cycle_detection_path
                FROM ${tableName} t
                ${customSqlSegment}  
                
                UNION ALL
                
                SELECT 
                    t.*,
                    ru.tree_depth + 1 as tree_depth,
                    'up' as tree_direction,
                    CONCAT(t.${id}, ',', ru.tree_path) as tree_path,
                    CASE 
                        WHEN ru.tree_breadcrumb IS NOT NULL AND ru.tree_breadcrumb != '' THEN
                            CONCAT(ru.tree_breadcrumb, ',', t.${breadcrumbField})
                        ELSE
                            CAST(t.${breadcrumbField} AS CHAR(1000))
                    END as tree_breadcrumb,
                    CONCAT(t.${id}, ',', ru.cycle_detection_path) as cycle_detection_path
                FROM ${tableName} t
                INNER JOIN recursive_data_up ru ON t.${id} = ru.${pid} AND ru.${pid} IS NOT NULL
                WHERE POSITION(CONCAT(',', t.${id}, ',') IN CONCAT(',', ru.cycle_detection_path, ',')) = 0
                  AND ru.tree_depth < 100
            )
        """.trimIndent()
    }
}