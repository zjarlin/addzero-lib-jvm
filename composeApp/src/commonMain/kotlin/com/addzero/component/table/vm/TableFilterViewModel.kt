package com.addzero.component.table.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.addzero.component.table.original.entity.ColumnConfig
import com.addzero.entity.low_table.StateSearch

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
