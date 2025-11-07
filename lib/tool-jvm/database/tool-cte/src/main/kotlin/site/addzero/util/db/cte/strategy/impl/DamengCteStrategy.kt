package site.addzero.util.db.cte.strategy.impl

import org.springframework.stereotype.Component
import site.addzero.util.db.DatabaseType
import site.addzero.util.db.cte.strategy.CteStrategy

/**
 * 达梦数据库的CTE策略实现
 */
@Component
class DamengCteStrategy : CteStrategy {

    override fun supports(databaseType: DatabaseType): Boolean {
        return databaseType == DatabaseType.DAMENG
    }

    override fun generateRecursiveTreeQuerySql(
        tableName: String, id: String, pid: String, customSqlSegment: String, finalCustomSqlSegment: String
    ): String {

        val trimIndent = """
       WITH 
recursive_data_down AS (
    SELECT 
        t.*,
        0 as tree_depth,
        'down' as tree_direction,
        CAST(t.${id} AS VARCHAR(1000)) as tree_path
    FROM ${tableName} t
    ${customSqlSegment}
    
    UNION ALL
    
    SELECT 
        t.*,
        rd.tree_depth + 1,
        'down',
        rd.tree_path || ',' || t.${id}
    FROM ${tableName} t
    INNER JOIN recursive_data_down rd ON t.${pid} = rd.${id}
),

recursive_data_up AS (
    SELECT 
        t.*,
        0 as tree_depth,
        'up' as tree_direction,
        CAST(t.${id} AS VARCHAR(1000)) as tree_path
    FROM ${tableName} t
    ${customSqlSegment}
    
    UNION ALL
    
    SELECT 
        t.*,
        ru.tree_depth + 1,
        'up',
        t.${id} || ',' || ru.tree_path
    FROM ${tableName} t
    INNER JOIN recursive_data_up ru ON t.${id} = ru.${pid}
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






        return trimIndent
    }
}
