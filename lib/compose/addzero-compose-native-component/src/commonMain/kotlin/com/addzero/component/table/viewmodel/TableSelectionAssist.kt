package com.addzero.component.table.viewmodel

import com.addzero.component.table.clean.AddCleanTableViewModel
import kotlin.collections.plus


/**
 * 是否有选中项目
 */
context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
val hasSelection: Boolean
    get() {
        return addCleanTableViewModel._selectedItemIds.isNotEmpty()
    }


context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
val  selectedCount: Int
    get() {
        return addCleanTableViewModel._selectedItemIds.size
    }


/**
 * 切换编辑模式
 */
context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
fun  toggleEditMode() {
    addCleanTableViewModel.enableEditMode = !addCleanTableViewModel.enableEditMode
    if (!addCleanTableViewModel.enableEditMode) {
        // 退出编辑模式时清空选择
        addCleanTableViewModel._selectedItemIds = emptySet()
    }
}

/**
 * 设置编辑模式
 */
context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
fun setEditMode(enabled: Boolean) {
    addCleanTableViewModel.enableEditMode = enabled
    if (!enabled) {
        addCleanTableViewModel._selectedItemIds = emptySet()
    }
}

/**
 * 切换单个项目的选中状态
 */
context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
fun toggleItemSelection(itemId: Any) {
    addCleanTableViewModel._selectedItemIds = if (addCleanTableViewModel._selectedItemIds.contains(itemId)) {
        addCleanTableViewModel._selectedItemIds.filter { it != itemId }.toSet()
    } else {
        addCleanTableViewModel._selectedItemIds + itemId
    }
}

context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
fun togglePageSelection(pageIds: List<Any>) {
    if (_root_ide_package_.com.addzero.component.table.viewmodel.isPageAllSelected(

            pageIds = pageIds
        )
    ) {
        _root_ide_package_.com.addzero.component.table.viewmodel.unselectPageItems(
            pageIds = pageIds
        )
    } else {
        _root_ide_package_.com.addzero.component.table.viewmodel.selectPageItems(
            pageIds = pageIds
        )
    }
}


/**
 * 选中当前页所有项目
 */
context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
fun selectPageItems(pageIds: List<Any>) {
    addCleanTableViewModel._selectedItemIds + pageIds
}

/**
 * 取消选中当前页所有项目
 */
context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
fun unselectPageItems(pageIds: List<Any>) {
    addCleanTableViewModel._selectedItemIds = addCleanTableViewModel._selectedItemIds.filter { it !in pageIds }.toSet()
}

context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
fun isPageAllSelected(pageIds: List<Any>): Boolean {
    val selectedPageIds = addCleanTableViewModel._selectedItemIds.map { it.toString() }.toSet().intersect(pageIds)
    return pageIds.isNotEmpty() && selectedPageIds.size == pageIds.size
}

/**
 * 清空所有选择
 */
context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
fun clearSelection() {
    addCleanTableViewModel._selectedItemIds = emptySet()
    addCleanTableViewModel._currentSelectItem = null
}

/**
 * 选中指定项目
 */
context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
fun selectItems(itemIds: Set<Any>) {
    addCleanTableViewModel._selectedItemIds + itemIds
}

/**
 * 取消选中指定项目
 */
context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
fun unselectItems(itemIds: Set<Any>) {
    addCleanTableViewModel._selectedItemIds = addCleanTableViewModel._selectedItemIds.filter { it !in itemIds }.toSet()
}

/**
 * 判断项目是否被选中
 */
context(addCleanTableViewModel: com.addzero.component.table.clean.AddCleanTableViewModel<*>)
fun isItemSelected(itemId: Any): Boolean {
    return addCleanTableViewModel._selectedItemIds.contains(itemId)
}

