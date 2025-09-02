package com.addzero.component.table

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.addzero.assist.getSortDirection
import com.addzero.component.button.AddIconButton
import com.addzero.component.card.MellumCardType
import com.addzero.component.drawer.AddDrawer
import com.addzero.component.dropdown.AddDropdownSelector
import com.addzero.component.form.DynamicFormItem
import com.addzero.component.high_level.AddTooltipBox
import com.addzero.component.table.clean.AddCleanTableViewModel
import com.addzero.component.table.pagination.AddTablePagination
import com.addzero.component.table.viewmodel.*
import com.addzero.entity.low_table.EnumLogicOperator
import com.addzero.entity.low_table.EnumSearchOperator
import com.addzero.entity.low_table.EnumSortDirection

@Composable
context(tableViewModel: AddCleanTableViewModel<T>) fun <T> RenderButtons() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tableViewModel.buttonSlot()
        // 多选模式按钮
        AddIconButton(
            text = if (tableViewModel.enableEditMode) "退出多选" else "多选", imageVector = if (tableViewModel.enableEditMode) Icons.Default.Deselect else Icons.Default.SelectAll
        ) {
            tableViewModel.enableEditMode = !tableViewModel.enableEditMode
        }
        // 新增按钮
        AddIconButton(
            text = "新增"
        ) {
            tableViewModel.onSaveClick()
        }

        // 导入按钮
        AddIconButton(
            text = "导入", imageVector = Icons.Default.UploadFile
        ) {
            tableViewModel.onImportClick()
        }

        // 导出按钮
        AddIconButton(
            text = "导出", imageVector = Icons.Default.DownloadForOffline
        ) {
            tableViewModel.onExportClick()

        }
    }
}


@Composable
context(addCleanTableViewModel: AddCleanTableViewModel<*>) fun <C> RenderSort(column: C, getColumnKey: (C) -> String) {
    val columnKey = getColumnKey(column)
    val sortDirection = getSortDirection(columnKey, addCleanTableViewModel._sortState)
    val (text, icon) = when (sortDirection) {
        EnumSortDirection.ASC -> "升序" to Icons.Default.ArrowUpward
        EnumSortDirection.DESC -> "降序" to Icons.Default.ArrowDownward
        else -> "默认" to Icons.AutoMirrored.Filled.Sort
    }

    AddIconButton(
        text = text, imageVector = icon, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)
    ) {
        changeSorting(columnKey)
    }
}

/**
 * 通用表格组件 - 对 TableOriginal 的高级封装
 * 集成了搜索、排序、分页、多选等完整功能
 */


//@Composable
//context(tableViewModel: AddCleanTableViewModel<T>)
//fun <T> AddGenericTable(modifier: Modifier = Modifier) {
//    TableOriginal(
//        columns = tableViewModel.visibleColumns,
//        data = tableViewModel.data,
//        getColumnLabel = { it.columnMetadata.comment },
//        modifier = modifier,
//        headerBar = {
//            AddSearchBar(
//                keyword = tableViewModel.keyword,
//                onKeyWordChanged = { tableViewModel.keyword = it },
//                onSearch = { tableViewModel.onSearch() },
//                leftSloat = {
//                    RenderButtons()
//                }
//            )
//        },
//        headerCheckbox = {
//            RenderCheckbox()
//        },
//        headerSort = { column ->
//            RenderSort(
//                column = column,
//                getColumnKey = { it.columnMetadata.columnName }
//            )
//        },
//        headerAction = {},
//        rowContent = { index, item ->
//            with(rememberScrollState()) {
//                TableRowImpl(
//                    index = index,
//                    item = item,
//                    onRowClick = { tableViewModel.onRowClick() },
//                    onDeleteItem = { id: Any ->
//                        tableViewModel.deleteRowData(id)
//                    },
//                    onCheckboxClick = { toggleItemSelection(tableViewModel.getIdFun(item)) },
//                    renderCustomActions = tableViewModel.actionSlot,
//                    onEditClick = {
//                        tableViewModel.onEditClick()
//                    },
//                    rowCusTomRender = {
//                        this.customFormRender(it)
//                    },
//                )
//            }
//        },
//        selectContent = {
//            RenderSelectContent()
//        },
//        pagination = {
//            RenderPagination()
//        }
//    )
//
//    // 高级搜索弹窗
//    RenderAdvSearchDrawer()
//}

