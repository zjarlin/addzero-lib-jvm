package com.addzero.component.table.biz

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.addzero.component.button.AddIconButton
import com.addzero.component.drawer.AddDrawer
import com.addzero.component.dropdown.AddDropdownSelector
import com.addzero.component.form.DynamicFormItem
import com.addzero.component.table.clean.AddCleanTableViewModel
import com.addzero.entity.low_table.EnumLogicOperator
import com.addzero.entity.low_table.EnumSearchOperator

@Composable
context(tableViewModel: AddCleanTableViewModel<T>) fun <T> RenderAdvSearchDrawer() {
    if (!tableViewModel.showAdvancedSearch) {
        return
    }
    AddDrawer(
        visible = tableViewModel.showFieldAdvSearch,
        title = "高级搜索",
        onClose = { tableViewModel.showFieldAdvSearch = false },
        onSubmit = {
            tableViewModel._filterStateMap = (tableViewModel._filterStateMap + mapOf(
                tableViewModel.currentColumnKey to tableViewModel._currentStateSearch
            )).toMutableMap()
        },
    ) {
        Column {
            // 逻辑操作符下拉选择
            AddDropdownSelector(
                title = "逻辑符",
                options = EnumLogicOperator.entries,
                getLabel = { it.displayName },
                onValueChange = {
                    tableViewModel._currentStateSearch =
                        tableViewModel._currentStateSearch.copy(logicType = it ?: EnumLogicOperator.AND)
                },
            )

            Spacer(modifier = Modifier.Companion.height(16.dp))


            // 操作符下拉选择
            AddDropdownSelector(
                title = "操作符",
                options = EnumSearchOperator.entries,
                getLabel = { it.displayName },
                initialValue = EnumSearchOperator.LIKE,
                onValueChange = {
                    tableViewModel._currentStateSearch =
                        tableViewModel._currentStateSearch.copy(operator = it ?: EnumSearchOperator.LIKE)
                })

            Spacer(modifier = Modifier.Companion.height(12.dp))


            // 输入框
            DynamicFormItem(
                value = tableViewModel._currentStateSearch.columnValue, onValueChange = {
                    tableViewModel._currentStateSearch = tableViewModel._currentStateSearch.copy(columnValue = it)

                }, title = tableViewModel.currentColumnLabel, kmpType = tableViewModel.currentColumnKmpType.toString()
            )

            Spacer(modifier = Modifier.Companion.height(16.dp))



            AddIconButton(
                text = "清除条件", imageVector = Icons.Default.Close,
                onClick = { tableViewModel._filterStateMap.toMutableMap().remove(tableViewModel.currentColumnKey) },
            )
        }
    }
}
