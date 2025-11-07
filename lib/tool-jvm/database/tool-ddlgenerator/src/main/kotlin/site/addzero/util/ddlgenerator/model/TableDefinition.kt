package site.addzero.util.ddlgenerator.model

/**
 * 表定义数据类
 */
data class TableDefinition(
    val name: String,
    val columns: List<ColumnDefinition>,
    val comment: String? = null,
    val foreignKeys: List<ForeignKeyDefinition> = emptyList(),
    val indexes: List<IndexDefinition> = emptyList()
)