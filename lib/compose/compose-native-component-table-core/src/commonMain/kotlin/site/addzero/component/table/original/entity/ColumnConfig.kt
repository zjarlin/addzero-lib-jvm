package site.addzero.component.table.original.entity

/**
 * 表格列配置 - 可序列化，支持后台配置
 * 只包含实际使用的配置项
 */
data class ColumnConfig(
    /**
     * 列的唯一标识符
     */
    val key: String = "",

    /**
     * 列标题或备注。
     */
    val comment: String = "",

    /**
     * 列数据类型标识。
     */
    val kmpType: String = "",

    /**
     * 列宽度（dp）
     */
    val width: Float = 150f,

    /**
     * 列顺序（用于排序）
     */
    val order: Int = 0,

    /**
     * 是否展示筛选入口。
     */
    val showFilter: Boolean = true,

    /**
     * 是否展示排序入口。
     */
    val showSort: Boolean = true,
)
