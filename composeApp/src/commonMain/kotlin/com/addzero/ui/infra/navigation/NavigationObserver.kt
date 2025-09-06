package com.addzero.ui.infra.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.addzero.di.NavgationViewModel
import com.addzero.ui.infra.model.menu.MenuViewModel
import com.addzero.ui.infra.model.navigation.RecentTabsManagerViewModel

/**
 * 导航观察器
 *
 * 用于监听导航变化，自动记录标签页
 *
 */
@Composable
context(
    recentViewModel: RecentTabsManagerViewModel,
    navgationViewModel: NavgationViewModel,
    menuViewModel:MenuViewModel
)
fun NavigationObserver( ) {
    val navController = navgationViewModel.getNavController()
    val currentOnDestinationChangedListener = remember {
        NavController.OnDestinationChangedListener { controller, destination, arguments ->
            // 获取完整路径
            val route = destination.route ?: return@OnDestinationChangedListener

            // 如果是已知需要忽略的路由，不记录
            if (shouldIgnoreRoute(route)) {
                return@OnDestinationChangedListener
            }


            // 获取路由标题
            val title = menuViewModel.getRouteTitleByKey(route)

            // 将路由添加到标签页管理器
            recentViewModel.addOrActivateTab(route, title)
        }
    }

    // 添加监听器
    DisposableEffect(navController) {
        navController.addOnDestinationChangedListener(currentOnDestinationChangedListener)

        // 移除监听器
        onDispose {
            navController.removeOnDestinationChangedListener(currentOnDestinationChangedListener)
        }
    }
}

/**
 * 判断是否应该忽略该路由（不添加到标签页）
 */
private fun shouldIgnoreRoute(route: String): Boolean {
    // 忽略登录、404等特殊页面
    val ignoredRoutes = listOf(
        "signFirst",
        "notFound",
        "splash"
    )

    return ignoredRoutes.any { route.startsWith(it) }
}
