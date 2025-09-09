package com.addzero.component.table.vm.koin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class TableSelectedViewModel<T> : ViewModel() {
    /** 选中的ids */
    var _selectedItemIds by mutableStateOf(setOf<Any>())


    /**
     * 是否全选
     * @param [currentPageIds] 当前页的ids
     * @return [Boolean]
     */
    fun isPageAllSelected(currentPageIds: List<Any>): Boolean {
        val selectedPageIds = _selectedItemIds.map { it.toString() }.toSet().intersect(currentPageIds.map { it.toString() }.toSet())
        return currentPageIds.isNotEmpty() && selectedPageIds.size == currentPageIds.size
    }


    fun togglePageSelection(
        pageIds: List<Any>
    ) {
        if (isPageAllSelected(
                currentPageIds = pageIds
            )
        ) {
            unselectPageItems(
                pageIds = pageIds
            )
        } else {
            selectPageItems(
                pageIds = pageIds
            )
        }
    }

    fun selectPageItems(pageIds: List<Any>) {
        _selectedItemIds = _selectedItemIds + pageIds
    }


    fun unselectPageItems(
        pageIds: List<Any>
    ) {
        _selectedItemIds = _selectedItemIds.filter { it !in pageIds }.toSet()
    }


}
