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
import com.addzero.component.table.clean.AddCleanTableViewModel

@Composable
context(tableViewModel: AddCleanTableViewModel<T>) fun <T> RenderSelectContent() {
    if (tableViewModel.enableEditMode && tableViewModel._selectedItemIds.isNotEmpty()) {
        Row(
            modifier = Modifier.Companion.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {

            AddIconButton(text = "清除已选择的", imageVector = Icons.Default.Close) {
                tableViewModel._selectedItemIds = emptySet()
            }

            Text("已选择 ${tableViewModel._selectedItemIds.size} 项")
            // @RBAC_PERMISSION: table.batch.delete - 批量删除权限
            AddButton(
                displayName = "批量删除",
                onClick = { tableViewModel.batchDelete() }
            )

            Spacer(modifier = Modifier.Companion.width(8.dp))

            AddButton(
                displayName = "导出选中",
                onClick = { tableViewModel.batchExport() }
            )
        }
    }
}
