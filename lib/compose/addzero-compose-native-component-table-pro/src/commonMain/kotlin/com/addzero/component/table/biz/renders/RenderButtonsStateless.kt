package com.addzero.component.table.biz.renders

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

@Composable
fun RenderButtons(
    buttonSlot: @Composable () -> Unit,
    editModeFlag: Boolean,
    onEditModeFlagChange: (Boolean) -> Unit,
    onSaveClick: () -> Unit,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        buttonSlot()
        // 多选模式按钮
        AddIconButton(
            text = if (editModeFlag) "退出多选" else "多选",
            imageVector = if (editModeFlag) Icons.Default.Deselect else Icons.Default.SelectAll
        ) {
            onEditModeFlagChange(!editModeFlag)
        }
        // 新增按钮
        AddIconButton(
            text = "新增"
        ) {
            onSaveClick()
        }

        // 导入按钮
        AddIconButton(
            text = "导入", imageVector = Icons.Default.UploadFile
        ) {
            onImportClick()
        }

        // 导出按钮
        AddIconButton(
            text = "导出", imageVector = Icons.Default.DownloadForOffline
        ) {
            onExportClick()
        }
    }
}
