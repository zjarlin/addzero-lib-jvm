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
import com.addzero.component.table.vm.koin.BizTableViewModel
import com.addzero.component.table.vm.koin.TableButtonViewModel
import com.addzero.component.table.vm.koin.TableSelectedViewModel

@Composable
context(tableSelectedViewModel: TableSelectedViewModel<T>, bizTableViewModel: BizTableViewModel<*>,
    tablebuttonViewModel: TableButtonViewModel
)

fun <T> RenderCheckbox() {
    if (!tablebuttonViewModel.editModeFlag) {
        return
    }
    val pageIds = bizTableViewModel.currentPageIds
    AddTooltipBox("全选") {
        Box(
            modifier = Modifier.padding(horizontal = 4.dp).width(40.dp), contentAlignment = Alignment.Center
        ) {
            Checkbox(
                checked = tableSelectedViewModel.isPageAllSelected(pageIds), onCheckedChange = {
                    tableSelectedViewModel.togglePageSelection(pageIds = pageIds)
                })
        }
    }
}
