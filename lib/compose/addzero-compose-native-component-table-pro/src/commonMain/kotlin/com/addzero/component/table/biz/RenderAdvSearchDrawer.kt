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
import com.addzero.component.table.vm.TableFilterViewModel
import com.addzero.entity.low_table.EnumLogicOperator
import com.addzero.entity.low_table.EnumSearchOperator

@Composable
context(tableFilterViewModel: TableFilterViewModel<*>)
fun RenderAdvSearchDrawer() {
    if (!tableFilterViewModel.showFieldAdvSearchDrawer) {
        return
    }
    AddDrawer(
        visible = tableFilterViewModel.showFieldAdvSearchDrawer,
        title = "高级搜索",
        onClose = { tableFilterViewModel.showFieldAdvSearchDrawer = false },
        onSubmit = {
            tableFilterViewModel._filterStateMap = (tableFilterViewModel._filterStateMap + mapOf(
                tableFilterViewModel.getCurrentColumnKey() to tableFilterViewModel._currentStateSearch
            )).toMutableMap()
           tableFilterViewModel.showFieldAdvSearchDrawer=false
        },
    ) {
        Column {
            AddSelect(
                title = "逻辑符",
                value = tableFilterViewModel._currentStateSearch.logicType,
                items = EnumLogicOperator.entries,
                onValueChange = {
                    tableFilterViewModel._currentStateSearch =
                        tableFilterViewModel._currentStateSearch.copy(logicType = it)
                },
            )

            Spacer(modifier = Modifier.height(16.dp))



            AddSelect(
                title = "操作符",
                value = tableFilterViewModel._currentStateSearch.operator,
                items = EnumSearchOperator.entries,
                onValueChange = {
                    tableFilterViewModel._currentStateSearch =
                        tableFilterViewModel._currentStateSearch.copy(operator = it)
                },
            )


            // 操作符下拉选择

            Spacer(modifier = Modifier.height(12.dp))


            // 输入框
            DynamicFormItem(
                value = tableFilterViewModel._currentStateSearch.columnValue,
                onValueChange = {
                    tableFilterViewModel._currentStateSearch =
                        tableFilterViewModel._currentStateSearch.copy(columnValue = it)

                },
                title = tableFilterViewModel.currentColumnLabel,
                kmpType = tableFilterViewModel.currentColumnKmpType.toString()
            )

            Spacer(modifier = Modifier.height(16.dp))



            AddIconButton(
                text = "清除条件", imageVector = Icons.Default.Close,
                onClick = {
                    tableFilterViewModel._filterStateMap.toMutableMap()
                        .remove(tableFilterViewModel.getCurrentColumnKey())
                },
            )
        }
    }
}
