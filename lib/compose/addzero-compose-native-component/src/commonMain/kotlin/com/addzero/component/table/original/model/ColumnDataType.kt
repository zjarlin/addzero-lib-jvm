package com.addzero.component.table.original.model

import kotlinx.serialization.Serializable

/**
 * 列数据类型
 */
@Serializable
enum class ColumnDataType {
    /**
     * 文本
     */
    TEXT,

    /**
     * 数字
     */
    NUMBER,

    /**
     * 日期
     */
    DATE,

    /**
     * 日期时间
     */
    DATETIME,

    /**
     * 布尔值
     */
    BOOLEAN,

    /**
     * 邮箱
     */
    EMAIL,

    /**
     * 链接
     */
    URL,

    /**
     * 电话
     */
    PHONE,

    /**
     * 货币
     */
    CURRENCY,

    /**
     * 百分比
     */
    PERCENTAGE,

    /**
     * 图片
     */
    IMAGE,

    /**
     * 自定义
     */
    CUSTOM
}
