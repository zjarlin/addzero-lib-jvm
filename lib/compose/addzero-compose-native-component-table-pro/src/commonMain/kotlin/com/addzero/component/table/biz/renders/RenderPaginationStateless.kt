package com.addzero.component.table.biz.renders

import androidx.compose.runtime.Composable
import com.addzero.component.card.MellumCardType
import com.addzero.component.table.pagination.AddTablePagination
import com.addzero.component.table.original.entity.StatePagination

@Composable
fun RenderPagination(
    showPagination: Boolean,
    pageState: StatePagination,
    onPageSizeChange: (Int) -> Unit,
    onGoFirstPage: () -> Unit,
    onPreviousPage: () -> Unit,
    onGoToPage: (Int) -> Unit,
    onNextPage: () -> Unit,
    onGoLastPage: () -> Unit
) {
    if (!showPagination) return
    AddTablePagination(
        statePagination = pageState,
        enablePagination = true,
        onPageSizeChange = onPageSizeChange,
        onGoFirstPage = onGoFirstPage,
        onPreviousPage = onPreviousPage,
        onGoToPage = onGoToPage,
        onNextPage = onNextPage,
        onGoLastPage = onGoLastPage,
        cardType = MellumCardType.Light,
        compactMode = true
    )
}
