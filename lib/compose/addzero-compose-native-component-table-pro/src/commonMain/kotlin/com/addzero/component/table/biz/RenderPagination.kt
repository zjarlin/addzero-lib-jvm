package com.addzero.component.table.biz

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.addzero.component.card.MellumCardType
import com.addzero.component.table.original.entity.StatePagination
import com.addzero.component.table.pagination.AddTablePagination

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


@Composable
context(
    tablePaginationViewModel: TablePaginationViewModel,
    bizTableViewModel: BizTableViewModel<*, *>
)
fun RenderPagination() {

    if (!tablePaginationViewModel.showPagination) return
    AddTablePagination(
        statePagination = tablePaginationViewModel._pageState,
        enablePagination = true,
        onPageSizeChange = {
            tablePaginationViewModel.setPageSize(it)
        },
        onGoFirstPage = {
            tablePaginationViewModel.goToFirstPage()
            bizTableViewModel.queryPage()
        },
        onPreviousPage = { tablePaginationViewModel.goToPreviousPage() },
        onGoToPage = { tablePaginationViewModel.goToPage(it) },
        onNextPage = { tablePaginationViewModel.goToNextPage() },
        onGoLastPage = {
            tablePaginationViewModel.goToLastPage()
            bizTableViewModel.queryPage()
        },
        cardType = MellumCardType.Light,
        //是否开启分页
        compactMode = true
    )
}
