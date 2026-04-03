package site.addzero.component.tree

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import site.addzero.component.button.AddIconButton
import site.addzero.component.tree.selection.SelectionState
import site.addzero.compose.applecorner.appleRounded

@Composable
fun <T> AddTree(
    viewModel: TreeViewModel<T>,
    modifier: Modifier = Modifier,
    compactMode: Boolean = false,
    selectableLabel: Boolean = false,
    metrics: AddTreeMetrics = AddTreeDefaults.AppleRoundedMetrics,
    colors: AddTreeColors? = null,
    nodeBadge: @Composable (T) -> Unit = {},
    nodeTrailingContent: @Composable RowScope.(T) -> Unit = {},
    content: @Composable TreeScope<T>.() -> Unit = {},
) {
    val resolvedColors = colors ?: AddTreeDefaults.appleRoundedColors()
    val treeScope = remember(viewModel) { TreeScopeImpl(viewModel) }

    Column(modifier = modifier) {
        treeScope.content()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(metrics.contentPadding),
            verticalArrangement = Arrangement.spacedBy(metrics.rowSpacing),
        ) {
            viewModel.filteredItems.forEach { item ->
                TreeNodeRenderer(
                    node = item,
                    viewModel = viewModel,
                    level = 0,
                    compactMode = compactMode,
                    selectableLabel = selectableLabel,
                    metrics = metrics,
                    colors = resolvedColors,
                    nodeBadge = nodeBadge,
                    nodeTrailingContent = nodeTrailingContent,
                )
            }
        }
    }
}

@Composable
private fun <T> TreeNodeRenderer(
    node: T,
    viewModel: TreeViewModel<T>,
    level: Int,
    compactMode: Boolean,
    selectableLabel: Boolean,
    metrics: AddTreeMetrics,
    colors: AddTreeColors,
    nodeBadge: @Composable (T) -> Unit,
    nodeTrailingContent: @Composable RowScope.(T) -> Unit,
) {
    val nodeId = viewModel.getId(node)
    val isExpanded = viewModel.isExpanded(nodeId)
    val isSelected = viewModel.isSelected(nodeId)
    val children = viewModel.getChildrenCached(node)
    val hasChildren = children.isNotEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(metrics.rowSpacing)) {
        TreeNodeContent(
            node = node,
            viewModel = viewModel,
            level = level,
            isExpanded = isExpanded,
            isSelected = isSelected,
            hasChildren = hasChildren,
            compactMode = compactMode,
            selectableLabel = selectableLabel,
            metrics = metrics,
            colors = colors,
            nodeBadge = nodeBadge,
            nodeTrailingContent = nodeTrailingContent,
            onToggleExpanded = { viewModel.toggleExpanded(nodeId) },
            onClick = { viewModel.clickNode(node) },
        )

        if (hasChildren && isExpanded) {
            children.forEach { child ->
                TreeNodeRenderer(
                    node = child,
                    viewModel = viewModel,
                    level = level + 1,
                    compactMode = compactMode,
                    selectableLabel = selectableLabel,
                    metrics = metrics,
                    colors = colors,
                    nodeBadge = nodeBadge,
                    nodeTrailingContent = nodeTrailingContent,
                )
            }
        }
    }
}

