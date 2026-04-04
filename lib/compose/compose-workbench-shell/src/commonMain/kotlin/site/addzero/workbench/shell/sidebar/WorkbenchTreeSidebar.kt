package site.addzero.workbench.shell.sidebar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import site.addzero.component.search_bar.AddSearchBar
import site.addzero.component.tree.AddTree
import site.addzero.component.tree.AddTreeColors
import site.addzero.component.tree.TreeViewModel
import site.addzero.component.tree.rememberTreeViewModel
import site.addzero.workbench.shell.metrics.WorkbenchMetrics
import site.addzero.workbench.shell.metrics.currentWorkbenchMetrics

@Composable
fun <T> WorkbenchTreeSidebar(
    items: List<T>,
    selectedId: Any?,
    onNodeClick: (T) -> Unit,
    modifier: Modifier = Modifier,
    searchEnabled: Boolean = true,
    searchPlaceholder: String = "搜索页面",
    metrics: WorkbenchMetrics = currentWorkbenchMetrics(),
    treeViewModel: TreeViewModel<T> = rememberTreeViewModel(),
    header: @Composable ColumnScope.() -> Unit = {},
    footer: @Composable ColumnScope.() -> Unit = {},
    getId: (T) -> Any,
    getLabel: (T) -> String,
    getChildren: (T) -> List<T>,
    getIcon: @Composable (T) -> ImageVector? = { null },
) {
    val currentOnNodeClick by rememberUpdatedState(onNodeClick)
    val treeMetrics = remember(metrics.treeMetrics) {
        metrics.treeMetrics.copy(
            rowHorizontalPadding = 10.dp,
            contentSpacing = 8.dp,
            toggleSlotWidth = 14.dp,
            selectedIndicatorSpacing = 8.dp,
        )
    }
    val treePanelColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)
    val treeColors = rememberWorkbenchTreeColors()

    LaunchedEffect(treeViewModel) {
        treeViewModel.configure(
            getId = getId,
            getLabel = getLabel,
            getChildren = getChildren,
            getIcon = getIcon,
        )
    }

    SideEffect {
        treeViewModel.onNodeClick = { node ->
            currentOnNodeClick(node)
        }
    }

    LaunchedEffect(treeViewModel, items) {
        treeViewModel.setItems(
            newItems = items,
            initiallyExpandedIds = items.allBranchIds(getId = getId, getChildren = getChildren),
        )
    }

    LaunchedEffect(treeViewModel, selectedId) {
        treeViewModel.selectNode(selectedId)
    }

    LaunchedEffect(treeViewModel, searchEnabled) {
        if (!searchEnabled && treeViewModel.searchQuery.isNotBlank()) {
            treeViewModel.updateSearchQuery("")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(metrics.sidebarOuterPadding),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(metrics.sidebarPanelRadius),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f)),
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(metrics.sidebarPanelInnerPadding),
                verticalArrangement = Arrangement.spacedBy(metrics.sidebarSectionGap),
            ) {
                header()

                if (searchEnabled) {
                    AddSearchBar(
                        keyword = treeViewModel.searchQuery,
                        onKeyWordChanged = { query ->
                            treeViewModel.updateSearchQuery(query)
                            if (query.isNotBlank()) {
                                treeViewModel.performSearch()
                            }
                        },
                        onSearch = treeViewModel::performSearch,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = searchPlaceholder,
                        fieldHeight = metrics.searchFieldHeight,
                        horizontalSpacing = metrics.searchFieldSpacing,
                        showRefreshButton = !metrics.searchFieldCompactRefreshHidden,
                    )
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(metrics.sidebarTreePanelRadius),
                    color = treePanelColor,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(metrics.sidebarTreePanelPadding),
                    ) {
                        AddTree(
                            viewModel = treeViewModel,
                            modifier = Modifier.fillMaxSize(),
                            metrics = treeMetrics,
                            colors = treeColors,
                            selectableLabel = true,
                        )
                    }
                }

                footer()
            }
        }
    }
}

@Composable
fun rememberWorkbenchTreeColors(): AddTreeColors {
    val colorScheme = MaterialTheme.colorScheme
    val darkTheme = colorScheme.background.luminance() < 0.5f
    return remember(colorScheme, darkTheme) {
        AddTreeColors(
            rowContainer = Color.Transparent,
            rowHoveredContainer = if (darkTheme) Color(0xFF111111) else colorScheme.surfaceVariant.copy(alpha = 0.65f),
            rowSelectedContainer = colorScheme.surfaceVariant.copy(alpha = 0.26f),
            rowSelectedBorder = colorScheme.outlineVariant.copy(alpha = 0.58f),
            rowSelectedIndicator = colorScheme.onSurface,
            content = colorScheme.onSurface,
            contentHovered = if (darkTheme) Color.White else colorScheme.onSurface,
            contentSelected = colorScheme.onSurface,
            secondaryContent = colorScheme.onSurfaceVariant,
            secondaryContentHovered = if (darkTheme) Color.White else colorScheme.onSurface,
            badgeContainer = colorScheme.surface.copy(alpha = 0.84f),
            badgeBorder = colorScheme.outlineVariant.copy(alpha = 0.56f),
            badgeContent = colorScheme.onSurfaceVariant,
        )
    }
}

private fun <T> List<T>.allBranchIds(
    getId: (T) -> Any,
    getChildren: (T) -> List<T>,
): Set<Any> {
    return buildSet {
        fun collect(nodes: List<T>) {
            nodes.forEach { node ->
                val children = getChildren(node)
                if (children.isNotEmpty()) {
                    add(getId(node))
                    collect(children)
                }
            }
        }
        collect(this@allBranchIds)
    }
}
