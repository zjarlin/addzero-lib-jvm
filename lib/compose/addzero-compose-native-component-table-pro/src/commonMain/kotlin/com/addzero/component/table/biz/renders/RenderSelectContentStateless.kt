package com.addzero.component.table.biz.renders

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

@Composable
fun RenderSelectContent(
    editModeFlag: Boolean,
    selectedItemIds: Set<Any>,
    onClearSelectedItems: () -> Unit,
    onBatchDelete: () -> Unit,
    onBatchExport: () -> Unit
) {
    if (editModeFlag && selectedItemIds.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically

        ) {

            AddIconButton(text = "清除已选择的", imageVector = Icons.Default.Close) {
                onClearSelectedItems()
            }
            Text("已选择 ${selectedItemIds.size} 项")
            // @RBAC_PERMISSION: table.batch.delete - 批量删除权限
            AddButton(
                displayName = "批量删除", onClick = onBatchDelete)
            Spacer(modifier = Modifier.width(8.dp))
            AddButton(
                displayName = "导出选中", onClick = onBatchExport)
        }
    }
}
