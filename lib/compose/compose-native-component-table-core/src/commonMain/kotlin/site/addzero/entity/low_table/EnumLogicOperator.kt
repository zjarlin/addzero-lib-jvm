package site.addzero.entity.low_table

/**
 * 高级搜索条件之间的逻辑连接方式。
 */
enum class EnumLogicOperator(val displayName: String) {
    AND("且"), OR("或")
}
