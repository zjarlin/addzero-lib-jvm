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
    if (!bizTableViewModel.editModeFlag) {
        Box(modifier = Modifier.width(40.dp).fillMaxHeight().zIndex(2f))
        return
    }

    val itemId = item.hashCode() // 使用hashCode作为默认ID
    val isChecked = tableSelectedViewModel._selectedItemIds.contains(itemId)

    Box(
        modifier = Modifier.width(40.dp).fillMaxHeight().zIndex(2f),
        contentAlignment = Alignment.Center
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = { checked ->
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
