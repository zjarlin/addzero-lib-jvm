package site.addzero.util.db.cte.strategy.impl

import org.springframework.stereotype.Component
import site.addzero.util.db.DatabaseType
import site.addzero.util.db.cte.strategy.CteStrategy

/**
 * 简化版MySQL数据库的CTE策略实现，解决列数不一致问题
 */
@Component
class SimpleMySQLCteStrategy : CteStrategy {

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
            $recursiveDataUp,
            
            deduplicated_data AS (
                SELECT rd.*,
                       ROW_NUMBER() OVER (PARTITION BY rd.$id ORDER BY rd.tree_depth ASC) as rn
                FROM recursive_data_up rd
            )
            
            SELECT *
            FROM deduplicated_data
            $whereClause
            AND rn = 1
            ORDER BY tree_depth, $id;
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

        // 处理最终的过滤条件
        val finalCondition = if (finalCustomSqlSegment.isNotBlank()) {
            val condition = finalCustomSqlSegment.trim().removePrefix("WHERE").trim()
            if (condition.isNotEmpty()) {
                "AND $condition"
            } else ""
        } else ""

        return """
            WITH RECURSIVE 
            $recursiveDataDown,
            
            $recursiveDataUp,
            
            combined_data AS (
                SELECT * FROM recursive_data_up WHERE tree_depth = 0
                UNION 
                SELECT * FROM recursive_data_up WHERE tree_depth > 0
                UNION
                SELECT * FROM recursive_data_down WHERE tree_depth > 0
            ),
            
            deduplicated_data AS (
                SELECT cd.*,
                       ROW_NUMBER() OVER (PARTITION BY cd.$id ORDER BY 
                           CASE cd.tree_direction 
                               WHEN 'up' THEN 1 
                               WHEN 'down' THEN 2 
                               ELSE 3 
                           END,
                           cd.tree_depth ASC
                       ) as rn
                FROM combined_data cd
            )
            
            SELECT *
            FROM deduplicated_data
            WHERE rn = 1
            $finalCondition
            ORDER BY tree_direction, tree_depth, $id;
        """.trimIndent()
    }

    /**
     * 生成向下递归的CTE片段 - 使用显式字段避免列数不一致问题
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
            "CAST(t.$breadcrumbField AS CHAR(1000)) AS tree_breadcrumb,"
        } else {
            "CAST(NULL AS CHAR(1000)) AS tree_breadcrumb,"
        }
        val breadcrumbRecursive = if (returnBreadcrumb) {
            "CONCAT(rd.tree_breadcrumb, ',', t.$breadcrumbField) AS tree_breadcrumb,"
        } else {
            "CAST(NULL AS CHAR(1000)) AS tree_breadcrumb,"
        }
        
        return """
            recursive_data_down AS (
                SELECT 
                    t.$id,
                    t.$pid,
                    0 AS tree_depth,
                    'down' AS tree_direction,
                    CAST(t.$id AS CHAR(1000)) AS tree_path,
                    ${breadcrumbAnchor}
                    CAST(t.$id AS CHAR(2000)) AS cycle_detection_path
                FROM $tableName t
                $customSqlSegment 
                
                UNION ALL
                
                SELECT 
                    t.$id,
                    t.$pid,
                    rd.tree_depth + 1 AS tree_depth,
                    'down' AS tree_direction,
                    CONCAT(rd.tree_path, ',', t.$id) AS tree_path,
                    ${breadcrumbRecursive}
                    CONCAT(rd.cycle_detection_path, ',', t.$id) AS cycle_detection_path
                FROM $tableName t
                INNER JOIN recursive_data_down rd ON t.$pid = rd.$id
                WHERE POSITION(CONCAT(',', t.$id, ',') IN CONCAT(',', rd.cycle_detection_path, ',')) = 0
                  AND rd.tree_depth < 100
            )
        """.trimIndent()
    }

    /**
     * 生成向上递归的CTE片段 - 使用显式字段避免列数不一致问题
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
            "CAST(t.$breadcrumbField AS CHAR(1000)) AS tree_breadcrumb,"
        } else {
            "CAST(NULL AS CHAR(1000)) AS tree_breadcrumb,"
        }
        
        return """
            recursive_data_up AS (
                SELECT 
                    t.$id,
                    t.$pid,
                    0 AS tree_depth,
                    'up' AS tree_direction,
                    CAST(t.$id AS CHAR(1000)) AS tree_path,
                    ${breadcrumbAnchor}
                    CAST(t.$id AS CHAR(2000)) AS cycle_detection_path
                FROM $tableName t
                $customSqlSegment  
                
                UNION ALL
                
                SELECT 
                    t.$id,
                    t.$pid,
                    ru.tree_depth + 1 AS tree_depth,
                    'up' AS tree_direction,
                    CONCAT(t.$id, ',', ru.tree_path) AS tree_path,
                    CASE 
                        WHEN ru.tree_breadcrumb IS NOT NULL AND ru.tree_breadcrumb != '' THEN
                            CONCAT(t.$breadcrumbField, ',', ru.tree_breadcrumb)
                        ELSE
                            CAST(t.$breadcrumbField AS CHAR(1000))
                    END AS tree_breadcrumb,
                    CONCAT(t.$id, ',', ru.cycle_detection_path) AS cycle_detection_path
                FROM $tableName t
                INNER JOIN recursive_data_up ru ON t.$id = ru.$pid AND ru.$pid IS NOT NULL
                WHERE POSITION(CONCAT(',', t.$id, ',') IN CONCAT(',', ru.cycle_detection_path, ',')) = 0
                  AND ru.tree_depth < 100
            )
        """.trimIndent()
    }
}