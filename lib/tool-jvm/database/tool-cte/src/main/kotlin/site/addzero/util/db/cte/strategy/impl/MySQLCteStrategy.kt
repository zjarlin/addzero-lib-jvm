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
            WITH RECURSIVE 
            ${recursiveDataUp},
            
            deduplicated_data AS (
                SELECT rd.*,
                       ROW_NUMBER() OVER (PARTITION BY rd.${id} ORDER BY rd.tree_depth ASC) as rn
                FROM recursive_data_up rd
                ${whereClause}
            )
            
            SELECT *
            FROM deduplicated_data
            WHERE rn = 1
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
        val recursiveDataUp = generateRecursiveDataUp(tableName, id, pid, customSqlSegment, returnBreadcrumb, breadcrumbColumn)

        return """
            WITH RECURSIVE 
            ${recursiveDataDown},
            
            ${recursiveDataUp},
            
            combined_data AS (
                SELECT * FROM recursive_data_up WHERE tree_depth > 0
                UNION 
                SELECT * FROM recursive_data_down
            ),
            
            deduplicated_data AS (
                SELECT cd.*,
                       ROW_NUMBER() OVER (PARTITION BY cd.${id} ORDER BY cd.tree_depth ASC) as rn
                FROM combined_data cd
            )
            
            SELECT *
            FROM deduplicated_data
            WHERE rn = 1
            ${finalCustomSqlSegment.let { if (it.isNotBlank()) "AND ${it.trim().removePrefix("WHERE").trim()}" else "" }}
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
                    rd.tree_depth + 1,
                    'down',
                    CONCAT(rd.tree_path, ',', t.${id}),
                    ${breadcrumbRecursive}
                    CONCAT(rd.cycle_detection_path, ',', t.${id})
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
                    ru.tree_depth + 1,
                    'up',
                    CONCAT(t.${id}, ',', ru.tree_path),
                    CASE 
                        WHEN t.${pid} IS NULL THEN
                            CAST(t.${breadcrumbField} AS CHAR(1000))
                        WHEN ru.tree_breadcrumb IS NOT NULL AND ru.tree_breadcrumb != '' THEN
                            CONCAT(t.${breadcrumbField}, ',', ru.tree_breadcrumb)
                        ELSE
                            CAST(t.${breadcrumbField} AS CHAR(1000))
                    END as tree_breadcrumb,
                    CONCAT(t.${id}, ',', ru.cycle_detection_path)
                FROM ${tableName} t
                INNER JOIN recursive_data_up ru ON t.${id} = ru.${pid} AND ru.${pid} IS NOT NULL
                WHERE POSITION(CONCAT(',', t.${id}, ',') IN CONCAT(',', ru.cycle_detection_path, ',')) = 0
                  AND ru.tree_depth < 100
            )
        """.trimIndent()
    }
}

