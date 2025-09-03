package com.addzero.component.table.original

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 表格插槽定义
 */
data class TableSlots<T>(
    // 表头区域插槽
    val topSlot: @Composable () -> Unit = {},
    // 行级操作插槽 - 支持事件处理
    val actionSlot: @Composable () -> Unit = {},
    val rowLeftSlot: @Composable () -> Unit = {},

    // 列级功能插槽
    val columnLeftSlot: @Composable () -> Unit = {},
    val columnRightSlot: @Composable () -> Unit = {},

    // 状态区域插槽
    val emptyContentSlot: @Composable () -> Unit = {
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
    },

    // 底部区域插槽
    val bottomSlot: @Composable () -> Unit = {},
)
