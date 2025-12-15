package site.addzero.jimmer.apt.processor.model

/**
 * Jimmer字段特定信息
 * 
 * 包含Jimmer框架特有的字段配置信息
 */
data class JimmerFieldInfo(
    /** 列名（如果在@Column中指定） */
    val columnName: String? = null,
    
    /** 是否可插入 */
    val insertable: Boolean = true,
    
    /** 是否可更新 */
    val updatable: Boolean = true,
    
    /** 计算公式（@Formula注解的值） */
    val formula: String? = null,
    
    /** IdView配置（@IdView注解的值） */
    val idView: String? = null,
    
    /** 是否为业务键 */
    val key: Boolean = false,
    
    /** 是否为版本字段 */
    val version: Boolean = false,
    
    /** 是否为逻辑删除字段 */
    val logicalDeleted: Boolean = false,
    
    /** 生成值策略 */
    val generatedValueStrategy: GeneratedValueStrategy? = null
)

/**
 * 生成值策略枚举
 */
enum class GeneratedValueStrategy {
    AUTO,
    IDENTITY,
    SEQUENCE,
    TABLE,
    UUID
}