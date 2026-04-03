package site.addzero.component.table.biz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.addzero.component.button.AddEditDeleteButton
import site.addzero.component.search_bar.AddSearchBar
import site.addzero.component.table.crud.CrudTableController
import site.addzero.component.table.crud.CrudTableQuery
import site.addzero.component.table.crud.CrudTableState
import site.addzero.component.table.crud.rememberCrudTableState
import site.addzero.component.table.original.TableOriginal
import site.addzero.component.table.original.entity.ColumnConfig
import site.addzero.component.table.original.entity.StatePagination
import site.addzero.component.table.original.entity.TableLayoutConfig
import site.addzero.entity.PageResult
import site.addzero.entity.low_table.EnumSortDirection

/**
 * 使用 renderless controller 的成品业务表格入口。
 */
@Composable
fun <T, C, ID : Any> AddTable(
    controller: CrudTableController<T, ID>,
    columns: List<C>,
    getColumnKey: (C) -> String,
    getRowId: (T) -> ID,
    columnConfigs: List<ColumnConfig> = emptyList(),
    layoutConfig: TableLayoutConfig = TableLayoutConfig(),
    getColumnLabel: (@Composable (C) -> Unit)? = null,
    topSlot: (@Composable () -> Unit)? = null,
    bottomSlot: (@Composable () -> Unit)? = null,
    emptyContentSlot: (@Composable () -> Unit)? = null,
    getCellContent: (@Composable (item: T, column: C) -> Unit)? = null,
    rowLeftSlot: (@Composable (item: T, index: Int) -> Unit)? = null,
    rowActionSlot: (@Composable (item: T) -> Unit)? = null,
    modifier: Modifier = Modifier,
    columnRightSlot: @Composable ((C) -> Unit)? = null,
    buttonSlot: @Composable () -> Unit = {},
    onCreateClick: (() -> Unit)? = null,
    onImportClick: (() -> Unit)? = null,
    onExportClick: ((CrudTableQuery) -> Unit)? = null,
    onBatchDelete: ((Set<ID>) -> Unit)? = null,
    onBatchExport: ((Set<ID>) -> Unit)? = null,
    onEditClick: ((ID) -> Unit)? = null,
    onDeleteClick: ((ID) -> Unit)? = null,
) {
    val resolvedBatchDelete = onBatchDelete ?: if (controller.supportsDelete) {
        { ids: Set<ID> -> controller.deleteByIds(ids) }
    } else {
        null
    }
    val resolvedDeleteClick = onDeleteClick ?: if (controller.supportsDelete) {
        { id: ID -> controller.deleteRow(id) }
    } else {
        null
    }

    AddTable(
        data = controller.rows,
        columns = columns,
        getColumnKey = getColumnKey,
        getRowId = getRowId,
        state = controller.state,
        pageResult = controller.pageResult,
        columnConfigs = columnConfigs,
        layoutConfig = layoutConfig,
        getColumnLabel = getColumnLabel,
        topSlot = topSlot,
        bottomSlot = bottomSlot,
        emptyContentSlot = emptyContentSlot,
        getCellContent = getCellContent,
        rowLeftSlot = rowLeftSlot,
        rowActionSlot = rowActionSlot,
        modifier = modifier,
        columnRightSlot = columnRightSlot,
        buttonSlot = buttonSlot,
        onSearch = controller::search,
        onCreateClick = onCreateClick,
        onImportClick = onImportClick,
        onExportClick = onExportClick,
        onBatchDelete = resolvedBatchDelete,
        onBatchExport = onBatchExport,
        onEditClick = onEditClick,
        onDeleteClick = resolvedDeleteClick,
    )
}

/**
 * 成品业务表格入口。
 *
 * 这一层只负责把查询状态与交互动作映射到视觉组件，
 * 数据加载、副作用和并发控制由外部 state/controller 负责。
 */
