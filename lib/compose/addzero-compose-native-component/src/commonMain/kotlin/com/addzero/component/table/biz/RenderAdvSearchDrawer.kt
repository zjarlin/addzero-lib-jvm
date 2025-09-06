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
import com.addzero.component.dropdown.AddSelect
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


            AddSelect(
                title = "逻辑符",
                value = tableViewModel._currentStateSearch.logicType,
                items = EnumLogicOperator.entries,
                onValueChange = {
                    tableViewModel._currentStateSearch = tableViewModel._currentStateSearch.copy(logicType = it)
                },
            )

            Spacer(modifier = Modifier.height(16.dp))



            AddSelect(
                title = "操作符",
                value = tableViewModel._currentStateSearch.operator,
                items = EnumSearchOperator.entries,
                onValueChange = {
                    tableViewModel._currentStateSearch = tableViewModel._currentStateSearch.copy(operator = it)
                },
            )


            // 操作符下拉选择

            Spacer(modifier = Modifier.height(12.dp))


            // 输入框
            DynamicFormItem(
                value = tableViewModel._currentStateSearch.columnValue, onValueChange = {
                    tableViewModel._currentStateSearch = tableViewModel._currentStateSearch.copy(columnValue = it)

                }, title = tableViewModel.currentColumnLabel, kmpType = tableViewModel.currentColumnKmpType.toString()
            )

            Spacer(modifier = Modifier.height(16.dp))



            AddIconButton(
                text = "清除条件", imageVector = Icons.Default.Close,
                onClick = { tableViewModel._filterStateMap.toMutableMap().remove(tableViewModel.currentColumnKey) },
            )
        }
    }
}
