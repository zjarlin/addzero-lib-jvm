package site.addzero.appsidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T> AppSidebar(
    title: String,
    items: List<T>,
    itemId: (T) -> String,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    state: AppSidebarState = rememberAppSidebarState(),
    config: AppSidebarConfig = appSidebarConfig(),
    icon: (T) -> ImageVector? = { null },
    children: (T) -> List<T> = { emptyList() },
    initiallyExpanded: (T) -> Boolean = { true },
    selectable: (T) -> Boolean = { item -> children(item).isEmpty() },
    events: AppSidebarEvents<T> = appSidebarEvents(),
    slots: AppSidebarSlots<T> = appSidebarSlots(),
) {
    val tokens = remember(config.style) {
        config.style.resolveTokens()
    }
    val visibleItems = remember(items, state.keyword) {
        items.filterSidebarItems(
            keyword = state.keyword,
            itemId = itemId,
            label = label,
            children = children,
        )
    }

    LaunchedEffect(items) {
        state.ensureInitialized(
            items = items,
            itemId = itemId,
            children = children,
            initiallyExpanded = initiallyExpanded,
            selectable = selectable,
        )
    }

    Box(
        modifier = modifier.sidebarContainer(tokens),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(tokens.contentPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SidebarHeader(
                title = title,
                supportText = config.supportText,
                headerSlot = slots.header,
                tokens = tokens,
            )

            if (config.searchEnabled) {
                SidebarSearchField(
                    value = state.keyword,
                    placeholder = config.searchPlaceholder,
                    onValueChange = { keyword ->
                        state.updateKeyword(keyword)
                        events.onKeywordChange(keyword)
                    },
                    onClear = {
                        state.clearKeyword()
                        events.onKeywordChange("")
                    },
                    tokens = tokens,
                )
            }

            Box(
                modifier = Modifier.weight(1f),
            ) {
                if (visibleItems.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(vertical = tokens.emptyVerticalPadding),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        slots.empty.invoke(this, state.keyword)
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        visibleItems.forEach { item ->
                            SidebarTreeItem(
                                node = item,
                                level = 0,
                                state = state,
                                tokens = tokens,
                                searchActive = state.keyword.isNotBlank(),
                                itemId = itemId,
                                label = label,
                                icon = icon,
                                children = children,
                                initiallyExpanded = initiallyExpanded,
                                selectable = selectable,
                                slots = slots,
                                onItemClick = events::onItemClick,
                            )
                        }
                    }
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = slots.footer,
            )
        }
    }
}

@Composable
private fun SidebarHeader(
    title: String,
    supportText: String?,
    headerSlot: @Composable ColumnScope.() -> Unit,
    tokens: SidebarStyleTokens,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            color = tokens.textPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
        )
        if (supportText != null) {
            Text(
                text = supportText,
                color = tokens.textMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        headerSlot()
    }
}

@Composable
private fun SidebarSearchField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    tokens: SidebarStyleTokens,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().searchFieldContainer(tokens),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = tokens.textPrimary,
        ),
        leadingIcon = {
            androidx.compose.material3.Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = tokens.textMuted,
            )
        },
        trailingIcon = {
            if (value.isNotBlank()) {
                Text(
                    text = "清空",
                    color = tokens.textMuted,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable(onClick = onClear),
                )
            }
        },
        placeholder = {
            Text(
                text = placeholder,
                color = tokens.textFaint,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = tokens.searchBackground,
            unfocusedContainerColor = tokens.searchBackground,
            disabledContainerColor = tokens.searchBackground,
            focusedTextColor = tokens.textPrimary,
            unfocusedTextColor = tokens.textPrimary,
            cursorColor = tokens.textPrimary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search,
        ),
    )
}

