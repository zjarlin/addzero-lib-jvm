package com.addzero.ui.infra.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.runtime.*
import com.addzero.component.button.AddIconButton

/**
 * 主题选择按钮
 * 点击打开主题选择对话框
 */
@Composable
fun ThemeSelectionButton() {
    var showThemeDialog by remember { mutableStateOf(false) }
    AddIconButton(
        text = "选择主题",
        imageVector = Icons.Default.Palette,
    ) {
        showThemeDialog = true
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            onDismiss = { showThemeDialog = false }
        )
    }
}
