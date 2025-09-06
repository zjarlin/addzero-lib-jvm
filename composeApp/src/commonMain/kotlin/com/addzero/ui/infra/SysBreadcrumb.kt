package com.addzero.ui.infra

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.addzero.di.NavgationViewModel
import com.addzero.generated.RouteTable
import com.addzero.viewmodel.SysRouteViewModel

/**
 * 面包屑导航组件
 */
@Composable
context(navgationViewModel: NavgationViewModel,sysRouteViewModel: SysRouteViewModel)
fun SysBreadcrumb(
) {
    val currentRoute = sysRouteViewModel.currentRoute

    val navController = navgationViewModel.getNavController()
    val breadcrumbs = remember(currentRoute) {
        getBreadcrumbs(currentRoute)
    }

    // 面包屑导航
    Row(
        modifier = Modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        breadcrumbs.forEachIndexed { index, breadcrumbItem ->
            // 面包屑项文本
            Text(
                text = breadcrumbItem.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (breadcrumbItem.isActive) FontWeight.Bold else FontWeight.Normal,
                color = if (breadcrumbItem.isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable(enabled = !breadcrumbItem.isActive) {
                    // 如果不是当前活动项且有导航控制器，则导航到对应路由
                    if (!breadcrumbItem.isActive) {
                        if (RouteTable.allRoutes.containsKey(breadcrumbItem.route)) {
                            navController.navigate(breadcrumbItem.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                            sysRouteViewModel.updateRoute(breadcrumbItem.route)
                        }
                    }
                })

            // 分隔符（除了最后一个项）
            if (index < breadcrumbs.size - 1) {
                Text(
                    modifier = Modifier.clickable(
                    onClick = {
                        navgationViewModel.navigate(breadcrumbItem.route)
                    }),
                    text = "/",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

// 面包屑项数据类
private data class BreadcrumbItem(
    val title: String, val route: String, val isActive: Boolean = false
)

// 根据当前路由生成面包屑项列表
context(sysRouteViewModel: SysRouteViewModel)
private fun getBreadcrumbs(currentRoute: String): List<BreadcrumbItem> {
    val cacleBreadcrumb = sysRouteViewModel.cacleBreadcrumb
    return cacleBreadcrumb.map {
        BreadcrumbItem(
            title = it.title, route = it.path, isActive = it.path == currentRoute
        )
    }


}
