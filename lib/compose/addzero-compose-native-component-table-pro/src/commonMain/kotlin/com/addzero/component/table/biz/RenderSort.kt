package com.addzero.component.table.biz

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.addzero.component.button.AddIconButton
import com.addzero.entity.low_table.EnumSortDirection
import com.addzero.entity.low_table.StateSort
import com.addzero.str.removeIf

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

@Composable
context(addCleanTableViewModel: TableSortViewModel)
fun <C> RenderSort(column: C, getColumnKey: (C) -> String) {
    val columnKey = getColumnKey(column)
    val sortDirection = addCleanTableViewModel.getSortDirection(
        columnKey
    )
    val (text, icon) = when (sortDirection) {
        EnumSortDirection.ASC -> "升序" to Icons.Default.ArrowUpward
        EnumSortDirection.DESC -> "降序" to Icons.Default.ArrowDownward
        else -> "默认" to Icons.AutoMirrored.Filled.Sort
    }

    AddIconButton(
        text = text,
        imageVector = icon,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(16.dp)
    ) {
        addCleanTableViewModel.changeSorting(columnKey)
    }
}
