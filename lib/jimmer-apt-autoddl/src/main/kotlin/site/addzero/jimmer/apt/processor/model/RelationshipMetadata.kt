package site.addzero.jimmer.apt.processor.model

/**
 * 关系元数据
 * 
 * 描述实体之间的关系信息
 */
data class RelationshipMetadata(
    /** 关系类型 */
    val type: RelationshipType,
    
    /** 源表名 */
    val sourceTable: String,
    
    /** 目标表名 */
    val targetTable: String,
    
    /** 源列名 */
    val sourceColumn: String,
    
    /** 目标列名 */
    val targetColumn: String,
    
    /** 连接表名（用于多对多关系） */
    val joinTable: String? = null,
    
    /** 连接表源列名 */
    val joinSourceColumn: String? = null,
    
    /** 连接表目标列名 */
    val joinTargetColumn: String? = null,
    
    /** 级联操作 */
    val cascadeActions: Set<CascadeAction> = emptySet(),
    
    /** 解除关联操作 */
    val dissociateAction: DissociateAction? = null
)

/**
 * 关系类型枚举
 */
enum class RelationshipType {
    ONE_TO_ONE,
    ONE_TO_MANY,
    MANY_TO_ONE,
    MANY_TO_MANY
}

/**
 * 级联操作枚举
 */
enum class CascadeAction {
    PERSIST,
    MERGE,
    REMOVE,
    REFRESH,
    DETACH,
    ALL
}

/**
 * 解除关联操作枚举
 */
enum class DissociateAction {
    NONE,
    SET_NULL,
    DELETE,
    CHECK
}