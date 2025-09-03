package com.addzero.component.table.original

import kotlinx.serialization.Serializable

/**
 * 表格配置类 - 支持序列化，方便后台配置
 * 只包含实际使用的配置项
 */
@Serializable
data class TableConfig(
    val columnConfigs: List<ColumnConfig>,
)