@Composable
private fun <T> ColumnScope.SidebarTreeItem(
    node: AppSidebarVisibleNode<T>,
    level: Int,
    state: AppSidebarState,
    tokens: SidebarStyleTokens,
    searchActive: Boolean,
    itemId: (T) -> String,
    label: (T) -> String,
    icon: (T) -> ImageVector?,
    children: (T) -> List<T>,
    initiallyExpanded: (T) -> Boolean,
    selectable: (T) -> Boolean,
    slots: AppSidebarSlots<T>,
    onItemClick: (T) -> Unit,
) {
    val item = node.item
    val id = itemId(item)
    val childItems = children(item)
    val isBranch = childItems.isNotEmpty()
    val selected = id == state.selectedId
    val descendantSelected = childItems.any { child ->
        child.containsSelection(
            selectedId = state.selectedId,
            itemId = itemId,
            children = children,
        )
    }
    val expanded = if (searchActive) {
        true
    } else {
        state.expandedItems[id] ?: initiallyExpanded(item)
    }
    val leadingSlot = slots.leading
    val labelSlot = slots.label
    val trailingSlot = slots.trailing

    Box(
        modifier = Modifier.fillMaxWidth()
            .sidebarItemFrame(
                selected = selected,
                descendantSelected = descendantSelected,
                tokens = tokens,
            )
            .clickable(
                onClick = {
                    if (isBranch) {
                        state.toggleExpanded(id)
                    } else {
                        state.select(
                            item = item,
                            itemId = itemId,
                            children = children,
                            selectable = selectable,
                        )
                        if (selectable(item)) {
                            onItemClick(item)
                        }
                    }
                },
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().sidebarItemPadding(level, tokens),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SidebarBranchToggle(
                isBranch = isBranch,
                expanded = expanded,
                tokens = tokens,
                onToggle = {
                    state.toggleExpanded(id)
                },
            )
            if (leadingSlot == null) {
                SidebarIcon(
                    icon = icon(item),
                    tokens = tokens,
                )
            } else {
                leadingSlot.invoke(this, item, selected, descendantSelected)
            }
            if (labelSlot == null) {
                DefaultSidebarLabel(
                    text = label(item),
                    selected = selected,
                    tokens = tokens,
                )
            } else {
                labelSlot.invoke(this, item, selected, descendantSelected)
            }
            if (trailingSlot != null) {
                trailingSlot.invoke(this, item, selected, descendantSelected)
            }
        }
    }

    if (isBranch && expanded) {
        node.children.forEach { child ->
            SidebarTreeItem(
                node = child,
                level = level + 1,
                state = state,
                tokens = tokens,
                searchActive = searchActive,
                itemId = itemId,
                label = label,
                icon = icon,
                children = children,
                initiallyExpanded = initiallyExpanded,
                selectable = selectable,
                slots = slots,
                onItemClick = onItemClick,
            )
        }
    }
}

@Composable
private fun RowScope.DefaultSidebarLabel(
    text: String,
    selected: Boolean,
    tokens: SidebarStyleTokens,
) {
    Text(
        text = text,
        color = tokens.textPrimary,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        modifier = Modifier.weight(1f),
    )
}

