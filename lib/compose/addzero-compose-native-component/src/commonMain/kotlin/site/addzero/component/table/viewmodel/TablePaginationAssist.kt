//package site.addzero.component.table.viewmodel
//
//import site.addzero.component.table.clean.AddCleanTableViewModel
//
//
//
///**
// * 更新当前页
// */
//context(addCleanTableViewModel: site.addzero.component.table.clean.AddCleanTableViewModel<*>)
//fun setCurrentPage(page: Int) {
//   addCleanTableViewModel. _pageState =addCleanTableViewModel. _pageState.copy(currentPage = page)
//}
//
///**
// * 更新页面大小
// */
//context(addCleanTableViewModel: site.addzero.component.table.clean.AddCleanTableViewModel<*>)
//fun setPageSize(size: Int) {
//    addCleanTableViewModel._pageState =addCleanTableViewModel. _pageState.copy(pageSize = size, currentPage = 1) // 重置到第一页
//}
//
///**
// * 更新总条目数
// */
//context(addCleanTableViewModel: site.addzero.component.table.clean.AddCleanTableViewModel<*>)
//fun setTotalItems(total: Int) {
//    addCleanTableViewModel._pageState =addCleanTableViewModel. _pageState.copy(totalItems = total)
//}
//
///**
// * 跳转到第一页
// */
//context(addCleanTableViewModel: site.addzero.component.table.clean.AddCleanTableViewModel<*>)
//fun goToFirstPage() {
//    addCleanTableViewModel._pageState = addCleanTableViewModel._pageState.copy(currentPage = 1)
//}
//
///**
// * 跳转到上一页
// */
//context(addCleanTableViewModel: site.addzero.component.table.clean.AddCleanTableViewModel<*>)
//fun goToPreviousPage() {
//    if (addCleanTableViewModel._pageState.hasPreviousPage) {
//        addCleanTableViewModel._pageState = addCleanTableViewModel._pageState.copy(currentPage =addCleanTableViewModel. _pageState.currentPage - 1)
//    }
//}
//
///**
// * 跳转到下一页
// */
//context(addCleanTableViewModel: site.addzero.component.table.clean.AddCleanTableViewModel<*>)
//fun goToNextPage() {
//    if (addCleanTableViewModel._pageState.hasNextPage) {
//        addCleanTableViewModel._pageState = addCleanTableViewModel._pageState.copy(currentPage =addCleanTableViewModel. _pageState.currentPage + 1)
//    }
//}
//
///**
// * 跳转到最后一页
// */
//context(addCleanTableViewModel: site.addzero.component.table.clean.AddCleanTableViewModel<*>)
//fun goToLastPage() {
//    addCleanTableViewModel._pageState = addCleanTableViewModel._pageState.copy(currentPage =addCleanTableViewModel. _pageState.totalPages)
//}
//
///**
// * 跳转到指定页
// */
//context(addCleanTableViewModel: site.addzero.component.table.clean.AddCleanTableViewModel<*>)
//fun goToPage(page: Int) {
//    if (page in 1..addCleanTableViewModel._pageState.totalPages) {
//        addCleanTableViewModel._pageState = addCleanTableViewModel._pageState.copy(currentPage = page)
//    }
//}
//
///**
// * 重置分页状态
// */
//context(addCleanTableViewModel: site.addzero.component.table.clean.AddCleanTableViewModel<*>)
//fun resetPagination() {
//    addCleanTableViewModel._pageState = _root_ide_package_.site.addzero.component.table.viewmodel.StatePagination()
//}
//
///**
// * 获取当前页的起始条目索引（从0开始）
// */
//context(addCleanTableViewModel: site.addzero.component.table.clean.AddCleanTableViewModel<*>)
//val startIndex: Int
//    get() = (addCleanTableViewModel._pageState.currentPage - 1) * addCleanTableViewModel._pageState.pageSize
//
///**
// * 获取当前页的结束条目索引（从0开始，不包含）
// */
//context(addCleanTableViewModel: site.addzero.component.table.clean.AddCleanTableViewModel<*>)
//val endIndex: Int
//    get() = minOf(_root_ide_package_.site.addzero.component.table.viewmodel.startIndex + addCleanTableViewModel._pageState.pageSize, addCleanTableViewModel._pageState.totalItems)
//
///**
// * 是否显示分页控件
// */
//context(addCleanTableViewModel: site.addzero.component.table.clean.AddCleanTableViewModel<*>)
//fun shouldShowPagination(): Boolean {
//    return addCleanTableViewModel._pageState.totalItems > addCleanTableViewModel._pageState.pageSize
//}
