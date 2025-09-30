package site.addzero.entity.low_table

import kotlinx.serialization.Serializable


/**
 * 排序状态
 *
 * @param columnKey 排序列的键
 * @param direction 排序方向
 */
data class StateSort(
    val columnKey: String,
    val direction: EnumSortDirection
)