@Composable
fun <T, C, ID : Any> AddTable(
    data: List<T>,
    columns: List<C>,
    getColumnKey: (C) -> String,
    getRowId: (T) -> ID,
    state: CrudTableState<ID> = rememberCrudTableState(),
    pageResult: PageResult<T>? = null,
    columnConfigs: List<ColumnConfig> = emptyList(),
    layoutConfig: TableLayoutConfig = TableLayoutConfig(),
    getColumnLabel: (@Composable (C) -> Unit)? = null,
    topSlot: (@Composable () -> Unit)? = null,
    bottomSlot: (@Composable () -> Unit)? = null,
    emptyContentSlot: (@Composable () -> Unit)? = null,
    getCellContent: (@Composable (item: T, column: C) -> Unit)? = null,
    rowLeftSlot: (@Composable (item: T, index: Int) -> Unit)? = null,
    rowActionSlot: (@Composable (item: T) -> Unit)? = null,
    modifier: Modifier = Modifier,
    columnRightSlot: @Composable ((C) -> Unit)? = null,
    buttonSlot: @Composable () -> Unit = {},
    onSearch: (CrudTableQuery) -> Unit,
    onCreateClick: (() -> Unit)? = null,
    onImportClick: (() -> Unit)? = null,
    onExportClick: ((CrudTableQuery) -> Unit)? = null,
    onBatchDelete: ((Set<ID>) -> Unit)? = null,
    onBatchExport: ((Set<ID>) -> Unit)? = null,
    onEditClick: ((ID) -> Unit)? = null,
    onDeleteClick: ((ID) -> Unit)? = null,
) {
    val chromeState = rememberAddTableChromeState<C>()
    val currentColumnKey by remember(chromeState.currentColumn, chromeState.editingSearch, getColumnKey) {
        derivedStateOf {
            chromeState.currentColumn?.let(getColumnKey).orEmpty().ifBlank {
                chromeState.editingSearch.columnKey
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

    LaunchedEffect(pageResult) {
        pageResult?.let(state::applyPageResult)
    }

    fun requestSearch(
        resetPage: Boolean = false,
        pagination: StatePagination = state.pagination,
    ) {
        val nextPagination = if (resetPage) {
            pagination.copy(currentPage = 1)
        } else {
            pagination
        }
        state.replacePagination(nextPagination)
        onSearch(state.currentQuery.copy(pagination = nextPagination))
    }

    val resolvedTopSlot = topSlot ?: {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (layoutConfig.showSearchBar) {
                AddSearchBar(
                    keyword = state.keyword,
                    onKeyWordChanged = state::updateKeyword,
                    onSearch = {
                        requestSearch(resetPage = true)
                    },
                    showRefreshButton = true,
                    modifier = Modifier,
                    leftSloat = {
                        RenderButtons(
                            editModeFlag = state.editModeEnabled,
                            onEditModeChange = state::toggleEditMode,
                            onCreateClick = onCreateClick,
                            onImportClick = onImportClick,
                            onExportClick = onExportClick?.let { export ->
                                {
                                    export(state.currentQuery)
                                }
                            },
                            buttonSlot = buttonSlot,
                        )
                    },
                )
            } else {
                RenderButtons(
                    editModeFlag = state.editModeEnabled,
                    onEditModeChange = state::toggleEditMode,
                    onCreateClick = onCreateClick,
                    onImportClick = onImportClick,
                    onExportClick = onExportClick?.let { export ->
                        {
                            export(state.currentQuery)
                        }
                    },
                    buttonSlot = buttonSlot,
                )
            }

            if (layoutConfig.showBatchActions && layoutConfig.showRowSelection) {
                RenderSelectContent(
                    editModeFlag = state.editModeEnabled,
                    selectedItemIds = state.selectedRowIds,
                    onClearSelection = state::clearSelection,
                    onBatchDelete = onBatchDelete?.let { action ->
                        {
                            action(state.selectedRowIds)
                        }
                    },
                    onBatchExport = onBatchExport?.let { action ->
                        {
                            action(state.selectedRowIds)
                        }
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
                requestSearch(
                    pagination = state.pagination.copy(
                        pageSize = nextPageSize,
                        currentPage = 1,
                    ),
                )
            },
            onGoFirstPage = {
                requestSearch(
                    pagination = state.pagination.copy(currentPage = 1),
                )
            },
            onPreviousPage = {
                if (state.pagination.hasPreviousPage) {
                    requestSearch(
                        pagination = state.pagination.copy(
                            currentPage = state.pagination.currentPage - 1,
                        ),
                    )
                }
            },
            onGoToPage = { nextPage ->
                if (nextPage in 1..state.pagination.totalPages) {
                    requestSearch(
                        pagination = state.pagination.copy(currentPage = nextPage),
                    )
                }
            },
            onNextPage = {
                if (state.pagination.hasNextPage) {
                    requestSearch(
                        pagination = state.pagination.copy(
                            currentPage = state.pagination.currentPage + 1,
                        ),
                    )
                }
            },
            onGoLastPage = {
                requestSearch(
                    pagination = state.pagination.copy(
                        currentPage = state.pagination.totalPages,
                    ),
                )
            },
        )
    }

    val resolvedRowLeftSlot = rowLeftSlot ?: if (layoutConfig.showRowSelection) {
        { item: T, _: Int ->
            val rowId = getRowId(item)
            RenderCheckbox(
                isSelected = state.selectedRowIds.contains(rowId),
                editModeFlag = state.editModeEnabled,
                slotWidthDp = layoutConfig.leftSlotWidthDp.dp,
                onSelectionChange = { checked ->
                    state.updateSelection(rowId = rowId, checked = checked)
                },
            )
        }
    } else {
        null
    }

    val resolvedRowActionSlot = rowActionSlot ?: if (
        layoutConfig.showDefaultRowActions &&
        (onEditClick != null || onDeleteClick != null)
    ) {
        { item: T ->
            val rowId = getRowId(item)
            AddEditDeleteButton(
                showDelete = onDeleteClick != null,
                showEdit = onEditClick != null,
                onEditClick = {
                    onEditClick?.invoke(rowId)
                },
                onDeleteClick = {
                    onDeleteClick?.invoke(rowId)
                },
            )
        }
    } else {
        null
    }

    val resolvedColumnRightSlot = columnRightSlot ?: { column: C ->
        val columnKey = getColumnKey(column)
        val sortDirection = state.sorts
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
                hasFilter = state.currentFilter(columnKey) != null,
                onClick = {
                    chromeState.openAdvancedSearch(
                        column = column,
                        columnKey = columnKey,
                        existingSearch = state.currentFilter(columnKey),
                    )
                },
            )
        }
    }

    TableOriginal(
        data = data,
        columns = columns,
        getColumnKey = getColumnKey,
        getRowId = getRowId,
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
            showFieldAdvSearchDrawer = chromeState.advancedSearchVisible,
            currentStateSearch = chromeState.editingSearch,
            currentColumnLabel = currentColumnLabel,
            currentColumnKmpType = currentColumnKmpType,
            onShowFieldAdvSearchDrawerChange = { visible ->
                if (visible) {
                    chromeState.advancedSearchVisible = true
                } else {
                    chromeState.closeAdvancedSearch()
                }
            },
            onCurrentStateSearchChange = { nextSearch ->
                chromeState.editingSearch = nextSearch.copy(
                    columnKey = currentColumnKey,
                )
            },
            onConfirm = {
                state.upsertFilter(
                    chromeState.editingSearch.copy(columnKey = currentColumnKey),
                )
                chromeState.closeAdvancedSearch()
                requestSearch(resetPage = true)
            },
            onClear = {
                state.removeFilter(currentColumnKey)
                chromeState.clearCurrentSearch(currentColumnKey)
                requestSearch(resetPage = true)
            },
        )
    }
}
