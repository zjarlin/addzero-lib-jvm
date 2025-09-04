package com.addzero.component.table.original.entity

import kotlinx.serialization.Serializable

/**
 * 表格整体布局配置
 */
@Serializable
data class TableLayoutConfig(
    val indexColumnWidthDp: Float = 80f,
    val actionColumnWidthDp: Float = 120f,
    val headerHeightDp: Float = 56f,
    val rowHeightDp: Float = 56f,
    val defaultColumnWidthDp: Float = 150f,
    // 自适应列宽相关
    val enableAutoWidth: Boolean = false,
    val autoWidthSampleRows: Int = 50,
    val autoWidthMinDp: Float = 80f,
    val autoWidthMaxDp: Float = 320f
)
