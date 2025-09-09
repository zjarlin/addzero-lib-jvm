package com.addzero.component.table.vm.koin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class TableButtonViewModel : ViewModel() {
    var editModeFlag by mutableStateOf(false)

}
