package com.addzero.component.table.clean

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.addzero.assist.api
import com.addzero.component.table.model.AddCleanColumn
import com.addzero.component.table.model.BaseCrudApi
import com.addzero.component.table.viewmodel.StatePagination
import com.addzero.entity.low_table.EnumSortDirection
import com.addzero.entity.low_table.StateSearch
import com.addzero.entity.low_table.StateSort

class AddCleanTableViewModel<T>(
    val data: List<T>,
    val columns: List<AddCleanColumn<T>>,
    val getIdFun: (T) -> Any,
    val baseCrudApi: BaseCrudApi<T>?=null,
) : ViewModel() {
    var buttonSlot: @Composable () -> Unit = {}
    //    -------------------------------------------选择---------------------------------------
    // 是否启用编辑模式（多选模式）
    var enableEditMode by mutableStateOf(false)

//    val taskList: StateFlow<List<T>>
//   field= MutableStateFlow(emptyList<T>())


    // 已选中的项目ID集合
    var _selectedItemIds by mutableStateOf(emptySet<Any>())
    // 当前选中的单个项目
    var _currentSelectItem by mutableStateOf(null as T?)

    var keyword by mutableStateOf("")


//    -------------------------------------------ui状态---------------------------------------

    var showForm by mutableStateOf(false)
    var showImportDialog by mutableStateOf(false)
    var showExportDropDown by mutableStateOf(false)
    var showPagination by mutableStateOf(true)
    var showAdvancedSearch by mutableStateOf(false)
    var showFieldAdvSearch by mutableStateOf(false)


    //    -------------------------------------------主体---------------------------------------
    var _data by mutableStateOf(data)
    val currentPageIds=_data.map { getIdFun(it) }
    var _columns by mutableStateOf(columns)

    var _currentClickColumn by mutableStateOf(null as com.addzero.component.table.model.AddCleanColumn<T>?)
    var _filterStateMap by mutableStateOf(mapOf<String, StateSearch>())

    // 获取过滤条件集合
    val filterState = _filterStateMap.values.toSet()

    val currentColumnKey = _currentClickColumn?.columnMetadata?.columnName ?: ""
    val currentColumnLabel = _currentClickColumn?.columnMetadata?.comment
    val currentColumnKmpType = _currentClickColumn?.columnMetadata?.kmpType

    var _currentStateSearch by mutableStateOf(StateSearch(columnKey = currentColumnKey))


    var _sortState by mutableStateOf(
        mutableSetOf(
            StateSort(
                "createTime", EnumSortDirection.DESC
            )
        )
    )
    val visibleColumns = _columns

    // 分页状态
    var _pageState by mutableStateOf(StatePagination())
    ////////////////////////////////////////增删改查事件

    fun onSearch() {
        baseCrudApi?:return
        api {
            val queryPage = baseCrudApi.queryPage<T>(
                _sortState,
                filterState.toMutableSet(),
                _pageState.currentPage,
                _pageState.pageSize
            )
            _data = queryPage.rows
            _pageState = _pageState.copy(totalItems = queryPage.totalRowCount.toInt())
        }
    }

    fun batchDelete() {
        baseCrudApi?:return
        api {
            val batchDelete = baseCrudApi.batchDelete(_selectedItemIds)
            if (batchDelete) {
                _root_ide_package_.com.addzero.component.toast.ToastManager.success("删除成功")
            } else {
                _root_ide_package_.com.addzero.component.toast.ToastManager.error("删除失败")
            }
            _currentSelectItem = _data.first()
        }
    }

    fun batchExport() {
        baseCrudApi?:return
        api {
            baseCrudApi.batchExport(
                _sortState,
                filterState.toMutableSet(),
                _selectedItemIds
            )
        }
    }

    //////////////////////////////////////////表格头部控件事件

    fun onSaveClick() {
        showForm = true
    }

    fun onImportClick() {
        showImportDialog = true
    }

    fun onExportClick() {
        showExportDropDown = true
    }


    fun queryPage() {
        baseCrudApi ?: return
    }

    //////////////////////////////////////////表格内容区事件
    fun onRowClick() {
    }

    fun onEditClick() {
        _currentSelectItem = null
        showForm = true
    }

    fun deleteRowData(id: Any) {
    }
}
