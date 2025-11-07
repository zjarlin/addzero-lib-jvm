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

    override fun generateRecursiveTreeQuerySql(
        tableName: String,
        id: String,
        pid: String,
        customSqlSegment: String,
        finalCustomSqlSegment: String
    ): String {
        return """
            WITH recursive_data_down (${id}, ${pid}, tree_depth, tree_direction, tree_path) AS (
                SELECT 
                    t.${id},
                    t.${pid},
                    0 as tree_depth,
                    'down' as tree_direction,
                    TO_CHAR(t.${id}) as tree_path
                FROM ${tableName} t
                ${customSqlSegment}
                
                UNION ALL
                
                SELECT 
                    t.${id},
                    t.${pid},
                    rd.tree_depth + 1,
                    'down',
                    rd.tree_path || ',' || TO_CHAR(t.${id})
                FROM ${tableName} t
                INNER JOIN recursive_data_down rd ON t.${pid} = rd.${id}
            ),
            
            recursive_data_up (${id}, ${pid}, tree_depth, tree_direction, tree_path) AS (
                SELECT 
                    t.${id},
                    t.${pid},
                    0 as tree_depth,
                    'up' as tree_direction,
                    TO_CHAR(t.${id}) as tree_path
                FROM ${tableName} t
                ${customSqlSegment}
                
                UNION ALL
                
                SELECT 
                    t.${id},
                    t.${pid},
                    ru.tree_depth + 1,
                    'up',
                    TO_CHAR(t.${id}) || ',' || ru.tree_path
                FROM ${tableName} t
                INNER JOIN recursive_data_up ru ON t.${id} = ru.${pid}
            ),
            
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
}