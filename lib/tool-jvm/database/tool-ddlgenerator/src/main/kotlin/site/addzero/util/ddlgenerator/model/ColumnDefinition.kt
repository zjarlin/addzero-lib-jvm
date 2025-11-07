package site.addzero.util.ddlgenerator.model

/**
 * 列定义数据类
 */
data class ColumnDefinition(
    val name: String,
    val type: ColumnType,
    val nullable: Boolean = true,
    val primaryKey: Boolean = false,
    val autoIncrement: Boolean = false,
    val defaultValue: String? = null,
    val comment: String? = null
)