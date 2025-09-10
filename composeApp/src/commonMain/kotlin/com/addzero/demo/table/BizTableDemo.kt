package com.addzero.demo.table

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import com.addzero.annotation.Route
import com.addzero.component.table.biz.AddTable

class AutoTableViewModel(routeKey: String) : ViewModel() {
    init {
        loadData()
    }

    private fun loadData() {
        TODO("Not yet implemented")
    }

}

@Route
@Composable
fun TableBigDataTest3() {
    val (bigDataSet, bigColumns) = mockData()
    AddTable(
        data = bigDataSet,
        columns = bigColumns,
        getColumnKey = { it.key },
        getColumnLabel = {
            Text(
                text = it.label,
                textAlign = TextAlign.Center,
            )
        },
        onSearch = { _, _, _, _ ->
            println("搜索")

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
//        buttonSlot = {},
//        topSlot = {
//            //一般渲染搜索区(搜索框,字段高级搜索)
//        },

//        bottomSlot = {
//            //一般渲染分页控件
//        },
//        emptyContentSlot = {
//            //当表格数据为空时,渲染自定义内容(可以是动画)
//        },
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
//        rowLeftSlot = { row, index ->
////           一般是每一个左侧的复选框(开启多选模式)
//        },
//        rowActionSlot = {
//            //操作区(编辑和删除)
//            AddEditDeleteButton(
//                onEditClick = {},
//                onDeleteClick = {}
//            )
//
//        },
//        buttonSlot = {}
    )


}
