package com.addzero.component.table.generictable.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.runtime.Composable
import com.addzero.component.button.AddIconButton
import com.addzero.component.dropdown.AddDropDown
import com.addzero.entity.low_table.EnumExportType
import com.addzero.entity.low_table.ExportParam

/**
 * 导出按钮
 * @param [permissionCode]
 * @param [selectedItems]
 * @param [showExportMenu]
 * @param [onExportButtonClick] 导出按钮点击
 * @param [onExportDropDownItemClick] 导出下拉点击
 * @param [onDismissDropDownMenu]
 */
@Composable
fun ExportButtonContent(
    showFlag: Boolean,
    selectedItems: Set<Any>,
    showExportMenu: Boolean,
    onExportButtonClick: () -> Unit,
    onExportDropDownItemClick: (ExportParam) -> Unit,
    onDismissDropDownMenu: () -> Unit
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

    AddDropDown(
        options = EnumExportType.entries,
        expanded = showExportMenu,
        getLabel = { it.name },
        onOptionSelected = {},
        onDismissRequest = onDismissDropDownMenu
    )
}



