package site.addzero.component.table.biz

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.launch

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
    // 行左侧插槽（如复选框）
    noinline rowLeftSlot: (@Composable (item: T, index: Int) -> Unit)? = null,
    noinline rowActionSlot: (@Composable (item: T) -> Unit)? = null,
    modifier: Modifier = Modifier,
    noinline columnRightSlot: @Composable ((C) -> Unit)? = null,
    noinline buttonSlot: @Composable () -> Unit = {},
    // 新增的回调函数参数
    noinline onSearch: ((String, Set<StateSearch>, Set<StateSort>, StatePagination) -> Unit),
    noinline onSaveClick: (() -> Unit),
    noinline onImportClick: (() -> Unit),
    noinline onExportClick: ((String, Set<StateSearch>, Set<StateSort>, StatePagination) -> Unit),
    noinline onBatchDelete: ((Set<Any>) -> Unit),
    noinline onBatchExport: ((Set<Any>) -> Unit),
    noinline onEditClick: ((Any) -> Unit),
    noinline onDeleteClick: ((Any) -> Unit)
) {
    // 状态定义
    var keyword by remember { mutableStateOf("") }
    var editModeFlag by remember { mutableStateOf(false) }
    var selectedItemIds by remember { mutableStateOf(setOf<Any>()) }
    var showPagination by remember { mutableStateOf(true) }
    var pageState by remember { mutableStateOf(StatePagination()) }
    var sortState by remember { mutableStateOf(setOf<StateSort>()) }
    var showFieldAdvSearchDrawer by remember { mutableStateOf(false) }
    var filterStateMap by remember { mutableStateOf(mapOf<String, StateSearch>()) }
    var currentStateSearch by remember { mutableStateOf(StateSearch()) }
    var currentClickColumn by remember { mutableStateOf(null as C?) }
    val rememberCoroutineScope = rememberCoroutineScope()

    // 计算属性
    val currentPageIds by remember(data) {
        derivedStateOf { data.map { it.hashCode() } }
    }


    val filterState by remember {
        derivedStateOf { filterStateMap.values.toSet() }
    }

    val currentColumnKey by remember(currentClickColumn) {
        derivedStateOf {
            if (currentClickColumn == null) "" else getColumnKey(currentClickColumn!!).ifBlank {
                currentStateSearch.hashCode().toString()
            }
        }
    }

    val currentColumnConfig by remember(currentColumnKey, columnConfigs) {
        derivedStateOf { columnConfigs.find { it.key == currentColumnKey } }
    }

    val currentColumnLabel by remember(currentColumnConfig) {
        derivedStateOf { currentColumnConfig?.comment }
    }

    val currentColumnKmpType by remember(currentColumnConfig) {
        derivedStateOf { currentColumnConfig?.kmpType }
    }

    TableOriginal(
        data = data,
        columns = columns,
        getColumnKey = getColumnKey,
        getRowId = getRowId,
        columnConfigs = columnConfigs,
        layoutConfig = layoutConfig,
        getColumnLabel = getColumnLabel,
        topSlot = topSlot ?: {
            AddSearchBar(keyword = keyword, onKeyWordChanged = { keyword = it }, onSearch = {
                rememberCoroutineScope.launch {
                    onSearch(keyword, filterState, sortState, pageState)
                }
            }, leftSloat = {
                RenderButtons(
                    editModeFlag = editModeFlag,
                    onEditModeChange = { editModeFlag = !editModeFlag },
                    onSaveClick = {
                        onSaveClick()
                    },
                    onImportClick = {
                        rememberCoroutineScope.launch {
                            onImportClick()
                        }

                    },
                    onExportClick = {
                        rememberCoroutineScope.launch {
                            onExportClick(keyword, filterState, sortState, pageState)
                        }


                    },
                    buttonSlot = buttonSlot
                )
            })
            RenderSelectContent(
                editModeFlag = editModeFlag,
                selectedItemIds = selectedItemIds,
                onClearSelection = { selectedItemIds = emptySet() },
                onBatchDelete = {

                    rememberCoroutineScope.launch {

                        onBatchDelete(selectedItemIds)
                    }
                },
                onBatchExport = {

                    rememberCoroutineScope.launch {

                        onBatchExport(selectedItemIds)
                    }
                })
        },
        bottomSlot = bottomSlot ?: {
            RenderPagination(showPagination = showPagination, pageState = pageState, onPageSizeChange = {
                pageState = pageState.copy(pageSize = it, currentPage = 1)
            }, onGoFirstPage = {
                pageState = pageState.copy(currentPage = 1)
                rememberCoroutineScope.launch {
                    onSearch(keyword, filterState, sortState, pageState)
                }


            }, onPreviousPage = {
                if (pageState.hasPreviousPage) {
                    pageState = pageState.copy(currentPage = pageState.currentPage - 1)
                }
            }, onGoToPage = {
                if (it in 1..pageState.totalPages) {
                    pageState = pageState.copy(currentPage = it)
                }
            }, onNextPage = {
                if (pageState.hasNextPage) {
                    pageState = pageState.copy(currentPage = pageState.currentPage + 1)
                }
            }, onGoLastPage = {
                pageState = pageState.copy(currentPage = pageState.totalPages)
                rememberCoroutineScope.launch {

                    onSearch(keyword, filterState, sortState, pageState)
                }


            })
        },
        emptyContentSlot = emptyContentSlot,
        getCellContent = getCellContent,
        rowLeftSlot = rowLeftSlot ?: { item, index ->
            val itemId = getRowId?.invoke(item)?:item.getIdExt
            val isSelected = selectedItemIds.contains(itemId)
            RenderCheckbox(
                item = item,
                itemId = itemId,
                isSelected = isSelected,
                editModeFlag = editModeFlag,
                onSelectionChange = { checked ->
                    val pageIds = listOf(itemId)
                    selectedItemIds = if (checked) {
                        selectedItemIds + pageIds
                    } else {
                        selectedItemIds.filter { it !in pageIds }.toSet()
                    }
                })
        },
        rowActionSlot = rowActionSlot ?: { item ->
            AddEditDeleteButton(showDelete = true, showEdit = true, onEditClick = {
                rememberCoroutineScope.launch {
                    val any = getRowId?.invoke(item) ?: (item.getIdExt)
                    onEditClick(any)
                }
            }, onDeleteClick = {
                rememberCoroutineScope.launch {
                    val any = getRowId?.invoke(item) ?: (item.getIdExt)
                    onDeleteClick(any)
                }
            })
        },
        modifier = modifier,
        columnRightSlot = columnRightSlot ?: { column ->
            val columnKey = getColumnKey(column)
            val sortDirection = sortState.find { it.columnKey == columnKey }?.direction ?: EnumSortDirection.NONE

            RenderSortButton(
                column = column,
                getColumnKey = getColumnKey,
                columnConfigs = columnConfigs,
                sortDirection = sortDirection,
                onClick = {
                    // 查找当前是否已有该列的排序状态
                    val existingSort = sortState.find { it.columnKey == columnKey }

                    // 根据当前排序状态决定下一个状态
                    val newDirection = when (existingSort?.direction) {
                        EnumSortDirection.ASC -> EnumSortDirection.DESC
                        EnumSortDirection.DESC -> EnumSortDirection.NONE
                        else -> EnumSortDirection.ASC // null或NONE时设为ASC
                    }

                    // 创建新的排序状态
                    val newSortState = sortState.toMutableSet()

                    // 移除旧的排序状态
                    newSortState.removeAll { it.columnKey == columnKey }

                    // 只有非NONE状态才添加
                    if (newDirection != EnumSortDirection.NONE) {
                        newSortState.add(StateSort(columnKey, newDirection))
                    }

                    // 更新排序状态
                    sortState = newSortState
                })

            val hasFilter = filterStateMap.containsKey(columnKey)
            RenderFilterButton(
                column = column,
                getColumnKey = getColumnKey,
                columnConfigs = columnConfigs,
                hasFilter = hasFilter,
                onClick = {
                    currentClickColumn = column
                    showFieldAdvSearchDrawer = !showFieldAdvSearchDrawer
                })
        })

    // 右侧的高级搜索面板
    RenderAdvSearchDrawer(
        showFieldAdvSearchDrawer = showFieldAdvSearchDrawer,
        currentStateSearch = currentStateSearch,
        currentColumnLabel = currentColumnLabel,
        currentColumnKmpType = currentColumnKmpType,
        onShowFieldAdvSearchDrawerChange = { showFieldAdvSearchDrawer = it },
        onCurrentStateSearchChange = { currentStateSearch = it },
        onFilterStateMapChange = { filterStateMap = it },
        getCurrentColumnKey = { currentColumnKey },
        filterStateMap = filterStateMap
    )
}