@Composable
context(tableViewModel: AddCleanTableViewModel<T>) private fun <T> RenderAdvSearchDrawer() {
    if (!tableViewModel.showAdvancedSearch) {
        return
    }
    AddDrawer(
        visible = tableViewModel.showFieldAdvSearch,
        title = "高级搜索",
        onClose = { tableViewModel.showFieldAdvSearch = false },
        onSubmit = {
            tableViewModel._filterStateMap = (tableViewModel._filterStateMap + mapOf(
                tableViewModel.currentColumnKey to tableViewModel._currentStateSearch
            )).toMutableMap()
        },
    ) {
        Column {
            // 逻辑操作符下拉选择
            AddDropdownSelector(
                title = "逻辑符",
                options = EnumLogicOperator.entries,
                getLabel = { it.displayName },
                onValueChange = {
                    tableViewModel._currentStateSearch = tableViewModel._currentStateSearch.copy(logicType = it ?: EnumLogicOperator.AND)
                },
            )

            Spacer(modifier = Modifier.height(16.dp))


            // 操作符下拉选择
            AddDropdownSelector(title = "操作符", options = EnumSearchOperator.entries, getLabel = { it.displayName }, initialValue = EnumSearchOperator.LIKE, onValueChange = {
                tableViewModel._currentStateSearch = tableViewModel._currentStateSearch.copy(operator = it ?: EnumSearchOperator.LIKE)
            })

            Spacer(modifier = Modifier.height(12.dp))


            // 输入框
            DynamicFormItem(
                value = tableViewModel._currentStateSearch.columnValue, onValueChange = {
                    tableViewModel._currentStateSearch = tableViewModel._currentStateSearch.copy(columnValue = it)

                }, title = tableViewModel.currentColumnLabel, kmpType = tableViewModel.currentColumnKmpType.toString()
            )

            Spacer(modifier = Modifier.height(16.dp))



            AddIconButton(
                text = "清除条件", imageVector = Icons.Default.Close,
                onClick = { tableViewModel._filterStateMap.toMutableMap().remove(tableViewModel.currentColumnKey) },
            )
        }
    }
}

@Composable
context(tableViewModel: AddCleanTableViewModel<T>) private fun <T> RenderPagination() {

    if (tableViewModel.showPagination) {
        AddTablePagination(
            statePagination = tableViewModel._pageState, enablePagination = true, onPageSizeChange = {
                setPageSize(it)
            }, onGoFirstPage = {
                goToFirstPage()
                tableViewModel.queryPage()
            }, onPreviousPage = { goToPreviousPage() }, onGoToPage = { goToPage(it) }, onNextPage = { goToNextPage() }, onGoLastPage = {
                goToLastPage()
                tableViewModel.queryPage()
            }, cardType = MellumCardType.Light,
            //是否开启分页
            compactMode = true
        )
    }
}


@Composable
context(tableViewModel: AddCleanTableViewModel<T>) private fun <T> RenderSelectContent() {
    if (tableViewModel.enableEditMode && tableViewModel._selectedItemIds.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically
        ) {

            AddIconButton(text = "清除已选择的", imageVector = Icons.Default.Close) {
                tableViewModel._selectedItemIds = emptySet()
            }

            Text("已选择 ${tableViewModel._selectedItemIds.size} 项")
            // @RBAC_PERMISSION: table.batch.delete - 批量删除权限
            Button(onClick = { tableViewModel.batchDelete() }) {
                Text("批量删除")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = { tableViewModel.batchExport() }) {
                Text("导出选中")
            }
        }
    }
}

@Composable
context(tableViewModel: AddCleanTableViewModel<T>) fun <T> RenderCheckbox() {
    if (!tableViewModel.enableEditMode) {
        return
    }
    val pageIds = tableViewModel.currentPageIds
    AddTooltipBox("全选") {
        Box(
            modifier = Modifier.padding(horizontal = 4.dp).width(40.dp), contentAlignment = Alignment.Center
        ) {
            Checkbox(
                checked = isPageAllSelected(pageIds), onCheckedChange = {
                    togglePageSelection(pageIds = pageIds)
                })
        }
    }
}

