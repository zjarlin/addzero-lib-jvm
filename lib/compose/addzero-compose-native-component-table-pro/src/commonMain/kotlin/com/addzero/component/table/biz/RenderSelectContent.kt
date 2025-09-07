package com.addzero.component.table.biz

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.addzero.component.button.AddButton
import com.addzero.component.button.AddIconButton

@Composable
context(tableSelectedViewModel: TableSelectedViewModel<T>, bizTableViewModel: BizTableViewModel<*, *>)
fun <T> RenderSelectContent() {
    if (tableSelectedViewModel.enableEditMode && tableSelectedViewModel._selectedItemIds.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically

        ) {

            AddIconButton(text = "清除已选择的", imageVector = Icons.Default.Close) {
                tableSelectedViewModel._selectedItemIds = emptySet()
            }
            Text("已选择 ${tableSelectedViewModel._selectedItemIds.size} 项")
            // @RBAC_PERMISSION: table.batch.delete - 批量删除权限
            AddButton(
                displayName = "批量删除", onClick = { bizTableViewModel.batchDelete() })
            Spacer(modifier = Modifier.width(8.dp))
            AddButton(
                displayName = "导出选中", onClick = { bizTableViewModel.batchExport() })
        }
    }
}

class TableSelectedViewModel<T> : ViewModel() {
    /** 编辑模式 */
    var enableEditMode by mutableStateOf(false)

    /** 选中的ids */
    var _selectedItemIds by mutableStateOf(setOf<Any>())


    /**
     * 是否全选
     * @param [currentPageIds] 当前页的ids
     * @return [Boolean]
     */
    fun isPageAllSelected(currentPageIds: List<Any>): Boolean {
        val selectedPageIds = _selectedItemIds.map { it.toString() }.toSet().intersect(currentPageIds)
        return currentPageIds.isNotEmpty() && selectedPageIds.size == currentPageIds.size
    }

//   context(addCleanTableViewModel: AddCleanTableViewModel<*>)

    fun togglePageSelection(
        pageIds: List<Any>
    ) {
        if (isPageAllSelected(
                currentPageIds = pageIds
            )
        ) {
            unselectPageItems(
                pageIds = pageIds
            )
        } else {
            selectPageItems(
                pageIds = pageIds
            )
        }
    }

    fun selectPageItems(pageIds: List<Any>) {
        _selectedItemIds + pageIds
    }


    fun unselectPageItems(
        pageIds: List<Any>
    ) {
        _selectedItemIds = _selectedItemIds.filter { it !in pageIds }.toSet()
    }


}
