package com.addzero.component.table.biz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.addzero.component.button.AddIconButton
import com.addzero.component.table.clean.AddCleanTableViewModel

@Composable
context(tableViewModel: AddCleanTableViewModel<T>) fun <T> RenderButtons() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tableViewModel.buttonSlot()
        // 多选模式按钮
        AddIconButton(
            text = if (tableViewModel.enableEditMode) "退出多选" else "多选",
            imageVector = if (tableViewModel.enableEditMode) Icons.Default.Deselect else Icons.Default.SelectAll
        ) {
            tableViewModel.enableEditMode = !tableViewModel.enableEditMode
        }
        // 新增按钮
        AddIconButton(
            text = "新增"
        ) {
            tableViewModel.onSaveClick()
        }

        // 导入按钮
        AddIconButton(
            text = "导入", imageVector = Icons.Default.UploadFile
        ) {
            tableViewModel.onImportClick()
        }

        // 导出按钮
        AddIconButton(
            text = "导出", imageVector = Icons.Default.DownloadForOffline
        ) {
            tableViewModel.onExportClick()
        }
    }
}
