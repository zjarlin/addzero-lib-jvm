package site.addzero.jimmer.apt.processor.model

import site.addzero.util.lsi.clazz.LsiClass

/**
 * Jimmer实体元数据
 * 
 * 包含从Jimmer实体类中提取的所有元数据信息
 */
data class EntityMetadata(
    /** LSI类抽象 */
    val lsiClass: LsiClass,
    
    /** 表名 */
    val tableName: String,
    
    /** 表注释 */
    val comment: String,
    
    /** 字段元数据列表 */
    val fields: List<FieldMetadata>,
    
    /** 关系元数据列表 */
    val relationships: List<RelationshipMetadata>,
    
    /** 索引元数据列表 */
    val indexes: List<IndexMetadata>,
    
    /** Jimmer特定的实体信息 */
    val jimmerEntityInfo: JimmerEntityInfo
)