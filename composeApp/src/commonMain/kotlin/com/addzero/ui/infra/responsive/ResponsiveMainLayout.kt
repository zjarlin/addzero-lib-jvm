package com.addzero.ui.infra.responsive

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.addzero.screens.ai.AiChatScreen
import com.addzero.ui.infra.AddRecentTabs
import com.addzero.ui.infra.MainContent
import com.addzero.ui.infra.SysBreadcrumb
import com.addzero.ui.infra.SysTopBar
import com.addzero.ui.infra.model.menu.MenuViewModel
import com.addzero.ui.infra.model.menu.SideMenu
import com.addzero.ui.infra.model.navigation.RecentTabsManager
import com.addzero.ui.infra.navigation.NavigationObserver
import com.addzero.viewmodel.ChatViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * 🚀 响应式主布局组件
 *
 * 根据屏幕尺寸自动切换侧边栏和顶部导航栏布局
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ResponsiveMainLayout(
    forceLayoutMode: LayoutMode? = null
) {
    val navController = rememberNavController()
    val vm = koinViewModel<RecentTabsManager>()
    val chatViewModel = koinViewModel<ChatViewModel>()

    // 获取响应式配置
    val config = rememberResponsiveConfig(forceLayoutMode)

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

/**
 * 🖥️ 侧边栏布局（桌面端）
 */
@Composable
private fun SidebarLayout(
    navController: NavHostController,
    vm: RecentTabsManager,
    chatViewModel: ChatViewModel,
    showChatBot: Boolean,
    isSearchOpen: MutableState<Boolean>,
    config: ResponsiveConfig
) {
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
            AnimatedVisibility(
                visible = showChatBot,
                enter = slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            ) {
                AiChatScreen()
            }
        }
    }
}

/**
 * 📱 顶部导航栏布局（移动端）
 */
@Composable
private fun TopbarLayout(
    navController: NavHostController,
    vm: RecentTabsManager,
    chatViewModel: ChatViewModel,
    showChatBot: Boolean,
    isSearchOpen: MutableState<Boolean>,
    config: ResponsiveConfig
) {
    Scaffold(
        topBar = {
            Column {
                // 系统顶部栏
                SysTopBar(
                    navController = navController,
                    isSearchOpen = isSearchOpen
                )

                // 顶部导航栏
                TopNavigationBar(
                    config = config
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 面包屑导航（移动端可选）
//            if (config.screenSize != ScreenSize.MOBILE) {
//                Breadcrumb(
//                    currentRouteRefPath = MenuViewModel.currentRoute,
//                    navController = navController
//                )
//            }

            // 最近访问标签页
            AddRecentTabs(
                navController = navController,
                listenShortcuts = false,
                recentViewModel = vm
            )

            // 主内容区和聊天界面
            Box(modifier = Modifier.weight(1f)) {
                // 主要内容
                MainContent(navController = navController)

                // AI聊天界面（移动端使用覆盖模式）
//                AnimatedVisibility(
//                    visible = showChatBot,
//                    enter = slideInVertically(
//                        initialOffsetY = { it },
//                        animationSpec = tween(300)
//                    ) + fadeIn(),
//                    exit = slideOutVertically(
//                        targetOffsetY = { it },
//                        animationSpec = tween(300)
//                    ) + fadeOut()
//                ) {
//                    Surface(
//                        modifier = Modifier.fillMaxSize(),
//                        tonalElevation = 8.dp
//                    ) {
//                        AiChatScreen()
//                    }
//                }
            }
        }
    }
}
