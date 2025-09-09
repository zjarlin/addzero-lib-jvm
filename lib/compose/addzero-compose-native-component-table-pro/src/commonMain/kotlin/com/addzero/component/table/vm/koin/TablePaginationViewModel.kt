package com.addzero.component.table.vm.koin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.addzero.component.table.original.entity.StatePagination

class TablePaginationViewModel : ViewModel() {
    var showPagination by mutableStateOf(true)
    var _pageState by mutableStateOf(StatePagination())

    fun setPageSize(size: Int) {
        _pageState = _pageState.copy(pageSize = size, currentPage = 1) // 重置到第一页
    }

    fun goToFirstPage() {
        _pageState = _pageState.copy(currentPage = 1)
    }

    fun goToPreviousPage() {
        if (_pageState.hasPreviousPage) {
            _pageState = _pageState.copy(currentPage = _pageState.currentPage - 1)
        }
    }

    fun goToPage(page: Int) {
        if (page in 1.._pageState.totalPages) {
            _pageState = _pageState.copy(currentPage = page)
        }
    }

    fun goToNextPage() {
        if (_pageState.hasNextPage) {
            _pageState = _pageState.copy(currentPage = _pageState.currentPage + 1)
        }
    }

    fun goToLastPage() {
        _pageState = _pageState.copy(currentPage = _pageState.totalPages)
    }

}
