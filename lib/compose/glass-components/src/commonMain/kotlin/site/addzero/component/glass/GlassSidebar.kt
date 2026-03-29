package site.addzero.component.glass

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import com.kyant.shapes.RoundedRectangle
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─────────────────────────────────────────────────────────────
// 泛型树形 GlassSidebar（支持任意树节点类型）
// ─────────────────────────────────────────────────────────────

/**
 * GlassSidebar — 玻璃风格侧边栏（泛型树形版）
 *
 * 接受任意类型 [T] 的树节点列表，通过 lambda 参数提取节点属性，
 * 从而与具体的数据模型（如 MenuNode）解耦。
 *
 * 功能：
 * - 树形缩进显示，缩进层级对应节点深度
 * - 可展开/折叠的父节点（有子节点的节点）
 * - 叶节点点击触发 [onLeafClick]
 * - 选中状态高亮
 *
 * @param T 树节点类型
 * @param roots 顶层树节点列表
 * @param nodeKey 提取节点唯一标识的 lambda
 * @param nodeTitle 提取节点显示标题的 lambda
 * @param nodeIcon 提取节点图标（emoji 字符串）的 lambda，返回 null 表示无图标
 * @param nodeChildren 提取节点子节点列表的 lambda
 * @param nodeVisible 判断节点是否可见的 lambda
 * @param selectedKey 当前选中节点的 key
 * @param onLeafClick 叶节点点击回调，参数为被点击的节点
 * @param modifier 外部修饰符
 * @param title 侧边栏标题（显示在顶部）
 * @param width 侧边栏宽度，默认 220dp
 * @param indentPerLevel 每层缩进的宽度，默认 16dp
 * @param initialExpandedKeys 初始展开的节点 key 集合，默认全部展开
 */
@Composable
fun <T> GlassSidebar(
    roots: List<T>,
    nodeKey: (T) -> String,
    nodeTitle: (T) -> String,
    nodeIcon: (T) -> String?,
    nodeChildren: (T) -> List<T>,
    nodeVisible: (T) -> Boolean,
    selectedKey: String,
    onLeafClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    width: Dp = 220.dp,
    indentPerLevel: Dp = 16.dp,
    initialExpandedKeys: Set<String>? = null,
) {
    // 展开状态管理：key -> 是否展开
    val expandedState = remember {
        mutableStateMapOf<String, Boolean>().apply {
            if (initialExpandedKeys != null) {
                initialExpandedKeys.forEach { put(it, true) }
            }
        }
    }

    // 默认：如果 initialExpandedKeys 为 null，所有父节点默认展开
    val defaultExpanded = initialExpandedKeys == null

    Column(
        modifier = modifier
            .width(width)
            .fillMaxHeight()
            .jbPurpleGlassEffect()
            .padding(vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        // 标题
        if (title != null) {
            Text(
                text = title,
                color = GlassTheme.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 递归渲染树节点
        roots.forEach { node ->
            if (nodeVisible(node)) {
                TreeNodeItem(
                    node = node,
                    depth = 0,
                    nodeKey = nodeKey,
                    nodeTitle = nodeTitle,
                    nodeIcon = nodeIcon,
                    nodeChildren = nodeChildren,
                    nodeVisible = nodeVisible,
                    selectedKey = selectedKey,
                    onLeafClick = onLeafClick,
                    expandedState = expandedState,
                    defaultExpanded = defaultExpanded,
                    indentPerLevel = indentPerLevel,
                )
            }
        }
    }
}

/**
 * 递归渲染单个树节点及其子节点。
 */
@Composable
private fun <T> TreeNodeItem(
    node: T,
    depth: Int,
    nodeKey: (T) -> String,
    nodeTitle: (T) -> String,
    nodeIcon: (T) -> String?,
    nodeChildren: (T) -> List<T>,
    nodeVisible: (T) -> Boolean,
    selectedKey: String,
    onLeafClick: (T) -> Unit,
    expandedState: MutableMap<String, Boolean>,
    defaultExpanded: Boolean,
    indentPerLevel: Dp,
) {
    val key = nodeKey(node)
    val children = nodeChildren(node).filter { nodeVisible(it) }
    val hasChildren = children.isNotEmpty()
    val isExpanded = expandedState[key] ?: defaultExpanded
    val isSelected = key == selectedKey
    val icon = nodeIcon(node)
    val titleText = nodeTitle(node)

    // 当前节点行
    val indentPadding = indentPerLevel * depth

    val backgroundColor = if (isSelected) {
        GlassTheme.JBPurpleHighlight.copy(alpha = 0.2f)
    } else {
        Color.Transparent
    }

    val textColor = when {
        isSelected -> GlassTheme.JBPurpleHighlight
        hasChildren -> GlassTheme.TextPrimary
        else -> GlassTheme.TextSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp + indentPadding, end = 8.dp, top = 2.dp, bottom = 2.dp)
            .clip(RoundedRectangle(10.dp))
            .background(backgroundColor)
            .clickable {
                if (hasChildren) {
                    // 虚拟父节点 / 有子节点的节点：切换展开/折叠
                    expandedState[key] = !isExpanded
                } else {
                    // 叶节点：触发导航
                    onLeafClick(node)
                }
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start,
    ) {
        // 展开/折叠指示器（仅父节点显示）
        if (hasChildren) {
            Text(
                text = if (isExpanded) "▾" else "▸",
                color = GlassTheme.TextTertiary,
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        // 图标
        if (icon != null) {
            Text(
                text = icon,
                fontSize = 16.sp,
            )
            Spacer(modifier = Modifier.width(10.dp))
        }

        // 标题
        Text(
            text = titleText,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = when {
                isSelected -> FontWeight.SemiBold
                hasChildren -> FontWeight.Medium
                else -> FontWeight.Normal
            },
        )
    }

    // 子节点（带展开/折叠动画）
    AnimatedVisibility(
        visible = isExpanded && hasChildren,
        enter = expandVertically(),
        exit = shrinkVertically(),
    ) {
        Column {
            children.forEach { child ->
                TreeNodeItem(
                    node = child,
                    depth = depth + 1,
                    nodeKey = nodeKey,
                    nodeTitle = nodeTitle,
                    nodeIcon = nodeIcon,
                    nodeChildren = nodeChildren,
                    nodeVisible = nodeVisible,
                    selectedKey = selectedKey,
                    onLeafClick = onLeafClick,
                    expandedState = expandedState,
                    defaultExpanded = defaultExpanded,
                    indentPerLevel = indentPerLevel,
                )
            }
        }
    }
}
