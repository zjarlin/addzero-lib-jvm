package site.addzero.ui.infra

import androidx.compose.animation.core.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import site.addzero.component.text.BlueText
import site.addzero.compose.icons.IconMap
import site.addzero.generated.RouteKeys
import site.addzero.generated.isomorphic.SysFavoriteTabIso
import site.addzero.ui.infra.model.favorite.FavoriteTabsViewModel
import site.addzero.viewmodel.SysRouteViewModel

/**
 * 常用标签页栏组件
 * 显示在顶部栏中的常用页面快捷访问
 */
@Composable
context(sysRouteViewModel: SysRouteViewModel, favoriteViewModel: FavoriteTabsViewModel)
fun FavoriteTabsBar(modifier: Modifier = Modifier) {
    val favoriteTabs = favoriteViewModel.favoriteTabs
    val currentRoute = sysRouteViewModel.currentRoute
    if (favoriteTabs.isEmpty() && !favoriteViewModel.isLoading) {
        return
    }
    // 星星动画效果
    val infiniteTransition = rememberInfiniteTransition(label = "star_animation")

    // 左侧星星的旋转动画
    val leftStarRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "left_star_rotation"
    )

    // 右侧星星的旋转动画（反向）
    val rightStarRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "right_star_rotation"
    )

    // 星星的缩放动画
    val starScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_scale"
    )

    // 直接显示内容，不使用Surface包装（因为已经在TopAppBar中）
    Row(
        modifier = modifier.padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // 左侧装饰星星
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier
                .size(18.dp)
                .rotate(leftStarRotation)
                .scale(starScale)
        )
        BlueText("常用标签页")

        Spacer(modifier = Modifier.width(8.dp))

        // 标签页列表（可水平滚动）
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            favoriteTabs.forEach { tab ->
                FavoriteTabItem(
                    tab = tab,
                    isActive = currentRoute == tab.routeKey,
                    onClick = {
                        sysRouteViewModel.updateRoute(tab.routeKey)
                    },
                    onRemove = {
                        favoriteViewModel.removeFromFavorites(tab.routeKey)
                    }
                )
            }
        }

        // 添加当前页面到常用标签页按钮
//        if (!favoriteViewModel.isFavorite(currentRoute)) {
//            Spacer(modifier = Modifier.width(8.dp))
//            AddIconButton(
//                imageVector = Icons.Default.StarBorder,
//                text = "添加到常用",
//                onClick = {
//                    favoriteViewModel.addToFavorites(currentRoute)
//                },
//                modifier = Modifier.size(28.dp)
//            )
//        }

        Spacer(modifier = Modifier.width(8.dp))

        // 右侧装饰星星
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier
                .size(18.dp)
                .rotate(rightStarRotation)
                .scale(starScale)
        )
    }

}

/**
 * 常用标签页项
 */
@Composable
private fun FavoriteTabItem(
    tab: SysFavoriteTabIso,
    isActive: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isActive) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    var showRemoveButton by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp)),
        color = backgroundColor,
        tonalElevation = if (isActive) 4.dp else 0.dp,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = if (isActive) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = RouteKeys.allMeta.find { it.routePath == tab.routeKey }?.title.toString(),
                color = contentColor,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // 移除按钮（仅在激活状态或鼠标悬停时显示）
            if (isActive) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "移除",
                        tint = contentColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}