@Composable
private fun SidebarBranchToggle(
    isBranch: Boolean,
    expanded: Boolean,
    tokens: SidebarStyleTokens,
    onToggle: () -> Unit,
) {
    if (!isBranch) {
        Spacer(modifier = Modifier.size(14.dp))
        return
    }

    Box(
        modifier = Modifier.size(16.dp).clickable(onClick = onToggle),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Icon(
            imageVector = if (expanded) {
                Icons.Rounded.ExpandMore
            } else {
                Icons.AutoMirrored.Rounded.KeyboardArrowRight
            },
            contentDescription = null,
            tint = tokens.textMuted,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun SidebarIcon(
    icon: ImageVector?,
    tokens: SidebarStyleTokens,
) {
    if (icon == null) {
        Spacer(modifier = Modifier.width(18.dp))
        return
    }

    androidx.compose.material3.Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tokens.textPrimary,
        modifier = Modifier.size(18.dp),
    )
}

@Composable
internal fun ColumnScope.DefaultSidebarEmpty(
    keyword: String,
) {
    Text(
        text = "没有找到 “$keyword” 对应的导航项。",
        color = AppSidebarStyle.Default.resolveTokens().textMuted,
        style = MaterialTheme.typography.bodyMedium,
    )
    Text(
        text = "可以试试更短的关键词，或者直接从完整菜单里选。",
        color = AppSidebarStyle.Default.resolveTokens().textFaint,
        style = MaterialTheme.typography.bodySmall,
    )
}

/** 侧栏容器：固定成商用暗色壳体，默认就能压住复杂工作台背景。 */
private fun Modifier.sidebarContainer(
    tokens: SidebarStyleTokens,
): Modifier {
    return fillMaxHeight()
        .background(tokens.containerBrush, tokens.containerShape)
        .border(1.dp, tokens.containerBorder, tokens.containerShape)
        .padding(1.dp)
        .background(tokens.containerBackground, tokens.containerShape)
}

/** 搜索框容器：保持低对比、低噪声，只给一个稳定的输入感。 */
private fun Modifier.searchFieldContainer(
    tokens: SidebarStyleTokens,
): Modifier {
    return height(48.dp)
        .background(tokens.searchBackground, tokens.searchShape)
        .border(1.dp, tokens.searchBorder, tokens.searchShape)
}

/** 树节点内边距：按层级递增左缩进，形成稳定的信息树。 */
private fun Modifier.sidebarItemPadding(
    level: Int,
    tokens: SidebarStyleTokens,
): Modifier {
    return padding(
        start = tokens.itemStartPadding + tokens.itemIndentStep * level,
        top = tokens.itemVerticalPadding,
        end = tokens.itemEndPadding,
        bottom = tokens.itemVerticalPadding,
    )
}

/** 节点底板：选中项更饱满，祖先项只给轻微提示，不制造视觉噪声。 */
private fun Modifier.sidebarItemFrame(
    selected: Boolean,
    descendantSelected: Boolean,
    tokens: SidebarStyleTokens,
): Modifier {
    val border = when {
        selected -> tokens.selectedBorder
        descendantSelected -> tokens.ancestorBorder
        else -> Color.Transparent
    }
    val baseModifier = when {
        selected -> background(tokens.selectedBackgroundBrush, tokens.itemShape)
        descendantSelected -> background(tokens.ancestorBackground, tokens.itemShape)
        else -> this
    }

    return baseModifier.border(1.dp, border, tokens.itemShape)
}

@Immutable
private data class SidebarStyleTokens(
    val contentPadding: PaddingValues,
    val emptyVerticalPadding: Dp,
    val containerShape: Shape,
    val searchShape: Shape,
    val itemShape: Shape,
    val containerBackground: Color,
    val containerBorder: Color,
    val containerBrush: Brush,
    val searchBackground: Color,
    val searchBorder: Color,
    val selectedBackgroundBrush: Brush,
    val selectedBorder: Color,
    val ancestorBackground: Color,
    val ancestorBorder: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val textFaint: Color,
    val itemStartPadding: Dp,
    val itemIndentStep: Dp,
    val itemVerticalPadding: Dp,
    val itemEndPadding: Dp,
)

private fun AppSidebarStyle.resolveTokens(): SidebarStyleTokens {
    return when (this) {
        AppSidebarStyle.Default -> SidebarStyleTokens(
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp),
            emptyVerticalPadding = 8.dp,
            containerShape = RoundedCornerShape(26.dp),
            searchShape = RoundedCornerShape(16.dp),
            itemShape = RoundedCornerShape(16.dp),
            containerBackground = Color(0xFF091221).copy(alpha = 0.94f),
            containerBorder = Color.White.copy(alpha = 0.08f),
            containerBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0E1A30).copy(alpha = 0.94f),
                    Color(0xFF091221).copy(alpha = 0.98f),
                ),
            ),
            searchBackground = Color.White.copy(alpha = 0.05f),
            searchBorder = Color.White.copy(alpha = 0.06f),
            selectedBackgroundBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF336DFF).copy(alpha = 0.34f),
                    Color(0xFF2448A8).copy(alpha = 0.26f),
                ),
            ),
            selectedBorder = Color(0xFF9BC1FF).copy(alpha = 0.26f),
            ancestorBackground = Color.White.copy(alpha = 0.05f),
            ancestorBorder = Color.White.copy(alpha = 0.05f),
            textPrimary = Color.White.copy(alpha = 0.96f),
            textMuted = Color.White.copy(alpha = 0.68f),
            textFaint = Color.White.copy(alpha = 0.46f),
            itemStartPadding = 12.dp,
            itemIndentStep = 18.dp,
            itemVerticalPadding = 12.dp,
            itemEndPadding = 12.dp,
        )

        AppSidebarStyle.FlushWorkbench -> SidebarStyleTokens(
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            emptyVerticalPadding = 0.dp,
            containerShape = RoundedCornerShape(0.dp),
            searchShape = RoundedCornerShape(12.dp),
            itemShape = RoundedCornerShape(12.dp),
            containerBackground = Color(0xFF081220).copy(alpha = 0.98f),
            containerBorder = Color.White.copy(alpha = 0.04f),
            containerBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF102136).copy(alpha = 0.98f),
                    Color(0xFF081220).copy(alpha = 1f),
                ),
            ),
            searchBackground = Color(0xFF112136).copy(alpha = 0.92f),
            searchBorder = Color.White.copy(alpha = 0.05f),
            selectedBackgroundBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF3A7BFF).copy(alpha = 0.42f),
                    Color(0xFF2749A8).copy(alpha = 0.34f),
                ),
            ),
            selectedBorder = Color(0xFFA9C8FF).copy(alpha = 0.22f),
            ancestorBackground = Color.White.copy(alpha = 0.04f),
            ancestorBorder = Color.Transparent,
            textPrimary = Color.White.copy(alpha = 0.97f),
            textMuted = Color.White.copy(alpha = 0.72f),
            textFaint = Color.White.copy(alpha = 0.48f),
            itemStartPadding = 10.dp,
            itemIndentStep = 16.dp,
            itemVerticalPadding = 10.dp,
            itemEndPadding = 10.dp,
        )
    }
}
