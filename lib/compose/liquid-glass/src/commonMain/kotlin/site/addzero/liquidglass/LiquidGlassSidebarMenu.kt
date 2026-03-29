package site.addzero.liquidglass

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun <T> LiquidGlassSidebarMenu(
    title: String,
    items: List<T>,
    selectedId: String,
    itemId: (T) -> String,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    description: String? = "Apple 风格更强调漂浮、单色图标、激活项高亮而不是满屏色块。",
    spec: LiquidGlassSpec = LiquidGlassDefaults.sidebar,
    itemSpec: LiquidGlassSpec = LiquidGlassDefaults.sidebarItem,
    selectedItemSpec: LiquidGlassSpec = LiquidGlassDefaults.sidebarItemSelected,
    colors: LiquidGlassContentColors = LiquidGlassContentColors(),
    subtitle: (T) -> String? = { null },
    icon: (T) -> ImageVector? = { null },
    children: (T) -> List<T> = { emptyList() },
    initiallyExpanded: (T) -> Boolean = { true },
    selectable: (T) -> Boolean = { item -> children(item).isEmpty() },
    leadingSlot: (@Composable RowScope.(T, Boolean, Boolean) -> Unit)? = null,
    contentSlot: (@Composable ColumnScope.(T, Boolean, Boolean) -> Unit)? = null,
    onSelect: (T) -> Unit,
) {
    LiquidGlassCard(
        modifier = modifier,
        spec = spec,
        contentPadding = PaddingValues(22.dp),
    ) {
        val expandedState = remember(items) {
            mutableStateMapOf<String, Boolean>().apply {
                items.registerInitialExpandedState(
                    expandedState = this,
                    itemId = itemId,
                    children = children,
                    initiallyExpanded = initiallyExpanded,
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                color = colors.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
            )
            if (description != null) {
                Text(
                    text = description,
                    color = colors.textMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items.forEach { item ->
                TreeSidebarItem(
                    item = item,
                    level = 0,
                    selectedId = selectedId,
                    expandedState = expandedState,
                    itemId = itemId,
                    label = label,
                    subtitle = subtitle,
                    icon = icon,
                    children = children,
                    initiallyExpanded = initiallyExpanded,
                    selectable = selectable,
                    leadingSlot = leadingSlot,
                    contentSlot = contentSlot,
                    itemSpec = itemSpec,
                    selectedItemSpec = selectedItemSpec,
                    colors = colors,
                    onSelect = onSelect,
                )
            }
        }
    }
}

@Composable
private fun <T> ColumnScope.TreeSidebarItem(
    item: T,
    level: Int,
    selectedId: String,
    expandedState: MutableMap<String, Boolean>,
    itemId: (T) -> String,
    label: (T) -> String,
    subtitle: (T) -> String?,
    icon: (T) -> ImageVector?,
    children: (T) -> List<T>,
    initiallyExpanded: (T) -> Boolean,
    selectable: (T) -> Boolean,
    leadingSlot: (@Composable RowScope.(T, Boolean, Boolean) -> Unit)?,
    contentSlot: (@Composable ColumnScope.(T, Boolean, Boolean) -> Unit)?,
    itemSpec: LiquidGlassSpec,
    selectedItemSpec: LiquidGlassSpec,
    colors: LiquidGlassContentColors,
    onSelect: (T) -> Unit,
) {
    val id = itemId(item)
    val childItems = children(item)
    val isBranch = childItems.isNotEmpty()
    val selected = selectable(item) && id == selectedId
    val descendantSelected = childItems.any { child ->
        child.containsSelection(
            selectedId = selectedId,
            itemId = itemId,
            children = children,
        )
    }
    val expanded = expandedState[id] ?: initiallyExpanded(item)
    val spec = when {
        selected -> selectedItemSpec
        descendantSelected -> itemSpec.copy(
            edge = maxOf(itemSpec.edge, selectedItemSpec.edge * 0.72f),
            surfaceColor = selectedItemSpec.surfaceColor.copy(alpha = selectedItemSpec.surfaceColor.alpha * 0.52f),
            tint = selectedItemSpec.tint.copy(alpha = selectedItemSpec.tint.alpha * 0.28f),
            borderColor = selectedItemSpec.borderColor.copy(alpha = selectedItemSpec.borderColor.alpha * 0.40f),
        )
        else -> itemSpec
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .liquidGlassSurface(spec)
            .clip(spec.shape)
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = {
                    if (selectable(item)) {
                        onSelect(item)
                    } else if (isBranch) {
                        expandedState[id] = !expanded
                    }
                },
            ),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(spec.shape),
        ) {
            if (selected || descendantSelected) {
                Box(modifier = selectedSidebarTopGlow())
                Box(modifier = selectedSidebarEndGlow())
                Box(modifier = selectedSidebarCornerCaustic())
                Box(modifier = selectedSidebarSweepHighlight())
            }
        }
        Row(
            modifier = Modifier.sidebarTreeItemPadding(level),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BranchToggle(
                isBranch = isBranch,
                expanded = expanded,
                colors = colors,
                onToggle = {
                    expandedState[id] = !expanded
                },
            )
            if (leadingSlot == null) {
                SidebarLeadingIcon(
                    icon = icon(item),
                    colors = colors,
                )
            } else {
                leadingSlot(item, selected, descendantSelected)
            }
            if (contentSlot == null) {
                DefaultSidebarContent(
                    title = label(item),
                    subtitle = subtitle(item),
                    selected = selected,
                    descendantSelected = descendantSelected,
                    colors = colors,
                )
            } else {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    contentSlot(item, selected, descendantSelected)
                }
            }
        }
    }

    if (isBranch && expanded) {
        childItems.forEach { child ->
            TreeSidebarItem(
                item = child,
                level = level + 1,
                selectedId = selectedId,
                expandedState = expandedState,
                itemId = itemId,
                label = label,
                subtitle = subtitle,
                icon = icon,
                children = children,
                initiallyExpanded = initiallyExpanded,
                selectable = selectable,
                leadingSlot = leadingSlot,
                contentSlot = contentSlot,
                itemSpec = itemSpec,
                selectedItemSpec = selectedItemSpec,
                colors = colors,
                onSelect = onSelect,
            )
        }
    }
}

