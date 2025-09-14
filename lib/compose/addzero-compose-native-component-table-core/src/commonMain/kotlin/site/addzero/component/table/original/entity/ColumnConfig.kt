package site.addzero.component.table.original.entity

import kotlinx.serialization.Serializable

/**
 * 表格列配置 - 可序列化，支持后台配置
 * 只包含实际使用的配置项
 */
@Serializable
data class ColumnConfig(
    /**
     * 列的唯一标识符
     */
    val key: String = "",

    val comment: String = "",

    val kmpType: String = "",

    /**
     * 列宽度（dp）
     */
    val width: Float = 150f,

    /**
     * 列顺序（用于排序）
     */
    val order: Int = 0,

    val showFilter: Boolean = true,

    val showSort: Boolean = true,
)

