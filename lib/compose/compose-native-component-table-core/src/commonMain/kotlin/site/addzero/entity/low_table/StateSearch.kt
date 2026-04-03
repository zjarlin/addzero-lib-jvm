package site.addzero.entity.low_table

import kotlinx.serialization.Contextual

/**
 * 单列搜索条件。
 */
data class StateSearch(
    val columnKey: String = "",
    val operator: EnumSearchOperator = EnumSearchOperator.EQ,
    @Contextual
    val columnValue: Any? = null,
    val logicType: EnumLogicOperator = EnumLogicOperator.AND,
)
