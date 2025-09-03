package com.addzero.component.table.original.model

import kotlinx.serialization.Serializable

/**
 * 列对齐方式
 */
@Serializable
enum class ColumnAlignment {
    /**
     *左对齐
     */
    START,

    /**
     *居中对齐
     */
    CENTER,

    /**
     *右对齐
     */
    END
}
