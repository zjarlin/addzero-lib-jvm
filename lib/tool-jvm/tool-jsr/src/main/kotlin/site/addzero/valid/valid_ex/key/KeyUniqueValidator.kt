package site.addzero.valid.valid_ex.key

/**
 * 唯一性校验器接口，用于校验标记了[@Key](file:///Users/zjarlin/IdeaProjects/addzero-lib-jvm/lib/tool-jvm/tool-jsr/src/main/kotlin/site/addzero/valid/valid_ex/key/Key.kt#L16-L19)注解的字段组合的唯一性
 *
 * @author zjarlin
 * @since 2025/10/22
 */
interface KeyUniqueValidator {
    
    /**
     * 校验指定分组的字段组合在数据库中的唯一性
     *
     * @param tableName 表名
     * @param group 分组名称
     * @param fieldValues 字段名和值的映射
     * @param excludeId 需要排除的记录ID（用于更新时避免与自己比较）
     * @return 如果唯一返回true，否则返回false
     */
    fun isUnique(
        tableName: String,
        group: String,
        fieldValues: Map<String, Any?>,
        excludeId: Any? = null
    ): Boolean
}