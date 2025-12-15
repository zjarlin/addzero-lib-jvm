package site.addzero.jimmer.apt.processor.model

/**
 * 索引元数据
 * 
 * 描述数据库索引信息
 */
data class IndexMetadata(
    /** 索引名称 */
    val name: String,
    
    /** 表名 */
    val tableName: String,
    
    /** 索引列名列表 */
    val columns: List<String>,
    
    /** 是否为唯一索引 */
    val unique: Boolean = false,
    
    /** 索引类型 */
    val type: IndexType = IndexType.BTREE
)

/**
 * 索引类型枚举
 */
enum class IndexType {
    BTREE,
    HASH,
    FULLTEXT,
    SPATIAL
}