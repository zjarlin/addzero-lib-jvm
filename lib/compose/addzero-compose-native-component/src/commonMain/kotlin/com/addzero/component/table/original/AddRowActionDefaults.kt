package com.addzero.component.table.original

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.layout.Row

@Composable
fun <T> AddRowActionDefaults(item: T, onEdit: (T) -> Unit = {}, onDelete: (T) -> Unit = {}) {
    Row {
        IconButton(onClick = { onEdit(item) }) {
            Icon(Icons.Filled.Edit, contentDescription = "编辑", tint = MaterialTheme.colorScheme.primary)
        }
        IconButton(onClick = { onDelete(item) }) {
            Icon(Icons.Filled.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
        }
    }
} 