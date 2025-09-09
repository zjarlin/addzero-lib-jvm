package com.addzero.component.table.biz.renders

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.addzero.component.high_level.AddTooltipBox

@Composable
fun RenderCheckbox(
    editModeFlag: Boolean,
    isPageAllSelected: Boolean,
    onTogglePageSelection: () -> Unit
) {
    if (!editModeFlag) {
        return
    }
    AddTooltipBox("全选") {
        Box(
            modifier = Modifier.padding(horizontal = 4.dp).width(40.dp), contentAlignment = Alignment.Center
        ) {
            Checkbox(
                checked = isPageAllSelected, onCheckedChange = {
                    onTogglePageSelection()
                })
        }
    }
}