@Composable
private fun RowScope.DefaultSidebarContent(
    title: String,
    subtitle: String?,
    selected: Boolean,
    descendantSelected: Boolean,
    colors: LiquidGlassContentColors,
) {
    Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = title,
            color = colors.textPrimary,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = when {
                selected -> FontWeight.Bold
                descendantSelected -> FontWeight.SemiBold
                else -> FontWeight.Medium
            },
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                color = when {
                    selected || descendantSelected -> colors.textSecondary
                    else -> colors.textMuted
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun BranchToggle(
    isBranch: Boolean,
    expanded: Boolean,
    colors: LiquidGlassContentColors,
    onToggle: () -> Unit,
) {
    if (!isBranch) {
        Box(modifier = Modifier.size(12.dp))
        return
    }

    Box(
        modifier = Modifier
            .size(12.dp)
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = onToggle,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (expanded) {
                Icons.Rounded.ExpandMore
            } else {
                Icons.AutoMirrored.Rounded.KeyboardArrowRight
            },
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun SidebarLeadingIcon(
    icon: ImageVector?,
    colors: LiquidGlassContentColors,
) {
    if (icon == null) {
        Box(modifier = Modifier.size(18.dp))
        return
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = colors.textSecondary,
        modifier = Modifier.size(18.dp),
    )
}

/** 树节点内容缩进：按层级递增左内边距，形成真正的树结构。 */
private fun Modifier.sidebarTreeItemPadding(
    level: Int,
): Modifier = padding(
    start = (14 + level * 18).dp,
    top = 14.dp,
    end = 14.dp,
    bottom = 14.dp,
)

private fun <T> List<T>.registerInitialExpandedState(
    expandedState: MutableMap<String, Boolean>,
    itemId: (T) -> String,
    children: (T) -> List<T>,
    initiallyExpanded: (T) -> Boolean,
) {
    forEach { item ->
        val childItems = children(item)
        if (childItems.isNotEmpty()) {
            expandedState[itemId(item)] = initiallyExpanded(item)
            childItems.registerInitialExpandedState(
                expandedState = expandedState,
                itemId = itemId,
                children = children,
                initiallyExpanded = initiallyExpanded,
            )
        }
    }
}

private fun <T> T.containsSelection(
    selectedId: String,
    itemId: (T) -> String,
    children: (T) -> List<T>,
): Boolean {
    if (itemId(this) == selectedId) {
        return true
    }
    return children(this).any { child ->
        child.containsSelection(
            selectedId = selectedId,
            itemId = itemId,
            children = children,
        )
    }
}

/** 激活项左上角高光：只在内部聚光，不形成整块选中面板。 */
private fun BoxScope.selectedSidebarTopGlow(): Modifier =
    Modifier.align(Alignment.TopStart)
        .offset(x = (-2).dp, y = (-2).dp)
        .size(64.dp)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.05f),
                    Color(0xFFDDF6FF).copy(alpha = 0.022f),
                    Color.Transparent,
                ),
            ),
            shape = CircleShape,
        )
        .blur(18.dp)

/** 激活项右侧折射辉光：让选中态更像液体鼓起，而不是边框发白。 */
private fun BoxScope.selectedSidebarEndGlow(): Modifier =
    Modifier.align(Alignment.CenterEnd)
        .offset(x = 8.dp)
        .size(70.dp)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    LiquidGlassDefaults.accentStrong.copy(alpha = 0.05f),
                    LiquidGlassDefaults.accent.copy(alpha = 0.020f),
                    Color.Transparent,
                ),
            ),
            shape = CircleShape,
        )
        .blur(20.dp)

/** 左上角弧形焦散：改用一团软高光代替直角线段，避免出现矩形尖角。 */
private fun BoxScope.selectedSidebarCornerCaustic(): Modifier =
    Modifier.align(Alignment.TopStart)
        .offset(x = (-10).dp, y = (-10).dp)
        .size(88.dp)
        .background(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.06f),
                    Color(0xFFDDF6FF).copy(alpha = 0.024f),
                    Color.Transparent,
                ),
            ),
            shape = CircleShape,
        )
        .blur(22.dp)

/** 选中项右下扫光：补一点流动感，但不形成边框。 */
private fun BoxScope.selectedSidebarSweepHighlight(): Modifier =
    Modifier.align(Alignment.BottomEnd)
        .offset(x = (-8).dp, y = (-6).dp)
        .fillMaxWidth(0.16f)
        .height(14.dp)
        .rotate(-14f)
        .background(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    LiquidGlassDefaults.accentStrong.copy(alpha = 0.022f),
                    Color.White.copy(alpha = 0.018f),
                ),
            ),
            shape = RoundedCornerShape(999.dp),
        )
        .blur(8.dp)
