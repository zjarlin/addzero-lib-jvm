package com.addzero.component.table.biz

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.addzero.component.button.AddButton
import com.addzero.component.button.AddIconButton
import com.addzero.component.table.vm.koin.BizTableViewModel
import com.addzero.component.table.vm.koin.TableSelectedViewModel

@Composable
context(tableSelectedViewModel: TableSelectedViewModel<T>, bizTableViewModel: BizTableViewModel<*>)
fun <T> RenderSelectContent() {
    if (bizTableViewModel.editModeFlag && tableSelectedViewModel._selectedItemIds.isNotEmpty()) {
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

