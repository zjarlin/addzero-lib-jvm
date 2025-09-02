package com.addzero.component.table

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * 表格插槽定义
 */
data class TableSlots<T>(
    // 表头区域插槽
    val topHeaderBar: (@Composable () -> Unit)? = null,
    val topSelectionPanel: (@Composable () -> Unit)? = null,

    // 行级操作插槽 - 支持事件处理
    val rowActions: (@Composable (item: T, index: Int) -> Unit)? = null,
    val rowCheckbox: (CheckboxSlotProps<T>.() -> @Composable (item: T, index: Int) -> Unit)? = null,

    // 列级功能插槽
    val columnSorting: (SortingSlotProps.() -> @Composable () -> Unit)? = null,
    val columnFiltering: (@Composable () -> Unit)? = null,

    // 状态区域插槽
    val emptyStateContent: (@Composable () -> Unit)? = null,

    // 底部区域插槽
    val bottomPagination: (@Composable () -> Unit)? = null,
    val bottomSummary: (@Composable (totalItems: Int) -> Unit)? = null
)

// 插槽属性定义
data class CheckboxSlotProps<T>(
    val showCheckbox: Boolean = false,
    val checkedItems: Set<T> = emptySet(),
    val onItemChecked: ((T, Boolean) -> Unit)? = null,
    val allChecked: Boolean = false,
    val onAllCheckedChange: ((Boolean) -> Unit)? = null
)

data class RowIndexSlotProps(
    val showIndex: Boolean = true,
    val startIndex: Int = 1
)

data class SortingSlotProps(
    val sortable: Boolean = false,
    val currentSortKey: String = "",
    val onSort: ((String) -> Unit)? = null
)

/**
 * 默认插槽实现
 */
object DefaultTableSlots {

    @Composable
    fun <T> DefaultHeaderBar(
        title: String,
        subtitle: String? = null,
        actions: @Composable RowScope.() -> Unit = {}
    ): @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                content = actions
            )
        }
    }

    @Composable
    fun DefaultHeaderActions(): @Composable () -> Unit = {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = { /* 编辑 */ }) {
                Icon(Icons.Default.Edit, contentDescription = "编辑", modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = { /* 删除 */ }) {
                Icon(Icons.Default.Delete, contentDescription = "删除", modifier = Modifier.size(16.dp))
            }
        }
    }

    @Composable
    fun <T> DefaultCheckbox(): CheckboxSlotProps<T>.() -> @Composable () -> Unit = {
        {
            if (showCheckbox) {
                Checkbox(
                    checked = allChecked,
                    onCheckedChange = onAllCheckedChange ?: {}
                )
            }
        }
    }

    @Composable
    fun DefaultRowIndex(): RowIndexSlotProps.() -> @Composable () -> Unit = {
        {
            if (showIndex) {
                Text(
                    text = "#",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    @Composable
    fun DefaultSorting(): SortingSlotProps.() -> @Composable () -> Unit = {
        {
            if (sortable) {
                IconButton(
                    onClick = { onSort?.invoke(currentSortKey) },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "排序",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    @Composable
    fun <T> DefaultSelectContent(
        checkedItems: Set<T>,
        onClearSelection: () -> Unit = {},
        onBatchAction: () -> Unit = {}
    ): @Composable () -> Unit = {
        if (checkedItems.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "已选择 ${checkedItems.size} 项",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onClearSelection) {
                            Text("清除选择")
                        }
                        Button(onClick = onBatchAction) {
                            Text("批量删除")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun DefaultEmptyContent(): @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "没有数据",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    @Composable
    fun DefaultPagination(
        totalCount: Int,
        currentPage: Int = 1,
        pageSize: Int = 20
    ): @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "显示 $totalCount 条记录",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
