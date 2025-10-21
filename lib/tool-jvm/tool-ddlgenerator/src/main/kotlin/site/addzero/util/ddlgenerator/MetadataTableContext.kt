package site.addzero.util.ddlgenerator

import site.addzero.util.ddlgenerator.inter.MetadataExtractor
import site.addzero.util.ddlgenerator.inter.TableContext
import site.addzero.util.ddlgenerator.model.TableDefinition

/**
 * 基于元数据提取器的表上下文实现
 * 用户可以通过提供MetadataExtractor列表来创建TableContext实例
 */
class MetadataTableContext(private val extractors: List<MetadataExtractor>) : TableContext {
    private val tables: List<TableDefinition> by lazy { extractors.map { it.extractTableDefinition() } }
    private val tableNameToDefinition: Map<String, TableDefinition> by lazy { tables.associateBy { it.name } }
    
    override fun getTableDefinitions(): List<TableDefinition> = tables
    
    override fun getTableDefinition(tableName: String): TableDefinition? {
        return tableNameToDefinition[tableName]
    }
    
    override fun getTableDependencies(): Map<String, List<String>> {
        val dependencies = mutableMapOf<String, List<String>>()
        for (extractor in extractors) {
            val table = extractor.extractTableDefinition()
            dependencies[table.name] = extractor.extractDependencies()
        }
        return dependencies
    }
    
    override fun getDependentTables(tableName: String): List<String> {
        val dependents = mutableListOf<String>()
        for (extractor in extractors) {
            val table = extractor.extractTableDefinition()
            if (extractor.extractDependencies().contains(tableName)) {
                dependents.add(table.name)
            }
        }
        return dependents
    }
    
    companion object {
        /**
         * 从元数据提取器列表创建TableContext实例
         */
        fun fromExtractors(extractors: List<MetadataExtractor>): TableContext {
            return MetadataTableContext(extractors)
        }
    }
}