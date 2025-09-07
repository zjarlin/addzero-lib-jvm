package com.addzero.component.table.biz

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.addzero.component.button.AddIconButton
import com.addzero.component.drawer.AddDrawer
import com.addzero.component.dropdown.AddSelect
import com.addzero.component.form.DynamicFormItem
import com.addzero.component.table.original.entity.ColumnConfig
import com.addzero.entity.low_table.EnumLogicOperator
import com.addzero.entity.low_table.EnumSearchOperator
import com.addzero.entity.low_table.StateSearch
import kotlin.collections.plus

class TableFilterViewModel<C>(getColumnKey: (C) -> String, columnConfigs: List<ColumnConfig>) : ViewModel() {
    var showFieldAdvSearch by mutableStateOf(false)

    //    var showAdvancedSearch by mutableStateOf(false)
    var _filterStateMap by mutableStateOf(mapOf<String, StateSearch>())
    var _currentClickColumn by mutableStateOf(null as C?)
    val currentColumnKey = if (_currentClickColumn == null) "" else getColumnKey(_currentClickColumn!!)
    var _currentStateSearch by mutableStateOf(StateSearch(columnKey = currentColumnKey))
    val currentColumnConfig = columnConfigs.find { it.key == currentColumnKey }
    val currentColumnLabel = currentColumnConfig?.comment
    val currentColumnKmpType = currentColumnConfig?.kmpType


}

@Composable
context(tableFilterViewModel: TableFilterViewModel<*>)
fun RenderAdvSearchDrawer() {
    if (!tableFilterViewModel.showFieldAdvSearch) {
        return
    }
    AddDrawer(
        visible = tableFilterViewModel.showFieldAdvSearch,
        title = "高级搜索",
        onClose = { tableFilterViewModel.showFieldAdvSearch = false },
        onSubmit = {
            tableFilterViewModel._filterStateMap = (tableFilterViewModel._filterStateMap + mapOf(
                tableFilterViewModel.currentColumnKey to tableFilterViewModel._currentStateSearch
            )).toMutableMap()
        },
    ) {
        Column {


            AddSelect(
                title = "逻辑符",
                value = tableFilterViewModel._currentStateSearch.logicType,
                items = EnumLogicOperator.entries,
                onValueChange = {
                    tableFilterViewModel._currentStateSearch = tableFilterViewModel._currentStateSearch.copy(logicType = it)
                },
            )

            Spacer(modifier = Modifier.height(16.dp))



            AddSelect(
                title = "操作符",
                value = tableFilterViewModel._currentStateSearch.operator,
                items = EnumSearchOperator.entries,
                onValueChange = {
                    tableFilterViewModel._currentStateSearch = tableFilterViewModel._currentStateSearch.copy(operator = it)
                },
            )


            // 操作符下拉选择

            Spacer(modifier = Modifier.height(12.dp))


            // 输入框
            DynamicFormItem(
                value = tableFilterViewModel._currentStateSearch.columnValue, onValueChange = {
                    tableFilterViewModel._currentStateSearch = tableFilterViewModel._currentStateSearch.copy(columnValue = it)

                }, title = tableFilterViewModel.currentColumnLabel, kmpType = tableFilterViewModel.currentColumnKmpType.toString()
            )

            Spacer(modifier = Modifier.height(16.dp))



            AddIconButton(
                text = "清除条件", imageVector = Icons.Default.Close,
                onClick = { tableFilterViewModel._filterStateMap.toMutableMap().remove(tableFilterViewModel.currentColumnKey) },
            )
        }
    }
}
