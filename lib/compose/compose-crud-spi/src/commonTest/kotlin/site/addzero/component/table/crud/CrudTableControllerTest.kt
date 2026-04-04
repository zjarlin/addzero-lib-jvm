package site.addzero.component.table.crud

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import site.addzero.component.table.original.entity.StatePagination
import site.addzero.entity.PageResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CrudTableControllerTest {
    @Test
    fun latestQueryWinsWhenPreviousRequestReturnsLater() = runTest {
        val dataSource = FakeCrudTableDataSource(
            queryHandler = { query ->
                if (query.keyword == "first") {
                    delay(100)
                }
                pageOf(
                    rows = listOf(RowItem(query.keyword.ifBlank { "empty" })),
                    currentPage = query.pagination.currentPage,
                    pageSize = query.pagination.pageSize,
                    total = 1,
                )
            },
        )
        val state = rememberlessCrudTableState<String>()
        val controller = CrudTableController<RowItem, String>()

        controller.bind(
            scope = this,
            state = state,
            dataSource = dataSource,
            rowIdOf = RowItem::id,
            keepSelectionAcrossRefresh = false,
            resolveQueryErrorMessage = { it.message ?: "查询失败" },
            resolveDeleteErrorMessage = { it.message ?: "删除失败" },
            onPageLoaded = {},
            onDeleteSuccess = { _, _ -> },
        )

        controller.search(CrudTableQuery(keyword = "first"))
        controller.search(CrudTableQuery(keyword = "second"))
        advanceUntilIdle()

        assertEquals(listOf(RowItem("second")), controller.rows)
        assertEquals("second", controller.currentQuery.keyword)
    }

    @Test
    fun deleteSelectedRemovesSelectionAndRefreshesRows() = runTest {
        val backingRows = mutableListOf(RowItem("a"), RowItem("b"))
        val dataSource = FakeCrudTableDataSource(
            queryHandler = { query ->
                pageOf(
                    rows = backingRows.toList(),
                    currentPage = query.pagination.currentPage,
                    pageSize = query.pagination.pageSize,
                    total = backingRows.size,
                )
            },
            deleteHandler = { ids ->
                backingRows.removeAll { it.id in ids }
                ids.size
            },
        )
        val state = rememberlessCrudTableState<String>()
        val controller = CrudTableController<RowItem, String>()

        controller.bind(
            scope = this,
            state = state,
            dataSource = dataSource,
            rowIdOf = RowItem::id,
            keepSelectionAcrossRefresh = false,
            resolveQueryErrorMessage = { it.message ?: "查询失败" },
            resolveDeleteErrorMessage = { it.message ?: "删除失败" },
            onPageLoaded = {},
            onDeleteSuccess = { _, _ -> },
        )

        controller.refresh()
        advanceUntilIdle()
        state.setEditMode(true)
        state.updateSelection("a", checked = true)

        controller.deleteSelected()
        advanceUntilIdle()

        assertEquals(listOf(RowItem("b")), controller.rows)
        assertTrue(state.selectedRowIds.isEmpty())
    }

    @Test
    fun refreshRetainsCurrentPageResultMetadata() = runTest {
        val dataSource = FakeCrudTableDataSource(
            queryHandler = { query ->
                pageOf(
                    rows = listOf(RowItem("p${query.pagination.currentPage}")),
                    currentPage = query.pagination.currentPage,
                    pageSize = query.pagination.pageSize,
                    total = 48,
                )
            },
        )
        val state = rememberlessCrudTableState<String>()
        val controller = CrudTableController<RowItem, String>()

        controller.bind(
            scope = this,
            state = state,
            dataSource = dataSource,
            rowIdOf = RowItem::id,
            keepSelectionAcrossRefresh = false,
            resolveQueryErrorMessage = { it.message ?: "查询失败" },
            resolveDeleteErrorMessage = { it.message ?: "删除失败" },
            onPageLoaded = {},
            onDeleteSuccess = { _, _ -> },
        )

        controller.updatePagination(
            StatePagination(
                currentPage = 3,
                pageSize = 20,
                totalItems = 0,
            ),
            submit = true,
        )
        advanceUntilIdle()

        assertEquals(3, controller.state.pagination.currentPage)
        assertEquals(20, controller.state.pagination.pageSize)
        assertEquals(48, controller.state.pagination.totalItems)
        assertFalse(controller.loading)
        assertFalse(controller.refreshing)
    }
}

private data class RowItem(
    val id: String,
)

private class FakeCrudTableDataSource(
    private val queryHandler: suspend (CrudTableQuery) -> PageResult<RowItem>,
    private val deleteHandler: suspend (Set<String>) -> Int = { 0 },
) : CrudTableDataSource<RowItem, String> {
    override val supportsDelete = true

    override suspend fun query(query: CrudTableQuery): PageResult<RowItem> {
        return queryHandler(query)
    }

    override suspend fun deleteByIds(ids: Set<String>): Int {
        return deleteHandler(ids)
    }
}

private fun pageOf(
    rows: List<RowItem>,
    currentPage: Int,
    pageSize: Int,
    total: Int,
): PageResult<RowItem> {
    val totalPages = if (total == 0) {
        0
    } else {
        (total + pageSize - 1) / pageSize
    }
    return PageResult(
        rows = rows,
        totalRowCount = total.toLong(),
        totalPageCount = totalPages,
        pageIndex = currentPage,
        pageSize = pageSize,
        isFirst = currentPage <= 1,
        isLast = currentPage >= totalPages,
    )
}

private fun <ID : Any> rememberlessCrudTableState(): CrudTableState<ID> {
    return CrudTableState(
        keyword = "",
        pagination = StatePagination(),
        sorts = emptySet(),
        filters = emptySet(),
        editModeEnabled = false,
        selectedRowIds = emptySet(),
    )
}
