package com.addzero.component.table.biz

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.addzero.component.high_level.AddTooltipBox
import com.addzero.component.table.clean.AddCleanTableViewModel
import com.addzero.component.table.viewmodel.isPageAllSelected
import com.addzero.component.table.viewmodel.togglePageSelection

@Composable
context(tableViewModel: AddCleanTableViewModel<T>) fun <T> RenderCheckbox() {
    if (!tableViewModel.enableEditMode) {
        return
    }
    val pageIds = tableViewModel.currentPageIds
    AddTooltipBox("全选") {
        Box(
            modifier = Modifier.Companion.padding(horizontal = 4.dp).width(40.dp),
            contentAlignment = Alignment.Companion.Center
        ) {
            Checkbox(
                checked = isPageAllSelected(pageIds), onCheckedChange = {
                    togglePageSelection(pageIds = pageIds)
                })
        }
    }
}
