package com.addzero.component.table.biz

import androidx.compose.runtime.Composable
import com.addzero.component.card.MellumCardType
import com.addzero.component.table.clean.AddCleanTableViewModel
import com.addzero.component.table.pagination.AddTablePagination
import com.addzero.component.table.viewmodel.*

@Composable
context(tableViewModel: AddCleanTableViewModel<T>) fun <T> RenderPagination() {

    if (!tableViewModel.showPagination) return
    AddTablePagination(
        statePagination = tableViewModel._pageState,
        enablePagination = true,
        onPageSizeChange = {
            setPageSize(it)
        },
        onGoFirstPage = {
            goToFirstPage()
            tableViewModel.queryPage()
        },
        onPreviousPage = { goToPreviousPage() },
        onGoToPage = { goToPage(it) },
        onNextPage = { goToNextPage() },
        onGoLastPage = {
            goToLastPage()
            tableViewModel.queryPage()
        },
        cardType = MellumCardType.Light,
        //是否开启分页
        compactMode = true
    )
}
