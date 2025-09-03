package com.addzero.component.table.original

import com.addzero.component.table.original.model.ColumnAlignment
import com.addzero.component.table.original.model.ColumnDataType
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
    val key: String,

    /**
     * 列显示名称
     */
    val label: String,

    /**
     * 列宽度（dp）
     */
    val width: Float = 150f,

    /**
     * 是否可见
     */
    val visible: Boolean = true,

    /**
     * 列对齐方式
     */
    val alignment: ColumnAlignment = ColumnAlignment.CENTER,

    /**
     * 数据类型
     */
    val dataType: ColumnDataType = ColumnDataType.TEXT,

    /**
     * 列顺序（用于排序）
     */
    val order: Int = 0
)

