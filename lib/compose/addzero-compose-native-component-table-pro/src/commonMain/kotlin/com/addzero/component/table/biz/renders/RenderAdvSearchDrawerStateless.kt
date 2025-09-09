package com.addzero.component.table.biz.renders

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
import com.addzero.entity.low_table.EnumLogicOperator
import com.addzero.entity.low_table.EnumSearchOperator
import com.addzero.entity.low_table.StateSearch

@Composable
fun RenderAdvSearchDrawer(
    showFieldAdvSearch: Boolean,
    currentColumnKey: String,
    currentColumnLabel: String?,
    currentColumnKmpType: String?,
    currentStateSearch: StateSearch,
    onCurrentStateSearchChange: (StateSearch) -> Unit,
    onFilterStateMapChange: (Map<String, StateSearch>) -> Unit,
    filterStateMap: Map<String, StateSearch>,
    onShowFieldAdvSearchChange: (Boolean) -> Unit
) {
    if (!showFieldAdvSearch) {
        return
    }
    AddDrawer(
        visible = showFieldAdvSearch,
        title = "高级搜索",
        onClose = { onShowFieldAdvSearchChange(false) },
        onSubmit = {
            onFilterStateMapChange(filterStateMap + mapOf(currentColumnKey to currentStateSearch))
        },
    ) {
        Column {
            AddSelect(
                title = "逻辑符",
                value = currentStateSearch.logicType,
                items = EnumLogicOperator.entries,
                onValueChange = {
                    onCurrentStateSearchChange(currentStateSearch.copy(logicType = it))
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            AddSelect(
                title = "操作符",
                value = currentStateSearch.operator,
                items = EnumSearchOperator.entries,
                onValueChange = {
                    onCurrentStateSearchChange(currentStateSearch.copy(operator = it))
                },
            )

            Spacer(modifier = Modifier.height(12.dp))

            DynamicFormItem(
                value = currentStateSearch.columnValue,
                onValueChange = {
                    onCurrentStateSearchChange(currentStateSearch.copy(columnValue = it))
                },
                title = currentColumnLabel,
                kmpType = currentColumnKmpType.toString()
            )

            Spacer(modifier = Modifier.height(16.dp))

            AddIconButton(
                text = "清除条件",
                imageVector = Icons.Default.Close,
                onClick = {
                    val newMap = filterStateMap.toMutableMap()
                    newMap.remove(currentColumnKey)
                    onFilterStateMapChange(newMap)
                },
            )
        }
    }
}
