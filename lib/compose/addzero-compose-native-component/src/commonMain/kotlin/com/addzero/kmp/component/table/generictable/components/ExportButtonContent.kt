package com.addzero.kmp.component.table.generictable.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.addzero.kmp.component.dropdown.AddDropDown
import com.addzero.kmp.component.high_level.AddTooltipBox
import com.addzero.kmp.entity.low_table.EnumExportType
import com.addzero.kmp.entity.low_table.ExportParam

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


    AddTooltipBox(string1) {
        IconButton(onClick = onExportButtonClick) {
            Icon(
                imageVector = Icons.Default.FileDownload,
                contentDescription = string1
            )
        }
        AddDropDown(
            options = EnumExportType.entries,
            expanded = showExportMenu,
            getLabel = { it.name },
            onOptionSelected = {},
            onDismissRequest = onDismissDropDownMenu
        )

    }


}

