package site.addzero.entity.low_table


/**
 * 搜索条件类
 */
data class StateSearch(
    val columnKey: String="",
    val operator: EnumSearchOperator = EnumSearchOperator.EQ,
    val columnValue: Any? = null,
    val logicType: EnumLogicOperator = EnumLogicOperator.AND
)
