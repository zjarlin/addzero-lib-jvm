package site.addzero.component.table.biz

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.component.button.AddIconButton
import site.addzero.component.drawer.AddDrawer
import site.addzero.component.dropdown.AddSelect
import site.addzero.component.form.DynamicFormItem
import site.addzero.entity.low_table.EnumLogicOperator
import site.addzero.entity.low_table.EnumSearchOperator
import site.addzero.entity.low_table.StateSearch

@Composable
fun RenderAdvSearchDrawer(
    showFieldAdvSearchDrawer: Boolean,
    currentStateSearch: StateSearch,
    currentColumnLabel: String?,
    currentColumnKmpType: String?,
    onShowFieldAdvSearchDrawerChange: (Boolean) -> Unit,
    onCurrentStateSearchChange: (StateSearch) -> Unit,
    onFilterStateMapChange: (Map<String, StateSearch>) -> Unit,
    getCurrentColumnKey: () -> String,
    filterStateMap: Map<String, StateSearch>
) {
//    if (!showFieldAdvSearchDrawer) {
//        return
//    }

    AddDrawer(
        visible = showFieldAdvSearchDrawer,
        title = "高级搜索",
        onClose = { onShowFieldAdvSearchDrawerChange(false) },
        onSubmit = {
            val newFilterStateMap = filterStateMap + mapOf(getCurrentColumnKey() to currentStateSearch)
            onFilterStateMapChange(newFilterStateMap)
            onShowFieldAdvSearchDrawerChange(false)
        },
    ) {
        Column {
            AddSelect(
                title = "逻辑符",
                value = currentStateSearch.logicType,
                items = EnumLogicOperator.entries,
                onValueChange = {
                    val newState = currentStateSearch.copy(logicType = it)
                    onCurrentStateSearchChange(newState)
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            AddSelect(
                title = "操作符",
                value = currentStateSearch.operator,
                items = EnumSearchOperator.entries,
                onValueChange = {
                    val newState = currentStateSearch.copy(operator = it)
                    onCurrentStateSearchChange(newState)
                },
            )

            // 操作符下拉选择

            Spacer(modifier = Modifier.height(12.dp))

            // 输入框
            DynamicFormItem(
                value = currentStateSearch.columnValue,
                onValueChange = {
                    val newState = currentStateSearch.copy(columnValue = it)
                    onCurrentStateSearchChange(newState)
                },
                title = currentColumnLabel,
                kmpType = currentColumnKmpType.toString()
            )

            Spacer(modifier = Modifier.height(16.dp))

            AddIconButton(
                text = "清除条件", imageVector = Icons.Default.Close,
                onClick = {
                    val newFilterStateMap = filterStateMap.toMutableMap().apply {
                        remove(getCurrentColumnKey())
                    }
                    onFilterStateMapChange(newFilterStateMap)
                },
            )
        }
    }
}
