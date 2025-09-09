package com.addzero.component.table.vm.koin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.addzero.assist.AddFun.getIdExt

class BizTableViewModel<T>() : ViewModel() {
    /** 编辑模式 */

    var editModeFlag by mutableStateOf(false)

    var keyword by mutableStateOf("")
    var _data by mutableStateOf(emptyList<T>())
    val currentPageIds = _data.map { it.getIdExt }
    fun onSearch() {
    }

    fun onSaveClick() {
        TODO("Not yet implemented")
    }

    fun onImportClick() {
        TODO("Not yet implemented")
    }

    fun onExportClick() {
        TODO("Not yet implemented")
    }

    fun batchDelete() {
        TODO("Not yet implemented")
    }

    fun batchExport() {
        TODO("Not yet implemented")
    }

    fun onEditClick() {
        TODO("Not yet implemented")
    }

    fun onDeleteClick() {
        TODO("Not yet implemented")
    }

    fun queryPage() {
        TODO("Not yet implemented")
    }

}
