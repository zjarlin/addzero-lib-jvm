package com.addzero.ui.infra.model.menu

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.addzero.component.tree.rememberTreeViewModel
import com.addzero.compose.icons.IconMap
import com.addzero.entity.sys.menu.EnumSysMenuType
import com.addzero.entity.sys.menu.SysMenuVO
import com.addzero.generated.RouteKeys
import com.addzero.ui.infra.theme.AppThemeType
import com.addzero.ui.infra.theme.ThemeViewModel
import com.addzero.util.str.isNotBlank
import com.addzero.viewmodel.SysRouteViewModel

/**
 * 侧边菜单组件
 *
 * 显示应用的主要导航菜单，支持多级菜单结构
 * 使用AddTree组件实现菜单树渲染
 */
@Composable
context(sysRouteViewModel: SysRouteViewModel, themeViewModel: ThemeViewModel)
fun SideMenu() {
    val currentTheme = themeViewModel.currentTheme

    // 🚀 纯粹的 AddTree 组件，使用 Surface 控制大小和样式
    Surface(
        modifier = Modifier
            .width(if (sysRouteViewModel.isExpand) 240.dp else 56.dp)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        TreeContent()
    }
}

/**
 * 🚀 纯粹的树组件内容
 */
@Composable
context(sysRouteViewModel: SysRouteViewModel)
private fun TreeContent() {
    // 🎯 使用新的 TreeViewModel API
    val viewModel = rememberTreeViewModel<SysMenuVO>()

    // 配置 ViewModel
    LaunchedEffect(sysRouteViewModel.menuItems) {
        viewModel.configure(
            getId = { it.path },
            getLabel = { it.title },
            getChildren = { it.children },
            getIcon = { getMenuIcon(it) }
        )
        viewModel.onNodeClick = { selectedMenu ->
            // 处理菜单项点击
            if (selectedMenu.enumSysMenuType == EnumSysMenuType.SCREEN && selectedMenu.children.isEmpty()) {
                // 如果是页面类型且没有子项，才进行导航
                sysRouteViewModel.updateRoute(selectedMenu.path)
            }
            // 注意：折叠/展开状态由AddTree内部管理，这里不需要手动处理
        }
        viewModel.setItems(
            sysRouteViewModel.menuItems,
            setOf(RouteKeys.HOME_SCREEN)
        )
    }

    com.addzero.component.tree.AddTree(
        viewModel = viewModel,
        modifier = Modifier.fillMaxSize(),
        compactMode = !sysRouteViewModel.isExpand // 🚀 传递收起状态，启用紧凑模式
    )
}

// 🎨 AppThemeType 已经有内置的 isGradient() 方法，无需重复定义

@Composable
fun getMenuIcon(vO: SysMenuVO): ImageVector? {
    val path = vO.path
    return if (vO.icon.isNotBlank()) {
        val vector = IconMap[vO.icon].vector
        vector
    } else {
        // 根据路径推断图标，移除重复条件
        when {
            vO.children.isNotEmpty() -> Icons.AutoMirrored.Filled.ViewList
            path.contains("home") -> Icons.Default.Home
            path.contains("dashboard") -> Icons.Default.Dashboard
            path.contains("user") || path.contains("account") -> Icons.Default.Person
            path.contains("setting") -> Icons.Default.Settings
            path.contains("report") -> Icons.Default.BarChart
            path.contains("data") -> Icons.Default.Storage
            path.contains("file") -> Icons.AutoMirrored.Filled.InsertDriveFile
            path.contains("notification") -> Icons.Default.Notifications
            path.contains("message") -> Icons.Default.Email
            path.contains("calendar") -> Icons.Default.Event
            path.contains("task") -> Icons.AutoMirrored.Filled.Assignment
            path.contains("analytics") -> Icons.Default.Analytics
            path.contains("help") -> Icons.AutoMirrored.Filled.Help
            path.contains("about") -> Icons.Default.Info
            else -> Icons.AutoMirrored.Filled.Article
        }
    }
}

// customRender4SysMenu 已移除，使用 AddTree 内置渲染

