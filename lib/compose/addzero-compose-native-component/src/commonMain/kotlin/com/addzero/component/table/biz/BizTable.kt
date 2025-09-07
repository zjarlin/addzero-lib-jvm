//package com.addzero.component.table.biz
//
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.addzero.component.table.original.TableOriginalState
//import com.addzero.component.table.original.TableOriginalWidget
//import com.addzero.component.table.original.entity.ColumnConfig
//import com.addzero.component.table.original.entity.TableLayoutConfig
//import com.addzero.component.table.original.rememberTableOriginalState
//import com.addzero.core.ext.toMap
//
//
//@Composable
//inline fun <reified T, C> BizTable(
//    data: List<T>,
//    columns: List<C>,
//    noinline getColumnKey: (C) -> String,
//    noinline getRowId: (T) -> Any = {
//        val toMap = it?.toMap()
//        toMap?.get("id") ?: ""
//    },
//    columnConfigs: List<ColumnConfig> = emptyList(),
//    layoutConfig: TableLayoutConfig = TableLayoutConfig(),
//    noinline getColumnLabel: @Composable (C) -> Unit,
//    topSlot: @Composable () -> Unit = {},
//    bottomSlot: @Composable () -> Unit = {},
//
//    noinline getCellContent: @Composable (item: T, column: C) -> Unit = { item, column ->
//        val toMap = item?.toMap()
//        val toString = toMap?.get(getColumnKey(column)).toString()
//        Text(text = toString)
//    },
//    // 行左侧插槽（如复选框）
//    noinline rowLeftSlot: @Composable (item: T, index: Int) -> Unit = { _, _ -> },
//    noinline rowActionSlot: (@Composable (item: T) -> Unit)? = null,
//    modifier: Modifier = Modifier,
//    noinline emptyContentSlot: @Composable () -> Unit = {
//        Box(
//            modifier = Modifier.fillMaxWidth().height(200.dp).padding(16.dp), contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = "没有数据",
//                style = MaterialTheme.typography.bodyLarge,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//        }
//    },
//) {
//    TableOriginalWidget(
//        state = state
//    )
//}
