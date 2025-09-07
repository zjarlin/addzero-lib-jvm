package com.addzero.component.table.biz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.addzero.component.button.AddIconButton

@Composable
context(tableButtonViewModel: TableButtonViewModel

, bizTableViewModel: BizTableViewModel<*, *>
)
fun RenderButtons(buttonSlot: @Composable () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        buttonSlot()
        // 多选模式按钮
        AddIconButton(
            text = if (tableButtonViewModel.editModeFlag) "退出多选" else "多选",
            imageVector = if (tableButtonViewModel.editModeFlag) Icons.Default.Deselect else Icons.Default.SelectAll
        ) {
            tableButtonViewModel.editModeFlag = !tableButtonViewModel.editModeFlag
        }
        // 新增按钮
        AddIconButton(
            text = "新增"
        ) {
            bizTableViewModel.onSaveClick()
        }

        // 导入按钮
        AddIconButton(
            text = "导入", imageVector = Icons.Default.UploadFile
        ) {
            bizTableViewModel.onImportClick()
        }

        // 导出按钮
        AddIconButton(
            text = "导出", imageVector = Icons.Default.DownloadForOffline
        ) {
            bizTableViewModel.onExportClick()
        }
    }
}

class TableButtonViewModel : ViewModel() {
    var editModeFlag by mutableStateOf(false)

}
