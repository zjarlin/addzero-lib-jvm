//package com.addzero.component.table.biz
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.Sort
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.addzero.assist.getSortDirection
//import com.addzero.component.button.AddIconButton
//import com.addzero.component.card.MellumCardType
//import com.addzero.component.search_bar.AddSearchBar
//import com.addzero.component.table.clean.AddCleanTableViewModel
//import com.addzero.component.table.original.TableConfig
//import com.addzero.component.table.original.TableOriginal
//import com.addzero.component.table.original.TableSlots
//import com.addzero.component.table.viewmodel.*
//import com.addzero.entity.low_table.EnumSortDirection
//
//
//@Composable
//context(addCleanTableViewModel: AddCleanTableViewModel<*>) fun <C> RenderSort(column: C, getColumnKey: (C) -> String) {
//    val columnKey = getColumnKey(column)
//    val sortDirection = getSortDirection(columnKey, addCleanTableViewModel._sortState)
//    val (text, icon) = when (sortDirection) {
//        EnumSortDirection.ASC -> "升序" to Icons.Default.ArrowUpward
//        EnumSortDirection.DESC -> "降序" to Icons.Default.ArrowDownward
//        else -> "默认" to Icons.AutoMirrored.Filled.Sort
//    }
//
//    AddIconButton(
//        text = text, imageVector = icon, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp)
//    ) {
//        changeSorting(columnKey)
//    }
//}
//
///**
// * 通用表格组件 - 对 TableOriginal 的高级封装
// * 集成了搜索、排序、分页、多选等完整功能
// */
//@Composable
//context(
//    getColumnKey: (C) -> String,
//tableViewModel: AddCleanTableViewModel<T>)
//fun <T> AddGenericTable(modifier: Modifier = Modifier) {
//    val tableSlots = TableSlots<T>(
//        topSlot = {
//            AddSearchBar(
//                keyword = tableViewModel.keyword,
//                onKeyWordChanged = { tableViewModel.keyword = it },
//                onSearch = { tableViewModel.onSearch() },
//                leftSloat = {
//                    RenderButtons()
//                }
//            )
//        },
//        topSelectionPanel = {
//            RenderSelectContent()
//        },
//        rowActionSlot = { item, index ->
//            // 简化的行操作，使用基本的编辑删除按钮
//            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
//                AddIconButton(
//                    text = "编辑",
//                    imageVector = Icons.Default.Edit,
//                    onClick = { tableViewModel.onEditClick() }
//                )
//                AddIconButton(
//                    text = "删除",
//                    imageVector = Icons.Default.Delete,
//                    onClick = {
//                        val id = tableViewModel.getIdFun(item)
//                        tableViewModel.deleteRowData(id)
//                    }
//                )
//            }
//        },
//        rowLeftSlot={
//        val props = this
//            //是否全选
////            val allChecked1 = props.allChecked
////            allChecked1
//        },
//
//        bottomSlot = {
//            RenderPagination()
//        }
//    )
//
//    val tableConfig = TableConfig<Any>(
//        headerCardType = MellumCardType.Dark,
//        headerCornerRadius = 8.dp,
//        headerElevation = 4.dp
//    )
//
//    TableOriginal(
//        columns = tableViewModel.visibleColumns,
//        data = tableViewModel.data,
//        getColumnKey = { it.co },
//        getRowId = { tableViewModel.getIdFun(it) },
//        getColumnLabel = { column ->
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(column.columnMetadata.comment)
//                RenderSort(
//                    column = column,
//                    getColumnKey = { it.columnMetadata.columnName }
//                )
//            }
//        },
//        getCellContent = { item, column ->
//            // 简化的单元格内容，显示基本信息
//            Text(
//                text = column.columnMetadata.comment,
//                style = MaterialTheme.typography.bodyMedium
//            )
//        },
//        modifier = modifier,
//        config = tableConfig,
//        slots = tableSlots
//    )
//
//    // 高级搜索弹窗
//    RenderAdvSearchDrawer()
//}
//
//
