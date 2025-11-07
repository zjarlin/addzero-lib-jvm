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

    override fun generateRecursiveTreeQuerySql(
        tableName: String,
        id: String,
        pid: String,
        customSqlSegment: String,
        finalCustomSqlSegment: String
    ): String {
        return """
            WITH RECURSIVE 
            recursive_data_down AS (
                SELECT 
                    t.*,
                    0 as tree_depth,
                    'down' as tree_direction,
                    CAST(t.${id} AS CHAR(1000)) as tree_path,
                    CAST(CONCAT(',', t.${id}, ',') AS CHAR(2000)) as cycle_detection_path
                FROM ${tableName} t
                ${customSqlSegment} 
                
                UNION ALL
                
                SELECT 
                    t.*,
                    rd.tree_depth + 1,
                    'down',
                    CONCAT(rd.tree_path, ',', t.${id}),
                    CONCAT(rd.cycle_detection_path, t.${id}, ',')
                FROM ${tableName} t
                INNER JOIN recursive_data_down rd ON t.${pid} = rd.${id}
                WHERE NOT FIND_IN_SET(t.${id}, rd.cycle_detection_path)
                  AND rd.tree_depth < 100
            ),
            
            recursive_data_up AS (
                SELECT 
                    t.*,
                    0 as tree_depth,
                    'up' as tree_direction,
                    CAST(t.${id} AS CHAR(1000)) as tree_path,
                    CAST(CONCAT(',', t.${id}, ',') AS CHAR(2000)) as cycle_detection_path
                FROM ${tableName} t
                ${customSqlSegment}  
                
                UNION ALL
                
                SELECT 
                    t.*,
                    ru.tree_depth + 1,
                    'up',
                    CONCAT(t.${id}, ',', ru.tree_path),
                    CONCAT(t.${id}, ',', ru.cycle_detection_path)
                FROM ${tableName} t
                INNER JOIN recursive_data_up ru ON t.${id} = ru.${pid}
                WHERE NOT FIND_IN_SET(t.${id}, ru.cycle_detection_path)
                  AND ru.tree_depth < 100
            ),
            
            combined_data AS (
                SELECT * FROM recursive_data_up
                UNION 
                SELECT * FROM recursive_data_down
            )
            
            SELECT *
            FROM combined_data
            ${finalCustomSqlSegment}  
            ORDER BY tree_direction, tree_depth;
        """.trimIndent()
    }
}