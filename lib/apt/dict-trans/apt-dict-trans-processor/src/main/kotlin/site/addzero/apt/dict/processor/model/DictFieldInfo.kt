package site.addzero.apt.dict.processor.model

/**
 * 字典字段信息
 */
data class DictFieldInfo(
    /**
     * 原始字段名
     */
    val fieldName: String,
    
    /**
     * 字段类型
     */
    val fieldType: String,
    
    /**
     * 字典配置列表（支持可重复注解）
     */
    val dictConfigs: List<DictConfig>
)

/**
 * 字典配置信息
 */
data class DictConfig(
    /**
     * 系统字典编码
     */
    val dictCode: String? = null,
    
    /**
     * 自定义表名
     */
    val tableName: String? = null,
    
    /**
     * 编码列名
     */
    val codeColumn: String = "code",
    
    /**
     * 名称列名
     */
    val nameColumn: String = "name",
    
    /**
     * 序列化别名
     */
    val serializationAlias: String? = null
) {
    /**
     * 是否为系统字典
     */
    val isSystemDict: Boolean
        get() = !dictCode.isNullOrBlank()
    
    /**
     * 获取生成的字典字段名
     */
    fun getGeneratedFieldName(): String {
        return serializationAlias?.takeIf { it.isNotBlank() }
            ?: toCamelCase(nameColumn)
    }
    
    /**
     * 将下划线命名转换为小驼峰命名
     */
    private fun toCamelCase(input: String): String {
        return input.split("_")
            .mapIndexed { index, part ->
                if (index == 0) part.lowercase()
                else part.lowercase().replaceFirstChar { it.uppercase() }
            }
            .joinToString("")
    }
}