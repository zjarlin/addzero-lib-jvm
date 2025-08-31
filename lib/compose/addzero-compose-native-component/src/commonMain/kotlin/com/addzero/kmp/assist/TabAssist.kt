package com.addzero.kmp.assist

import com.addzero.kmp.entity.low_table.EnumSortDirection
import com.addzero.kmp.entity.low_table.StateSort

fun getSortDirection(key: String, stateSorts: MutableSet<StateSort>):
        EnumSortDirection {
    val sortDirection = stateSorts.find { it.columnKey == key }?.direction ?: EnumSortDirection.NONE
    return sortDirection
}


