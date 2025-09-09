package com.addzero.component.table.biz

import androidx.compose.runtime.Composable
import com.addzero.component.card.MellumCardType
import com.addzero.component.table.pagination.AddTablePagination
import com.addzero.component.table.vm.koin.BizTableViewModel
import com.addzero.component.table.vm.koin.TablePaginationViewModel


@Composable
context(
    tablePaginationViewModel: TablePaginationViewModel,
    bizTableViewModel: BizTableViewModel<*>
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
