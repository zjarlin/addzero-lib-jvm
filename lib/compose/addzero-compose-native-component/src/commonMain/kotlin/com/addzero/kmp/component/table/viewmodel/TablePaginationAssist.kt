package com.addzero.kmp.component.table.viewmodel

import com.addzero.kmp.component.table.clean.AddCleanTableViewModel

/**
 * 分页状态
 */
data class StatePagination(
    var currentPage: Int = 1,
    var pageSize: Int = 10,
    var totalItems: Int = 0
) {
    val totalPages: Int get() = if (totalItems == 0) 1 else (totalItems + pageSize - 1) / pageSize
    val hasPreviousPage: Boolean get() = currentPage > 1
    val hasNextPage: Boolean get() = currentPage < totalPages
    val startItem: Int get() = (currentPage - 1) * pageSize + 1
    val endItem: Int get() = minOf(currentPage * pageSize, totalItems)
}


/**
 * 更新当前页
 */
context(addCleanTableViewModel: AddCleanTableViewModel<*>)
fun setCurrentPage(page: Int) {
   addCleanTableViewModel. _pageState =addCleanTableViewModel. _pageState.copy(currentPage = page)
}

/**
 * 更新页面大小
 */
context(addCleanTableViewModel: AddCleanTableViewModel<*>)
fun setPageSize(size: Int) {
    addCleanTableViewModel._pageState =addCleanTableViewModel. _pageState.copy(pageSize = size, currentPage = 1) // 重置到第一页
}

/**
 * 更新总条目数
 */
context(addCleanTableViewModel: AddCleanTableViewModel<*>)
fun setTotalItems(total: Int) {
    addCleanTableViewModel._pageState =addCleanTableViewModel. _pageState.copy(totalItems = total)
}

/**
 * 跳转到第一页
 */
context(addCleanTableViewModel: AddCleanTableViewModel<*>)
fun goToFirstPage() {
    addCleanTableViewModel._pageState = addCleanTableViewModel._pageState.copy(currentPage = 1)
}

/**
 * 跳转到上一页
 */
context(addCleanTableViewModel: AddCleanTableViewModel<*>)
fun goToPreviousPage() {
    if (addCleanTableViewModel._pageState.hasPreviousPage) {
        addCleanTableViewModel._pageState = addCleanTableViewModel._pageState.copy(currentPage =addCleanTableViewModel. _pageState.currentPage - 1)
    }
}

/**
 * 跳转到下一页
 */
context(addCleanTableViewModel: AddCleanTableViewModel<*>)
fun goToNextPage() {
    if (addCleanTableViewModel._pageState.hasNextPage) {
        addCleanTableViewModel._pageState = addCleanTableViewModel._pageState.copy(currentPage =addCleanTableViewModel. _pageState.currentPage + 1)
    }
}

/**
 * 跳转到最后一页
 */
context(addCleanTableViewModel: AddCleanTableViewModel<*>)
fun goToLastPage() {
    addCleanTableViewModel._pageState = addCleanTableViewModel._pageState.copy(currentPage =addCleanTableViewModel. _pageState.totalPages)
}

/**
 * 跳转到指定页
 */
context(addCleanTableViewModel: AddCleanTableViewModel<*>)
fun goToPage(page: Int) {
    if (page in 1..addCleanTableViewModel._pageState.totalPages) {
        addCleanTableViewModel._pageState = addCleanTableViewModel._pageState.copy(currentPage = page)
    }
}

/**
 * 重置分页状态
 */
context(addCleanTableViewModel: AddCleanTableViewModel<*>)
fun resetPagination() {
    addCleanTableViewModel._pageState = StatePagination()
}

/**
 * 获取当前页的起始条目索引（从0开始）
 */
context(addCleanTableViewModel: AddCleanTableViewModel<*>)
val startIndex: Int
    get() = (addCleanTableViewModel._pageState.currentPage - 1) * addCleanTableViewModel._pageState.pageSize

/**
 * 获取当前页的结束条目索引（从0开始，不包含）
 */
context(addCleanTableViewModel: AddCleanTableViewModel<*>)
val endIndex: Int
    get() = minOf(startIndex + addCleanTableViewModel._pageState.pageSize, addCleanTableViewModel._pageState.totalItems)

/**
 * 是否显示分页控件
 */
context(addCleanTableViewModel: AddCleanTableViewModel<*>)
fun shouldShowPagination(): Boolean {
    return addCleanTableViewModel._pageState.totalItems > addCleanTableViewModel._pageState.pageSize
}
