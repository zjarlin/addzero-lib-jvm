package com.gisroad.business.util.easyexcel.find_merge_range


/**
 * @author zjarlin
 * @since 2023/11/14 17:30
 */
internal enum class MergeType(string: String) {
    横向优先("1"), 纵向优先("2"), Range面积优先("3");

    val type: String? = null
}
