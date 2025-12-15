package site.addzero.apt.dict.processor.model

import javax.lang.model.element.TypeElement

/**
 * 实体信息
 */
data class EntityInfo(
    /**
     * 实体类型元素
     */
    val typeElement: TypeElement,
    
    /**
     * 包名
     */
    val packageName: String,
    
    /**
     * 简单类名
     */
    val simpleName: String,
    
    /**
     * 完全限定类名
     */
    val qualifiedName: String,
    
    /**
     * 所有字段信息
     */
    val allFields: List<FieldInfo>,
    
    /**
     * 字典字段信息
     */
    val dictFields: List<DictFieldInfo>,
    
    /**
     * 嵌套实体字段信息
     */
    val nestedEntityFields: List<NestedEntityField>
)

/**
 * 字段信息
 */
data class FieldInfo(
    /**
     * 字段名
     */
    val name: String,
    
    /**
     * 字段类型
     */
    val type: String,
    
    /**
     * 是否为集合类型
     */
    val isCollection: Boolean = false,
    
    /**
     * 集合元素类型（如果是集合）
     */
    val elementType: String? = null
)

/**
 * 嵌套实体字段信息
 */
data class NestedEntityField(
    /**
     * 字段名
     */
    val fieldName: String,
    
    /**
     * 字段类型
     */
    val fieldType: String,
    
    /**
     * 是否为集合类型
     */
    val isCollection: Boolean,
    
    /**
     * 元素类型（如果是集合）
     */
    val elementType: String? = null,
    
    /**
     * 嵌套实体信息
     */
    val nestedEntityInfo: EntityInfo? = null
)