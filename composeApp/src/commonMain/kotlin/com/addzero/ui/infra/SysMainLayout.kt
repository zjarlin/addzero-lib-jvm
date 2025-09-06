package com.addzero.ui.infra

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.addzero.screens.ai.AiChatScreen
import com.addzero.ui.infra.model.menu.MenuViewModel
import com.addzero.ui.infra.model.menu.SideMenu
import com.addzero.ui.infra.model.navigation.RecentTabsManagerViewModel
import com.addzero.ui.infra.navigation.NavigationObserver
import com.addzero.viewmodel.ChatViewModel
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MainLayout() {
    // 🚀 使用新的响应式主布局
    val navController = rememberNavController()
    val vm = koinViewModel<RecentTabsManagerViewModel>()
    val chatViewModel = koinViewModel<ChatViewModel>()
    // 添加导航观察器
    with(vm) {
        NavigationObserver(
            navController = navController,
            getRouteTitle = { route ->
                MenuViewModel.getRouteTitleByKey(route)
            }
        )
    }
    // 搜索框状态
    val isSearchOpen = remember { mutableStateOf(false) }
    // 根据布局模式渲染不同的布局

    Scaffold(
        topBar = {
            SysTopBar(
                navController = navController,
                isSearchOpen = isSearchOpen
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 侧边栏
            SideMenu()

            // 主内容区
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 面包屑导航
                SysBreadcrumb(
                    navController = navController,
                    onNavigate = {
                        navController.navigate(it)
                    },
                    currentRoute = MenuViewModel.currentRoute
                )

                // 最近访问标签页
                AddRecentTabs(
                    navController = navController,
                    listenShortcuts = false,
                    recentViewModel = vm
                )

                // 主要内容
                MainContent(navController = navController)
            }

            // AI聊天界面
            this.AnimatedVisibility(
                visible = chatViewModel.showChatBot,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween<androidx.compose.ui.unit.IntOffset>(300)
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween<androidx.compose.ui.unit.IntOffset>(300)
                )
            ) {
                AiChatScreen()
            }
        }
    }
}
