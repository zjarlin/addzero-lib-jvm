package com.addzero.ui.infra

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import com.addzero.ui.infra.responsive.ResponsiveMainLayout


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainLayout() {
    // 🚀 使用新的响应式主布局
    ResponsiveMainLayout()
}
