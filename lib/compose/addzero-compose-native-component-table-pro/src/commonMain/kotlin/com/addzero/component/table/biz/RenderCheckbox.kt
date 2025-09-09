package com.addzero.component.table.biz

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.addzero.component.table.vm.koin.BizTableViewModel
import com.addzero.component.table.vm.koin.TableSelectedViewModel

@Composable
context(tableSelectedViewModel: TableSelectedViewModel<T>, bizTableViewModel: BizTableViewModel<*>
)

fun <T> RenderCheckbox(item: T) {
    val itemId = item.hashCode()
    val isChecked = tableSelectedViewModel._selectedItemIds.contains(itemId)
    Box(
        modifier = Modifier
            .width(80.dp)
            .fillMaxHeight()
            .zIndex(2f),
        contentAlignment = Alignment.Center
    ) {
        if (bizTableViewModel.editModeFlag) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { checked ->
//                println("Checkbox clicked, checked: $checked")
                    val pageIds = listOf(itemId)
                    if (checked) {
                        tableSelectedViewModel.selectPageItems(pageIds)
                    } else {
                        tableSelectedViewModel.unselectPageItems(pageIds)
                    }
                }
            )

        }
    }
}
