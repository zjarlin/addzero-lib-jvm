package com.addzero.component.table.generictable.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.runtime.Composable
import com.addzero.component.button.AddIconButton

@Composable
fun ExportButtonContent(
    showFlag: Boolean,
    selectedItems: Set<Any>,
    onExportButtonClick: () -> Unit,
) {
    if (!showFlag) return

    //到处所选
    val notEmpty = selectedItems.isNotEmpty()
    val string1 = if (notEmpty) {
        "导出所选"
    } else {
        "导出全部"
    }


    AddIconButton(string1, imageVector = Icons.Default.FileDownload) {
        onExportButtonClick()
    }

}



