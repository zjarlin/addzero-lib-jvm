package com.addzero.component.table.vm.koin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.addzero.entity.low_table.EnumSortDirection
import com.addzero.entity.low_table.StateSort
import com.addzero.str.removeIf
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class TableSortViewModel : ViewModel() {
    var _sortState by mutableStateOf(
        mutableSetOf(
            StateSort(
                "createTime", EnumSortDirection.DESC
            )
        )
    )

   fun getSortDirection(columnKey: String): EnumSortDirection {
    return _sortState.find {
        it.columnKey ==
                columnKey
    }?.direction ?: EnumSortDirection.NONE
}


    fun changeSorting(columnKey: String) {
        // 查找当前是否已有该列的排序状态
        val existingSort = _sortState.find {
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
        val newSortState = _sortState.toMutableList()


        // 移除旧的排序状态
        newSortState.removeIf { it.columnKey == columnKey }

        // 只有非NONE状态才添加
        if (newDirection != EnumSortDirection.NONE) {
            newSortState.add(StateSort(columnKey, newDirection))
        }

        // 更新排序状态 - 强制创建新实例
        _sortState = newSortState.toMutableSet()
    }


}
