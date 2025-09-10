package com.addzero.component.table.original.entity

import kotlinx.serialization.Serializable

/**
 * 表格整体布局配置
 *
 * @property indexColumnWidthDp 索引列宽度（单位：dp）
 * @property actionColumnWidthDp 操作列宽度（单位：dp）
 * @property headerHeightDp 表头高度（单位：dp）
 * @property rowHeightDp 表格行高（单位：dp）
 * @property defaultColumnWidthDp 默认列宽（单位：dp）
 * @property enableAutoWidth 是否启用自适应列宽
 * @property autoWidthSampleRows 自适应列宽采样行数
 * @property autoWidthMinDp 自适应列宽最小值（单位：dp）
 * @property autoWidthMaxDp 自适应列宽最大值（单位：dp）
 */
@Serializable
data class TableLayoutConfig(
    // AddBizTable 特有参数
    val showPagination: Boolean = true,
    val showSearchBar: Boolean = true,
    val showBatchActions: Boolean = true,
    val showRowSelection: Boolean = true,
    val showDefaultRowActions: Boolean = true,
    val enableSorting: Boolean = true,
    val enableAdvancedSearch: Boolean = true,
    val indexColumnWidthDp: Float = 80f,
    val actionColumnWidthDp: Float = 150f,
    // ui
    val headerHeightDp: Float = 56f,
    // ui
    val rowHeightDp: Float = 56f,
    // ui
    val defaultColumnWidthDp: Float = 150f,
    // ui
    val enableAutoWidth: Boolean = false,
    val autoWidthSampleRows: Int = 50,
    val autoWidthMinDp: Float = 80f,
    val autoWidthMaxDp: Float = 320f
)
