package site.addzero.util.db.cte.strategy.impl

import org.springframework.stereotype.Component
import site.addzero.util.db.DatabaseType
import site.addzero.util.db.cte.strategy.CteStrategy

/**
 * 华为高斯数据库的CTE策略实现
 */
@Component
class GaussDbCteStrategy : CteStrategy {

    override fun supports(databaseType: DatabaseType): Boolean {
        return databaseType == DatabaseType.GAUSSDB
    }

    override fun generateRecursiveTreeQuerySqlUp(
        tableName: String,
        id: String,
        pid: String,
        customSqlSegment: String,
        finalCustomSqlSegment: String
    ): String {
        val recursiveDataUp = generateRecursiveDataUp(tableName, id, pid, customSqlSegment)

        return """
            WITH RECURSIVE 
            ${recursiveDataUp}
            
            SELECT *
            FROM recursive_data_up
            ${finalCustomSqlSegment}  
            ORDER BY tree_depth;
        """.trimIndent()
    }

    override fun generateRecursiveTreeQuerySqlUpAndDown(
        tableName: String,
        id: String,
        pid: String,
        customSqlSegment: String,
        finalCustomSqlSegment: String
    ): String {
        val recursiveDataDown = generateRecursiveDataDown(tableName, id, pid, customSqlSegment)
        val recursiveDataUp = generateRecursiveDataUp(tableName, id, pid, customSqlSegment)

        return """
            WITH RECURSIVE 
            ${recursiveDataDown},
            
            ${recursiveDataUp},
            
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

    /**
     * 生成向下递归的CTE片段
     */
    private fun generateRecursiveDataDown(
        tableName: String,
        id: String,
        pid: String,
        customSqlSegment: String
    ): String {
        return """
            recursive_data_down AS (
                SELECT 
                    t.*,
                    0 as tree_depth,
                    'down' as tree_direction,
                    CAST(t.${id} AS TEXT) as tree_path
                FROM ${tableName} t
                ${customSqlSegment} 
                
                UNION ALL
                
                SELECT 
                    t.*,
                    rd.tree_depth + 1,
                    'down',
                    rd.tree_path || ',' || CAST(t.${id} AS TEXT)
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
        customSqlSegment: String
    ): String {
        return """
            recursive_data_up AS (
                SELECT 
                    t.*,
                    0 as tree_depth,
                    'up' as tree_direction,
                    CAST(t.${id} AS TEXT) as tree_path
                FROM ${tableName} t
                ${customSqlSegment}  
                
                UNION ALL
                
                SELECT 
                    t.*,
                    ru.tree_depth + 1,
                    'up',
                    CAST(t.${id} AS TEXT) || ',' || ru.tree_path
                FROM ${tableName} t
                INNER JOIN recursive_data_up ru ON t.${id} = ru.${pid}
            )
        """.trimIndent()
    }
}
