package site.addzero.util.ddlgenerator.model

/**
 * 索引定义
 */
data class IndexDefinition(
    val name: String,
    val columnNames: List<String>,
    val unique: Boolean = false
)