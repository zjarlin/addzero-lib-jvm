package site.addzero.ui.infra

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import site.addzero.di.NavgationViewModel
import site.addzero.screens.ai.AiChatScreen
import site.addzero.ui.infra.model.favorite.FavoriteTabsViewModel
import site.addzero.viewmodel.SysRouteViewModel
import site.addzero.ui.infra.model.menu.SideMenu
import site.addzero.ui.infra.model.navigation.RecentTabsManagerViewModel
import site.addzero.ui.infra.navigation.NavigationObserver
import site.addzero.ui.infra.theme.ThemeViewModel
import site.addzero.viewmodel.ChatViewModel


@OptIn(ExperimentalComposeUiApi::class)
@Composable
context(navgationViewModel: NavgationViewModel, recentTabsManagerViewModel: RecentTabsManagerViewModel, themeViewModel: ThemeViewModel, chatViewModel: ChatViewModel, sysRouteViewModel: SysRouteViewModel, favouriteTabsViewModel: FavoriteTabsViewModel) fun MainLayout() {
    // 添加导航观察器
    NavigationObserver()
    // 根据布局模式渲染不同的布局
    Scaffold(
        topBar = {
            SysTopBar()
        }) { paddingValues ->
        Row(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            // 侧边栏
            SideMenu()
            // 主内容区
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 面包屑导航
//                SysBreadcrumb()

                // 最近访问标签页
                AddRecentTabs()

                // 主要内容
                MainContent()
            }

            // AI聊天界面
            this.AnimatedVisibility(
                visible = chatViewModel.showChatBot, enter = slideInHorizontally(
                    initialOffsetX = { it }, animationSpec = tween(300)
                ), exit = slideOutHorizontally(
                    targetOffsetX = { it }, animationSpec = tween(300)
                )
            ) {
                AiChatScreen()
            }
        }
    }
}
