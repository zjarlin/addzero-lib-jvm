package com.addzero.component.table.row

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


/**
 * 表格行组件
 */
@Composable
context(tableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<T>, horizontalScrollState: ScrollState) fun <T> TableRowImpl(
    item: T,
    onRowClick: ((T) -> Unit)?,
    onDeleteItem: ((Any) -> Unit)?,
    onCheckboxClick: () -> Unit,
    renderCustomActions: @Composable () -> Unit,
    onEditClick: (T) -> Unit, rowCusTomRender: @Composable com.addzero.component.table.model.AddCleanColumn<T>.(T) -> Unit,
    index: Int
) {
    val getidFun = tableViewModel.getIdFun
    val isSelected = tableViewModel._selectedItemIds.contains(getidFun(item))

    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else if (index % 2 == 0) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = 0.3f
        )
    val rowNumber = (tableViewModel._pageState.currentPage - 1) * tableViewModel._pageState.pageSize + index + 1
    val rowHeight = tableViewModel.tableMetadata.rowheight.toInt().takeIf { it != 0 } ?: 36
    val showActions = true

    _root_ide_package_.com.addzero.component.table.row.TableRow(item, onRowClick, rowCusTomRender, index, {
        // 多选框列
        if (tableViewModel.enableEditMode) {
            Box(
                modifier = Modifier.padding(horizontal = 4.dp).width(40.dp), contentAlignment = Alignment.Center
            ) {
                // @RBAC_PERMISSION: table.row.select - 行选择权限
                Checkbox(
                    checked = isSelected, onCheckedChange = { onCheckboxClick() })
            }
        }


    }, {

        _root_ide_package_.com.addzero.component.button.AddEditDeleteButton(onEditClick = {
            onEditClick(item)
        }, onDeleteClick = {
            onDeleteItem?.invoke(getidFun(item))
        }, renderCustomActions = {
            renderCustomActions()
        })


    })


}

