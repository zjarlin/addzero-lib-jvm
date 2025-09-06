package com.addzero.ui.infra

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.addzero.di.NavgationViewModel
import com.addzero.ui.infra.theme.MainContentGradientBackground
import com.addzero.ui.infra.theme.ThemeViewModel

/**
 * 渲染导航内容
 */
@Composable
context(navgationViewModel: NavgationViewModel,
    themeViewModel: ThemeViewModel
)
fun MainContent() {
    val currentTheme = themeViewModel.currentTheme

    MainContentGradientBackground(
        themeType = currentTheme,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 0.dp,
            color = androidx.compose.ui.graphics.Color.Transparent // 透明背景显示渐变
        ) {
            // 渲染导航内容
            val navController = navgationViewModel.getNavController()
            navgationViewModel.Initialize(navController)
        }
    }
}

