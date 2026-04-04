package site.addzero.appsidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import site.addzero.appsidebar.spi.AppSidebarStyleConfig

@Composable
fun <T> AppSidebar(
    title: String,
    items: List<T>,
    itemId: (T) -> String,
    label: (T) -> String,
    style: AppSidebarStyleConfig,
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
        modifier = modifier.sidebarContainer(style),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(style.contentPadding),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SidebarHeader(
                title = title,
                supportText = config.supportText,
                headerSlot = slots.header,
                style = style,
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
                    style = style,
                )
            }

            Box(
                modifier = Modifier.weight(1f),
            ) {
                if (visibleItems.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(vertical = style.emptyVerticalPadding),
                        verticalArrangement = Arrangement.Center,
                    ) emptyColumn@{
                        slots.empty.invoke(this@emptyColumn, state.keyword)
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
                                style = style,
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
    style: AppSidebarStyleConfig,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            color = style.textPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
        )
        if (supportText != null) {
            Text(
                text = supportText,
                color = style.textMuted,
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
    style: AppSidebarStyleConfig,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().searchFieldContainer(style),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = style.textPrimary,
        ),
        leadingIcon = {
            androidx.compose.material3.Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = style.textMuted,
            )
        },
        trailingIcon = {
            if (value.isNotBlank()) {
                Text(
                    text = "清空",
                    color = style.textMuted,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable(onClick = onClear),
                )
            }
        },
        placeholder = {
            Text(
                text = placeholder,
                color = style.textFaint,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = style.searchBackground,
            unfocusedContainerColor = style.searchBackground,
            disabledContainerColor = style.searchBackground,
            focusedTextColor = style.textPrimary,
            unfocusedTextColor = style.textPrimary,
            cursorColor = style.textPrimary,
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
    style: AppSidebarStyleConfig,
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
                style = style,
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
            modifier = Modifier.fillMaxWidth().sidebarItemPadding(level, style),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SidebarBranchToggle(
                isBranch = isBranch,
                expanded = expanded,
                style = style,
                onToggle = {
                    state.toggleExpanded(id)
                },
            )
            if (leadingSlot == null) {
                SidebarIcon(
                    icon = icon(item),
                    style = style,
                )
            } else {
                leadingSlot.invoke(this, item, selected, descendantSelected)
            }
            if (labelSlot == null) {
                DefaultSidebarLabel(
                    text = label(item),
                    selected = selected,
                    style = style,
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
                style = style,
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
    style: AppSidebarStyleConfig,
) {
    Text(
        text = text,
        color = style.textPrimary,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        modifier = Modifier.weight(1f),
    )
}

@Composable
private fun SidebarBranchToggle(
    isBranch: Boolean,
    expanded: Boolean,
    style: AppSidebarStyleConfig,
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
            tint = style.textMuted,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun SidebarIcon(
    icon: ImageVector?,
    style: AppSidebarStyleConfig,
) {
    if (icon == null) {
        Spacer(modifier = Modifier.width(18.dp))
        return
    }

    androidx.compose.material3.Icon(
        imageVector = icon,
        contentDescription = null,
        tint = style.textPrimary,
        modifier = Modifier.size(18.dp),
    )
}

@Composable
internal fun ColumnScope.DefaultSidebarEmpty(
    keyword: String,
) {
    Text(
        text = "没有找到 “$keyword” 对应的导航项。",
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium,
    )
    Text(
        text = "可以试试更短的关键词，或者直接从完整菜单里选。",
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.74f),
        style = MaterialTheme.typography.bodySmall,
    )
}

private fun Modifier.sidebarContainer(
    style: AppSidebarStyleConfig,
): Modifier {
    return fillMaxHeight()
        .background(style.containerBrush, style.containerShape)
        .border(1.dp, style.containerBorder, style.containerShape)
        .padding(1.dp)
        .background(style.containerBackground, style.containerShape)
}

private fun Modifier.searchFieldContainer(
    style: AppSidebarStyleConfig,
): Modifier {
    return height(48.dp)
        .background(style.searchBackground, style.searchShape)
        .border(1.dp, style.searchBorder, style.searchShape)
}

private fun Modifier.sidebarItemPadding(
    level: Int,
    style: AppSidebarStyleConfig,
): Modifier {
    return padding(
        start = style.itemStartPadding + style.itemIndentStep * level,
        top = style.itemVerticalPadding,
        end = style.itemEndPadding,
        bottom = style.itemVerticalPadding,
    )
}

private fun Modifier.sidebarItemFrame(
    selected: Boolean,
    descendantSelected: Boolean,
    style: AppSidebarStyleConfig,
): Modifier {
    val border = when {
        selected -> style.selectedBorder
        descendantSelected -> style.ancestorBorder
        else -> Color.Transparent
    }
    val baseModifier = when {
        selected -> background(style.selectedBackgroundBrush, style.itemShape)
        descendantSelected -> background(style.ancestorBackground, style.itemShape)
        else -> this
    }

    return baseModifier.border(1.dp, border, style.itemShape)
}
