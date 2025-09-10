package com.addzero.demo.table

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import com.addzero.annotation.Route
import com.addzero.assist.api
import com.addzero.component.table.biz.AddTable
import com.addzero.component.table.original.entity.ColumnConfig
import com.addzero.component.table.original.entity.StatePagination
import com.addzero.component.table.original.entity.TableLayoutConfig
import com.addzero.entity.PageResult
import com.addzero.entity.low_table.CommonTableDaTaInputDTO
import com.addzero.entity.low_table.StateSearch
import com.addzero.entity.low_table.StateSort
import com.addzero.generated.api.ApiProvider.sysDictApi
import com.addzero.generated.api.SysDictApi
import com.addzero.generated.isomorphic.JdbcColumnMetadataIso
import com.addzero.generated.isomorphic.SysDictIso

 class SysDictTableViewModel(val routeKey: String) : ViewModel() {
    var _data by mutableStateOf(emptyList<SysDictIso>())
    var _columns by mutableStateOf(emptyList<JdbcColumnMetadataIso>())
    var _tableConfig by mutableStateOf(TableLayoutConfig())
    var _columnConfigs by mutableStateOf(listOf<ColumnConfig>())
    fun loadTableConfig() {

//        sysDictApi.loadTableConfig(routeKey)
    }
    fun loadColumnConfig() {
    }

    fun loadData(
        keword: String="",
        filterState: Set<StateSearch> =emptySet() ,
        sortState: Set<StateSort> =emptySet(),
        pageState: StatePagination = StatePagination(1,10)
    ){
        api {
            val page = sysDictApi.page(
                CommonTableDaTaInputDTO(
                    pageNo = pageState.currentPage,
                    pageSize = pageState.pageSize,
                    keyword = keword,
                    stateSorts = sortState.toMutableSet(),
                    stateSearches = filterState.toMutableSet()
                )
            )
          _data= page.rows
        }
    }

    init {
        loadData()
        loadTableConfig()
        loadColumnConfig()
    }
}

@Route
@Composable
fun TableBigDataTest3() {
    val (bigDataSet, bigColumns) = mockData()

    val vm = SysDictTableViewModel("/sysDict")

    AddTable(
        data = vm._data,
        columns = vm._columns,
        getColumnKey = { it.key },
        getColumnLabel = {
            Text(
                text = it.label,
                textAlign = TextAlign.Center,
            )
        },
        onSearch = { keword, filterState, sortState, pageState ->
            val loadData2 = vm.loadData2(keword, filterState, sortState, pageState)

        },
        onSaveClick = {
            println("新增")
        },
        onImportClick = {

            println("导入")
        },
        onExportClick = { _, _, _, _ ->
            println("导出")


        },

        onBatchDelete = {
            println("批量删除")

        },
        onBatchExport = {
            println("批量导出")

        },
        onEditClick = {
            println("编辑")
        },
        onDeleteClick = {
            println("删了")
        },
//        getCellContent = { row, col ->
//            //自定义渲染
//            when (col.key) {
//                "field001" -> Text(text = row.field001)
//                "field002" -> Text(text = row.field002)
//                "field003" -> Text(text = row.field003)
//                "field004" -> Text(text = row.field004)
//                "field005" -> Text(text = row.field005)
//                "field006" -> Text(text = row.field006)
//                "field007" -> Text(text = row.field007)
//            }
//        },
    )


}
