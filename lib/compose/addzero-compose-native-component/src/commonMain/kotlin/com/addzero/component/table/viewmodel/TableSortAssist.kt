package com.addzero.component.table.viewmodel

import com.addzero.component.table.clean.AddCleanTableViewModel
import com.addzero.entity.low_table.EnumSortDirection
import com.addzero.entity.low_table.StateSort
import com.addzero.kt_util.removeIf


// 排序状态集合
/**
 * 切换排序状态
 * 点击一次：升序(ASC)
 * 点击两次：降序(DESC)
 * 点击三次：无序(NONE)
 */
context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
fun changeSorting(columnKey: String) {
    // 查找当前是否已有该列的排序状态
    val existingSort = addCleanTableViewModel._sortState.find {
        it
            .columnKey == columnKey
    }
    println("【排序处理】列: $columnKey, 当前排序状态: ${existingSort?.direction ?: "NONE"}")

    // 根据当前排序状态决定下一个状态
    val newDirection = when (existingSort?.direction) {
        EnumSortDirection.ASC -> EnumSortDirection.DESC
        EnumSortDirection.DESC -> EnumSortDirection.NONE
        else -> EnumSortDirection.ASC // null或NONE时设为ASC
    }
    println("【排序处理】列: $columnKey, 新排序状态: $newDirection")

    // 创建新的排序状态
    val newSortState = addCleanTableViewModel._sortState.toMutableList()

    // 移除旧的排序状态
    newSortState.removeIf { it.columnKey == columnKey }

    // 只有非NONE状态才添加
    if (newDirection != EnumSortDirection.NONE) {
        newSortState.add(StateSort(columnKey, newDirection))
    }

    // 更新排序状态 - 强制创建新实例
    addCleanTableViewModel._sortState = newSortState.toMutableSet()
}

/**
 * 设置指定列的排序方向
 */
context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
fun setSorting(columnKey: String, direction: EnumSortDirection) {
    val newSortState = addCleanTableViewModel._sortState.toMutableList()

    // 移除旧的排序状态
    newSortState.removeIf { it.columnKey == columnKey }

    // 只有非NONE状态才添加
    if (direction != EnumSortDirection.NONE) {
        newSortState.add(StateSort(columnKey, direction))
    }

    addCleanTableViewModel._sortState = newSortState.toMutableSet()
}

/**
 * 清除指定列的排序
 */
context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
fun clearSorting(columnKey: String) {
    val newSortState = addCleanTableViewModel._sortState.toMutableList()
    newSortState.removeIf { it.columnKey == columnKey }
    addCleanTableViewModel._sortState = newSortState.toMutableSet()
}

/**
 * 清除所有排序
 */
context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
fun clearAllSorting() {
    addCleanTableViewModel._sortState = mutableSetOf()
}

/**
 * 判断某列是否有排序状态
 */
context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
fun isSorted(columnKey: String): Boolean {
    return addCleanTableViewModel._sortState.any {
        it.columnKey ==
                columnKey
    }
}

/**
 * 获取某列的排序方向
 */
context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
fun getSortDirection(columnKey: String): EnumSortDirection {
    return addCleanTableViewModel._sortState.find {
        it.columnKey ==
                columnKey
    }?.direction ?: EnumSortDirection.NONE
}


/**
 * 添加排序条件
 */
context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
fun addSort(columnKey: String, direction: EnumSortDirection) {
    if (direction != EnumSortDirection.NONE) {
        val newSortState = addCleanTableViewModel._sortState.toMutableList()
        newSortState.removeIf { it.columnKey == columnKey } // 先移除旧的
        newSortState.add(StateSort(columnKey, direction))
        addCleanTableViewModel._sortState = newSortState.toMutableSet()
    }
}

/**
 * 是否有任何排序条件
 */
context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
val hasAnySorting: Boolean
    get() = addCleanTableViewModel._sortState.isNotEmpty()

/**
 * 获取排序条件数量
 */
context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
val sortCount: Int
    get() = addCleanTableViewModel._sortState.size