@Composable
private fun <T> TreeNodeContent(
    node: T,
    viewModel: TreeViewModel<T>,
    level: Int,
    isExpanded: Boolean,
    isSelected: Boolean,
    hasChildren: Boolean,
    compactMode: Boolean,
    selectableLabel: Boolean,
    metrics: AddTreeMetrics,
    colors: AddTreeColors,
    nodeBadge: @Composable (T) -> Unit,
    nodeTrailingContent: @Composable RowScope.(T) -> Unit,
    onToggleExpanded: () -> Unit,
    onClick: () -> Unit,
) {
    val nodeId = viewModel.getId(node)
    val interactionSource = remember(nodeId) { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val rowModifier = Modifier
        .fillMaxWidth()
        .padding(
            start = metrics.sideInset + metrics.levelIndent * level.toFloat(),
            end = metrics.sideInset,
        )
        .let { current ->
            if (compactMode) {
                current
            } else {
                current.clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) {
                    if (hasChildren) {
                        onToggleExpanded()
                    }
                    onClick()
                }
            }
        }

    val rowContainerColor = when {
        isHovered -> colors.rowHoveredContainer
        isSelected -> colors.rowSelectedContainer
        else -> colors.rowContainer
    }
    val contentColor = when {
        isHovered -> colors.contentHovered
        isSelected -> colors.contentSelected
        else -> colors.content
    }
    val secondaryContentColor = if (isHovered) {
        colors.secondaryContentHovered
    } else {
        colors.secondaryContent
    }

    Box(
        modifier = rowModifier
            .appleRounded(
                shape = metrics.rowShape,
                containerColor = rowContainerColor,
                border = if (isSelected) {
                    BorderStroke(metrics.selectedBorderWidth, colors.rowSelectedBorder)
                } else {
                    null
                },
            ),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isSelected && !compactMode) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = metrics.selectedIndicatorSpacing)
                        .width(metrics.selectedIndicatorWidth)
                        .height(metrics.selectedIndicatorHeight)
                        .appleRounded(
                            shape = metrics.badgeShape,
                            containerColor = colors.rowSelectedIndicator,
                        ),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = metrics.rowMinHeight)
                    .padding(
                        horizontal = metrics.rowHorizontalPadding,
                        vertical = metrics.rowVerticalPadding,
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (compactMode) {
                    Arrangement.Center
                } else {
                    Arrangement.spacedBy(metrics.contentSpacing)
                },
            ) {
                if (viewModel.multiSelectMode && !compactMode) {
                    val selectionState = viewModel.getNodeSelectionState(nodeId)
                    TriStateCheckbox(
                        state = when (selectionState) {
                            SelectionState.SELECTED -> ToggleableState.On
                            SelectionState.INDETERMINATE -> ToggleableState.Indeterminate
                            SelectionState.UNSELECTED -> ToggleableState.Off
                        },
                        onClick = { viewModel.toggleItemSelection(nodeId) },
                    )
                }

                if (!compactMode) {
                    Box(
                        modifier = Modifier.width(metrics.toggleSlotWidth),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (hasChildren) {
                            Icon(
                                imageVector = if (isExpanded) {
                                    Icons.Default.KeyboardArrowDown
                                } else {
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight
                                },
                                contentDescription = if (isExpanded) "折叠" else "展开",
                                modifier = Modifier.size(metrics.expandIconSize),
                                tint = secondaryContentColor,
                            )
                        }
                    }
                }

                val icon = viewModel.getIconCached(node)
                if (icon != null) {
                    if (compactMode) {
                        AddIconButton(
                            text = viewModel.getLabelCached(node),
                            imageVector = icon,
                            modifier = Modifier.size(32.dp),
                            tint = resolveNodeIconTint(
                                node = node,
                                viewModel = viewModel,
                                hasChildren = hasChildren,
                                isHovered = isHovered,
                                colors = colors,
                            ),
                        ) {
                            if (hasChildren) {
                                onToggleExpanded()
                            }
                            onClick()
                        }
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(metrics.iconSize),
                            tint = resolveNodeIconTint(
                                node = node,
                                viewModel = viewModel,
                                hasChildren = hasChildren,
                                isHovered = isHovered,
                                colors = colors,
                            ),
                        )
                    }
                }

                if (!compactMode) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        if (selectableLabel) {
                            SelectionContainer(
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(
                                    text = viewModel.getLabelCached(node),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isSelected || hasChildren) FontWeight.SemiBold else FontWeight.Medium,
                                    color = contentColor,
                                )
                            }
                        } else {
                            Text(
                                text = viewModel.getLabelCached(node),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected || hasChildren) FontWeight.SemiBold else FontWeight.Medium,
                                color = contentColor,
                            )
                        }
                        nodeBadge(node)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(metrics.contentSpacing),
                        content = {
                            nodeTrailingContent(node)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> resolveNodeIconTint(
    node: T,
    viewModel: TreeViewModel<T>,
    hasChildren: Boolean,
    isHovered: Boolean,
    colors: AddTreeColors,
): Color {
    if (isHovered) {
        return colors.contentHovered
    }
    val configuredType = viewModel.getNodeType(node)
    val nodeType = if (configuredType.isBlank()) {
        getDefaultNodeType(
            node = node,
            getLabel = viewModel.getLabel,
            getId = viewModel.getId,
            getChildren = viewModel.getChildren,
        )
    } else {
        NodeType.findByKeyword(configuredType)
    }
    return if (hasChildren) {
        getNodeTypeColor(nodeType)
    } else {
        getNodeTypeColor(nodeType).copy(alpha = 0.82f)
    }
}

@Composable
fun <T> AddTree(
    items: List<T>,
    getId: (T) -> Any,
    getLabel: (T) -> String,
    getChildren: (T) -> List<T> = { emptyList() },
    modifier: Modifier = Modifier,
    compactMode: Boolean = false,
    selectableLabel: Boolean = false,
    getNodeType: (T) -> String = { "" },
    getIcon: @Composable (T) -> ImageVector? = { node ->
        NodeType.guessIcon(
            label = getLabel(node),
            hasChildren = getChildren(node).isNotEmpty(),
        )
    },
    initiallyExpandedIds: Set<Any> = emptySet(),
    onNodeClick: (T) -> Unit = {},
    onNodeContextMenu: (T) -> Unit = {},
    onSelectionChange: (List<T>) -> Unit = {},
    metrics: AddTreeMetrics = AddTreeDefaults.AppleRoundedMetrics,
    colors: AddTreeColors? = null,
    nodeBadge: @Composable (T) -> Unit = {},
    nodeTrailingContent: @Composable RowScope.(T) -> Unit = {},
    content: @Composable TreeScope<T>.() -> Unit = {},
) {
    val viewModel = rememberTreeViewModel<T>()

    LaunchedEffect(items, getId, getLabel, getChildren) {
        viewModel.configure(
            getId = getId,
            getLabel = getLabel,
            getChildren = getChildren,
            getNodeType = getNodeType,
            getIcon = getIcon,
        )
        viewModel.onNodeClick = onNodeClick
        viewModel.onNodeContextMenu = onNodeContextMenu
        viewModel.onSelectionChange = onSelectionChange
        viewModel.setItems(items, initiallyExpandedIds)
    }

    AddTree(
        viewModel = viewModel,
        modifier = modifier,
        compactMode = compactMode,
        selectableLabel = selectableLabel,
        metrics = metrics,
        colors = colors,
        nodeBadge = nodeBadge,
        nodeTrailingContent = nodeTrailingContent,
        content = content,
    )
}
