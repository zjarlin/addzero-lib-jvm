package com.addzero.component.table.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.addzero.component.table.original.entity.ColumnConfig
import com.addzero.entity.low_table.StateSearch

class TableFilterViewModel<C>(private val getColumnKey: (C) -> String, columnConfigs: List<ColumnConfig>) :
    ViewModel() {
    var showFieldAdvSearchDrawer by mutableStateOf(false)

    var _filterStateMap by mutableStateOf(mapOf<String, StateSearch>())
    var _currentClickColumn by mutableStateOf(null as C?)

    // 将val改为fun，避免初始化时的递归依赖
    fun getCurrentColumnKey(): String =
        if (_currentClickColumn == null) "" else getColumnKey(_currentClickColumn!!).ifBlank {
            _currentStateSearch.hashCode().toString()
        }

    var _currentStateSearch by mutableStateOf(StateSearch(columnKey = getCurrentColumnKey()))
    val currentColumnConfig = columnConfigs.find { it.key == getCurrentColumnKey() }
    val currentColumnLabel = currentColumnConfig?.comment
    val currentColumnKmpType = currentColumnConfig?.kmpType


}
