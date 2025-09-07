package com.addzero.component.table.biz

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

abstract class BizTableViewModel<T, C> : ViewModel() {
    abstract fun onSearch()

    var keyword by mutableStateOf("")
}
