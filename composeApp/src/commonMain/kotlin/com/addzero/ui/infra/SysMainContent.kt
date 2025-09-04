package com.addzero.ui.infra

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.addzero.di.NavgationService
import com.addzero.generated.RouteTable
import com.addzero.settings.SettingContext4Compose
import com.addzero.ui.infra.theme.MainContentGradientBackground
import com.addzero.ui.infra.theme.ThemeViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * 主内容区组件
 *
 * @param navController 导航控制器
 */
@Composable
fun MainContent(navController: NavHostController) {
    val themeViewModel = koinViewModel<ThemeViewModel>()
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
            RenderNavContent(navController)
        }
    }
}

@Composable
fun RenderNavContent(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = SettingContext4Compose.HOME_SCREEN,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        // 动态生成导航目标
        RouteTable.allRoutes.forEach { (route, content) ->
            composable(route) {
                content()
            }
        }
    }
    NavgationService.initialize(navController)
}
