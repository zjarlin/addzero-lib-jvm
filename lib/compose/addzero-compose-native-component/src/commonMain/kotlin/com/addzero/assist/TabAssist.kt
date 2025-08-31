package com.addzero.assist

import com.addzero.entity.low_table.EnumSortDirection
import com.addzero.entity.low_table.StateSort

fun getSortDirection(key: String, stateSorts: MutableSet<StateSort>):
        EnumSortDirection {
    val sortDirection = stateSorts.find { it.columnKey == key }?.direction ?: EnumSortDirection.NONE
    return sortDirection
}


