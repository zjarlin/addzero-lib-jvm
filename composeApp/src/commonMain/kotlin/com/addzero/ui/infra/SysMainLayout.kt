package com.addzero.ui.infra

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.compose.rememberNavController
import com.addzero.ui.infra.model.menu.MenuViewModel
import com.addzero.ui.infra.model.navigation.RecentTabsManagerViewModel
import com.addzero.ui.infra.navigation.NavigationObserver
import com.addzero.ui.infra.responsive.LayoutMode
import com.addzero.ui.infra.responsive.SidebarLayout
import com.addzero.ui.infra.responsive.TopbarLayout
import com.addzero.ui.infra.responsive.rememberResponsiveConfig
import com.addzero.viewmodel.ChatViewModel
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainLayout() {
    // 🚀 使用新的响应式主布局
    val navController = rememberNavController()
    val vm = koinViewModel<RecentTabsManagerViewModel>()
    val chatViewModel = koinViewModel<ChatViewModel>()
    // 获取响应式配置
    val config = rememberResponsiveConfig()
    // 添加导航观察器
    NavigationObserver(
        recentViewModel = vm,
        navController = navController,
        getRouteTitle = { route ->
            MenuViewModel.getRouteTitleByKey(route)
        }
    )
    // 搜索框状态
    val isSearchOpen = remember { mutableStateOf(false) }
    // 根据布局模式渲染不同的布局
    val layoutMode = config.layoutMode
    when (layoutMode) {
        LayoutMode.SIDEBAR -> {
            // 桌面端：侧边栏布局
            SidebarLayout(
                navController = navController,
                vm = vm,
                chatViewModel = chatViewModel,
                showChatBot = chatViewModel.showChatBot,
                isSearchOpen = isSearchOpen,
                config = config
            )
        }

        LayoutMode.TOPBAR -> {
            // 移动端：顶部导航栏布局
            TopbarLayout(
                navController = navController,
                vm = vm,
                chatViewModel = chatViewModel,
                showChatBot = chatViewModel.showChatBot,
                isSearchOpen = isSearchOpen,
                config = config
            )
        }
    }
}
