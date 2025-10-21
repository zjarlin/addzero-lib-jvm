package site.addzero.util.ddlgenerator.model

/**
 * 外键约束定义
 */
data class ForeignKeyDefinition(
    val name: String,
    val columnName: String,
    val referencedTable: String,
    val referencedColumnName: String
)