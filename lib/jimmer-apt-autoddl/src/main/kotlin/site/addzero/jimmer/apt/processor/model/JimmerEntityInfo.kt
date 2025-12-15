package site.addzero.jimmer.apt.processor.model

/**
 * Jimmer实体特定信息
 * 
 * 包含Jimmer框架特有的实体配置信息
 */
data class JimmerEntityInfo(
    /** 表名（如果在@Entity中指定） */
    val tableName: String? = null,
    
    /** 微服务名称 */
    val microServiceName: String? = null,
    
    /** 是否启用逻辑删除 */
    val logicalDeleted: Boolean = false,
    
    /** 是否为不可变实体 */
    val immutable: Boolean = true,
    
    /** 是否为抽象实体 */
    val isAbstract: Boolean = false
)