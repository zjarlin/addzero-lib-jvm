package site.addzero.apt.dict.annotations

/**
 * 字典字段注解
 * 
 * 支持两种翻译模式：
 * 1. 系统内置字典：使用 dictCode
 * 2. 自定义表字典：使用 table + codeColumn + nameColumn
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class DictField(
    /**
     * 系统内置字典编码
     */
    val dictCode: String = "",
    
    /**
     * 自定义表名
     */
    val table: String = "",
    
    /**
     * 编码列名（用于自定义表）
     */
    val codeColumn: String = "",
    
    /**
     * 名称列名（用于自定义表）
     */
    val nameColumn: String = "",
    
    /**
     * 目标字段名（翻译后的字段名）
     * 如果不指定，默认为原字段名 + "Name"
     */
    val targetField: String = "",
    
    /**
     * SpEL 表达式（用于复杂翻译逻辑）
     */
    val spelExp: String = "",
    
    /**
     * 是否忽略空值，默认为 true
     */
    val ignoreNull: Boolean = true,
    
    /**
     * 默认值（当翻译失败时使用）
     */
    val defaultValue: String = "",
    
    /**
     * 是否缓存翻译结果，默认为 true
     */
    val cached: Boolean = true
)