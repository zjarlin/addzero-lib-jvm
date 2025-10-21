package site.addzero.util.ddlgenerator

import site.addzero.util.ddlgenerator.inter.TableContext
import site.addzero.util.ddlgenerator.model.TableDefinition

/**
 * 依赖解析器
 * 用于解析表之间的依赖关系，确保正确的创建顺序
 */
class DependencyResolver {
    
    /**
     * 根据依赖关系对表进行排序
     * 使用拓扑排序算法确保依赖表在被依赖表之前创建
     */
    fun resolveCreationOrder(context: TableContext): List<TableDefinition> {
        val dependencies = context.getTableDependencies()
        val tables = context.getTableDefinitions()
        val tableNameToDefinition = tables.associateBy { it.name }
        
        // 构建依赖图
        val graph = mutableMapOf<String, MutableList<String>>()
        for (table in tables) {
            graph[table.name] = dependencies[table.name]?.toMutableList() ?: mutableListOf()
        }
        
        // 拓扑排序
        val result = mutableListOf<TableDefinition>()
        val visited = mutableSetOf<String>()
        val tempMark = mutableSetOf<String>()
        
        fun visit(node: String) {
            if (tempMark.contains(node)) {
                throw IllegalStateException("Circular dependency detected involving table: $node")
            }
            
            if (!visited.contains(node)) {
                tempMark.add(node)
                val dependencies = graph[node] ?: emptyList()
                for (dependency in dependencies) {
                    visit(dependency)
                }
                tempMark.remove(node)
                visited.add(node)
                
                tableNameToDefinition[node]?.let { result.add(it) }
            }
        }
        
        for (table in tables) {
            if (!visited.contains(table.name)) {
                visit(table.name)
            }
        }
        
        return result
    }
    
    /**
     * 获取删除顺序（与创建顺序相反）
     */
    fun resolveDeletionOrder(context: TableContext): List<TableDefinition> {
        return resolveCreationOrder(context).reversed()
    }
}