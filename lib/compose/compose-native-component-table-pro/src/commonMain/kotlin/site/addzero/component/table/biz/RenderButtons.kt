package site.addzero.component.table.biz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 表格工具条按钮组。
 */
@Composable
fun RenderButtons(
    editModeFlag: Boolean,
    onEditModeChange: () -> Unit,
    onCreateClick: (() -> Unit)?,
    onImportClick: (() -> Unit)?,
    onExportClick: (() -> Unit)?,
    buttonSlot: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        buttonSlot()
        TableToolbarActionButton(
            label = if (editModeFlag) "退出多选" else "批量选择",
            icon = if (editModeFlag) Icons.Default.Deselect else Icons.Default.SelectAll,
            onClick = onEditModeChange,
            highlighted = editModeFlag,
        )
        onCreateClick?.let { action ->
            TableToolbarActionButton(
                label = "新增",
                icon = Icons.Default.EditNote,
                onClick = action,
                highlighted = true,
            )
        }
        onImportClick?.let { action ->
            TableToolbarActionButton(
                label = "导入",
                icon = Icons.Default.UploadFile,
                onClick = action,
            )
        }
        onExportClick?.let { action ->
            TableToolbarActionButton(
                label = "导出",
                icon = Icons.Default.DownloadForOffline,
                onClick = action,
            )
        }
    }
}
