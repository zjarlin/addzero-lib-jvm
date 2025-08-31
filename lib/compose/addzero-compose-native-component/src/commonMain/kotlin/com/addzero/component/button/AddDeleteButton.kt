package com.addzero.component.button

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@Composable
fun AddDeleteButton(onDeleteClick: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        // 显示确认和取消按钮
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 确认删除按钮 (红色)
            _root_ide_package_.com.addzero.component.button.AddIconButton(
                text = "确认删除",
                imageVector = Icons.Default.Check,
                tint = MaterialTheme.colorScheme.error,
            ) {
                onDeleteClick()
                showDeleteConfirm = false
            }

            // 取消按钮 (蓝色)
            _root_ide_package_.com.addzero.component.button.AddIconButton(
                text = "取消",
                imageVector = Icons.Default.Close,
            ) { showDeleteConfirm = false }
        }
    } else {
        // 显示删除按钮
        _root_ide_package_.com.addzero.component.button.AddIconButton(
            text = "删除",
            imageVector = Icons.Default.Delete,
            tint = MaterialTheme.colorScheme.error,
        ) { showDeleteConfirm = true }
    }
}
