package site.addzero.component.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 旧版玻璃侧栏的包装实体。
 *
 * 新代码应直接使用泛型 `GlassSidebar`，避免为了渲染再额外定义一层菜单 DTO。
 */
@Deprecated(
    message = "请改用泛型 GlassSidebar，直接传业务节点和 lambda。",
    replaceWith = ReplaceWith(
        expression = "GlassSidebar(items = items, onItemClick = onItemClick, modifier = modifier, title = title, width = width, label = { it.title }, icon = { it.icon }, badge = { it.badge }, selected = { it.isSelected })",
    ),
)
data class SidebarItem(
    val id: String,
    val title: String,
    val icon: ImageVector? = null,
    val badge: String? = null,
    val isSelected: Boolean = false,
)

/**
 * 玻璃态侧栏。
 *
 * 新入口直接接业务节点和语义提取 lambda，不再强制业务先适配成 `SidebarItem`。
 *
 * @param T 业务节点类型
 * @param items 侧栏节点列表
 * @param onItemClick 节点点击回调
 * @param modifier 外层布局修饰
 * @param title 侧栏标题
 * @param width 侧栏宽度
 * @param label 节点标题提取函数
 * @param icon 节点图标提取函数
 * @param badge 节点角标提取函数
 * @param selected 节点选中态判断函数
 */
@Composable
fun <T> GlassSidebar(
    items: List<T>,
    onItemClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Menu",
    width: Dp = 280.dp,
    label: (T) -> String,
    icon: (T) -> ImageVector? = { null },
    badge: (T) -> String? = { null },
    selected: (T) -> Boolean = { false },
) {
    LiquidGlassCard(
        modifier = modifier.width(width),
        shape = RoundedCornerShape(
            topEnd = 24.dp,
            bottomEnd = 24.dp,
            topStart = 0.dp,
            bottomStart = 0.dp,
        ),
        contentPadding = PaddingValues(0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = items, key = { label(it) }) { item ->
                    SidebarMenuItem(
                        item = item,
                        label = label,
                        icon = icon,
                        badge = badge,
                        selected = selected,
                        onClick = { onItemClick(item) },
                    )
                }
            }
        }
    }
}

/**
 * 旧版 `SidebarItem` 兼容入口。
 */
@Deprecated(
    message = "请改用泛型 GlassSidebar，直接传业务节点和 lambda。",
    replaceWith = ReplaceWith(
        expression = "GlassSidebar(items = items, onItemClick = onItemClick, modifier = modifier, title = title, width = width, label = { it.title }, icon = { it.icon }, badge = { it.badge }, selected = { it.isSelected })",
    ),
)
@Suppress("DEPRECATION")
@Composable
fun GlassSidebar(
    items: List<SidebarItem>,
    onItemClick: (SidebarItem) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Menu",
    width: Dp = 280.dp,
) {
    GlassSidebar(
        items = items,
        onItemClick = onItemClick,
        modifier = modifier,
        title = title,
        width = width,
        label = SidebarItem::title,
        icon = SidebarItem::icon,
        badge = SidebarItem::badge,
        selected = SidebarItem::isSelected,
    )
}

/**
 * 兼容旧视觉的紧凑玻璃侧栏。
 *
 * 这一版同样直接接业务节点，适合只显示图标的导航条。
 */
@Composable
fun <T> CompactGlassSidebar(
    items: List<T>,
    onItemClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 80.dp,
    label: (T) -> String,
    icon: (T) -> ImageVector? = { null },
    badge: (T) -> String? = { null },
    selected: (T) -> Boolean = { false },
) {
    GlassCard(
        modifier = modifier.width(width),
        shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 8.dp),
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(items = items, key = { label(it) }) { item ->
                CompactMenuItem(
                    item = item,
                    label = label,
                    icon = icon,
                    badge = badge,
                    selected = selected,
                    onClick = { onItemClick(item) },
                )
            }
        }
    }
}

/**
 * 旧版紧凑侧栏兼容入口。
 */
@Deprecated(
    message = "请改用泛型 CompactGlassSidebar，直接传业务节点和 lambda。",
    replaceWith = ReplaceWith(
        expression = "CompactGlassSidebar(items = items, onItemClick = onItemClick, modifier = modifier, width = width, label = { it.title }, icon = { it.icon }, badge = { it.badge }, selected = { it.isSelected })",
    ),
)
@Suppress("DEPRECATION")
@Composable
fun CompactGlassSidebar(
    items: List<SidebarItem>,
    onItemClick: (SidebarItem) -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 80.dp,
) {
    CompactGlassSidebar(
        items = items,
        onItemClick = onItemClick,
        modifier = modifier,
        width = width,
        label = SidebarItem::title,
        icon = SidebarItem::icon,
        badge = SidebarItem::badge,
        selected = SidebarItem::isSelected,
    )
}

/**
 * 侧栏列表项。
 *
 * 视觉上保持旧玻璃风格，语义上改为泛型业务节点。
 */
@Composable
private fun <T> SidebarMenuItem(
    item: T,
    label: (T) -> String,
    icon: (T) -> ImageVector?,
    badge: (T) -> String?,
    selected: (T) -> Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isSelected = selected(item)
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = tween(200),
        label = "menu_item_scale",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .then(
                if (isSelected) {
                    Modifier.neonGlassEffect(
                        shape = RoundedCornerShape(12.dp),
                        glowColor = GlassColors.NeonCyan,
                        intensity = 0.4f,
                    )
                } else {
                    Modifier.glassEffect(
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = GlassColors.Surface.copy(alpha = 0.3f),
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon(item)?.let { currentIcon ->
                Icon(
                    imageVector = currentIcon,
                    contentDescription = null,
                    tint = if (isSelected) {
                        GlassColors.NeonCyan
                    } else {
                        Color.White.copy(alpha = 0.7f)
                    },
                    modifier = Modifier.size(20.dp),
                )
            }

            Text(
                text = label(item),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                ),
                color = if (isSelected) {
                    Color.White
                } else {
                    Color.White.copy(alpha = 0.8f)
                },
                modifier = Modifier.weight(1f),
            )

            badge(item)?.let { currentBadge ->
                Box(
                    modifier = Modifier
                        .neonGlassEffect(
                            shape = RoundedCornerShape(8.dp),
                            glowColor = GlassColors.NeonMagenta,
                            intensity = 0.6f,
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = currentBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

/**
 * 紧凑菜单项。
 */
@Composable
private fun <T> CompactMenuItem(
    item: T,
    label: (T) -> String,
    icon: (T) -> ImageVector?,
    badge: (T) -> String?,
    selected: (T) -> Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isSelected = selected(item)

    Box(
        modifier = modifier
            .size(48.dp)
            .then(
                if (isSelected) {
                    Modifier.neonGlassEffect(
                        shape = RoundedCornerShape(12.dp),
                        glowColor = GlassColors.NeonCyan,
                        intensity = 0.5f,
                    )
                } else {
                    Modifier.glassEffect(
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = Color.Transparent,
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        icon(item)?.let { currentIcon ->
            Icon(
                imageVector = currentIcon,
                contentDescription = label(item),
                tint = if (isSelected) {
                    GlassColors.NeonCyan
                } else {
                    Color.White.copy(alpha = 0.7f)
                },
                modifier = Modifier.size(24.dp),
            )
        }

        badge(item)?.let {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.TopEnd)
                    .neonGlassEffect(
                        shape = RoundedCornerShape(4.dp),
                        glowColor = GlassColors.NeonMagenta,
                        intensity = 0.8f,
                    ),
            )
        }
    }
}
