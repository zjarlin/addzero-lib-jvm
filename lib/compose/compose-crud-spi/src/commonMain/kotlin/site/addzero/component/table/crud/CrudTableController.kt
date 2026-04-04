package site.addzero.component.table.crud

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import site.addzero.component.table.original.entity.StatePagination
import site.addzero.entity.PageResult
import site.addzero.entity.low_table.StateSearch
import site.addzero.entity.low_table.StateSort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 渲染无关的表格控制器。
 *
 * 它对应 React 里常见的 `useServerTableController`：
 * UI 只绑定状态和事件，查询与删除副作用统一交给控制器调度。
 */
@Stable
class CrudTableController<T, ID : Any> internal constructor() {
    private lateinit var scope: CoroutineScope
    private lateinit var stateHolder: CrudTableState<ID>
    private lateinit var dataSource: CrudTableDataSource<T, ID>
    private var rowIdOf: (T) -> ID = { error("CrudTableController 尚未绑定 rowIdOf") }
    private var keepSelectionAcrossRefresh = false
    private var resolveQueryErrorMessage: (Throwable) -> String = { it.message ?: "加载表格失败" }
    private var resolveDeleteErrorMessage: (Throwable) -> String = { it.message ?: "删除数据失败" }
    private var onPageLoaded: (PageResult<T>) -> Unit = {}
    private var onDeleteSuccess: (Set<ID>, Int) -> Unit = { _, _ -> }

    private var queryRequestToken by mutableStateOf(0)

    var rows by mutableStateOf<List<T>>(emptyList())
        private set

    var pageResult by mutableStateOf<PageResult<T>?>(null)
        private set

    var loading by mutableStateOf(false)
        private set

    var refreshing by mutableStateOf(false)
        private set

    var deleting by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    val supportsDelete
        get() = ::dataSource.isInitialized && dataSource.supportsDelete

    val state: CrudTableState<ID>
        get() = stateHolder

    val currentQuery
        get() = stateHolder.currentQuery

    internal fun bind(
        scope: CoroutineScope,
        state: CrudTableState<ID>,
        dataSource: CrudTableDataSource<T, ID>,
        rowIdOf: (T) -> ID,
        keepSelectionAcrossRefresh: Boolean,
        resolveQueryErrorMessage: (Throwable) -> String,
        resolveDeleteErrorMessage: (Throwable) -> String,
        onPageLoaded: (PageResult<T>) -> Unit,
        onDeleteSuccess: (Set<ID>, Int) -> Unit,
    ) {
        this.scope = scope
        stateHolder = state
        this.dataSource = dataSource
        this.rowIdOf = rowIdOf
        this.keepSelectionAcrossRefresh = keepSelectionAcrossRefresh
        this.resolveQueryErrorMessage = resolveQueryErrorMessage
        this.resolveDeleteErrorMessage = resolveDeleteErrorMessage
        this.onPageLoaded = onPageLoaded
        this.onDeleteSuccess = onDeleteSuccess
    }

    fun refresh(resetPage: Boolean = false) {
        val nextQuery = if (resetPage) {
            currentQuery.resetToFirstPage()
        } else {
            currentQuery
        }
        search(nextQuery)
    }

    fun search(query: CrudTableQuery = currentQuery) {
        stateHolder.applyQuery(query)
        val requestToken = ++queryRequestToken
        val hasRenderedRows = rows.isNotEmpty()
        loading = !hasRenderedRows
        refreshing = hasRenderedRows
        errorMessage = null

        scope.launch {
            runCatching {
                dataSource.query(query)
            }.onSuccess { nextPage ->
                if (requestToken != queryRequestToken) {
                    return@launch
                }

                rows = nextPage.rows
                pageResult = nextPage
                stateHolder.applyPageResult(nextPage)
                if (!keepSelectionAcrossRefresh) {
                    stateHolder.retainSelection(nextPage.rows.map(rowIdOf).toSet())
                }
                loading = false
                refreshing = false
                onPageLoaded(nextPage)
            }.onFailure { error ->
                if (requestToken != queryRequestToken) {
                    return@launch
                }

                loading = false
                refreshing = false
                errorMessage = resolveQueryErrorMessage(error)
            }
        }
    }

    fun updateKeyword(
        keyword: String,
        submit: Boolean = false,
    ) {
        stateHolder.updateKeyword(keyword)
        if (submit) {
            refresh(resetPage = true)
        }
    }

    fun updateFilters(
        filters: Set<StateSearch>,
        submit: Boolean = false,
    ) {
        stateHolder.replaceFilters(filters)
        if (submit) {
            refresh(resetPage = true)
        }
    }

    fun updateSorts(
        sorts: Set<StateSort>,
        submit: Boolean = false,
    ) {
        stateHolder.replaceSorts(sorts)
        if (submit) {
            refresh()
        }
    }

    fun updatePagination(
        pagination: StatePagination,
        submit: Boolean = false,
    ) {
        stateHolder.replacePagination(pagination)
        if (submit) {
            refresh()
        }
    }

    fun toggleSort(columnKey: String) {
        stateHolder.toggleSort(columnKey)
    }

    fun deleteSelected(refresh: Boolean = true) {
        deleteByIds(stateHolder.selectedRowIds, refresh = refresh)
    }

    fun deleteRow(
        id: ID,
        refresh: Boolean = true,
    ) {
        deleteByIds(setOf(id), refresh = refresh)
    }

    fun deleteByIds(
        ids: Set<ID>,
        refresh: Boolean = true,
    ) {
        if (ids.isEmpty()) {
            return
        }

        val requestIds = ids.toSet()
        deleting = true
        errorMessage = null

        scope.launch {
            runCatching {
                dataSource.deleteByIds(requestIds)
            }.onSuccess { deletedCount ->
                deleting = false
                stateHolder.removeSelection(requestIds)
                onDeleteSuccess(requestIds, deletedCount)
                if (refresh) {
                    this@CrudTableController.refresh()
                }
            }.onFailure { error ->
                deleting = false
                errorMessage = resolveDeleteErrorMessage(error)
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }
}

@Composable
fun <T, ID : Any> rememberCrudTableController(
    dataSource: CrudTableDataSource<T, ID>,
    rowIdOf: (T) -> ID,
    state: CrudTableState<ID> = rememberCrudTableState(),
    autoLoad: Boolean = true,
    keepSelectionAcrossRefresh: Boolean = false,
    reloadKey: Any? = dataSource,
    resolveQueryErrorMessage: (Throwable) -> String = { it.message ?: "加载表格失败" },
    resolveDeleteErrorMessage: (Throwable) -> String = { it.message ?: "删除数据失败" },
    onPageLoaded: (PageResult<T>) -> Unit = {},
    onDeleteSuccess: (Set<ID>, Int) -> Unit = { _, _ -> },
): CrudTableController<T, ID> {
    val scope = rememberCoroutineScope()
    val controller = remember {
        CrudTableController<T, ID>()
    }

    controller.bind(
        scope = scope,
        state = state,
        dataSource = dataSource,
        rowIdOf = rowIdOf,
        keepSelectionAcrossRefresh = keepSelectionAcrossRefresh,
        resolveQueryErrorMessage = resolveQueryErrorMessage,
        resolveDeleteErrorMessage = resolveDeleteErrorMessage,
        onPageLoaded = onPageLoaded,
        onDeleteSuccess = onDeleteSuccess,
    )

    LaunchedEffect(
        controller,
        autoLoad,
        reloadKey,
    ) {
        if (autoLoad) {
            controller.refresh()
        }
    }

    return controller
}
