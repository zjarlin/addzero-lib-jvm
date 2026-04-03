package site.addzero.component.table.biz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.assist.AddFun.getIdExt
import site.addzero.component.button.AddEditDeleteButton
import site.addzero.component.search_bar.AddSearchBar
import site.addzero.component.table.original.TableOriginal
import site.addzero.component.table.original.entity.ColumnConfig
import site.addzero.component.table.original.entity.StatePagination
import site.addzero.component.table.original.entity.TableLayoutConfig
import site.addzero.entity.low_table.EnumSortDirection
import site.addzero.entity.low_table.StateSearch
import site.addzero.entity.low_table.StateSort

/**
 * 成品业务表格入口。
 *
 * 这一层负责把搜索、分页、排序、筛选、多选和默认行操作收口成一个稳定入口，
 * 调用方只需要提供数据、列定义和业务回调。
 */
@Composable
inline fun <reified T, reified C> AddTable(
    data: List<T>,
    columns: List<C>,
    noinline getColumnKey: (C) -> String,
    noinline getRowId: ((T) -> Any)? = null,
    columnConfigs: List<ColumnConfig> = emptyList(),
    layoutConfig: TableLayoutConfig = TableLayoutConfig(),
    noinline getColumnLabel: (@Composable (C) -> Unit)? = null,
    noinline topSlot: (@Composable () -> Unit)? = null,
    noinline bottomSlot: (@Composable () -> Unit)? = null,
    noinline emptyContentSlot: (@Composable () -> Unit)? = null,
    noinline getCellContent: (@Composable (item: T, column: C) -> Unit)? = null,
    noinline rowLeftSlot: (@Composable (item: T, index: Int) -> Unit)? = null,
    noinline rowActionSlot: (@Composable (item: T) -> Unit)? = null,
    modifier: Modifier = Modifier,
    noinline columnRightSlot: @Composable ((C) -> Unit)? = null,
    noinline buttonSlot: @Composable () -> Unit = {},
    noinline onSearch: (String, Set<StateSearch>, Set<StateSort>, StatePagination) -> Unit,
    noinline onSaveClick: () -> Unit,
    noinline onImportClick: () -> Unit,
    noinline onExportClick: (String, Set<StateSearch>, Set<StateSort>, StatePagination) -> Unit,
    noinline onBatchDelete: (Set<Any>) -> Unit,
    noinline onBatchExport: (Set<Any>) -> Unit,
    noinline onEditClick: (Any) -> Unit,
    noinline onDeleteClick: (Any) -> Unit,
) {
    val state = rememberAddTableState<C>()
    val resolvedGetRowId: (T) -> Any = getRowId ?: { item -> item.getIdExt }
    val currentColumnKey by remember(state.currentColumn, state.editingSearch, getColumnKey) {
        derivedStateOf {
            state.currentColumn?.let(getColumnKey).orEmpty().ifBlank {
                state.editingSearch.columnKey
            }
        }
    }
    val currentColumnConfig by remember(currentColumnKey, columnConfigs) {
        derivedStateOf {
            columnConfigs.find { config -> config.key == currentColumnKey }
        }
    }
    val currentColumnLabel by remember(currentColumnConfig) {
        derivedStateOf { currentColumnConfig?.comment }
    }
    val currentColumnKmpType by remember(currentColumnConfig) {
        derivedStateOf { currentColumnConfig?.kmpType }
    }

    fun requestSearch(pagination: StatePagination = state.pagination) {
        onSearch(
            state.keyword,
            state.filters,
            state.sortState,
            pagination,
        )
    }

    val resolvedTopSlot = topSlot ?: {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (layoutConfig.showSearchBar) {
                AddSearchBar(
                    keyword = state.keyword,
                    onKeyWordChanged = { nextKeyword ->
                        state.keyword = nextKeyword
                    },
                    onSearch = ::requestSearch,
                    showRefreshButton = true,
                    modifier = Modifier,
                    leftSloat = {
                        RenderButtons(
                            editModeFlag = state.editModeEnabled,
                            onEditModeChange = state::toggleEditMode,
                            onSaveClick = onSaveClick,
                            onImportClick = onImportClick,
                            onExportClick = {
                                onExportClick(
                                    state.keyword,
                                    state.filters,
                                    state.sortState,
                                    state.pagination,
                                )
                            },
                            buttonSlot = buttonSlot,
                        )
                    },
                )
            } else {
                RenderButtons(
                    editModeFlag = state.editModeEnabled,
                    onEditModeChange = state::toggleEditMode,
                    onSaveClick = onSaveClick,
                    onImportClick = onImportClick,
                    onExportClick = {
                        onExportClick(
                            state.keyword,
                            state.filters,
                            state.sortState,
                            state.pagination,
                        )
                    },
                    buttonSlot = buttonSlot,
                )
            }

            if (layoutConfig.showBatchActions && layoutConfig.showRowSelection) {
                RenderSelectContent(
                    editModeFlag = state.editModeEnabled,
                    selectedItemIds = state.selectedItemIds,
                    onClearSelection = state::clearSelection,
                    onBatchDelete = {
                        onBatchDelete(state.selectedItemIds)
                        state.clearSelection()
                    },
                    onBatchExport = {
                        onBatchExport(state.selectedItemIds)
                    },
                )
            }
        }
    }

    val resolvedBottomSlot = bottomSlot ?: {
        RenderPagination(
            showPagination = layoutConfig.showPagination,
            pageState = state.pagination,
            onPageSizeChange = { nextPageSize ->
                val nextPagination = state.pagination.copy(
                    pageSize = nextPageSize,
                    currentPage = 1,
                )
                state.pagination = nextPagination
                requestSearch(nextPagination)
            },
            onGoFirstPage = {
                val nextPagination = state.pagination.copy(currentPage = 1)
                state.pagination = nextPagination
                requestSearch(nextPagination)
            },
            onPreviousPage = {
                if (!state.pagination.hasPreviousPage) {
                    return@RenderPagination
                }
                val nextPagination = state.pagination.copy(
                    currentPage = state.pagination.currentPage - 1,
                )
                state.pagination = nextPagination
                requestSearch(nextPagination)
            },
            onGoToPage = { nextPage ->
                if (nextPage !in 1..state.pagination.totalPages) {
                    return@RenderPagination
                }
                val nextPagination = state.pagination.copy(currentPage = nextPage)
                state.pagination = nextPagination
                requestSearch(nextPagination)
            },
            onNextPage = {
                if (!state.pagination.hasNextPage) {
                    return@RenderPagination
                }
                val nextPagination = state.pagination.copy(
                    currentPage = state.pagination.currentPage + 1,
                )
                state.pagination = nextPagination
                requestSearch(nextPagination)
            },
            onGoLastPage = {
                val nextPagination = state.pagination.copy(
                    currentPage = state.pagination.totalPages,
                )
                state.pagination = nextPagination
                requestSearch(nextPagination)
            },
        )
    }

    val resolvedRowLeftSlot = rowLeftSlot ?: if (layoutConfig.showRowSelection) {
        { item: T, _: Int ->
            val itemId = resolvedGetRowId(item)
            RenderCheckbox(
                item = item,
                itemId = itemId,
                isSelected = state.selectedItemIds.contains(itemId),
                editModeFlag = state.editModeEnabled,
                slotWidthDp = layoutConfig.leftSlotWidthDp.dp,
                onSelectionChange = { checked ->
                    state.updateSelection(itemId = itemId, checked = checked)
                },
            )
        }
    } else {
        null
    }

    val resolvedRowActionSlot = rowActionSlot ?: if (layoutConfig.showDefaultRowActions) {
        { item: T ->
            AddEditDeleteButton(
                showDelete = true,
                showEdit = true,
                onEditClick = {
                    onEditClick(resolvedGetRowId(item))
                },
                onDeleteClick = {
                    onDeleteClick(resolvedGetRowId(item))
                },
            )
        }
    } else {
        null
    }

    val resolvedColumnRightSlot = columnRightSlot ?: { column: C ->
        val columnKey = getColumnKey(column)
        val sortDirection = state.sortState
            .find { sort -> sort.columnKey == columnKey }
            ?.direction
            ?: EnumSortDirection.NONE

        if (layoutConfig.enableSorting) {
            RenderSortButton(
                column = column,
                getColumnKey = getColumnKey,
                columnConfigs = columnConfigs,
                sortDirection = sortDirection,
                onClick = {
                    state.toggleSort(columnKey)
                    requestSearch()
                },
            )
        }

        if (layoutConfig.enableAdvancedSearch) {
            RenderFilterButton(
                column = column,
                getColumnKey = getColumnKey,
                columnConfigs = columnConfigs,
                hasFilter = state.filterStateMap.containsKey(columnKey),
                onClick = {
                    state.openAdvancedSearch(
                        column = column,
                        columnKey = columnKey,
                        existingSearch = state.filterStateMap[columnKey],
                    )
                },
            )
        }
    }

    TableOriginal(
        data = data,
        columns = columns,
        getColumnKey = getColumnKey,
        getRowId = resolvedGetRowId,
        columnConfigs = columnConfigs,
        layoutConfig = layoutConfig,
        getColumnLabel = getColumnLabel,
        topSlot = resolvedTopSlot,
        bottomSlot = resolvedBottomSlot,
        emptyContentSlot = emptyContentSlot,
        getCellContent = getCellContent,
        rowLeftSlot = resolvedRowLeftSlot,
        rowActionSlot = resolvedRowActionSlot,
        modifier = modifier,
        columnRightSlot = resolvedColumnRightSlot,
    )

    if (layoutConfig.enableAdvancedSearch) {
        RenderAdvSearchDrawer(
            showFieldAdvSearchDrawer = state.advancedSearchVisible,
            currentStateSearch = state.editingSearch,
            currentColumnLabel = currentColumnLabel,
            currentColumnKmpType = currentColumnKmpType,
            onShowFieldAdvSearchDrawerChange = { visible ->
                if (visible) {
                    state.advancedSearchVisible = true
                } else {
                    state.closeAdvancedSearch()
                }
            },
            onCurrentStateSearchChange = { nextSearch ->
                state.editingSearch = nextSearch.copy(
                    columnKey = currentColumnKey,
                )
            },
            onFilterStateMapChange = { nextFilterMap ->
                state.filterStateMap = nextFilterMap
                requestSearch()
            },
            getCurrentColumnKey = { currentColumnKey },
            filterStateMap = state.filterStateMap,
        )
    }
}
