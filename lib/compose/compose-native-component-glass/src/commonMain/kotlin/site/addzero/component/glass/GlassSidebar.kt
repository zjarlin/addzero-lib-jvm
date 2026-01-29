package site.addzero.component.glass

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 侧边栏菜单项数据类
 */
data class SidebarItem(
    val id: String,
    val title: String,
    val icon: ImageVector? = null,
    val badge: String? = null,
    val isSelected: Boolean = false
)

/**
 * 玻璃侧边栏
 */
@Composable
fun GlassSidebar(
    items: List<SidebarItem>,
    onItemClick: (SidebarItem) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Menu",
    width: androidx.compose.ui.unit.Dp = 280.dp
) {
    LiquidGlassCard(
        modifier = modifier.width(width),
        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp, topStart = 0.dp, bottomStart = 0.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 标题区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
            
            // 菜单项列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    SidebarMenuItem(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

/**
 * 侧边栏菜单项
 */
@Composable
private fun SidebarMenuItem(
    item: SidebarItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (item.isSelected) 1.02f else 1f,
        animationSpec = tween(200),
        label = "menu_item_scale"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .then(
                if (item.isSelected) {
                    Modifier.neonGlassEffect(
                        shape = RoundedCornerShape(12.dp),
                        glowColor = GlassColors.NeonCyan,
                        intensity = 0.4f
                    )
                } else {
                    Modifier.glassEffect(
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = GlassColors.Surface.copy(alpha = 0.3f)
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            item.icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (item.isSelected) GlassColors.NeonCyan else Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // 标题
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (item.isSelected) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = if (item.isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                modifier = Modifier.weight(1f)
            )
            
            // 徽章
            item.badge?.let { badge ->
                Box(
                    modifier = Modifier
                        .neonGlassEffect(
                            shape = RoundedCornerShape(8.dp),
                            glowColor = GlassColors.NeonMagenta,
                            intensity = 0.6f
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * 紧凑型侧边栏 - 只显示图标
 */
@Composable
fun CompactGlassSidebar(
    items: List<SidebarItem>,
    onItemClick: (SidebarItem) -> Unit,
    modifier: Modifier = Modifier,
    width: androidx.compose.ui.unit.Dp = 80.dp
) {
    GlassCard(
        modifier = modifier.width(width),
        shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 8.dp)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items) { item ->
                CompactMenuItem(
                    item = item,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

/**
 * 紧凑菜单项
 */
@Composable
private fun CompactMenuItem(
    item: SidebarItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = modifier
            .size(48.dp)
            .then(
                if (item.isSelected) {
                    Modifier.neonGlassEffect(
                        shape = RoundedCornerShape(12.dp),
                        glowColor = GlassColors.NeonCyan,
                        intensity = 0.5f
                    )
                } else {
                    Modifier.glassEffect(
                        shape = RoundedCornerShape(12.dp),
                        backgroundColor = Color.Transparent
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        item.icon?.let { icon ->
            Icon(
                imageVector = icon,
                contentDescription = item.title,
                tint = if (item.isSelected) GlassColors.NeonCyan else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
        
        // 徽章指示器
        item.badge?.let {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .align(Alignment.TopEnd)
                    .neonGlassEffect(
                        shape = RoundedCornerShape(4.dp),
                        glowColor = GlassColors.NeonMagenta,
                        intensity = 0.8f
                    )
            )
        }
    }
}
